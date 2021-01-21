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
package ca.stellardrift.permissionsex;

import ca.stellardrift.permissionsex.context.ContextDefinitionProvider;
import ca.stellardrift.permissionsex.context.ContextInheritance;
import ca.stellardrift.permissionsex.datastore.DataStore;
import ca.stellardrift.permissionsex.rank.RankLadderCollection;
import ca.stellardrift.permissionsex.subject.CalculatedSubject;
import ca.stellardrift.permissionsex.subject.SubjectRef;
import ca.stellardrift.permissionsex.subject.SubjectType;
import ca.stellardrift.permissionsex.subject.SubjectTypeCollection;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * A PermissionsEx engine.
 *
 * @since 2.0.0
 */
public interface PermissionsEngine extends ContextDefinitionProvider {

    /**
     * Create a new builder for an engine.
     *
     * @return the engine builder
     * @since 2.0.0
     */
    static PermissionsEngineBuilder<Void> builder() {
        return EngineBuilderService.ACTIVE_BUILDER.newBuilder();
    }

    /**
     * Create a new builder for an engine.
     *
     * @return the engine builder
     * @since 2.0.0
     */
    static <C> PermissionsEngineBuilder<C> builder(final Class<C> configType) {
        return EngineBuilderService.ACTIVE_BUILDER.newBuilder(configType);
    }

    // -- Internal subject types -- //

    /**
     * Subjects holding data that will be applied to every subject of a type.
     *
     * @return the {@code fallback} subject type
     * @since 2.0.0
     */
    SubjectTypeCollection<SubjectType<?>> defaults();

    /**
     * Subjects holding data that will be applied to subjects of each type when no other data is available.
     *
     * @return the {@code fallback} subject type
     * @since 2.0.0
     */
    SubjectTypeCollection<SubjectType<?>> fallbacks();

    // -- Working with subject types -- //

    /**
     * Register subject types with the engine.
     *
     * @param types the types to register
     * @since 2.0.0
     */
    void registerSubjectTypes(final SubjectType<?>... types);

    /**
     * Get a subject type by name.
     *
     * <p>If this subject type has not been seen before, it will be registered.</p>
     *
     * @param type the type identifier
     * @return a subject type instance, never null
     * @since 2.0.0
     */
    <I> SubjectTypeCollection<I> subjects(final SubjectType<I> type);

    /**
     * Resolve a subject from a reference.
     *
     * @param reference the subject reference to resolve
     * @param <I> identifier type
     * @return a future providing the resolved subject
     * @since 2.0.0
     */
    default <I> CompletableFuture<CalculatedSubject> subject(final SubjectRef<I> reference) {
        return this.subjects(reference.type()).get(reference.identifier());
    }

    /**
     * Get subject types with actively stored data.
     *
     * @return an unmodifiable view of the actively loaded subject types
     */
    Collection<? extends SubjectTypeCollection<?>> loadedSubjectTypes();

    /**
     * Get all registered subject types
     *
     * @return a stream producing all subject types
     * @since 2.0.0
     */
    Set<SubjectType<?>> knownSubjectTypes();

    /**
     * Get a cache for rank ladders known by the engine.
     *
     * <p>Rank ladders allow for promotion and demotion of a subject among a
     * set of ranked parents.</p>
     *
     * @return the ladder cache
     * @since 2.0.0
     */
    RankLadderCollection ladders();

    /**
     * Perform a low-level bulk operation.
     *
     * <p>This can be used for transforming subjects if the subject type definition changes, and
     * any large data changes that require information that may no longer be valid with current
     * subject type options.</p>
     *
     * <p>When possible, higher-level bulk query API (not yet written) should be used instead.</p>
     *
     * @param actor the action to perform
     * @param <V> the result type
     * @return a future completing with the result of the action
     */
    <V> CompletableFuture<V> doBulkOperation(final Function<DataStore, CompletableFuture<V>> actor);

    /**
     * Get the current context inheritance.
     *
     * <p>No update listener will be registered</p>
     *
     * @return a future providing the current context inheritance data
     * @see #contextInheritance(Consumer) for more details on context inheritance
     * @since 2.0.0
     */
    default CompletableFuture<ContextInheritance> contextInheritance() {
        return this.contextInheritance((Consumer<ContextInheritance>) null);
    }

    /**
     * Get context inheritance data.
     *
     * <p>The result of the future is immutable -- to take effect, the object returned by any
     * update methods in {@link ContextInheritance} must be passed to {@link #contextInheritance(ContextInheritance)}.
     *  It follows that anybody else's changes will not appear in the returned inheritance object -- so if updates are
     *  desired providing a callback function is important.</p>
     *
     * @param listener A callback function that will be triggered whenever there is a change to the context inheritance
     * @return A future providing the current context inheritance data
     * @since 2.0.0
     */
    CompletableFuture<ContextInheritance> contextInheritance(final @Nullable Consumer<ContextInheritance> listener);

    /**
     * Update the context inheritance when values have been changed
     *
     * @param newInheritance The modified inheritance object
     * @return A future containing the latest context inheritance object
     * @since 2.0.0
     */
    CompletableFuture<ContextInheritance> contextInheritance(ContextInheritance newInheritance);

    // -- Engine state -- //

    /**
     * Get whether or not debug mode is enabled.
     *
     * <p>When debug mode is enabled, all permission queries will be logged to the engine's
     *    active logger.</p>
     *
     * @return the debug mode state
     * @since 2.0.0
     */
    boolean debugMode();

    /**
     * Set the active debug mode state.
     *
     * @param enabled whether debug mode is enabled
     * @since 2.0.0
     * @see #debugMode() for information on the consequences of debug mode
     */
    default void debugMode(final boolean enabled) {
        this.debugMode(enabled, null);
    }

    /**
     * Set the active debug mode state, and a filter.
     *
     * @param enabled whether debug mode is enabled
     * @param filter a filter for values (permissions, options, and subject names) that will be
     *               logged by debug mode.
     * @since 2.0.0
     * @see #debugMode() for information on the consequences of debug mode
     */
    void debugMode(final boolean enabled, final @Nullable Pattern filter);

}
