#!/usr/bin/env bash
set -exuo pipefail

jvtr=8

version=$1
dry_run=false
if [[ "${2:-}" == "--dry-run" ]]; then
    dry_run=true
    echo "========================================"
    echo "  DRY RUN MODE"
    echo "  Running all release steps locally."
    echo "  Nothing will be pushed to git remote."
    echo "  Nothing will be published to Maven Central."
    echo "  Local git changes will be undone at the end."
    echo "========================================"
fi

mvn versions:set -DnewVersion="${version}"
mvn versions:commit

mvn -f for-release-pom.xml versions:set -DnewVersion="${version}"
mvn -f for-release-pom.xml versions:commit

git commit -a -m "Release version ${version}"
git tag -a "v${version}" -m "Release ${version}"

mvn clean install -Djava.version.to.run="${jvtr}"

if [[ "$dry_run" == "true" ]]; then
    mvn clean verify -f for-release-pom.xml -P sign-release
    git tag -d "v${version}"
    git reset --hard HEAD~1
    echo "========================================"
    echo "  DRY RUN COMPLETE"
    echo "  All checks passed. No remote changes made."
    echo "  Local state restored to pre-release."
    echo "========================================"
else
    mvn clean -f for-release-pom.xml
    mvn -f for-release-pom.xml deploy -P sign-release
    git push --follow-tags
fi
