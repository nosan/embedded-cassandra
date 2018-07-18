# Embedded Cassandra JUnit5 
[![Maven Central](https://img.shields.io/maven-central/v/com.github.nosan/embedded-cassandra.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.nosan/embedded-cassandra-jupiter)


For running `Embedded Cassandra ` with `Junit 5`, `@RegisterExtension` has to be used. 

```java
public class CassandraTests {

	@RegisterExtension
	public static CassandraExtension cassandra = new CassandraExtension();

	@BeforeEach
	public void setUp() {
		CqlScripts.executeScripts(cassandra.getSession(), new ClassPathCqlResource("init.cql"));
	}

	@Test
	public void select() {
		assertThat(cassandra.getSession().execute("query").wasApplied())
				.isTrue();
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
        <version>${embedded-cassandra-jupiter.version}</version>
         <scope>test</scope>
     </dependency>
    <dependency>
        <groupId>org.yaml</groupId>
        <artifactId>snakeyaml</artifactId>
        <version>${snakeyaml.version}</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>com.datastax.cassandra</groupId>
        <artifactId>cassandra-driver-core</artifactId>
        <version>${cassandra-driver-core.version}</version>
        <scope>test</scope>
    </dependency>   
</dependencies>
```





