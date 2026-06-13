#!/bin/bash
cd /Users/ascendking/IdeaProjects/my_projects/awesome-codebuddy-online-train/backend
mvn compile -DskipTests -q 2>&1 | tail -5
