# Embedded Cassandra TestNG
[![Maven Central](https://img.shields.io/maven-central/v/com.github.nosan/embedded-cassandra.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.nosan/embedded-cassandra-testng)
 

For running `Embedded Cassandra` with `TestNG`, `AbstractCassandraTests` has to be extended.

```java
public class CassandraTests extends AbstractCassandraTestNG {

	@BeforeMethod
	public void setUp() {
		executeScripts(new ClassPathCqlResource("init.cql"));
	}

	@Test
	public void select() {
		assertThat(getSession().execute("SELECT * FROM  test.roles").wasApplied())
				.isTrue();
	}

}
```

By default `Embedded Cassandra` will be started on the random ports.
`Embedded Cassandra` configuration could be overriden via `super(...) Constructor`.


## Maven

```xml
<dependencies>
    <!--TestNG-->
    <dependency>
        <groupId>com.github.nosan</groupId>
        <artifactId>embedded-cassandra-testng</artifactId>
        <version>${version}</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```





