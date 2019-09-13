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
  ./mvnw clean deploy -Prelease,docs -B -V -DskipTests
}

nextRelease() {
  ./mvnw clean -q && ./mvnw versions:set -DnewVersion="${release_version}" -q && ./mvnw versions:commit -q &&
    git add . && git commit -m "Release: '${release_version}'" && deleteTag "${release_version}" && git tag "${release_version}"

}

nextDevevelopment() {
  ./mvnw clean -q && ./mvnw versions:set -DnewVersion="${development_version}" -q && ./mvnw versions:commit -q &&
    git add . && git commit -m "Next development version: '${development_version}'"
}

pages() {
  docs="embedded-cassandra-docs/target/generated-docs/"
  git add -f "${docs}" && git stash push -- "${docs}" &&
    deleteBranch "gh-pages" && git checkout -f "gh-pages" && git reset --hard HEAD && git clean -fd && rm -rf -- * &&
    git add . && git commit -m "Prepare to Release: '${release_version}'" &&
    git stash pop && cp -r "${docs}" . && rm -rf "embedded-cassandra-docs" &&
    git add . && git commit -m "Release: '${release_version}'" &&
    git checkout -f "${branch_name}" && git reset --hard HEAD && git clean -fd
}

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

printf "\e[1;34mRelease Version: '%s' and Development Version: '%s'. 'YES' ? \e[0m\n" "${release_version}" "${development_version}"
read -r answer

if [ "${answer}" != "YES" ]; then
  printf "\e[1;91mAborted! Use 'YES' to procced a release!\e[0m\n"
  exit 1
fi

if [ "$(git status --porcelain)" ]; then
  printf "\e[1;91mCommit or Revert following files:\e[0m\n%s\n" "$(git status --porcelain)"
  exit 1
fi

branch_name=$(git branch | grep "\*" | cut -d ' ' -f2)
revesion=$(git rev-parse HEAD)

nextRelease && deploy && pages && nextDevevelopment

exitCode=$?

if [ "${exitCode}" = "0" ]; then
  printf "\e[1;34mDeploy 'gh-pages', '%s' and '%s'. 'YES' ?\e[0m\n" "${release_version}" "${branch_name}"
  read -r answer
  if [ "${answer}" = "YES" ]; then
    git push "${branch_name}" && git push "gh-pages" && git push --tags
  fi
else
  printf "\e[1;91mROLLBACK!\e[0m\n"
  git reset --hard "${revesion}" && deleteTag "${release_version}" && deleteBranch "gh-pages"
fi
