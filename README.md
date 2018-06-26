# Embedded Cassandra ![Build Status](https://travis-ci.org/nosan/embedded-cassandra.svg?branch=master)

`Embedded Cassandra` provides an easy way to run `Cassandra` in the unit tests. `Embedded Cassandra` is built 
on top of [Flapdoodle OSS's embed process](https://github.com/flapdoodle-oss/de.flapdoodle.embed.process).

## License

This project uses [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## Java

Minimum `java` version is `1.8`. Also supports `java 9` and `java 10`.

## Dependencies
Embedded Cassandra has `compile` dependency on `de.flapdoodle.embed:de.flapdoodle.embed.process:2.0.5` and 
`optional-compile` dependency  on `org.yaml:snakeyaml:1.21` (also supports lower versions)

## Usage

Following code will create an `Embedded Cassandra` with the default configuration.

```java
public class CassandraTests {

	@Test
	public void createUserTable() throws IOException {
		CassandraStarter cassandraStarter = new CassandraStarter();
		CassandraConfig cassandraConfig = new CassandraConfigBuilder().build();
		CassandraExecutable executable = cassandraStarter.prepare(cassandraConfig);
		try {
			executable.start();
			try (Cluster cluster = cluster(cassandraConfig.getConfig())) {
				Session session = cluster.connect();
				//
			}
		}
		finally {
			executable.stop();
		}
	}

}
```

### JUnit

`Embedded Cassandra` also could be run via JUnit `ClassRule`. `Embedded Cassandra` will be started on the random ports.

```java
public class CassandraTests {

	@ClassRule
	public static Cassandra cassandra = new Cassandra();

	@Test
	public void createUserTable() {
		try (Cluster cluster = cluster(cassandra.getCassandraConfig().getConfig())) {
			Session session = cluster.connect();
			//
		}
	}
}
```


### TestNG

For running `Embedded Cassandra` with `TestNG` you should extend `AbstractCassandraTests` class.
`Embedded Cassandra` will be started on the random ports.
 

```java
public class CassandraTests extends AbstractCassandraTests {


	@Test
	public void createUserTable() {
		try (Cluster cluster = cluster(getCassandraConfig().getConfig())) {
			Session session = cluster.connect();
			//
		}
	}

}
```


## Maven

```xml
<dependencies>

    <dependency>
        <groupId>org.yaml</groupId>
        <artifactId>snakeyaml</artifactId>
        <version>1.21</version>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>com.github.nosan</groupId>
        <artifactId>embedded-cassandra</artifactId>
        <version>0.0.2</version>
        <scope>test</scope>
    </dependency>

    <!--JUnit-->
    <dependency>
        <groupId>com.github.nosan</groupId>
        <artifactId>embedded-cassandra-junit</artifactId>
        <version>0.0.2</version>
        <scope>test</scope>
    </dependency>

    <!--TestNG-->
    <dependency>
        <groupId>com.github.nosan</groupId>
        <artifactId>embedded-cassandra-testng</artifactId>
        <version>0.0.2</version>
        <scope>test</scope>
    </dependency>
    
</dependencies>
```





