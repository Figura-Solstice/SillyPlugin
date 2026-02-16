#!/bin/bash
mkdir -p ./silly_out
rm ./silly_out/*
./gradlew clean
./gradlew chiseledBuild

for dir in ./versions/*/; do
  ver=$(basename "$dir")
  path=$(ls ./versions/"$ver"/build/libs/sillyplugin-*.jar)
  filename=$(basename "$path")
  filename=${filename%.jar}
  echo "copying silly plugin $ver to silly_out!"
  cp "$path" "./silly_out/$filename-$ver.jar"
done