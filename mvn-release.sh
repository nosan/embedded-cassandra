#!/bin/sh
# shellcheck disable=SC2039
read -r -p "Release Version: " releaseVersion
read -r -p "Development Version: " developmentVersion
read -r -p "Release: '${releaseVersion}' and Development: '${developmentVersion}'. Y/N? " answer

if [ "${answer}" != "Y" ]; then
  echo "Abort"
  exit 1
fi

release() {
  git reset HEAD --hard && git clean -df &&
    ./mvnw release:prepare -Prelease -DautoVersionSubmodules=true \
      -Dtag="${releaseVersion}" -DreleaseVersion="${releaseVersion}" \
      -DdevelopmentVersion="${developmentVersion}" &&
    ./mvnw release:perform -Prelease
}

rollback() {
  ./mvnw release:rollback && ./mvnw release:clean && git tag -d "${releaseVersion}" && git push origin :refs/tags/"${releaseVersion}"
}

release || rollback
