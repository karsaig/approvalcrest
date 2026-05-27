#!/usr/bin/env bash
set -exuo pipefail

jvtr=8

version=$1

mvn versions:set -DnewVersion="${version}"
mvn versions:commit

mvn -f for-release-pom.xml versions:set -DnewVersion="${version}"
mvn -f for-release-pom.xml versions:commit

git commit -a -m "Release version ${version}"
git tag -a "v${version}" -m "Release ${version}"

mvn clean install -Djava.version.to.run="${jvtr}"

mvn clean -f for-release-pom.xml
mvn -f for-release-pom.xml deploy -P sign-release

git push --follow-tags
