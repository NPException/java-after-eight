#!/bin/bash
set -e

java -version
echo ""

echo "building..."
rm -f recommendations.json
rm -rf jars/*
mvn clean verify -q

./stats-time.sh java --enable-preview -p jars -m org.codefx.java_after_eight.genealogy
./stats-code.sh
