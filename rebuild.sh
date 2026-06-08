#!/bin/bash
set -e
cd /Users/ascendking/IdeaProjects/my_projects/awesome-codebuddy-online-train

echo "=== Stopping old processes ==="
lsof -ti:8088 -ti:18088 -ti:3001 | xargs kill -9 2>/dev/null || true
sleep 2

echo "=== Building backend ==="
cd backend
mvn compile -q
mvn package -DskipTests -q
echo "Backend built OK"

echo "=== Starting backend ==="
nohup java -jar target/online-train-backend-1.0.0.jar > /tmp/backend.log 2>&1 &
echo "Backend PID: $!"

echo "=== Building H5 ==="
cd ../h5
npm run build
echo "H5 built OK"

echo "=== Starting H5 ==="
nohup npm run dev > /tmp/h5.log 2>&1 &
echo "H5 PID: $!"

echo "=== All done ==="
