#!/bin/bash
SCRIPT_DIR=`dirname "$0"`
cd "$SCRIPT_DIR/../playground/"
echo "Building playgound"
./gradlew clean build -PpluginVersion=${1:-1.0.0-SNAPSHOT}
