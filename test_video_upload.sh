#!/bin/bash
echo "=== 1. 管理员登录 ==="
LOGIN_RESP=$(curl -s -X POST http://localhost:8088/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"account":"admin","password":"123456"}')
TOKEN=$(echo "$LOGIN_RESP" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['data']['token'])")
echo "Token获取成功: ${TOKEN:0:30}..."

echo ""
echo "=== 2. 上传视频到VOD ==="
VIDEO_RESP=$(curl -s -X POST http://localhost:8088/api/admin/upload/video \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/tmp/test_video.mp4;type=video/mp4")
echo "$VIDEO_RESP" | python3 -m json.tool
