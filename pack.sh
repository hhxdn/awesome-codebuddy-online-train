#!/bin/bash
set -e
BASE="/Users/ascendking/IdeaProjects/my_projects/awesome-codebuddy-online-train"
cd "$BASE"
rm -f h5-dist.zip admin-dist.zip
cd h5/dist && zip -rq ../../h5-dist.zip . && cd ../..
cd admin/dist && zip -rq ../../admin-dist.zip . && cd ../..
echo "=== h5 ==="
unzip -l h5-dist.zip | grep -E "index\.html|index-B5pw|index-Cq8c" | head -5
echo "=== admin ==="
unzip -l admin-dist.zip | grep -E "index\.html|index-C5RR" | head -5
