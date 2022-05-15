#!/usr/bin/env bash

java.version.to.run=${java.version.to.run}
set -exuo pipefail

version=$1

mvn versions:set -DnewVersion="${version}"
mvn versions:commit

mvn -f for-release-pom.xml versions:set -DnewVersion="${version}"
mvn -f for-release-pom.xml versions:commit

git commit -a -m "Release version ${version}"

mvn clean install -Djava.version.to.run="${java.version.to.run}"
mvn clean -Djava.version.to.run="${java.version.to.run}"


mvn -f for-release-pom.xml clean deploy -P sign-release,ossrh --settings ../../Installed/settings.xml -DskipRemoteStaging=true -Djava.version.to.run="${java.version.to.run}"

mvn -f for-release-pom.xml nexus-staging:deploy-staged -P ossrh --settings ../../Installed/settings.xml -DstagingDescription="Description of the staged repository" -Djava.version.to.run="${java.version.to.run}"

git clean -f
git push
