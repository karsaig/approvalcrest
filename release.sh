#/bin/bash

set -euo pipefail

version=$1

mvn versions:set -DnewVersion="${version}"
mvn versions:commit

mvn -f release-pom.xml versions:set -DnewVersion="${version}"
mvn -f release-pom.xml versions:commit

git commit -a -m "Next release version"

mvn clean install

mvn -f release-pom.xml clean deploy -P sign-release,ossrh --settings ../../Installed/settings.xml
