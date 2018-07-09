# Embedded Cassandra JUnit

`Embedded Cassandra` could be run via JUnit `ClassRule`. 

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



## Maven

```xml
<dependencies>

    <dependency>
        <groupId>com.github.nosan</groupId>
        <artifactId>embedded-cassandra-junit</artifactId>
        <version>${version}</version>
        <scope>test</scope>
    </dependency>
    
</dependencies>
```





