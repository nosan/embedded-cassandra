/*
 * Copyright 2018-2019 the original author or authors.
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

package com.github.nosan.embedded.cassandra.test.support;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @DisabledIfOs} is used to signal that the annotated test class or
 * test method is <em>disabled</em> on one or more specified {@link #value operating systems}.
 * <p>The typical usage of this annotation is like:
 * <pre class="code">
 * public class SomeTests {
 * &#064;Rule
 * public final OSRule osRule = new OSRule();
 * &#064;Test
 * &#064;DisableIfOs("windows")
 * public void testMeExceptWindows(){
 * }
 * }
 * </pre>
 *
 * @author Dmytro Nosan
 * @see OSRule
 * @since 1.4.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface DisableIfOS {

	/**
	 * Operating systems on which the annotated class or method should be
	 * disabled.
	 *
	 * @return the operating systems
	 */
	String[] value();
}
