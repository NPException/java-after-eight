#!/bin/bash
set -e
export SDKMAN_DIR="$HOME/.sdkman"
[[ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]] && source "$HOME/.sdkman/bin/sdkman-init.sh"

git log --pretty='%h %s' -1
javaVersion=$(git log --pretty='%s' -1 | sed -n 's/Upgrade to Java \([[:digit:]]\+\).*/\1/p')
if [ -n "$javaVersion" ]
then
	sdk use java jdk-"$javaVersion"
fi
# java -version 2>&1 | head -n 1

# building
rm -f recommendations.json
rm -rf jars/*
mvn clean verify -q

./stats-time.sh java -XX:+IgnoreUnrecognizedVMOptions --enable-preview -XX:+ShowCodeDetailsInExceptionMessages -cp jars/genealogy.jar:jars/genealogists.jar org.codefx.java_after_eight.Main | grep "runtime"
./stats-code.sh
