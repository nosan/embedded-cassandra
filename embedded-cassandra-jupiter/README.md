# Embedded Cassandra JUnit5 
[![Maven Central](https://img.shields.io/maven-central/v/com.github.nosan/embedded-cassandra.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.nosan/embedded-cassandra-jupiter)


For running `Embedded Cassandra ` with `Junit 5`, `@RegisterExtension` has to be used. 

```java
public class CassandraTests {

	@RegisterExtension
	public static CassandraExtension cassandra = new CassandraExtension();

	@BeforeEach
	public void setUp() {
		cassandra.executeScripts(new ClassPathCqlResource("init.cql"));
	}

	@Test
	public void select() {
		assertThat(cassandra.getSession().execute("SELECT * FROM  test.roles").wasApplied())
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
        <version>${version}</version>
         <scope>test</scope>
     </dependency>
</dependencies>
```





