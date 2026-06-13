# Database Schema Reference

## Certificate Table (`certificate`)

```sql
CREATE TABLE certificate (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '学员ID',
    course_id BIGINT COMMENT '关联课程ID (COURSE类型)',
    cert_type VARCHAR(32) NOT NULL DEFAULT 'COURSE' COMMENT '证书类型: COURSE',
    title VARCHAR(255) NOT NULL COMMENT '证书标题',
    content TEXT COMMENT '证书正文内容',
    cert_no VARCHAR(128) COMMENT '证书编号',
    issue_time DATETIME COMMENT '颁发时间',
    attachment_url VARCHAR(512) COMMENT '证书附件URL (COS)',
    status TINYINT DEFAULT 1 COMMENT '状态: 1=已颁发',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_course_id (course_id),
    INDEX idx_status (status)
);
```

## Related Tables

### User (`user`)
```sql
-- Key fields: id, phone, real_name, nickname, role (USER/ADMIN)
-- Test user: phone=13800001001, password=123456, role=USER
-- Admin: account=admin, password=123456
```

### Course (`course`)
```sql
-- Key fields: id, title, category_id, price, status, deleted
```

## API Endpoints

### Admin Certificate Creation
```
POST /api/admin/certificates?userId={userId}&certType=COURSE&courseId={courseId}
Authorization: Bearer {admin_token}
Content-Type: multipart/form-data
Body: file={certificate_image}.png
```

### Admin Certificate Attachment Upload (existing certificate)
```
POST /api/admin/certificates/{certId}/attachment
Authorization: Bearer {admin_token}
Content-Type: multipart/form-data
Body: file={certificate_image}.png
```

### User Certificate List
```
GET /api/certificates
Authorization: Bearer {user_token}
Response: [{ id, title, certNo, courseTitle, content, attachmentUrl, issueTime, ... }]
```

## Authentication

- **Admin login**: `POST /api/admin/auth/login` with `{account, password}`
- **User login**: `POST /api/user/login` with `{phone, password}`
- Default test passwords are `123456`

## Database Connection

```
Host: 14.103.222.243
Port: 3306
User: online_train
Password: D3AhKexTMcLXLBmK
Database: online_train
```
