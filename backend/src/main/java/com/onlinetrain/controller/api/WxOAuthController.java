package com.onlinetrain.controller.api;

import com.onlinetrain.common.Result;
import com.onlinetrain.entity.User;
import com.onlinetrain.service.OAuthStateStore;
import com.onlinetrain.service.UserService;
import com.onlinetrain.service.WxPayService;
import com.onlinetrain.config.WxPayProperties;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信OAuth控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/wx")
@Api(tags = "微信OAuth接口")
public class WxOAuthController {

    @Autowired
    private WxPayProperties wxPayProperties;

    @Autowired
    private WxPayService wxPayService;

    @Autowired
    private UserService userService;

    @Autowired
    private OAuthStateStore oauthStateStore;

    /**
     * 准备OAuth授权（已登录用户调用）
     * 返回微信OAuth授权URL，用户需将浏览器重定向到该URL
     */
    @PostMapping("/oauth-prepare")
    @ApiOperation("准备微信OAuth授权")
    public Result<Map<String, String>> prepareOAuth(@RequestBody Map<String, String> params,
                                                     HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String redirectUri = params.getOrDefault("redirect", "/");

        // 生成state并存储
        String state = oauthStateStore.put(userId, redirectUri);

        // 构造微信OAuth URL (snsapi_base 静默授权，不弹窗)
        String callbackUrl = getCallbackBaseUrl(request) + "/api/wx/oauth-callback";
        String oauthUrl = "https://open.weixin.qq.com/connect/oauth2/authorize"
                + "?appid=" + wxPayProperties.getAppId()
                + "&redirect_uri=" + URLEncoder.encode(callbackUrl)
                + "&response_type=code"
                + "&scope=snsapi_base"
                + "&state=" + state
                + "#wechat_redirect";

        Map<String, String> result = new HashMap<>();
        result.put("oauthUrl", oauthUrl);
        return Result.ok(result);
    }

    /**
     * 微信OAuth回调（无需登录，已排除拦截器）
     * 微信服务器会将浏览器重定向到此URL
     */
    @GetMapping("/oauth-callback")
    @ApiOperation("微信OAuth回调")
    public void oauthCallback(@RequestParam String code,
                              @RequestParam String state,
                              HttpServletResponse response) throws IOException {
        try {
            // 1. 从state存储中获取用户信息
            OAuthStateStore.OAuthState oauthState = oauthStateStore.consume(state);
            if (oauthState == null) {
                log.warn("OAuth state无效或已过期: {}", state);
                response.sendRedirect("/#/login?error=oauth_expired");
                return;
            }

            // 2. 通过code换取openid
            String openid = wxPayService.getOpenidByCode(code);
            log.info("OAuth获取openid成功: userId={}, openid={}", oauthState.getUserId(), openid);

            // 3. 保存openid到用户记录
            User user = userService.getById(oauthState.getUserId());
            if (user != null) {
                user.setOpenid(openid);
                userService.updateById(user);
            }

            // 4. 重定向回前端页面
            String redirectUri = oauthState.getRedirectUri();
            if (!redirectUri.startsWith("/")) redirectUri = "/" + redirectUri;
            response.sendRedirect("/#" + redirectUri);

        } catch (Exception e) {
            log.error("OAuth回调处理失败", e);
            response.sendRedirect("/#/login?error=oauth_error");
        }
    }

    /**
     * 获取当前用户的openid
     */
    @GetMapping("/openid")
    @ApiOperation("获取当前用户openid")
    public Result<Map<String, String>> getOpenid(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        Map<String, String> result = new HashMap<>();
        result.put("openid", user != null ? user.getOpenid() : null);
        result.put("hasOpenid", String.valueOf(user != null && user.getOpenid() != null
                && !user.getOpenid().isEmpty()));
        return Result.ok(result);
    }

    /**
     * 获取OAuth回调的基础URL
     */
    private String getCallbackBaseUrl(HttpServletRequest request) {
        String scheme = request.getHeader("X-Forwarded-Proto");
        if (scheme == null) scheme = request.getScheme();
        String host = request.getHeader("X-Forwarded-Host");
        if (host == null) host = request.getHeader("Host");
        if (host == null) host = "localhost:" + request.getServerPort();
        return scheme + "://" + host;
    }
}
