#!/bin/bash
# VOD上传全流程测试脚本
set -e

API_HOST="http://127.0.0.1:8088"
ADMIN_ACCOUNT="admin"
ADMIN_PASSWORD="test123"

echo "=========================================="
echo "  VOD 上传全流程测试"
echo "=========================================="

# Step 1: 管理员登录获取 token
echo ""
echo "[1/5] 管理员登录..."
LOGIN_RESP=$(curl -s -X POST "${API_HOST}/api/admin/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"account\":\"${ADMIN_ACCOUNT}\",\"password\":\"${ADMIN_PASSWORD}\"}")
echo "登录响应: $LOGIN_RESP" | head -c 500
TOKEN=$(echo "$LOGIN_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['token'])" 2>/dev/null)
if [ -z "$TOKEN" ]; then
  echo "❌ 登录失败，无法获取 token"
  exit 1
fi
echo "✅ 登录成功, token=${TOKEN:0:30}..."

# Step 2: 创建测试视频文件（最小有效MP4）
echo ""
echo "[2/5] 创建测试视频文件..."
TEST_VIDEO="/tmp/test_vod_video.mp4"
# 尝试用 ffmpeg 生成，如果没有则用极小的空文件模拟
if command -v ffmpeg &> /dev/null; then
  ffmpeg -y -f lavfi -i "testsrc=duration=3:size=320x240:rate=15" \
    -f lavfi -i "sine=frequency=440:duration=3" \
    -c:v libx264 -preset ultrafast -c:a aac \
    -shortest "$TEST_VIDEO" -hide_banner -loglevel error 2>&1
  echo "✅ 用 ffmpeg 生成了测试视频: $(ls -lh "$TEST_VIDEO" | awk '{print $5}')"
else
  # 生成一个极小的单帧 h264 视频（可能无法播放，但用来测试上传流程是可以的）
  python3 -c "
import struct
# 最小的有效 MP4 文件（ftyp + moov atom）
data = bytearray()
# ftyp box
data += b'\x00\x00\x00\x20'  # size
data += b'ftyp'
data += b'mp42'
data += b'\x00\x00\x00\x00'
data += b'mp42'
data += b'isom'
# moov box (empty)
data += b'\x00\x00\x00\x08'
data += b'moov'
with open('$TEST_VIDEO', 'wb') as f:
    f.write(data)
"
  echo "✅ 生成最小MP4文件: $(ls -lh "$TEST_VIDEO" | awk '{print $5}')"
fi

# Step 3: 上传视频到 VOD
echo ""
echo "[3/5] 上传视频到 VOD..."
UPLOAD_RESP=$(curl -s -X POST "${API_HOST}/api/admin/upload/video" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@${TEST_VIDEO};type=video/mp4")
echo "上传响应: $UPLOAD_RESP"

# 解析 fileId 和 playbackUrl
FILE_ID=$(echo "$UPLOAD_RESP" | python3 -c "import sys,json; d=json.load(sys.stdin)['data']; print(d.get('fileId',''))" 2>/dev/null)
PLAYBACK_URL=$(echo "$UPLOAD_RESP" | python3 -c "import sys,json; d=json.load(sys.stdin)['data']; print(d.get('playbackUrl',''))" 2>/dev/null)

if [ -z "$FILE_ID" ]; then
  echo "❌ 上传失败，未获取到 fileId"
  echo "完整响应: $UPLOAD_RESP"
  exit 1
fi

echo "✅ 上传成功!"
echo "   fileId: $FILE_ID"
echo "   playbackUrl: $PLAYBACK_URL"

# Step 4: 创建测试课程（带 VOD 章节）
echo ""
echo "[4/5] 创建测试课程（含 VOD 章节）..."
COURSE_RESP=$(curl -s -X POST "${API_HOST}/api/admin/courses" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"VOD转码测试课程\",
    \"categoryId\": 1,
    \"description\": \"用于测试 VOD 上传和转码轮询的课程\",
    \"status\": \"PUBLISH\",
    \"chapters\": [
      {
        \"title\": \"VOD测试章节\",
        \"videoUrl\": \"${PLAYBACK_URL}\",
        \"fileId\": \"${FILE_ID}\",
        \"playbackUrl\": \"${PLAYBACK_URL}\",
        \"sortOrder\": 1
      }
    ]
  }")
echo "创建课程响应: $(echo $COURSE_RESP | head -c 500)"

COURSE_ID=$(echo "$COURSE_RESP" | python3 -c "import sys,json; d=json.load(sys.stdin)['data']; print(d.get('id',''))" 2>/dev/null)
if [ -z "$COURSE_ID" ]; then
  echo "❌ 课程创建失败"
  echo "完整响应: $COURSE_RESP"
  exit 1
fi
echo "✅ 课程创建成功, courseId=$COURSE_ID"

# Step 5: 验证数据库记录
echo ""
echo "[5/5] 验证数据库记录..."
echo "--- chapter 表记录 ---"
mysql -h 14.103.222.243 -P 3306 -u online_train -p'D3AhKexTMcLXLBmK' online_train -e "
SELECT 
  id,
  course_id,
  title,
  video_url,
  vod_file_id,
  vod_playback_url,
  vod_transcode_status,
  video_duration
FROM chapter 
WHERE course_id = $COURSE_ID
ORDER BY id DESC LIMIT 5;
" 2>&1 | grep -v Warning

echo ""
echo "--- 轮询任务检查 ---"
echo "等待5秒查看后台日志..."
sleep 5
grep -i "VodTranscode\|pollTranscode\|transcode" /tmp/backend.log | tail -10

echo ""
echo "=========================================="
echo "  测试完成!"
echo "  courseId: $COURSE_ID"
echo "  fileId: $FILE_ID"
echo "  playbackUrl: $PLAYBACK_URL"
echo "=========================================="

# 清理测试视频
rm -f "$TEST_VIDEO"

# 恢复 admin 密码
ORIGINAL_HASH='$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi'
# mysql -h 14.103.222.243 -P 3306 -u online_train -p'D3AhKexTMcLXLBmK' online_train \
#   -e "UPDATE user SET password='$ORIGINAL_HASH' WHERE phone='admin';" 2>&1 | grep -v Warning
# echo "admin密码已恢复"

echo ""
echo "📝 测试数据保留在数据库中，可以手动清理："
echo "   DELETE FROM chapter WHERE course_id = $COURSE_ID;"
echo "   DELETE FROM course WHERE id = $COURSE_ID;"
