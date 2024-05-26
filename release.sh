#!/usr/bin/env bash

jvtr=8
set -exuo pipefail

version=$1

mvn versions:set -DnewVersion="${version}"
mvn versions:commit

mvn -f for-release-pom.xml versions:set -DnewVersion="${version}"
mvn -f for-release-pom.xml versions:commit

git commit -a -m "Release version ${version}"

mvn clean install -Djava.version.to.run="${jvtr}"
mvn clean -Djava.version.to.run="${jvtr}"


#mvn -f for-release-pom.xml clean deploy -P sign-release,ossrh --settings ../../Installed/settings.xml -DskipRemoteStaging=true -Djava.version.to.run="${jvtr}"

#mvn -f for-release-pom.xml nexus-staging:deploy-staged -P ossrh --settings ../../Installed/settings.xml -DstagingDescription="Description of the staged repository" -Djava.version.to.run="${jvtr}"

mvn -f for-release-pom.xml clean deploy -P sign-release,ossrh -DskipRemoteStaging=true -Djava.version.to.run="${jvtr}"

mvn -f for-release-pom.xml nexus-staging:deploy-staged -P ossrh -DstagingDescription="Description of the staged repository" -Djava.version.to.run="${jvtr}"

git clean -f
git push
