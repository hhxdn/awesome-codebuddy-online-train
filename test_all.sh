#!/bin/bash
# 全面功能测试脚本
BASE="http://127.0.0.1:8088"
PASS=0
FAIL=0
echo "============================================"
echo "  在线学习平台 - 全功能测试"
echo "  $(date '+%Y-%m-%d %H:%M:%S')"
echo "============================================"

test_api() {
    local desc="$1"
    local method="$2"
    local url="$3"
    local data="$4"
    local expect_code="${5:-200}"
    
    if [ -n "$TOKEN" ]; then
        AUTH_HEADER="-H 'Authorization: Bearer $TOKEN'"
    else
        AUTH_HEADER=""
    fi
    
    if [ -n "$data" ]; then
        resp=$(curl -s -w "\n%{http_code}" -X "$method" "$BASE$url" \
            -H "Content-Type: application/json" \
            $AUTH_HEADER \
            -d "$data")
    else
        resp=$(curl -s -w "\n%{http_code}" -X "$method" "$BASE$url" \
            $AUTH_HEADER)
    fi
    
    http_code=$(echo "$resp" | tail -1)
    body=$(echo "$resp" | sed '$d')
    
    if [ "$http_code" = "$expect_code" ]; then
        echo "  ✅ $desc (HTTP $http_code)"
        PASS=$((PASS+1))
        if [ "$SHOW_BODY" = "1" ] && [ -n "$body" ]; then
            echo "     ${body:0:200}"
        fi
    else
        echo "  ❌ $desc - 预期 $expect_code 实际 $http_code"
        echo "     响应: ${body:0:200}"
        FAIL=$((FAIL+1))
    fi
}

# ==================== 1. 管理端登录 ====================
echo ""
echo "--- 1. 管理端认证 ---"
LOGIN_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/admin/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"account":"admin","password":"123456"}')

