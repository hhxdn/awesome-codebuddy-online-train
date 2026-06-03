#!/usr/bin/env python3
"""测试腾讯云 VOD API 凭证是否有效"""
import hashlib, hmac, json, time, requests
from datetime import datetime

SECRET_ID = "AKIDzIagaHXcUBh7PUP8e096ayD1e50kJky3"
SECRET_KEY = "ymOnz0pSlzCMRSGrHVzKDya2JPK4edR2"
ENDPOINT = "vod.tencentcloudapi.com"
SERVICE = "vod"
REGION = "ap-beijing"
VERSION = "2018-07-17"

def sign(key, msg):
    return hmac.new(key, msg.encode("utf-8"), hashlib.sha256).digest()

def sha256_hex(s):
    return hashlib.sha256(s.encode("utf-8")).hexdigest()

def call_vod_api(action, params):
    timestamp = int(time.time())
    date = datetime.utcfromtimestamp(timestamp).strftime("%Y-%m-%d")
    payload = json.dumps(params)
    
    # Step 1: Canonical Request
    http_method = "POST"
    canonical_uri = "/"
    canonical_querystring = ""
    canonical_headers = f"content-type:application/json; charset=utf-8\nhost:{ENDPOINT}\nx-tc-action:{action.lower()}\n"
    signed_headers = "content-type;host;x-tc-action"
    hashed_payload = sha256_hex(payload)
    canonical_request = f"{http_method}\n{canonical_uri}\n{canonical_querystring}\n{canonical_headers}\n{signed_headers}\n{hashed_payload}"
    
    # Step 2: String to Sign
    algorithm = "TC3-HMAC-SHA256"
    credential_scope = f"{date}/{SERVICE}/tc3_request"
    hashed_canonical_request = sha256_hex(canonical_request)
    string_to_sign = f"{algorithm}\n{timestamp}\n{credential_scope}\n{hashed_canonical_request}"
    
    # Step 3: Signature
    secret_date = sign(("TC3" + SECRET_KEY).encode("utf-8"), date)
    secret_service = sign(secret_date, SERVICE)
    secret_signing = sign(secret_service, "tc3_request")
    signature = hmac.new(secret_signing, string_to_sign.encode("utf-8"), hashlib.sha256).hexdigest()
    
    # Step 4: Authorization
    authorization = f"{algorithm} Credential={SECRET_ID}/{credential_scope}, SignedHeaders={signed_headers}, Signature={signature}"
    
    # Step 5: Request
    headers = {
        "Authorization": authorization,
        "Content-Type": "application/json; charset=utf-8",
        "Host": ENDPOINT,
        "X-TC-Action": action,
        "X-TC-Timestamp": str(timestamp),
        "X-TC-Version": VERSION,
        "X-TC-Region": REGION,
    }
    
    print(f"Action: {action}")
    print(f"Timestamp: {timestamp} ({date})")
    print(f"Payload: {payload}")
    print(f"StringToSign:\n{string_to_sign}")
    print(f"Signature: {signature}")
    print(f"Authorization: {authorization[:80]}...")
    
    try:
        resp = requests.post(f"https://{ENDPOINT}", headers=headers, data=payload, timeout=30)
        print(f"\nStatus: {resp.status_code}")
        result = resp.json()
        print(f"Response: {json.dumps(result, indent=2, ensure_ascii=False)}")
        
        if "Response" in result and "Error" in result["Response"]:
            print(f"\n*** API ERROR: {result['Response']['Error']['Code']} - {result['Response']['Error']['Message']}")
        else:
            print("\n*** SUCCESS! Credentials are valid.")
    except Exception as e:
        print(f"\n*** NETWORK ERROR: {e}")

# Test 1: DescribeSubAppIds (no params needed)
print("=== Test 1: DescribeSubAppIds ===")
call_vod_api("DescribeSubAppIds", {})

print("\n=== Test 2: DescribeMediaInfos (with SubAppId) ===")
call_vod_api("DescribeMediaInfos", {"FileIds": ["test123"], "SubAppId": 1500065209})
