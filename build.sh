#!/bin/bash

DEV_FLAG="false"
if [ "$1" == "-dev" ]; then
  DEV_FLAG="true"
fi

mkdir -p ./silly_out
rm ./silly_out/*
./gradlew clean
./gradlew chiseledBuild -Psilly_dev_mode="$DEV_FLAG"

for dir in ./versions/*/; do
  ver=$(basename "$dir")
  path=$(ls ./versions/"$ver"/build/libs/sillyplugin-*.jar 2>/dev/null)
  if [ -z "$path" ]; then
    echo "$ver failed to build!";
    continue;
  fi
  filename=$(basename "$path")
  filename=${filename%.jar}
  echo "copying silly plugin $ver to silly_out!"
  cp "$path" "./silly_out/$filename-$ver.jar"
done
