# Embedded Cassandra 

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

It is possible to use a custom configuration via `constuctor`. 

