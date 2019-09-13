#!/bin/sh

set -e

deleteTag() {
  if [ "$(git rev-parse -q --verify "refs/tags/$1")" ]; then
    git tag -d "$1"
  fi
}

deleteBranch() {
  if [ "$(git rev-parse -q --verify "$1")" ]; then
    git branch -D "$1"
  fi
}

deploy() {
  ./mvnw clean verify -Prelease,docs -B -V -DskipTests
}

nextRelease() {
  ./mvnw clean && ./mvnw versions:set -DnewVersion="${release_version}" && ./mvnw versions:commit &&
    git add . && git commit -m "Release: '${release_version}'" && deleteTag "${release_version}" && git tag "${release_version}"

}

nextDevevelopment() {
  ./mvnw clean && ./mvnw versions:set -DnewVersion="${development_version}" && ./mvnw versions:commit &&
    git add . && git commit -m "Next development version: '${development_version}'"
}

ghPages() {
  docs="embedded-cassandra-docs/target/generated-docs/"
  git add -f "${docs}" && git stash push -- "${docs}" &&
    deleteBranch "${gh_pages_branch}" && git checkout -f "${gh_pages_branch}" && git reset --hard HEAD && git clean -fd && rm -rf -- * &&
    git add . && git commit -m "Prepare to Release: '${release_version}'" &&
    git stash pop && cp -r "${docs}" . && rm -rf "embedded-cassandra-docs" &&
    git add . && git commit -m "Release: '${release_version}'" &&
    git checkout -f "${branch}" && git reset --hard HEAD && git clean -fd
}

parseArgs() {
  while [ $# -gt 0 ]; do
    case "$1" in
    --release)
      release_version="$2"
      shift
      ;;
    --development)
      development_version="$2"
      shift
      ;;
    *)
      shift
      ;;
    esac
  done

  if [ ! "${release_version}" ]; then
    printf "\e[1;91m--release option is absent\e[0m. Use --release <version>.\n"
    exit 1
  fi

  if [ ! "${development_version}" ]; then
    printf "\e[1;91m--development option is absent\e[0m. Use --development <version>.\n"
    exit 1
  fi

  if [ "$(git status --porcelain)" ]; then
    printf "\e[1;91mCommit or Revert following files:\e[0m\n%s\n" "$(git status --porcelain)"
    exit 1
  fi

}

parseArgs "$@"
git fetch origin
branch=$(git branch | grep "\*" | cut -d ' ' -f2)
gh_pages_branch="gh-pages"
revesion=$(git rev-parse HEAD)

nextRelease && deploy && ghPages && nextDevevelopment

exitCode=$?

if [ "${exitCode}" = "0" ]; then
  git --no-pager log --stat --oneline "${branch}"...origin/"${branch}"
  git --no-pager log --stat --oneline "${gh_pages_branch}"...origin/"${gh_pages_branch}"
else
  git reset --hard "${revesion}" && deleteTag "${release_version}" && deleteBranch "${gh_pages_branch}"
fi
