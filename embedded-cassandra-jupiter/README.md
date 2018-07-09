# Embedded Cassandra JUnit5 

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

By default `Embedded Cassandra` will be started on the random ports. `Embedded Cassandra` configuration could be overriden via `constructor`.

## Maven

```xml
<dependencies>
     <dependency>
        <groupId>com.github.nosan</groupId>
        <artifactId>embedded-cassandra-jupiter</artifactId>
        <version>${version}</version>
         <scope>test</scope>
     </dependency>
</dependencies>
```





