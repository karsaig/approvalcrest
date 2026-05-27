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

# State tracking for cleanup
_commit_made=false
_tag_made=false
_push_done=false

cleanup() {
    # Always remove versionsBackup files — they are never needed after versions:commit
    find . -name "*.versionsBackup" -delete 2>/dev/null || true
    # If push already succeeded the release is done — leave local state as-is
    if [[ "$_push_done" == "true" ]]; then
        return
    fi
    if [[ "$_tag_made" == "true" ]]; then
        git tag -d "v${version}" 2>/dev/null || true
    fi
    if [[ "$_commit_made" == "true" ]]; then
        git reset --hard HEAD~1 || true
    else
        # versions:set ran but commit was never made — restore pom files
        git checkout -- . 2>/dev/null || true
    fi
}
trap cleanup EXIT

# Pre-flight: check if the tag already exists
if git tag -l "v${version}" | grep -q "v${version}"; then
    if git ls-remote --tags origin "refs/tags/v${version}" 2>/dev/null | grep -q "v${version}"; then
        echo "ERROR: tag 'v${version}' already exists and has been pushed to remote." >&2
        echo "       Use a different version number." >&2
    else
        echo "ERROR: tag 'v${version}' already exists locally (not yet pushed)." >&2
        echo "       To remove it and retry: git tag -d 'v${version}'" >&2
    fi
    exit 1
fi

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
_commit_made=true
git tag -a "v${version}" -m "Release ${version}"
_tag_made=true

mvn clean install -Djava.version.to.run="${jvtr}"

if [[ "$dry_run" == "true" ]]; then
    mvn clean verify -f for-release-pom.xml -P sign-release
    echo "========================================"
    echo "  DRY RUN COMPLETE"
    echo "  All checks passed. No remote changes made."
    echo "  Local state restored to pre-release."
    echo "========================================"
else
    mvn clean -f for-release-pom.xml
    mvn -f for-release-pom.xml deploy -P sign-release
    git push --follow-tags
    _push_done=true
fi
