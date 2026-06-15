#!/bin/bash
PROJECT_DIR="/Users/ascendking/IdeaProjects/my_projects/awesome-codebuddy-online-train/backend"
cd "$PROJECT_DIR"
# First try to remove target if a previous build left it in a bad state
rm -rf target 2>/dev/null
# Build
mvn clean package -DskipTests -q
exit_code=$?
if [ $exit_code -eq 0 ]; then
    echo "BUILD_OK"
else
    echo "BUILD_FAIL"
fi
exit $exit_code
