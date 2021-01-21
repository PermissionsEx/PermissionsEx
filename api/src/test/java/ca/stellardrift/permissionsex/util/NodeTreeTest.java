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
package ca.stellardrift.permissionsex.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class NodeTreeTest {

    @Test
    void testAsMap() {
        final Map<String, Integer> testPermissions = new HashMap<>();
        testPermissions.put("generate.rainbow", 1);
        testPermissions.put("generate.sunset", -1);
        testPermissions.put("generate", 1);
        testPermissions.put("generate.thunderstorm.explosive", -1);

        final NodeTree oldTree = NodeTree.of(testPermissions);
        assertEquals(testPermissions, oldTree.asMap());
    }

    @Test
    void testWithValue() {
        final Map<String, Integer> testPermissions = new HashMap<>();
        testPermissions.put("generate.rainbow", 1);
        testPermissions.put("generate.sunset", -1);
        testPermissions.put("generate", 1);
        testPermissions.put("generate.thunderstorm.explosive", -1);

        final NodeTree oldTree = NodeTree.of(testPermissions);
        assertEquals(-1, oldTree.get("generate.thunderstorm.explosive"));
        final NodeTree newTree = oldTree.withValue("generate.thunderstorm.explosive", 1);
        assertEquals(-1, oldTree.get("generate.thunderstorm.explosive"));
        assertEquals(1, newTree.get("generate.thunderstorm.explosive"));
    }

    @Test
    void testWithAll() {
        final Map<String, Integer> testPermissions = new HashMap<>();
        testPermissions.put("generate.rainbow", 1);
        testPermissions.put("generate.sunset", -1);
        testPermissions.put("generate", 1);
        testPermissions.put("generate.thunderstorm.explosive", -1);

        final NodeTree oldTree = NodeTree.of(testPermissions);

        final Map<String, Integer> newPermissions = new HashMap<>();
        newPermissions.put("generate.sunset.red", 1);
        newPermissions.put("generate.thunderstorm.explosive", 0);
        newPermissions.put("something.new", -1);

        final NodeTree newTree = oldTree.withAll(newPermissions);

        assertEquals(-1, oldTree.get("generate.sunset.red"));
        assertEquals(1, newTree.get("generate.sunset.red"));

        assertEquals(-1, oldTree.get("generate.thunderstorm.explosive"));
        assertEquals(0, newTree.get("generate.thunderstorm.explosive"));

        assertEquals(0, oldTree.get("something.new"));
        assertEquals(-1, newTree.get("something.new"));
    }

    @Test
    void testCreateFromValues() {
        final Map<String, Integer> testPermissions = new HashMap<>();
        testPermissions.put("generate.rainbow", 1);
        testPermissions.put("generate.sunset", -1);
        testPermissions.put("generate", 1);
        testPermissions.put("generate.thunderstorm.explosive", -1);

        final NodeTree nodes = NodeTree.of(testPermissions, 0);

        assertEquals(1, nodes.get("generate.rainbow"));
        assertEquals(1, nodes.get("generate.rainbow.double"));
        assertEquals(-1, nodes.get("generate.sunset"));
        assertEquals(-1, nodes.get("generate.sunset.east"));
        assertEquals(1, nodes.get("generate.thunderstorm"));
        assertEquals(-1, nodes.get("generate.thunderstorm.explosive"));
        assertEquals(0, nodes.get("random.perm"));
    }

    @Test
    void testAnyInPrefix() {
        final Map<String, Integer> testPermissions = new HashMap<>();
        testPermissions.put("my.action.user.one", 1);
        testPermissions.put("my.action.group", -1);
        testPermissions.put("my.hopes.user.a", -1);
        testPermissions.put("my.hopes.user", 1);
        testPermissions.put("my.hopes", 1);

        final NodeTree nodes = NodeTree.of(testPermissions);

        assertTrue(nodes.anyInPrefixMatching("my.action", n -> n > 0));
        assertFalse(nodes.anyInPrefixMatching("my.action.group", n -> n > 0));
        assertFalse(nodes.anyInPrefixMatching("my.hopes.user.a", n -> n > 0));

        assertFalse(nodes.anyInPrefixMatching("unrelated", n -> n > 0));
    }
}
