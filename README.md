Embedded Cassandra 
===========

## Getting Started

Embedded Cassandra provides an easy way to start and stop [Apache Cassandra](https://cassandra.apache.org).

To learn more about Embedded Cassandra, please consult the [reference documentation](https://nosan.github.io/embedded-cassandra/4.1.1).

All versions of Embedded Cassandra reference documentation are [here](https://nosan.github.io/embedded-cassandra).

Here is a quick teaser of starting Cassandra: 

```java
import java.net.InetSocketAddress;

import com.datastax.oss.driver.api.core.CqlSession;

import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraBuilder;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.cql.CqlScript;

public class CassandraExample {

	public static void main(String[] args) {
		Cassandra cassandra = new CassandraBuilder().build();
		cassandra.start();
		try {
			Settings settings = cassandra.getSettings();
			try (CqlSession session = CqlSession.builder()
					.addContactPoint(new InetSocketAddress(settings.getAddress(), settings.getPort()))
					.withLocalDatacenter("datacenter1")
					.build()) {
				CqlScript.ofClassPath("schema.cql").forEachStatement(session::execute);
			}
		}
		finally {
			cassandra.stop();
		}
	}

}
```

## Issues

`Embedded Cassandra` uses GitHub's issue tracking system to report bugs and feature requests. If you want to raise an
issue, please follow this [link](https://github.com/nosan/embedded-cassandra/issues)

Also see [CONTRIBUTING.md](CONTRIBUTING.md) if you wish to submit pull requests.

## Build

`Embedded Cassandra` can be easily built with the [maven wrapper](https://github.com/takari/maven-wrapper). You also need `JDK 1.8`.

## License

Embedded Cassandra is released under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

___

[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/donate/?business=D3ESQ4RY4XN7J&no_recurring=0&currency_code=USD) <a href="https://www.buymeacoffee.com/nosan" target="_blank">
