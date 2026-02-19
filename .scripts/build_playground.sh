#!/bin/bash
SCRIPT_DIR=`dirname "$0"`
cd "$SCRIPT_DIR/../playground/"
echo "Building playground"
if [ -n "$2" ]; then
    echo "Using AGP version: $2"
    ./gradlew clean build -PpluginVersion=${1:-1.0.0-SNAPSHOT} -PagpVersion=$2
else
    ./gradlew clean build -PpluginVersion=${1:-1.0.0-SNAPSHOT}
fi
