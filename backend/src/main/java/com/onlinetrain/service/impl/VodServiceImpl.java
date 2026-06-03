package com.onlinetrain.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.onlinetrain.config.TencentCloudConfig;
import com.onlinetrain.service.VodService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.*;
import java.io.IOException;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class VodServiceImpl implements VodService {

    private static final String ENDPOINT = "vod.tencentcloudapi.com";
    private static final String SERVICE = "vod";
    private static final String VERSION = "2018-07-17";
    private static final MediaType JSON_MEDIA = MediaType.parse("application/json; charset=utf-8");

    private final TencentCloudConfig config;
    private final OkHttpClient httpClient;

    public VodServiceImpl(TencentCloudConfig config) {
        this.config = config;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(java.time.Duration.ofSeconds(30))
                .readTimeout(java.time.Duration.ofSeconds(60))
                .proxy(Proxy.NO_PROXY) // 禁用系统代理，避免 macOS 误读已禁用的代理配置
                .sslSocketFactory(createTrustAllSslFactory(), (X509TrustManager) TRUST_ALL_CERTS[0])
                .hostnameVerifier((hostname, session) -> true)
                .build();
    }

    private static final TrustManager[] TRUST_ALL_CERTS = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }
    };

    private static SSLSocketFactory createTrustAllSslFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, TRUST_ALL_CERTS, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, String> pullUpload(String cosUrl, String mediaName) {
        JSONObject params = new JSONObject();
        params.put("MediaUrl", cosUrl);
        params.put("MediaName", mediaName);
        if (config.getVod().getSubAppId() != null && config.getVod().getSubAppId() > 0) {
            params.put("SubAppId", config.getVod().getSubAppId());
        }

        try {
            String response = callApi("PullUpload", params.toJSONString());
            JSONObject resp = JSON.parseObject(response).getJSONObject("Response");
            if (resp.containsKey("Error")) {
                JSONObject error = resp.getJSONObject("Error");
                log.error("VOD PullUpload API error: code={}, message={}",
                        error.getString("Code"), error.getString("Message"));
                throw new RuntimeException("VOD上传失败: " + error.getString("Message"));
            }

            String taskId = resp.getString("TaskId");
            String fileId = resp.getString("FileId");
            log.info("VOD PullUpload success: taskId={}, fileId={}, mediaName={}", taskId, fileId, mediaName);

            Map<String, String> result = new HashMap<>();
            result.put("taskId", taskId);
            result.put("fileId", fileId);
            if (fileId != null) {
                result.put("playbackUrl", "https://" + fileId + ".vod2.myqcloud.com");
            }
            return result;
        } catch (IOException e) {
            log.error("VOD PullUpload HTTP error: mediaName={}", mediaName, e);
            throw new RuntimeException("视频上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, String> directUpload(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String mediaName = originalFilename;
        String mediaType = "mp4";
        if (originalFilename != null && originalFilename.contains(".")) {
            mediaName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
            String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (!ext.isEmpty()) {
                mediaType = ext;
            }
        }

        // 1. ApplyUpload - 申请上传凭证
        JSONObject applyParams = new JSONObject();
        applyParams.put("MediaType", mediaType);
        applyParams.put("MediaName", originalFilename);
        if (config.getVod().getSubAppId() != null && config.getVod().getSubAppId() > 0) {
            applyParams.put("SubAppId", config.getVod().getSubAppId());
        }

        String applyResponse = callApi("ApplyUpload", applyParams.toJSONString());
        JSONObject applyResp = JSON.parseObject(applyResponse).getJSONObject("Response");
        if (applyResp.containsKey("Error")) {
            JSONObject error = applyResp.getJSONObject("Error");
            log.error("VOD ApplyUpload error: {}", error);
            throw new RuntimeException("VOD上传失败: " + error.getString("Message"));
        }

        String uploadAddress = applyResp.getString("UploadAddress");
        String uploadAuth = applyResp.getString("UploadAuth");
        String vodSessionKey = applyResp.getString("VodSessionKey");
        log.info("VOD ApplyUpload success: mediaName={}, uploadAddress={}", mediaName, uploadAddress);

        // 2. 上传文件到 VOD 存储（使用长超时的 client）
        OkHttpClient uploadClient = new OkHttpClient.Builder()
                .connectTimeout(java.time.Duration.ofSeconds(30))
                .readTimeout(java.time.Duration.ofMinutes(30))
                .writeTimeout(java.time.Duration.ofMinutes(30))
                .proxy(Proxy.NO_PROXY)
                .sslSocketFactory(createTrustAllSslFactory(), (X509TrustManager) TRUST_ALL_CERTS[0])
                .hostnameVerifier((hostname, session) -> true)
                .build();

        byte[] fileBytes = file.getBytes();
        RequestBody fileBody = RequestBody.create(fileBytes, MediaType.parse("video/" + mediaType));
        Request uploadRequest = new Request.Builder()
                .url(uploadAddress)
                .put(fileBody)
                .addHeader("Authorization", uploadAuth)
                .addHeader("Content-Type", "video/" + mediaType)
                .build();

        try (Response uploadResponse = uploadClient.newCall(uploadRequest).execute()) {
            if (!uploadResponse.isSuccessful()) {
                String errBody = uploadResponse.body() != null ? uploadResponse.body().string() : "";
                log.error("VOD file upload failed: status={}, body={}", uploadResponse.code(), errBody);
                throw new RuntimeException("VOD上传失败: HTTP " + uploadResponse.code());
            }
            log.info("VOD file upload success: status={}", uploadResponse.code());
        }

        // 3. CommitUpload - 确认上传
        JSONObject commitParams = new JSONObject();
        commitParams.put("VodSessionKey", vodSessionKey);
        if (config.getVod().getSubAppId() != null && config.getVod().getSubAppId() > 0) {
            commitParams.put("SubAppId", config.getVod().getSubAppId());
        }

        String commitResponse = callApi("CommitUpload", commitParams.toJSONString());
        JSONObject commitResp = JSON.parseObject(commitResponse).getJSONObject("Response");
        if (commitResp.containsKey("Error")) {
            JSONObject error = commitResp.getJSONObject("Error");
            log.error("VOD CommitUpload error: {}", error);
            throw new RuntimeException("VOD上传失败: " + error.getString("Message"));
        }

        String fileId = commitResp.getString("FileId");
        log.info("VOD CommitUpload success: fileId={}", fileId);

        Map<String, String> result = new HashMap<>();
        result.put("fileId", fileId);
        if (fileId != null) {
            result.put("playbackUrl", "https://" + fileId + ".vod2.myqcloud.com");
        }
        return result;
    }

    @Override
    public Map<String, String> getMediaInfo(String fileId) {
        JSONObject params = new JSONObject();
        params.put("FileIds", Collections.singletonList(fileId));
        if (config.getVod().getSubAppId() != null && config.getVod().getSubAppId() > 0) {
            params.put("SubAppId", config.getVod().getSubAppId());
        }

        try {
            String response = callApi("DescribeMediaInfos", params.toJSONString());
            JSONObject resp = JSON.parseObject(response).getJSONObject("Response");

            Map<String, String> result = new HashMap<>();
            if (resp.containsKey("MediaInfoSet") && !resp.getJSONArray("MediaInfoSet").isEmpty()) {
                JSONObject info = resp.getJSONArray("MediaInfoSet").getJSONObject(0);
                result.put("fileId", info.getString("FileId"));
                result.put("name", info.getString("Name"));
            }
            return result;
        } catch (IOException e) {
            log.error("VOD DescribeMediaInfos error: fileId={}", fileId, e);
            return Collections.emptyMap();
        }
    }

    /**
     * 调用腾讯云 API v3（TC3-HMAC-SHA256 签名）
     */
    private String callApi(String action, String payload) throws IOException {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date(Long.parseLong(timestamp) * 1000));

        log.info("VOD callApi: action={}, secretId={}, secretKeyLen={}, region={}, subAppId={}",
                action,
                config.getCloud().getSecretId(),
                config.getCloud().getSecretKey() != null ? config.getCloud().getSecretKey().length() : 0,
                config.getVod().getRegion(),
                config.getVod().getSubAppId());

        // 1. 拼接规范请求串
        String httpMethod = "POST";
        String canonicalUri = "/";
        String canonicalQuery = "";
        String canonicalHeaders = "content-type:application/json; charset=utf-8\n"
                + "host:" + ENDPOINT + "\n"
                + "x-tc-action:" + action.toLowerCase() + "\n";
        String signedHeaders = "content-type;host;x-tc-action";
        String hashedPayload = sha256Hex(payload);
        String canonicalRequest = httpMethod + "\n"
                + canonicalUri + "\n"
                + canonicalQuery + "\n"
                + canonicalHeaders + "\n"
                + signedHeaders + "\n"
                + hashedPayload;

        log.debug("VOD canonicalRequest: {}", canonicalRequest);

        // 2. 拼接待签名字符串
        String algorithm = "TC3-HMAC-SHA256";
        String credentialScope = date + "/" + SERVICE + "/tc3_request";
        String hashedCanonicalRequest = sha256Hex(canonicalRequest);
        String stringToSign = algorithm + "\n"
                + timestamp + "\n"
                + credentialScope + "\n"
                + hashedCanonicalRequest;

        log.debug("VOD stringToSign: {}", stringToSign);

        // 3. 计算签名
        byte[] secretDate = hmac256(("TC3" + config.getCloud().getSecretKey()).getBytes(StandardCharsets.UTF_8), date);
        byte[] secretService = hmac256(secretDate, SERVICE);
        byte[] secretSigning = hmac256(secretService, "tc3_request");
        byte[] signature = hmac256(secretSigning, stringToSign);
        String signatureHex = bytesToHex(signature);

        // 4. 拼接 Authorization
        String authorization = algorithm + " "
                + "Credential=" + config.getCloud().getSecretId() + "/" + credentialScope + ", "
                + "SignedHeaders=" + signedHeaders + ", "
                + "Signature=" + signatureHex;

        log.info("VOD Authorization: {}", authorization);
        log.info("VOD payload: {}", payload);

        // 5. 发送请求
        RequestBody body = RequestBody.create(payload, JSON_MEDIA);
        Request request = new Request.Builder()
                .url("https://" + ENDPOINT)
                .post(body)
                .addHeader("Authorization", authorization)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Host", ENDPOINT)
                .addHeader("X-TC-Action", action)
                .addHeader("X-TC-Timestamp", timestamp)
                .addHeader("X-TC-Version", VERSION)
                .addHeader("X-TC-Region", config.getVod().getRegion())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.body() == null) {
                throw new IOException("Empty response body");
            }
            String respBody = response.body().string();
            log.info("VOD response: {}", respBody);
            return respBody;
        }
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] hmac256(byte[] key, String msg) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(msg.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}
