/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nosan.embedded.cassandra.support;

import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.config.store.IPackageResolver;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;

/**
 * {@link IPackageResolver Package Resolver} for cassandra
 * {@link com.github.nosan.embedded.cassandra.Version#LATEST LATEST} version.
 *
 * @author Dmytro Nosan
 */
public class LatestPackageResolver implements IPackageResolver {

	@Override
	public FileSet getFileSet(Distribution distribution) {

		FileSet.Builder builder = FileSet.builder();

		switch (distribution.getPlatform()) {
			case Windows:
				builder.addEntry(FileType.Executable,
						"apache-cassandra-3.11.2/bin/cassandra.ps1");
				break;

			default:
				builder.addEntry(FileType.Executable,
						"apache-cassandra-3.11.2/bin/cassandra");
		}
		builder.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/bin/cassandra.in.bat")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/bin/cassandra.bat")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/bin/cassandra.in.sh")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/bin/cqlsh")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/bin/cqlsh.bat")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/bin/cqlsh.py")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/bin/debug-cql")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/bin/debug-cql.bat")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/bin/nodetool")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/bin/nodetool.bat")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/bin/source-conf.ps1")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/bin/sstableloader")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/bin/sstableloader.bat")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/bin/sstablescrub")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/bin/sstablescrub.bat")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/bin/sstableupgrade")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/bin/sstableupgrade.bat")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/bin/sstableutil")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/bin/sstableutil.bat")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/bin/sstableverify")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/bin/sstableverify.bat")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/bin/stop-server")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/bin/stop-server.bat")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/bin/stop-server.ps1")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/conf/cassandra-env.ps1")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/conf/cassandra-env.sh")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/conf/cassandra-jaas.config")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/conf/cassandra-rackdc.properties")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/conf/cassandra-topology.properties")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/conf/cassandra.yaml")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/conf/commitlog_archiving.properties")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/conf/cqlshrc.sample")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/conf/hotspot_compiler")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/conf/jvm.options")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/conf/logback-tools.xml")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/conf/logback.xml")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/conf/metrics-reporter-config-sample.yaml")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/conf/triggers/README.txt")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/interface/cassandra.thrift")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/HdrHistogram-2.1.9.jar")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/lib/ST4-4.0.8.jar")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/lib/airline-0.6.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/antlr-runtime-3.5.2.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/apache-cassandra-3.11.2.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/apache-cassandra-thrift-3.11.2.jar")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/lib/asm-5.0.4.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/caffeine-2.2.6.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/cassandra-driver-core-3.0.1-shaded.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/cassandra-driver-internal-only-3.10.zip")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/cassandra-driver-internal-only-3.11.0-bb96859b.zip")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/commons-cli-1.1.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/commons-codec-1.9.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/commons-lang3-3.1.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/commons-math3-3.2.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/compress-lzf-0.8.4.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/concurrent-trees-2.4.0.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/concurrentlinkedhashmap-lru-1.4.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/disruptor-3.0.1.jar")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/lib/ecj-4.4.2.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/futures-2.1.6-py2.py3-none-any.zip")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/lib/guava-18.0.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/high-scale-lib-1.0.6.jar")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/lib/hppc-0.5.4.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/jackson-core-asl-1.9.13.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/jackson-mapper-asl-1.9.13.jar")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/lib/jamm-0.3.0.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/javax.inject.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/jbcrypt-0.3m.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/jcl-over-slf4j-1.7.7.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/jctools-core-1.2.1.jar")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/lib/jflex-1.6.0.jar")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/lib/jna-4.2.2.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/joda-time-2.4.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/json-simple-1.1.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/jstackjunit-0.0.1.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/libthrift-0.9.2.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/log4j-over-slf4j-1.7.7.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/logback-classic-1.1.3.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/logback-core-1.1.3.jar")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/lib/lz4-1.3.0.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/metrics-core-3.1.0.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/metrics-jvm-3.1.0.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/metrics-logback-3.1.0.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/netty-all-4.0.44.Final.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/ohc-core-0.4.4.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/ohc-core-j8-0.4.4.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/reporter-config-base-3.0.3.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/reporter-config3-3.0.3.jar")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/lib/sigar-1.6.4.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/sigar-bin/libsigar-amd64-freebsd-6.so")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/sigar-bin/libsigar-amd64-linux.so")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/sigar-bin/libsigar-amd64-solaris.so")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/sigar-bin/libsigar-ia64-hpux-11.sl")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/sigar-bin/libsigar-ia64-linux.so")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/sigar-bin/libsigar-pa-hpux-11.sl")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/sigar-bin/libsigar-ppc-aix-5.so")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/sigar-bin/libsigar-ppc-linux.so")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/sigar-bin/libsigar-ppc64-aix-5.so")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/sigar-bin/libsigar-ppc64-linux.so")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/sigar-bin/libsigar-s390x-linux.so")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/sigar-bin/libsigar-sparc-solaris.so")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/sigar-bin/libsigar-sparc64-solaris.so")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/sigar-bin/libsigar-universal-macosx.dylib")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/sigar-bin/libsigar-universal64-macosx.dylib")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/sigar-bin/libsigar-x86-freebsd-5.so")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/sigar-bin/libsigar-x86-freebsd-6.so")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/sigar-bin/libsigar-x86-linux.so")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/sigar-bin/libsigar-x86-solaris.so")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/sigar-bin/sigar-amd64-winnt.dll")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/sigar-bin/sigar-x86-winnt.dll")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/sigar-bin/sigar-x86-winnt.lib")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/six-1.7.3-py2.py3-none-any.zip")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/slf4j-api-1.7.7.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/snakeyaml-1.11.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/snappy-java-1.1.1.7.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/snowball-stemmer-1.3.0.581.1.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/stream-2.5.2.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/lib/thrift-server-0.3.7.jar")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/pylib/cqlshlib/__init__.py")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/pylib/cqlshlib/copyutil.py")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/pylib/cqlshlib/cql3handling.py")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/pylib/cqlshlib/cqlhandling.py")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/pylib/cqlshlib/cqlshhandling.py")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/pylib/cqlshlib/displaying.py")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/pylib/cqlshlib/formatting.py")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/pylib/cqlshlib/helptopics.py")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/pylib/cqlshlib/pylexotron.py")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/pylib/cqlshlib/saferscanner.py")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/pylib/cqlshlib/sslhandling.py")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/pylib/cqlshlib/test/__init__.py")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/pylib/cqlshlib/test/ansi_colors.py")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/pylib/cqlshlib/test/basecase.py")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/pylib/cqlshlib/test/cassconnect.py")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/pylib/cqlshlib/test/run_cqlsh.py")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/pylib/cqlshlib/test/test_cql_parsing.py")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/pylib/cqlshlib/test/test_cqlsh_commands.py")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/pylib/cqlshlib/test/test_cqlsh_completion.py")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/pylib/cqlshlib/test/test_cqlsh_invocation.py")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/pylib/cqlshlib/test/test_cqlsh_output.py")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/pylib/cqlshlib/test/test_cqlsh_parsing.py")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/pylib/cqlshlib/test/test_keyspace_init.cql")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/pylib/cqlshlib/test/winpty.py")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/pylib/cqlshlib/tracing.py")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/pylib/cqlshlib/util.py")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/pylib/cqlshlib/wcwidth.py")
				.addEntry(FileType.Library, "apache-cassandra-3.11.2/pylib/setup.py")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/tools/bin/cassandra-stress")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/tools/bin/cassandra-stress.bat")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/tools/bin/cassandra-stressd")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/tools/bin/cassandra.in.bat")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/tools/bin/cassandra.in.sh")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/tools/bin/compaction-stress")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/tools/bin/sstabledump")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/tools/bin/sstabledump.bat")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/tools/bin/sstableexpiredblockers")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/tools/bin/sstableexpiredblockers.bat")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/tools/bin/sstablelevelreset")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/tools/bin/sstablelevelreset.bat")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/tools/bin/sstablemetadata")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/tools/bin/sstablemetadata.bat")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/tools/bin/sstableofflinerelevel")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/tools/bin/sstableofflinerelevel.bat")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/tools/bin/sstablerepairedset")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/tools/bin/sstablerepairedset.bat")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/tools/bin/sstablesplit")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/tools/bin/sstablesplit.bat")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/tools/cqlstress-counter-example.yaml")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/tools/cqlstress-example.yaml")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/tools/cqlstress-insanity-example.yaml")
				.addEntry(FileType.Library,
						"apache-cassandra-3.11.2/tools/lib/stress.jar");

		return builder.build();

	}

	@Override
	public ArchiveType getArchiveType(Distribution distribution) {
		return ArchiveType.TGZ;
	}

	@Override
	public String getPath(Distribution distribution) {
		return "/cassandra/" + "3.11.2" + "/apache-cassandra-3.11.2-bin" + ".tar.gz";
	}

}
