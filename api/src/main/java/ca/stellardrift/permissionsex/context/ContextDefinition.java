/*
 * PermissionsEx
 * Copyright (C) zml and PermissionsEx contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.stellardrift.permissionsex.context;

import ca.stellardrift.permissionsex.subject.CalculatedSubject;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.pcollections.HashTreePSet;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * A specific type of context, for example {@code world}, {@code server-tag}, or {@code until}.
 *
 * @since 2.0.0
 */
public abstract class ContextDefinition<V> {
    private final String name;

    protected ContextDefinition(final String name) {
        this.name = requireNonNull(name, "name");
    }

    /**
     * Create a new context value based on this definition.
     *
     * @param value the value to associate with this definition
     * @return a new context value holder
     * @since 2.0.0
     */
    public final ContextValue<V> createValue(final V value) {
        return new ContextValue<>(this, value);
    }

    /**
     * Gets the name for this context definition.
     *
     * @return the definition name
     * @since 2.0.0
     */
    public final String name() {
        return this.name;
    }

    /**
     * Given a parsed value, write data out as a string.
     *
     * @param canonicalValue Parsed value
     * @return serialized form of the value
     * @since 2.0.0
     */
    public abstract String serialize(V canonicalValue);

    /**
     * Given a string (which may be in user format), return a parsed object.
     *
     * @param userValue the value as a string, such as when provided by user input
     * @return V a deserialized value, or {@code null if unsuccessful}
     * @since 2.0.0
     */
    public abstract @Nullable V deserialize(String userValue);

    /**
     * Given a defined context and the active value (provided by {@link #accumulateCurrentValues(CalculatedSubject, Consumer)}),
     * return whether the active value matches the defined value.
     *
     * @param ctx a defined context
     * @param activeValue a value to test for membership in {@code ctx}
     * @return whether a match was found
     * @since 2.0.0
     */
    public final boolean matches(final ContextValue<V> ctx, final V activeValue) {
        return matches(ctx.getParsedValue(this), activeValue);
    }

    /**
     * Get whether two values match.
     *
     * @param ownVal the defined value
     * @param testVal the value being tested against
     * @return whether {@code testVal} is an element of {@code ownVal}
     */
    public boolean matches(V ownVal, V testVal) {
        return Objects.equals(ownVal, testVal);
    }

    /**
     * Given a player, calculate active context types
     *
     * @param subject  The subject active contexts are being calculated for
     * @param consumer A function that will take the returned value and add it to the active context set
     * @since 2.0.0
     */
    public abstract void accumulateCurrentValues(CalculatedSubject subject, Consumer<V> consumer);

    /**
     * Given a subject, suggest a set of values that may be valid for this context. This need not be an exhaustive list,
     * or could even be an empty list, but allows providing users possible suggestions to what sensible values for a context may be.
     *
     * @param subject a subject to query for environment information
     * @return a set of possible values
     * @since 2.0.0
     */
    public Set<V> suggestValues(final CalculatedSubject subject) {
        return HashTreePSet.empty();
    }

    @Override
    public String toString() {
        return "ContextDefinition{name='" + this.name + "'}";
    }

    @Override
    public boolean equals(final @Nullable Object other) {
        if (this == other) return true;
        if (!(other instanceof ContextDefinition<?>)) return false;

        return this.name.equals(((ContextDefinition<?>) other).name);
    }

    @Override
    public int hashCode() {
        return 31 * this.name.hashCode();
    }
}


