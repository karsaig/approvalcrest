#/bin/bash

set -euo pipefail

version=$1

mvn versions:set -DnewVersion="${version}"
mvn versions:commit
git commit -a -m "Next release version"

mvn clean install

mvn clean deploy -P sign-release,ossrh --settings ../../Installed/settings.xml