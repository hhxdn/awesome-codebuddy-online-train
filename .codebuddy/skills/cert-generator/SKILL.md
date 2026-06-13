---
name: cert-generator
description: "Generate certificate images (结业证书) for the awesome-codebuddy-online-train platform and upload them to the backend via admin API. This skill should be used when the user asks to create certificates for users, generate certificate attachments, 生成证书, 创建证书, 造证书, or needs to batch-generate certificate images for course completion. Covers both image generation (Pillow) and API-based certificate creation with attachment upload."
---

# Cert Generator

## Overview

Generate Chinese-style "结业证书" (Certificate of Completion) images using Python Pillow
and create certificate records with attachment uploads via the online-train admin API.
The skill handles both image generation and backend integration in one workflow.

## When to Use

- User asks to "生成证书", "创建证书", "造几个证书"
- Need to create certificate records with downloadable attachments for specific users
- Batch-generate certificate images for multiple courses

## Workflow

### Step 1: Gather Prerequisites

Before generating certificates, confirm the following:

1. **Backend is running** on `http://127.0.0.1:8088` (check with `lsof -ti:8088`)
2. **Pillow is installed** (`pip3 install Pillow requests -q`)
3. **Admin account credentials**: default `admin` / `123456`
4. **Target user ID** - query from database if only phone number is known:

```bash
mysql -h 14.103.222.243 -P 3306 -u online_train -pD3AhKexTMcLXLBmK online_train \
  -e "SELECT id, phone, real_name FROM user WHERE phone LIKE '%<phone>%'"
```

5. **Course IDs** - query available courses:

```bash
mysql -h 14.103.222.243 -P 3306 -u online_train -pD3AhKexTMcLXLBmK online_train \
  -e "SELECT id, title FROM course WHERE deleted=0"
```

### Step 2: Generate Certificate Images

Use `scripts/generate_cert.py` to produce certificate PNG images. The script generates
a styled certificate with:

- Gold outer border (`#C8963E`) and inner border (`#D4A84B`)
- "结业证书" title in dark brown (`#8B4513`)
- Student name, phone, course name in the body text
- Certificate number, course name, issue date in info section
- Red round seal (印章) with "在线学习" / "证书专用章"
- Footer verification note

Run the script:

```bash
python3 .codebuddy/skills/cert-generator/scripts/generate_cert.py \
  --output /tmp/certs \
  --student "张三" \
  --phone "13800001001" \
  --courses "Spring Boot 入门到精通,Spring Cloud 微服务实战,Vue3 + TypeScript 实战"
```

Or invoke the Python code inline for ad-hoc generation — the script is the canonical
implementation and should be used when possible. See `scripts/generate_cert.py` for
the full Pillow-based generation logic.

### Step 3: Obtain Admin Token

Login to the admin API to get a bearer token:

```bash
TOKEN=$(curl -s --max-time 5 -X POST http://127.0.0.1:8088/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"account":"admin","password":"123456"}' | jq -r '.data.token')
```

### Step 4: Create Certificates with Attachments

For each course, upload the certificate image as an attachment:

```bash
curl -s --max-time 10 -X POST "http://127.0.0.1:8088/api/admin/certificates?userId=<USER_ID>&certType=COURSE&courseId=<COURSE_ID>" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/tmp/certs/<course_id>.png;type=image/png"
```

**API endpoint**: `POST /api/admin/certificates`
- Query params: `userId`, `certType` (COURSE), `courseId`
- Multipart file upload: `file` field with PNG image
- Auth: Bearer token from admin login

### Step 5: Verify Results

Confirm certificates were created with attachments:

```bash
mysql -h 14.103.222.243 -P 3306 -u online_train -pD3AhKexTMcLXLBmK online_train \
  -e "SELECT id, user_id, course_id, title, CASE WHEN attachment_url IS NOT NULL THEN '可下载' ELSE '无附件' END as has_attach FROM certificate WHERE user_id=<USER_ID> AND status=1 AND deleted=0"
```

Also verify via the user-facing API to confirm H5/miniapp can see the download buttons:

```bash
USER_TOKEN=$(curl -s --max-time 5 -X POST http://127.0.0.1:8088/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"<PHONE>","password":"123456"}' | jq -r '.data.token')

curl -s --max-time 5 http://127.0.0.1:8088/api/certificates \
  -H "Authorization: Bearer $USER_TOKEN" | \
  jq '.data[] | {id, title, has_attachment: (.attachmentUrl != null)}'
```

## Database Schema

Refer to `references/db_schema.md` for the certificate table structure and related tables.

## Common Pitfalls

- **Font availability**: The script tries system fonts first (`/System/Library/Fonts/STHeiti Light.ttc` on macOS) and falls back to default font. On Linux, install `fonts-wqy-zenhei` or similar CJK fonts.
- **Duplicate certificates**: Always check existing certificates before creating new ones to avoid duplicates for the same user+course combination.
- **COS upload**: The backend uploads attachments to Tencent Cloud COS. Ensure COS credentials in `application.yml` are valid.
- **Certificate image quality**: The generated image is 800x566px at 72 DPI. For print-quality certificates, adjust the `W`/`H` constants and use a higher-quality font.

## Resources

### scripts/generate_cert.py
Standalone Python script that generates certificate PNG images using Pillow.
Can be run directly or imported as a module.

### references/db_schema.md
Database schema for certificate-related tables (certificate, course, user).
