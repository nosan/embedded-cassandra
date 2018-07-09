# Embedded Cassandra 

[![Maven Central](https://img.shields.io/maven-central/v/com.github.nosan/embedded-cassandra.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.nosan/embedded-cassandra)
[![Build Status (Travis: Linux/OSX)](https://img.shields.io/travis/nosan/embedded-cassandra/master.svg?label=linux/osx)](https://travis-ci.org/nosan/embedded-cassandra) 
[![Build Status (AppVeyor: Windows)](https://img.shields.io/appveyor/ci/nosan/embedded-cassandra/master.svg?label=windows)](https://ci.appveyor.com/project/nosan/embedded-cassandra) 


`Embedded Cassandra` provides an easy way to run `Cassandra` in the unit tests. `Embedded Cassandra` is built 
on top of [Flapdoodle OSS's embed process](https://github.com/flapdoodle-oss/de.flapdoodle.embed.process).

## License

This project uses [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## Why?
 - It is much easier than installing right version by hand;
 - It is possible to use different versions/configuration per project without any local set-up;
 - Supports multiple platforms: `Windows`, `Linux` and `OSX`;
 - Provides different extensions for popular frameworks.
 - Easy to configure.
 - Experimentally supports `java 9` and `java 10` (only for `unix-like`).

## See More
 - [Embedded Cassandra](embedded-cassandra/README.md)
 - [Embedded Cassandra JUnit](embedded-cassandra-junit)
 - [Embedded Cassandra JUnit5](embedded-cassandra-jupiter)
 - [Embedded Cassandra TestNG](embedded-cassandra-testng)





