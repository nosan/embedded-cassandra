#!/bin/sh
# shellcheck disable=SC2039
read -r -p "Release Version: " releaseVersion
read -r -p "Development Version: " developmentVersion

echo "Release: ${releaseVersion} and Development: ${developmentVersion}. Y/N"

read -r -p proceed

if [ "${proceed}" != "Y" ]; then
  echo "Abort"
  exit 1
fi

release() {
  ./mvnw versions:set -DnewVersion="${releaseVersion}" -DgenerateBackupPoms=false && ./mvnw clean verify -Prelease -DskipTests && git reset HEAD --hard && git clean -df &&
    ./mvnw release:prepare -Prelease -DautoVersionSubmodules=true -Dtag="${releaseVersion}" -DreleaseVersion="${releaseVersion}" -DdevelopmentVersion="${developmentVersion}" &&
    ./mvnw release:perform -Prelease
}

rollback() {
  ./mvnw release:rollback && ./mvnw release:clean && git tag -d "${releaseVersion}" && git push origin :refs/tags/"${releaseVersion}"
}

release || rollback
