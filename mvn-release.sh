#!/bin/sh
# shellcheck disable=SC2039
read -r -p "Release Version: " releaseVersion
read -r -p "Development Version: " developmentVersion
read -r -p "Release: '${releaseVersion}' and Development: '${developmentVersion}'. Y/N? " answer

if [ "${answer}" != "Y" ]; then
  echo "Abort"
  exit 1
fi

git reset HEAD --hard && git clean -df
revesion=$(git rev-parse HEAD)

release() {
  echo "Release..."
  ./mvnw versions:set -DnewVersion="${releaseVersion}" && ./mvnw versions:commit &&
    git add . && git commit -m "Release: ${releaseVersion}" && git tag "${releaseVersion}" &&
    ./mvnw clean deploy -Prelease -B -V -DskipTests && ./mvnw clean &&
    ./mvnw versions:set -DnewVersion="${developmentVersion}" && ./mvnw versions:commit &&
    git add . && git commit -m "Next Development Version: ${developmentVersion}"
}

rollback() {
  echo "Rollback...."
  git reset --hard "${revesion}" && git rev-parse -q --verify "refs/tags/${releaseVersion}" && git tag -d "${releaseVersion}"
}

release || rollback
