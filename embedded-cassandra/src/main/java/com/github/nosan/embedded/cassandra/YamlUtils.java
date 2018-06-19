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
package com.github.nosan.embedded.cassandra;

import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.nosan.embedded.cassandra.config.CassandraConfig;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.BaseRepresenter;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

/**
 * Utility class for serializing {@link CassandraConfig config}.
 *
 * @author Dmytro Nosan
 */
abstract class YamlUtils {

	static void serialize(CassandraConfig config, Writer writer) {
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		Yaml yaml = new Yaml(new ConfigConstructor(), new ConfigRepresenter(), options);
		yaml.dump(config, writer);
	}

	/**
	 * Utility class for serializing {@link CassandraConfig.ParameterizedClass}.
	 * Represents a block: seed_provider: - class_name:
	 * org.apache.cassandra.locator.SimpleSeedProvider parameters: - seeds: "localhost"
	 */
	private static final class ConfigRepresenter extends Representer {

		private ConfigRepresenter() {
			this.representers.put(CassandraConfig.ParameterizedClass.class,
					new RepresentParameterizedClass(this));
			addClassTag(CassandraConfig.class, Tag.MAP);
		}

		@Override
		protected NodeTuple representJavaBeanProperty(Object javaBean, Property property,
				Object propertyValue, Tag customTag) {

			if (propertyValue == null) {
				return null;
			}
			return super.representJavaBeanProperty(javaBean, property, propertyValue,
					customTag);
		}

		private Node representSequence(Tag tag, Iterable<?> iterable) {
			// snake <= 1.19
			try {
				Method method = BaseRepresenter.class.getDeclaredMethod(
						"representSequence", Tag.class, Iterable.class, Boolean.class);

				return (Node) method.invoke(this, tag, iterable, false);
			}
			catch (NoSuchMethodException ignore) {
			}
			catch (Throwable ex) {
				throw new IllegalStateException(ex);
			}

			return super.representSequence(tag, iterable, DumperOptions.FlowStyle.BLOCK);
		}

		private static final class RepresentParameterizedClass implements Represent {

			private final ConfigRepresenter delegate;

			RepresentParameterizedClass(ConfigRepresenter delegate) {
				this.delegate = delegate;
			}

			@Override
			public Node representData(Object data) {

				CassandraConfig.ParameterizedClass parameterizedClass = (CassandraConfig.ParameterizedClass) data;

				LinkedHashMap<String, Object> source = new LinkedHashMap<>();

				if (parameterizedClass.getParameters() != null) {
					List<Map<String, String>> parameters = parameterizedClass
							.getParameters().entrySet().stream()
							.map((entry) -> Collections.singletonMap(entry.getKey(),
									entry.getValue()))
							.collect(Collectors.toList());
					source.put("parameters", parameters);
				}

				if (parameterizedClass.getClassName() != null) {
					source.put("class_name", parameterizedClass.getClassName());
				}

				return this.delegate.representSequence(Tag.SEQ,
						Collections.singletonList(source));
			}

		}

	}

	private static final class ConfigConstructor extends Constructor {

		private ConfigConstructor() {
			super(CassandraConfig.class);
			PropertyUtils propertyUtils = new PropertyUtils();
			propertyUtils.setBeanAccess(BeanAccess.FIELD);
			setPropertyUtils(propertyUtils);
		}

	}

}
