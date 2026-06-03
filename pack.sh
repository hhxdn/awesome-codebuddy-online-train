#!/bin/bash
set -e
BASE="/Users/ascendking/IdeaProjects/my_projects/awesome-codebuddy-online-train"
cd "$BASE"
rm -rf _pkg h5-dist.zip admin-dist.zip
mkdir -p _pkg/h5 _pkg/admin
cp -r h5/dist _pkg/h5/
cp -r admin/dist _pkg/admin/
cd _pkg && zip -r ../h5-dist.zip h5/ && zip -r ../admin-dist.zip admin/
cd "$BASE" && rm -rf _pkg
echo "=== h5 ==="
unzip -l h5-dist.zip | grep -E "index\.html|index-B5pw|index-Cq8c" | head -5
echo "=== admin ==="
unzip -l admin-dist.zip | grep -E "index\.html|index-C5RR" | head -5
