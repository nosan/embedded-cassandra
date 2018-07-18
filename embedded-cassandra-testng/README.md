# Embedded Cassandra TestNG
[![Maven Central](https://img.shields.io/maven-central/v/com.github.nosan/embedded-cassandra.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.nosan/embedded-cassandra-testng)
 

For running `Embedded Cassandra` with `TestNG`, `AbstractCassandraTests` has to be extended.

```java
public class CassandraTests extends AbstractCassandraTestNG {

	@BeforeMethod
	public void setUp() {
		CqlScripts.executeScripts(getSession(), new ClassPathCqlResource("init.cql"));
	}

	@Test
	public void select() {
		assertThat(getSession().execute("query").wasApplied())
				.isTrue();
	}

}
```

By default `Embedded Cassandra` will be started on the random ports.
`Embedded Cassandra` configuration could be overriden via `super(...) Constructor`.


## Maven

```xml
<dependencies>
    <dependency>
        <groupId>com.github.nosan</groupId>
        <artifactId>embedded-cassandra-testng</artifactId>
        <version>${embedded-cassandra-testng.version}</version>
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





