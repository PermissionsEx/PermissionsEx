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
package ca.stellardrift.permissionsex.impl.logging;

import ca.stellardrift.permissionsex.context.ContextValue;
import ca.stellardrift.permissionsex.logging.PermissionCheckNotifier;
import ca.stellardrift.permissionsex.subject.SubjectRef;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Notification delegate for permissions checks that tracks recent permission checks.
 *
 * Allows accessing currently known permissions
 */
public class RecordingPermissionCheckNotifier implements PermissionCheckNotifier {
    private static final int MAX_SIZE = 500;

    private final Set<String> knownPermissions = sizeLimitedSet(MAX_SIZE);
    private final Set<String> knownOptions = sizeLimitedSet(MAX_SIZE);

    private static <T> Set<T> sizeLimitedSet(final int maxSize) {
        return Collections.newSetFromMap(new LinkedHashMap<T, Boolean>() {
            private static final long serialVersionUID = 9025221898274056636L;

            @Override
            protected boolean removeEldestEntry(Map.Entry<T, Boolean> eldest) {
                return size() > maxSize;
            }
        });
    }
    @Override
    public void onPermissionCheck(SubjectRef<?> subject, Set<ContextValue<?>> contexts, String permission, int value) {
        knownPermissions.add(permission);

    }

    @Override
    public void onOptionCheck(SubjectRef<?> subject, Set<ContextValue<?>> contexts, String option, String value) {
        knownOptions.add(option);
    }

    @Override
    public void onParentCheck(SubjectRef<?> subject, Set<ContextValue<?>> contexts, List<SubjectRef<?>> parents) {
    }

    public Set<String> getKnownPermissions() {
        return Collections.unmodifiableSet(knownPermissions);
    }

    public Set<String> getKnownOptions() {
        return Collections.unmodifiableSet(knownOptions);
    }
}
