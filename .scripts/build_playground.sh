#!/bin/bash
SCRIPT_DIR=`dirname "$0"`
cd "$SCRIPT_DIR/../playground/"
echo "Building playgound"
./gradlew clean build
