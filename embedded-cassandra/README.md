# Embedded Cassandra
[![Maven Central](https://img.shields.io/maven-central/v/com.github.nosan/embedded-cassandra.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.nosan/embedded-cassandra)
 

`Embedded Cassandra` provides an easy way to run `Cassandra` in the unit tests.

## Usage

Following code creates `Embedded Cassandra` with the default configuration. 

```java

public class CassandraTests {

	private final EmbeddedCassandra cassandra = new EmbeddedCassandra();

	@Before
	public void setUp() throws Exception {
		this.cassandra.start();
		this.cassandra.executeScripts(new ClassPathCqlResource("init.cql"));
	}

	@After
	public void tearDown() throws Exception {
		this.cassandra.stop();

	}

	@Test
	public void test() throws IOException {
		assertThat(this.cassandra.getSession().execute("SELECT * FROM  test.roles").wasApplied())
				.isTrue();
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

