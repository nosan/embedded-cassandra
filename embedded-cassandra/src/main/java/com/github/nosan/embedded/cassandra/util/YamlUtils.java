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

package com.github.nosan.embedded.cassandra.util;

import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

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
import org.yaml.snakeyaml.representer.Representer;

import com.github.nosan.embedded.cassandra.config.Config;

/**
 * Utility class for serializing and deserializing{@link Config config}.
 *
 * @author Dmytro Nosan
 */
public abstract class YamlUtils {

	/**
	 * Serialize Cassandra config.
	 * @param config cassandra config.
	 * @param writer yaml output.
	 */
	public static void serialize(Config config, Writer writer) {
		Objects.requireNonNull(config, "Config must not be null");
		Objects.requireNonNull(writer, "Writer must not be null");
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		Yaml yaml = new Yaml(new ConfigConstructor(), new ConfigSerializer(), options);
		yaml.dump(config, writer);
	}

	/**
	 * Deserialize Cassandra config from the YAML resource.
	 * @param stream yaml resource.
	 * @return Cassandra config
	 */
	public static Config deserialize(InputStream stream) {
		Objects.requireNonNull(stream, "InputStream must not be null");
		Yaml yaml = new Yaml(new ConfigConstructor());
		return yaml.loadAs(stream, Config.class);
	}

	/**
	 * Utility class for serializing {@link Config.ParameterizedClass}.
	 */
	private static final class ConfigSerializer extends Representer {

		private static final Method REPRESENT_SEQUENCE;

		static {
			Method method = null;
			try {
				method = BaseRepresenter.class.getDeclaredMethod("representSequence",
						Tag.class, Iterable.class, Boolean.class);
			}
			catch (Throwable ignore) {
			}

			REPRESENT_SEQUENCE = method;
		}

		private ConfigSerializer() {
			addClassTag(Config.class, Tag.MAP);

			this.representers.put(Config.ParameterizedClass.class, (data) -> {
				Config.ParameterizedClass parameterizedClass = (Config.ParameterizedClass) data;
				Map<String, Object> source = new LinkedHashMap<>();
				source.put("class_name", parameterizedClass.getClassName());
				source.put("parameters",
						Collections.singletonList(parameterizedClass.getParameters()));

				if (REPRESENT_SEQUENCE != null) {
					try {
						return (Node) REPRESENT_SEQUENCE.invoke(this, Tag.SEQ,
								Collections.singletonList(source), false);
					}
					catch (InvocationTargetException | IllegalAccessException ex) {
						throw new IllegalStateException(ex);
					}
				}

				return representSequence(Tag.SEQ, Collections.singletonList(source),
						DumperOptions.FlowStyle.AUTO);
			});
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

	}

	/**
	 * A custom constructor for overriding {@link BeanAccess}.
	 */
	private static final class ConfigConstructor extends Constructor {

		private ConfigConstructor() {
			super(Config.class);
			PropertyUtils propertyUtils = new PropertyUtils();
			propertyUtils.setBeanAccess(BeanAccess.FIELD);
			setPropertyUtils(propertyUtils);
		}

	}

}
