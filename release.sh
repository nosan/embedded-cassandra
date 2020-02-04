#!/bin/bash

set -e

function abort() {
  echo "$*" >&2
  exit 1
}

function usage() {
  echo "Usage: $0 [ -r RELEASE_VERSION -n NEXT_DEVELOPMENT_VERSION ]"
}

while getopts ":r:n:" opt; do
  case ${opt} in
  r)
    RELEASE_VERSION="${OPTARG}"
    ;;
  n)
    DEVELOPMENT_VERSION="${OPTARG}"
    ;;
  \?)
    usage
    abort "Invalid option: '-${OPTARG}'" 1>&2
    ;;
  :)
    usage
    abort "Invalid option: '-${OPTARG}' requires an argument" 1>&2
    ;;
  esac
done
shift $((OPTIND - 1))

if [ -z "${RELEASE_VERSION}" ]; then
  usage
  abort "The Release version is not set"
fi

if [ -z "${DEVELOPMENT_VERSION}" ]; then
  usage
  abort "Next development version is not set"
fi

if [ "$(git status -s)" ]; then
  git status -s
  abort "There are uncommitted or untracked changes, please commit or stash them to continue with the release!"
fi

VCS_RELEASE_TAG="${RELEASE_VERSION}"
GIT_REMOTE=$(git config remote.origin.url)
BASE_DIRECTORY=$(pwd)
PAGES_DIRECTORY="embedded-cassandra-docs/target/generated-docs/"

if [ -z "${GIT_REMOTE}" ]; then
  abort "'git config remote.origin.url' is not present"
fi

if [ -z "${BASE_DIRECTORY}" ]; then
  abort "Directory is not detected!"
fi

if [ "$(git rev-parse -q --verify "refs/tags/${VCS_RELEASE_TAG}")" ]; then
  abort "A tag '${VCS_RELEASE_TAG}' already exists. Use 'git tag -d ${VCS_RELEASE_TAG}' to delete a tag."
fi

#clean
./mvnw -q clean || abort "Failed to clean project!"

#set release version
./mvnw -q versions:set -DnewVersion="${RELEASE_VERSION}" versions:commit || abort "Failed to set release version!"
git commit -a -m "Release version ${RELEASE_VERSION}" || abort "Failed to commit a release version!"

#deploy to nexus
./mvnw clean deploy -V -B -Pembedded-cassandra-release,embedded-cassandra-docs -DskipTests || (git reset --hard HEAD^1 || abort "Git reset command failed!" && abort "Aborted")

#prepare gh-pages
cd "${PAGES_DIRECTORY}"
git init || abort "Git cannot be initialized"
git checkout --orphan gh-pages || abort "Git cannot checkout gh-pages branch"
git remote add origin "${GIT_REMOTE}" || abort "Git cannot set origin for gh-pages branch"
git add . || abort "Git cannot add gh-pages resources"
git commit -m "Update Embedded Cassandra Reference Documentation ${VERSION}" || abort "Failed to commit gh-pages resources!"
cd "${BASE_DIRECTORY}"

#create release tag
git tag "${VCS_RELEASE_TAG}" || abort "Failed to create a tag ${VCS_RELEASE_TAG}!"

#set next development version
./mvnw -q versions:set -DnewVersion="${DEVELOPMENT_VERSION}" versions:commit || abort "Failed to set next development version!"
git commit -a -m "Start next development version ${DEVELOPMENT_VERSION}" || abort "Failed to commit next development version!"

read -n1 -r -p "Proceed with Git deploy? [y]" proceed
case $proceed in
y | Y)
  cd "${PAGES_DIRECTORY}"
  git push -f origin gh-pages || abort "Failed to push gh-pages!"
  cd "${BASE_DIRECTORY}"
  rm -rf "${PAGES_DIRECTORY}" || abort "Failed 'rm -rf' ${PAGES_DIRECTORY}"
  git push || abort "Failed to push commits!"
  git push --tags || abort "Failed to push tags!"
  ./mvnw clean -q || abort "Failed to clean project!"
  ;;
*)
  echo ""
  git reset --hard HEAD~2 || abort "Git reset command failed!"
  git tag -d "${VCS_RELEASE_TAG}" || abort "Failed to delete a tag ${VCS_RELEASE_TAG}!"
  ./mvnw clean -q || abort "Failed to clean project!"
  ;;
esac
