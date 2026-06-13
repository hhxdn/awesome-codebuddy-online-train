#!/bin/bash
set -e

PROJECT_DIR="/Users/ascendking/IdeaProjects/my_projects/awesome-codebuddy-online-train"
LOG_DIR="/tmp"

echo "===== 1. 停止旧进程 ====="
lsof -ti:8088 -ti:18088 -ti:3001 2>/dev/null | xargs kill -9 2>/dev/null || true
echo "旧进程已停止"

echo "===== 2. 构建后端 JAR ====="
cd "$PROJECT_DIR/backend"
mvn clean package -DskipTests -q
if [ $? -ne 0 ]; then
    echo "后端构建失败，退出"
    exit 1
fi
echo "后端构建成功"

echo "===== 3. 构建 Admin 管理后台 ====="
cd "$PROJECT_DIR/admin"
npm run build
if [ $? -ne 0 ]; then
    echo "Admin 构建失败，退出"
    exit 1
fi
echo "Admin 构建成功"

echo "===== 4. 构建 H5 学员端 ====="
cd "$PROJECT_DIR/h5"
npm run build
if [ $? -ne 0 ]; then
    echo "H5 构建失败，退出"
    exit 1
fi
echo "H5 构建成功"

echo "===== 5. 压缩产物 ====="
cd "$PROJECT_DIR/admin/dist" && zip -rq "$PROJECT_DIR/admin-dist.zip" . && echo "admin-dist.zip 完成"
cd "$PROJECT_DIR/h5/dist" && zip -rq "$PROJECT_DIR/h5-dist.zip" . && echo "h5-dist.zip 完成"

echo "===== 6. 启动后端 ====="
cd "$PROJECT_DIR/backend"
nohup java -jar target/online-train-backend-1.0.0.jar > /tmp/backend.log 2>&1 &
echo "后端启动中，PID=$!"

echo "===== 7. 启动 Admin ====="
cd "$PROJECT_DIR/admin"
nohup npm run dev > /tmp/admin.log 2>&1 &
echo "Admin 启动中"

echo "===== 8. 启动 H5 ====="
cd "$PROJECT_DIR/h5"
nohup npm run dev > /tmp/h5.log 2>&1 &
echo "H5 启动中"

echo "===== 完成 ====="
echo "后端: http://localhost:8088"
echo "Admin: http://localhost:18088/admin/"
echo "H5: http://localhost:3001/h5/"
