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
package ca.stellardrift.permissionsex.impl.util;

import org.spongepowered.configurate.util.CheckedFunction;
import org.spongepowered.configurate.util.CheckedSupplier;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class Util {
    /**
     * Given an {@link Optional} of an unknown type, safely cast it to the expected type.
     * If the optional is not of the required type, an empty optional is returned.
     *
     * @param input The input value
     * @param clazz The class to cast to
     * @param <T> The type of the class
     * @return A casted or empty Optional
     */
    public static <T> Optional<T> castOptional(Optional<?> input, Class<T> clazz) {
        return input.filter(clazz::isInstance).map(clazz::cast);
    }

    public static <T> CompletableFuture<T> failedFuture(Throwable error) {
        CompletableFuture<T> ret = new CompletableFuture<>();
        ret.completeExceptionally(error);
        return ret;
    }

    private static final CompletableFuture<Object> EMPTY_FUTURE = new CompletableFuture<>();
    static {
        EMPTY_FUTURE.complete(null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> CompletableFuture<T> emptyFuture() {
        return (CompletableFuture) EMPTY_FUTURE;
    }

    public static <I, T> CompletableFuture<T> failableFuture(I value, CheckedFunction<I, T, ?> func) {
        return failableFuture(() -> func.apply(value));
    }

    public static <T> CompletableFuture<T> failableFuture(CheckedSupplier<T, ?> func) {
        CompletableFuture<T> ret = new CompletableFuture<>();
        try {
            ret.complete(func.get());
        } catch (Throwable e) {
            ret.completeExceptionally(e);
        }
        return ret;
    }

    public static <T> CompletableFuture<T> asyncFailableFuture(CheckedSupplier<T, ?> supplier, Executor exec) {
        CompletableFuture<T> ret = new CompletableFuture<>();
        exec.execute(() -> {
            try {
                ret.complete(supplier.get());
            } catch (Throwable e) {
                ret.completeExceptionally(e);
            }

        });
        return ret;
    }
}
