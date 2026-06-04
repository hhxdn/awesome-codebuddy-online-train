#!/bin/bash
# 打包前端产物并推送到 Git
set -e
cd "$(dirname "$0")"

echo "=== 打包 h5-dist.zip ==="
rm -f h5-dist.zip
cd h5/dist && zip -rq ../../h5-dist.zip . && cd ../..

echo "=== 打包 admin-dist.zip ==="
rm -f admin-dist.zip
cd admin/dist && zip -rq ../../admin-dist.zip . && cd ../..

echo "=== 验证 h5-dist.zip ==="
echo "h5 index.html 引用:"
unzip -p h5-dist.zip index.html | grep -E "script.*src|link.*css"

echo "=== 验证 admin-dist.zip ==="
echo "admin index.html 引用:"
unzip -p admin-dist.zip index.html | grep -E "script.*src|link.*css"

echo ""
echo "=== 推送到 Git ==="
git add h5-dist.zip admin-dist.zip
git commit -m "chore: 重新打包前端产物" || true
git push gitee master
git push github master

echo ""
echo "=== 完成 ==="
echo "上传到服务器后执行:"
echo "  cd /opt/online-train"
echo "  cd /opt/online-train/h5 && unzip -o /tmp/h5-dist.zip"
echo "  cd /opt/online-train/admin && unzip -o /tmp/admin-dist.zip"
echo "  chown -R www:www /opt/online-train/"
echo "  nginx -s reload"
