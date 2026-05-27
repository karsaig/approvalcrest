#!/usr/bin/env bash

show_help() {
    cat <<'EOF'
Usage: release.sh <version> [--dry-run] [--jdk <version>]

Arguments:
  <version>          Release version to set (e.g. 0.63.0)
  --dry-run          Test mode: runs all steps locally without pushing
                     to git remote or publishing to Maven Central.
                     Local git changes are undone at the end.
  --jdk <version>    JDK version to use for testing (default: 8)

Steps performed:
  1. Bump version in pom.xml and for-release-pom.xml
  2. git commit + annotated tag v<version>
  3. mvn clean install  (JDK <version>)
  4. mvn deploy -P sign-release via for-release-pom.xml
     (or mvn verify in --dry-run: signs artifacts but does not upload)
  5. git push --follow-tags  (skipped in --dry-run)

After a real release, bump to next snapshot with:
  ./nextversion.sh <next-snapshot-version>
EOF
}

version=""
dry_run=false
jvtr=8

while [[ $# -gt 0 ]]; do
    case "$1" in
        -h|--help|help)
            show_help; exit 0 ;;
        --dry-run)
            dry_run=true; shift ;;
        --jdk)
            jvtr="$2"; shift 2 ;;
        --jdk=*)
            jvtr="${1#--jdk=}"; shift ;;
        *)
            if [[ -z "$version" ]]; then
                version="$1"; shift
            else
                echo "Unknown argument: $1" >&2
                show_help; exit 1
            fi ;;
    esac
done

if [[ -z "$version" ]]; then
    show_help; exit 1
fi

set -exuo pipefail

if [[ "$dry_run" == "true" ]]; then
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
