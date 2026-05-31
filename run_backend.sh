#!/bin/bash
cd /Users/ascendking/IdeaProjects/my_projects/awesome-codebuddy-online-train/backend
# 杀掉旧进程
kill $(lsof -t -i:8088) 2>/dev/null
sleep 2
# 启动
nohup java -jar target/online-train-backend-1.0.0.jar --server.port=8088 > /tmp/backend.log 2>&1 &
echo "启动完成 PID=$!"
