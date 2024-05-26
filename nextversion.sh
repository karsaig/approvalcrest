#!/usr/bin/env bash

set -exuo pipefail

version=$1

mvn versions:set -DnewVersion="${version}"
mvn versions:commit

mvn -f for-release-pom.xml versions:set -DnewVersion="${version}"
mvn -f for-release-pom.xml versions:commit

git commit -a -m "Next version ${version}"
