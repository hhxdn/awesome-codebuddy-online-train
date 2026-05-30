#!/bin/bash
echo "=== 编译 + 重启后端 ==="
cd /Users/ascendking/IdeaProjects/my_projects/awesome-codebuddy-online-train/backend

# 编译
echo "编译中..."
mvn compile -q
if [ $? -ne 0 ]; then
    echo "编译失败！"
    exit 1
fi
echo "编译成功"

# 杀掉旧进程
OLD=$(lsof -t -i:8088 2>/dev/null)
if [ -n "$OLD" ]; then
    kill -9 $OLD
    sleep 2
fi

# 打包并启动
mvn package -DskipTests -q
nohup java -jar target/online-train-0.0.1-SNAPSHOT.jar > /tmp/backend.log 2>&1 &
echo "后端已启动 PID=$!"
sleep 5
echo "等待启动..."
sleep 5
curl -s http://127.0.0.1:8088/api/admin/auth/login -X POST -H "Content-Type: application/json" -d '{"account":"admin","password":"123456"}' | python3 -c "import sys,json;d=json.load(sys.stdin);print('状态:', d['code'])" 2>/dev/null || echo "还在启动中..."
echo "完成！"
