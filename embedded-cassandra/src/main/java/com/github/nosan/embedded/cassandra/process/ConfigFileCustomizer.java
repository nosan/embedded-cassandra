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

package com.github.nosan.embedded.cassandra.process;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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

import com.github.nosan.embedded.cassandra.Config;
import com.github.nosan.embedded.cassandra.ExecutableConfig;

/**
 * {@link FileCustomizer File Customizer} which serializes {@link Config } into the
 * cassandra.yaml file.
 *
 * @author Dmytro Nosan
 */
class ConfigFileCustomizer extends AbstractFileCustomizer {

	@Override
	protected boolean isMatch(File file, Context context) throws IOException {
		return file.getName().equals("cassandra.yaml");
	}

	@Override
	protected void process(File file, Context context) throws IOException {
		Config config = getConfig(context);
		try (FileWriter writer = new FileWriter(file)) {
			DumperOptions options = new DumperOptions();
			options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			Yaml yaml = new Yaml(new ConfigConstructor(), new ConfigSerializer(),
					options);
			yaml.dump(config, writer);
		}
	}

	private Config getConfig(Context context) {
		ExecutableConfig executableConfig = context.getExecutableConfig();
		return executableConfig.getConfig();
	}

	/**
	 * Utility class to serialize {@link Config.ParameterizedClass}.
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
	 * A custom constructor to use {@link BeanAccess#FIELD Field Access}.
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
