/*
 * Copyright 2020-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nosan.embedded.cassandra.commons.function;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Supplier that can safely throws {@link IOException}.
 *
 * @param <T> the supplied type
 * @author Dmytro Nosan
 * @see #wrap(Supplier)
 * @since 4.0.0
 */
@FunctionalInterface
public interface IOSupplier<T> {

	/**
	 * Wrap a provided {@link Supplier} into {@link IOSupplier}.
	 *
	 * @param supplier the underlying supplier
	 * @param <T> the supplied type
	 * @return the new {@link IOSupplier}
	 */
	static <T> IOSupplier<T> wrap(Supplier<? extends T> supplier) {
		Objects.requireNonNull(supplier, "Supplier must not be null");
		return supplier::get;
	}

	/**
	 * Gets the supplied value.
	 *
	 * @return the supplied value
	 * @throws IOException an I/O error occurs
	 */
	T get() throws IOException;

}
