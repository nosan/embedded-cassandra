# Embedded Cassandra [![Build Status OSX/Linux](https://img.shields.io/travis/nosan/embedded-cassandra/master.svg?logo=travis&logoColor=white&style=flat)](https://travis-ci.org/nosan/embedded-cassandra) [![Build Status Windows](https://img.shields.io/appveyor/ci/nosan/embedded-cassandra/master.svg?logo=appveyor&logoColor=white&style=flat)](https://ci.appveyor.com/project/nosan/embedded-cassandra)
`Embedded Cassandra` provides an easy way to run [Apache Cassandra](https://cassandra.apache.org/) and extensions to test your code.

You can find more information _here_:
 - [Embedded Cassandra 2.X.X](https://github.com/nosan/embedded-cassandra/wiki).
 - [Embedded Cassandra 1.X.X](https://github.com/nosan/embedded-cassandra/wiki/1.X.X).

Here is a quick example how to run `Apache Cassandra`:

```java
import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraFactory;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.local.LocalCassandraFactory;

class Scratch {

	public static void main(String[] args) {
		CassandraFactory cassandraFactory = new LocalCassandraFactory();
		Cassandra cassandra = cassandraFactory.create();
		cassandra.start();
		try {
			Settings settings = cassandra.getSettings();
		}
		finally {
			cassandra.stop();
		}
	}

}
```


#### Issues

`Embedded Cassandra` uses GitHub's issue tracking system to report bugs and feature
requests. If you want to raise an issue, please follow this [link](https://github.com/nosan/embedded-cassandra/issues)
and use predefined `GitHub` templates.

Also see [CONTRIBUTING.md](CONTRIBUTING.md) if you wish to submit pull requests.

#### Build

`Embedded Cassandra` can be easily built with the [maven wrapper](https://github.com/takari/maven-wrapper). You also need `JDK 1.8`.

```bash
$ ./mvnw clean install
```

#### License

This project uses [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
