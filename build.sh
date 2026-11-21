#!/bin/sh
# mac / Linux 构建脚本
set -e
cd "$(dirname "$0")"

JAVA_BIN=""
if command -v java >/dev/null 2>&1; then
  if java -version 2>&1 | grep -q 'version "21'; then JAVA_BIN=java; fi
fi
if [ -z "$JAVA_BIN" ] && [ -x ".jdk/bin/java" ]; then JAVA_BIN=".jdk/bin/java"; fi
if [ -z "$JAVA_BIN" ]; then
  echo "未找到 JDK 21，请先安装 JDK 21（例如 Temurin）"
  exit 1
fi
export JAVA_HOME="$(dirname "$(dirname "$(command -v "$JAVA_BIN")")")"
echo "使用 Java: $JAVA_BIN"

if [ ! -x ".gradle-dist/gradle-8.10.2/bin/gradle" ]; then
  echo "下载 Gradle 8.10.2 ..."
  curl -L -o gradle.zip https://services.gradle.org/distributions/gradle-8.10.2-bin.zip
  unzip -q gradle.zip -d .gradle-dist
  rm gradle.zip
fi

".gradle-dist/gradle-8.10.2/bin/gradle" --no-daemon build
echo "构建成功：ls build/libs"
