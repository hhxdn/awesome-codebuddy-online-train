#!/bin/bash
# 测试腾讯云上传功能

echo "=== 1. 管理员登录 ==="
LOGIN_RESP=$(curl -s -X POST http://localhost:8088/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"account":"admin","password":"123456"}')
echo "$LOGIN_RESP" | python3 -m json.tool

TOKEN=$(echo "$LOGIN_RESP" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['data']['token'])")
echo ""
echo "Token获取: ${TOKEN:0:30}..."

echo ""
echo "=== 2. 测试图片上传 (上传SVG封面) ==="
IMAGE_RESP=$(curl -s -X POST http://localhost:8088/api/admin/upload/image \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@backend/uploads/covers/course_01.svg")
echo "$IMAGE_RESP" | python3 -m json.tool

echo ""
echo "=== 完成 ==="
