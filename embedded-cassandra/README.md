# Embedded Cassandra
[![Maven Central](https://img.shields.io/maven-central/v/com.github.nosan/embedded-cassandra.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.nosan/embedded-cassandra)
 

`Embedded Cassandra` provides an easy way to run `Cassandra` in the unit tests.

## Usage

Following code creates `Embedded Cassandra` with the default configuration. 

```java

public class CassandraTests {

	private static Cluster cluster(Config config) {
		return Cluster.builder().addContactPoint(config.getRpcAddress())
				.withPort(config.getNativeTransportPort()).build();
	}

	@Test
	public void test() throws Exception {
		Cassandra cassandra = new Cassandra();
		try {
			cassandra.start();
			ExecutableConfig executableConfig = cassandra.getExecutableConfig();
			Config config = executableConfig.getConfig();
			try (Cluster cluster = cluster(config)) {
				//tests 
			}
		}
		finally {
			cassandra.stop();
		}

	}
}

```

It is possible to use a custom configuration via `constructor`. 

## Maven

```xml
<dependencies>
    <dependency>
        <groupId>com.github.nosan</groupId>
        <artifactId>embedded-cassandra</artifactId>
        <version>${version}</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

