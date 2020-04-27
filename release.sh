#/bin/bash

set -euo pipefail

mvn versions:set -DnewVersion="$1"
mvn versions:commit
git commit -a -m "Next release version"

mvn clean install
