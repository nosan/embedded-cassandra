# Embedded Cassandra 

[![Maven Central](https://img.shields.io/maven-central/v/com.github.nosan/embedded-cassandra.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.nosan/embedded-cassandra)
[![Build Status (Travis: Linux/OSX)](https://img.shields.io/travis/nosan/embedded-cassandra/master.svg?label=linux%2Fosx%20%28java%208%209%2010%29)](https://travis-ci.org/nosan/embedded-cassandra) 
[![Build Status (AppVeyor: Windows)](https://img.shields.io/appveyor/ci/viliusl/wix-embedded-mysql/master.svg?label=windows%20%28java%208%29)](https://ci.appveyor.com/project/viliusl/wix-embedded-mysql) 


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

Following code creates `Embedded Cassandra` with the default configuration. 

```java
public class CassandraTests {

	@Test
	public void test() throws Exception {
		CassandraStarter cassandraStarter = new CassandraStarter();
		ExecutableConfig executableConfig = new ExecutableConfigBuilder().build();
		CassandraExecutable executable = cassandraStarter.prepare(executableConfig);
		try {
			executable.start();
			try (Cluster cluster = cluster(executableConfig.getConfig())) {
				Session session = cluster.connect();
				//
			}
		}
		finally {
			executable.stop();
		}
	}
	
	private static Cluster cluster(Config config) {
    		return Cluster.builder().addContactPoint(config.getRpcAddress())
    				.withPort(config.getNativeTransportPort()).build(); 
	}

}

```

For start `Embedded Cassandra` on the random ports it is possible to use either `0` for port properties
 or `new ExecutableConfigBuilder().useRandomPorts().build()`  method.
 
### JUnit

`Embedded Cassandra` also could be run via JUnit `ClassRule`. 

```java
public class CassandraTests {

	@ClassRule
	public static Cassandra cassandra = new Cassandra();

	@Test
	public void test() throws Exception {
		try (Cluster cluster = cluster(cassandra.getExecutableConfig().getConfig())) {
			Session session = cluster.connect();
			//
		}
	}
	
	private static Cluster cluster(Config config) {
    		return Cluster.builder().addContactPoint(config.getRpcAddress())
    				.withPort(config.getNativeTransportPort()).build(); 
	}	
}
```

By default `Embedded Cassandra` will be started on the random ports.
`Embedded Cassandra` configuration could be overriden via `constructor`.



### TestNG

For running `Embedded Cassandra` with `TestNG`, `AbstractCassandraTests` has to be extended.

```java
public class CassandraTests extends AbstractCassandraTests {


	@Test
	public void test() throws Exception{
		try (Cluster cluster = cluster(getExecutableConfig().getConfig())) {
			Session session = cluster.connect();
			//
		}
	}
	
	private static Cluster cluster(Config config) {
    		return Cluster.builder().addContactPoint(config.getRpcAddress())
    				.withPort(config.getNativeTransportPort()).build(); 
	}
}
```

By default `Embedded Cassandra` will be started on the random ports.
`Embedded Cassandra` configuration could be overriden via `super(...) Constructor`.

### JUnit5

For running `Embedded Cassandra ` with `Junit 5`, `@RegisterExtension` has to be used. 

```java
public class CassandraTests {

	
	@RegisterExtension
	public static Cassandra cassandra = new Cassandra();
	
	@Test
	public void test() throws Exception {
		try (Cluster cluster = cluster(cassandra.getExecutableConfig().getConfig())) {
			Session session = cluster.connect();
		}
	}
	
	private static Cluster cluster(Config config) {
    		return Cluster.builder().addContactPoint(config.getRpcAddress())
    				.withPort(config.getNativeTransportPort()).build(); 
	}

}
```

By default `Embedded Cassandra` will be started on the random ports.
`Embedded Cassandra` configuration could be overriden via `constructor`.

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
        <version>${version}</version>
        <scope>test</scope>
    </dependency>

    <!--JUnit-->
    <dependency>
        <groupId>com.github.nosan</groupId>
        <artifactId>embedded-cassandra-junit</artifactId>
        <version>${version}</version>
        <scope>test</scope>
    </dependency>

    <!--TestNG-->
    <dependency>
        <groupId>com.github.nosan</groupId>
        <artifactId>embedded-cassandra-testng</artifactId>
        <version>${version}</version>
        <scope>test</scope>
    </dependency>
    
    <!--JUnit5-->
    
     <dependency>
        <groupId>com.github.nosan</groupId>
        <artifactId>embedded-cassandra-jupiter</artifactId>
        <version>${version}</version>
         <scope>test</scope>
     </dependency>
    

    
</dependencies>
```





