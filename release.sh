#/bin/bash

set -euo pipefail

version=$1

mvn versions:set -DnewVersion="${version}"
mvn versions:commit

mvn -f for-release-pom.xml versions:set -DnewVersion="${version}"
mvn -f for-release-pom.xml versions:commit

git commit -a -m "Release version ${version}"

mvn clean install
mvn clean


mvn -f for-release-pom.xml clean deploy -P sign-release,ossrh --settings ../../Installed/settings.xml -DskipRemoteStaging=true

mvn -f for-release-pom.xml nexus:deploy-staged -P ossrh --settings ../../Installed/settings.xml -DstagingDescription="Description of the staged repository"

git clean -f
#git push
