# Embedded Cassandra ![Build Status](https://travis-ci.org/nosan/embedded-cassandra.svg?branch=master)

`Embedded Cassandra` provides an easy way to run `Cassandra` in the unit tests.

## License

This project use (http://www.apache.org/licenses/LICENSE-2.0).

## Java

Minimum `java` version is `1.8`.

## Dependencies
Embedded Cassandra has `compile` dependency on `de.flapdoodle.embed:de.flapdoodle.embed.process:2.0.3` and 
`optional-compile` dependency  on `org.yaml:snakeyaml:1.21` (also supports lower versions)

## Usage

Following code will create an `Embedded Cassandra` with the default properties.

```java
public class CassandraTests {

	@Test
	public void createUserTable(){
		CassandraServerStarter cassandraServerStarter = new CassandraServerStarter();
		CassandraProcessConfig processConfig = new CassandraProcessConfig();
		CassandraServerExecutable executable = cassandraServerStarter
				.prepare(processConfig);
		executable.start();
		try (Cluster cluster = cluster(processConfig.getConfig())) {
        			Session session = cluster.connect();
        			//
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
		try (Cluster cluster = cluster(cassandra.getConfig())) {
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
		try (Cluster cluster = cluster(getConfig())) {
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
        <version>0.0.1</version>
        <scope>test</scope>
    </dependency>

    <!--JUnit-->
    <dependency>
        <groupId>com.github.nosan</groupId>
        <artifactId>embedded-cassandra-junit</artifactId>
        <version>0.0.1</version>
        <scope>test</scope>
    </dependency>

    <!--TestNG-->
    <dependency>
        <groupId>com.github.nosan</groupId>
        <artifactId>embedded-cassandra-testng</artifactId>
        <version>0.0.1</version>
        <scope>test</scope>
    </dependency>
    
</dependencies>
```