ADMIN_TOKEN=$(echo "$LOGIN_RESP" | sed '$d' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
if [ -n "$ADMIN_TOKEN" ]; then
    echo "  ✅ 管理员登录成功"
    PASS=$((PASS+1))
else
    echo "  ❌ 管理员登录失败"
    FAIL=$((FAIL+1))
    echo "  响应: ${LOGIN_RESP:0:200}"
fi

TOKEN="$ADMIN_TOKEN"

# ==================== 2. 分类管理 ====================
echo ""
echo "--- 2. 分类管理 ---"
test_api "获取树形分类" GET "/api/admin/categories/tree"
test_api "获取扁平分类" GET "/api/admin/categories"

# 创建一级分类
CAT1_RESP=$(curl -s -X POST "$BASE/api/admin/categories" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d '{"name":"测试一级分类","parentId":0,"isFree":false,"status":1}')

echo "$CAT1_RESP" | grep -q '"code":200'
if [ $? -eq 0 ]; then
    CAT1_ID=$(echo "$CAT1_RESP" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
    echo "  ✅ 创建一级分类 (id=$CAT1_ID)"
    PASS=$((PASS+1))
else
    echo "  ❌ 创建一级分类失败: ${CAT1_RESP:0:200}"
    FAIL=$((FAIL+1))
    CAT1_ID=""
fi

# 创建二级分类
if [ -n "$CAT1_ID" ]; then
    CAT2_RESP=$(curl -s -X POST "$BASE/api/admin/categories" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $ADMIN_TOKEN" \
        -d "{\"name\":\"测试二级分类\",\"parentId\":$CAT1_ID,\"price\":99.00,\"isFree\":false,\"status\":1}")
    
    echo "$CAT2_RESP" | grep -q '"code":200'
    if [ $? -eq 0 ]; then
        CAT2_ID=$(echo "$CAT2_RESP" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
        echo "  ✅ 创建二级分类 (id=$CAT2_ID)"
        PASS=$((PASS+1))
    else
        echo "  ❌ 创建二级分类失败: ${CAT2_RESP:0:200}"
        FAIL=$((FAIL+1))
        CAT2_ID=""
    fi
fi

# 更新分类
if [ -n "$CAT2_ID" ]; then
    test_api "更新分类" PUT "/api/admin/categories/$CAT2_ID" \
        "{\"name\":\"测试二级分类(已更新)\",\"parentId\":$CAT1_ID,\"price\":199.00,\"isFree\":false,\"status\":1}"
fi

# ==================== 3. 课程管理 ====================
echo ""
echo "--- 3. 课程管理 ---"
test_api "课程列表" GET "/api/admin/courses"
test_api "课程列表(分页)" GET "/api/admin/courses?page=1&size=10"

# 创建课程
COURSE_RESP=$(curl -s -X POST "$BASE/api/admin/courses" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d "{\"title\":\"测试课程-全面测试\",\"categoryId\":${CAT2_ID:-0},\"type\":\"VIDEO\",\"description\":\"测试用课程\",\"price\":0,\"isFree\":false,\"status\":1}")

echo "$COURSE_RESP" | grep -q '"code":200'
if [ $? -eq 0 ]; then
    COURSE_ID=$(echo "$COURSE_RESP" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
    echo "  ✅ 创建课程 (id=$COURSE_ID)"
    PASS=$((PASS+1))
else
    echo "  ❌ 创建课程失败: ${COURSE_RESP:0:200}"
    FAIL=$((FAIL+1))
    COURSE_ID=""
fi

if [ -n "$COURSE_ID" ]; then
    test_api "课程详情" GET "/api/admin/courses/$COURSE_ID"
    test_api "更新课程" PUT "/api/admin/courses/$COURSE_ID" \
        "{\"title\":\"测试课程(已更新)\",\"categoryId\":${CAT2_ID:-0},\"type\":\"VIDEO\",\"description\":\"更新后的描述\",\"price\":0,\"isFree\":false,\"status\":1}"
    
    # 添加章节
    CH_RESP=$(curl -s -X POST "$BASE/api/admin/courses/$COURSE_ID/chapters" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $ADMIN_TOKEN" \
        -d '{"title":"第一章-基础知识","sort":1}')
    
    echo "$CH_RESP" | grep -q '"code":200'
    if [ $? -eq 0 ]; then
        CH_ID=$(echo "$CH_RESP" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
        echo "  ✅ 添加章节 (id=$CH_ID)"
        PASS=$((PASS+1))
    else
        echo "  ❌ 添加章节失败: ${CH_RESP:0:200}"
        FAIL=$((FAIL+1))
        CH_ID=""
    fi
    
    if [ -n "$CH_ID" ]; then
        test_api "更新章节" PUT "/api/admin/courses/$COURSE_ID/chapters/$CH_ID" \
            "{\"title\":\"第一章(已更新)\",\"sort\":1}"
    fi
fi

# ==================== 4. 题目管理 ====================
echo ""
echo "--- 4. 题目管理 ---"
test_api "题目列表" GET "/api/admin/questions"

# 创建单选题
if [ -n "$COURSE_ID" ] && [ -n "$CH_ID" ]; then
    Q_RESP=$(curl -s -X POST "$BASE/api/admin/questions" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $ADMIN_TOKEN" \
        -d "{\"content\":\"测试题-1+1=?\",\"type\":\"SINGLE\",\"courseId\":$COURSE_ID,\"chapterId\":$CH_ID,\"categoryId\":${CAT2_ID:-0},\"difficulty\":\"EASY\",\"options\":[{\"label\":\"A\",\"content\":\"1\",\"isCorrect\":false},{\"label\":\"B\",\"content\":\"2\",\"isCorrect\":true},{\"label\":\"C\",\"content\":\"3\",\"isCorrect\":false},{\"label\":\"D\",\"content\":\"4\",\"isCorrect\":false}],\"analysis\":\"基础加法\"}")
    
    echo "$Q_RESP" | grep -q '"code":200'
    if [ $? -eq 0 ]; then
        Q_ID=$(echo "$Q_RESP" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
        echo "  ✅ 创建单选题 (id=$Q_ID)"
        PASS=$((PASS+1))
    else
        echo "  ❌ 创建单选题失败: ${Q_RESP:0:200}"
        FAIL=$((FAIL+1))
        Q_ID=""
    fi
    
    # 创建多选题
    Q2_RESP=$(curl -s -X POST "$BASE/api/admin/questions" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $ADMIN_TOKEN" \
        -d "{\"content\":\"测试题-以下哪些是数字?\",\"type\":\"MULTIPLE\",\"courseId\":$COURSE_ID,\"chapterId\":$CH_ID,\"categoryId\":${CAT2_ID:-0},\"difficulty\":\"MEDIUM\",\"options\":[{\"label\":\"A\",\"content\":\"1\",\"isCorrect\":true},{\"label\":\"B\",\"content\":\"2\",\"isCorrect\":true},{\"label\":\"C\",\"content\":\"a\",\"isCorrect\":false},{\"label\":\"D\",\"content\":\"b\",\"isCorrect\":false}],\"analysis\":\"数字识别\"}")
    
    echo "$Q2_RESP" | grep -q '"code":200'
    if [ $? -eq 0 ]; then
        Q2_ID=$(echo "$Q2_RESP" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
        echo "  ✅ 创建多选题 (id=$Q2_ID)"
        PASS=$((PASS+1))
    else
        echo "  ❌ 创建多选题失败: ${Q2_RESP:0:200}"
        FAIL=$((FAIL+1))
        Q2_ID=""
    fi
    
    # 创建判断题
    Q3_RESP=$(curl -s -X POST "$BASE/api/admin/questions" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $ADMIN_TOKEN" \
        -d "{\"content\":\"测试题-1+1=2?\",\"type\":\"JUDGE\",\"courseId\":$COURSE_ID,\"chapterId\":$CH_ID,\"categoryId\":${CAT2_ID:-0},\"difficulty\":\"EASY\",\"options\":[{\"label\":\"A\",\"content\":\"正确\",\"isCorrect\":true},{\"label\":\"B\",\"content\":\"错误\",\"isCorrect\":false}],\"analysis\":\"基本判断\"}")
    
    echo "$Q3_RESP" | grep -q '"code":200'
    if [ $? -eq 0 ]; then
        Q3_ID=$(echo "$Q3_RESP" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
        echo "  ✅ 创建判断题 (id=$Q3_ID)"
        PASS=$((PASS+1))
    else
        echo "  ❌ 创建判断题失败: ${Q3_RESP:0:200}"
        FAIL=$((FAIL+1))
        Q3_ID=""
    fi
fi

if [ -n "$Q_ID" ]; then
    test_api "题目详情" GET "/api/admin/questions/$Q_ID"
    test_api "更新题目" PUT "/api/admin/questions/$Q_ID" \
        "{\"content\":\"测试题-1+1=?(已更新)\",\"type\":\"SINGLE\",\"courseId\":$COURSE_ID,\"chapterId\":$CH_ID,\"categoryId\":${CAT2_ID:-0},\"difficulty\":\"EASY\",\"options\":[{\"label\":\"A\",\"content\":\"1\",\"isCorrect\":false},{\"label\":\"B\",\"content\":\"2\",\"isCorrect\":true},{\"label\":\"C\",\"content\":\"3\",\"isCorrect\":false},{\"label\":\"D\",\"content\":\"4\",\"isCorrect\":false}],\"analysis\":\"基础加法\"}"
fi

# ==================== 5. 试卷管理 ====================
echo ""
echo "--- 5. 试卷管理 ---"
test_api "试卷列表" GET "/api/admin/exams"

# 创建试卷
if [ -n "$Q_ID" ] && [ -n "$Q2_ID" ] && [ -n "$Q3_ID" ]; then
    EXAM_RESP=$(curl -s -X POST "$BASE/api/admin/exams" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $ADMIN_TOKEN" \
        -d "{\"title\":\"测试试卷-综合测试\",\"description\":\"全面功能测试试卷\",\"categoryId\":${CAT2_ID:-0},\"duration\":60,\"totalScore\":100,\"passScore\":60,\"questionIds\":[$Q_ID,$Q2_ID,$Q3_ID],\"type\":\"FIXED\"}")
    
    echo "$EXAM_RESP" | grep -q '"code":200'
    if [ $? -eq 0 ]; then
        EXAM_ID=$(echo "$EXAM_RESP" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
        echo "  ✅ 创建试卷 (id=$EXAM_ID)"
        PASS=$((PASS+1))
    else
        echo "  ❌ 创建试卷失败: ${EXAM_RESP:0:200}"
        FAIL=$((FAIL+1))
        EXAM_ID=""
    fi
fi

if [ -n "$EXAM_ID" ]; then
    test_api "试卷详情" GET "/api/admin/exams/$EXAM_ID"
    test_api "发布试卷" PUT "/api/admin/exams/$EXAM_ID/publish"
    test_api "更新试卷" PUT "/api/admin/exams/$EXAM_ID" \
        "{\"title\":\"测试试卷(已更新)\",\"description\":\"更新后试卷\",\"categoryId\":${CAT2_ID:-0},\"duration\":90,\"totalScore\":100,\"passScore\":60,\"questionIds\":[$Q_ID,$Q2_ID,$Q3_ID],\"type\":\"FIXED\"}"
fi

# 随机组卷
if [ -n "$CAT2_ID" ]; then
    test_api "随机组卷" POST "/api/admin/exams/random" \
        "{\"title\":\"随机组卷测试\",\"categoryId\":$CAT2_ID,\"duration\":60,\"totalScore\":100,\"passScore\":60,\"singleCount\":1,\"singleScore\":30,\"multipleCount\":1,\"multipleScore\":30,\"judgeCount\":1,\"judgeScore\":40}"
fi

# ==================== 6. 学员管理 ====================
echo ""
echo "--- 6. 学员管理 ---"
test_api "学员列表" GET "/api/admin/students"
test_api "学员列表(分页)" GET "/api/admin/students?page=1&size=10&keyword=13800001001"

# ==================== 7. 订单管理 ====================
echo ""
echo "--- 7. 订单管理 ---"
test_api "订单列表" GET "/api/admin/orders"
test_api "订单列表(分页)" GET "/api/admin/orders?page=1&size=10"

# ==================== 8. 证书管理 ====================
echo ""
echo "--- 8. 证书管理 ---"
test_api "证书列表" GET "/api/admin/certificates"

# ==================== 9. 打卡管理 ====================
echo ""
echo "--- 9. 打卡管理 ---"
test_api "打卡记录" GET "/api/admin/checkins"

# ==================== 10. 数据统计 ====================
echo ""
echo "--- 10. 数据统计 ---"
test_api "仪表盘" GET "/api/admin/statistics/dashboard"
test_api "营收统计" GET "/api/admin/statistics/revenue"
test_api "学情统计" GET "/api/admin/statistics/learning"
test_api "考试统计" GET "/api/admin/statistics/exam"

# ==================== 11. H5学员端 ====================
echo ""
echo "--- 11. H5学员端 ---"

# 学员登录
STU_RESP=$(curl -s -X POST "$BASE/api/user/login" \
    -H "Content-Type: application/json" \
    -d '{"phone":"13800001001","password":"123456"}')

STU_TOKEN=$(echo "$STU_RESP" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
if [ -n "$STU_TOKEN" ]; then
    echo "  ✅ 学员登录成功 (13800001001)"
    PASS=$((PASS+1))
else
    # 试试注册
    REG_RESP=$(curl -s -X POST "$BASE/api/user/register" \
        -H "Content-Type: application/json" \
        -d '{"phone":"13800001001","password":"123456","nickname":"测试学员"}')
    STU_TOKEN=$(echo "$REG_RESP" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    if [ -n "$STU_TOKEN" ]; then
        echo "  ✅ 学员注册+登录成功 (13800001001)"
        PASS=$((PASS+1))
    else
        echo "  ❌ 学员登录/注册失败"
        echo "  登录: ${STU_RESP:0:200}"
        echo "  注册: ${REG_RESP:0:200}"
        FAIL=$((FAIL+1))
    fi
fi

TOKEN="$STU_TOKEN"

# H5 分类
test_api "H5-分类树" GET "/api/categories/tree"

if [ -n "$CAT2_ID" ]; then
    test_api "H5-分类详情" GET "/api/categories/$CAT2_ID"
fi

# H5 课程
test_api "H5-课程列表" GET "/api/courses"
test_api "H5-课程列表(分页)" GET "/api/courses?page=1&size=10"

if [ -n "$COURSE_ID" ]; then
    test_api "H5-课程详情" GET "/api/courses/$COURSE_ID"
fi

# H5 订单
if [ -n "$CAT2_ID" ]; then
    test_api "H5-购买分类" POST "/api/order/create" \
        "{\"productType\":\"CATEGORY\",\"categoryId\":$CAT2_ID}"
fi

test_api "H5-我的订单" GET "/api/order/my-orders"

# H5 我的课程
test_api "H5-我的课程" GET "/api/user/my-courses"

# H5 用户信息
test_api "H5-用户信息" GET "/api/user/info"

# H5 错题本
test_api "H5-错题本" GET "/api/user/wrong-questions"

# H5 考试记录
test_api "H5-考试记录" GET "/api/order/my-orders"

# ==================== 12. 前端页面可达性 ====================
echo ""
echo "--- 12. 前端页面可达性 ---"

test_page() {
    local desc="$1"
    local url="$2"
    http_code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 10 "$url")
    if [ "$http_code" = "200" ]; then
        echo "  ✅ $desc (HTTP $http_code)"
        PASS=$((PASS+1))
    else
        echo "  ❌ $desc - HTTP $http_code"
        FAIL=$((FAIL+1))
    fi
}

# Admin 页面
test_page "Admin-登录页" "http://127.0.0.1:5173/#/login"
test_page "Admin-首页" "http://127.0.0.1:5173/"

# H5 页面
test_page "H5-首页" "http://127.0.0.1:3001/"
test_page "H5-课程列表" "http://127.0.0.1:3001/#/courses"

# ==================== 13. 清理测试数据 ====================
echo ""
echo "--- 13. 清理测试数据 ---"
TOKEN="$ADMIN_TOKEN"

if [ -n "$EXAM_ID" ]; then
    test_api "删除试卷" DELETE "/api/admin/exams/$EXAM_ID"
fi

if [ -n "$Q3_ID" ]; then
    test_api "删除判断题" DELETE "/api/admin/questions/$Q3_ID"
fi
if [ -n "$Q2_ID" ]; then
    test_api "删除多选题" DELETE "/api/admin/questions/$Q2_ID"
fi
if [ -n "$Q_ID" ]; then
    test_api "删除单选题" DELETE "/api/admin/questions/$Q_ID"
fi

if [ -n "$CH_ID" ]; then
    test_api "删除章节" DELETE "/api/admin/courses/$COURSE_ID/chapters/$CH_ID"
fi

if [ -n "$COURSE_ID" ]; then
    test_api "删除课程" DELETE "/api/admin/courses/$COURSE_ID"
fi

if [ -n "$CAT2_ID" ]; then
    test_api "删除二级分类" DELETE "/api/admin/categories/$CAT2_ID"
fi

if [ -n "$CAT1_ID" ]; then
    test_api "删除一级分类" DELETE "/api/admin/categories/$CAT1_ID"
fi

echo ""
echo "============================================"
echo "  测试完成: 通过 $PASS / 失败 $FAIL"
echo "  总计: $((PASS+FAIL)) 项测试"
echo "============================================"
