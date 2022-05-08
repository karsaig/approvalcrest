#!/usr/bin/env bash

java.version.to.run=${java.version.to.run}
set -exuo pipefail

version=$1

mvn versions:set --global-toolchains .github/ubuntu-toolchains.xml -DnewVersion="${version}"
mvn versions:commit --global-toolchains .github/ubuntu-toolchains.xml

mvn -f for-release-pom.xml versions:set --global-toolchains .github/ubuntu-toolchains.xml -DnewVersion="${version}"
mvn -f for-release-pom.xml versions:commit --global-toolchains .github/ubuntu-toolchains.xml

git commit -a -m "Release version ${version}"

mvn clean install --global-toolchains .github/ubuntu-toolchains.xml -Djava.version.to.run="${java.version.to.run}"
mvn clean --global-toolchains .github/ubuntu-toolchains.xml -Djava.version.to.run="${java.version.to.run}"


mvn -f for-release-pom.xml clean deploy -P sign-release,ossrh --settings ../../Installed/settings.xml --global-toolchains .github/ubuntu-toolchains.xml -DskipRemoteStaging=true -Djava.version.to.run="${java.version.to.run}"

mvn -f for-release-pom.xml nexus-staging:deploy-staged -P ossrh --settings ../../Installed/settings.xml --global-toolchains .github/ubuntu-toolchains.xml -DstagingDescription="Description of the staged repository" -Djava.version.to.run="${java.version.to.run}"

git clean -f
git push
