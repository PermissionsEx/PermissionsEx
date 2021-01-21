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

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.IntPredicate;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * An immutable tree structure for determining node data.
 *
 * <p>Any changes will create new copies of the necessary tree objects.</p>
 *
 * <p>Keys are case-insensitive.</p>
 *
 * <p>Segments of nodes are split by the '.' character</p>
 *
 * @since 2.0.0
 */
public final class NodeTree {
    public static final int PERMISSION_UNDEFINED = 0;

    private static final Pattern SPLIT_REGEX = Pattern.compile("\\.");
    private final Node rootNode;

    private NodeTree(int value) {
        this.rootNode = new Node(new HashMap<>());
        this.rootNode.value = value;
    }

    private NodeTree(Node rootNode) {
        this.rootNode = rootNode;
    }

    /**
     * Create a new node tree with the given values, and a default value of UNDEFINED.
     *
     * @param values The values to set
     * @return The new node tree
     * @since 2.0.0
     */
    public static NodeTree of(Map<String, Integer> values) {
        return of(values, PERMISSION_UNDEFINED);
    }

    /**
     * Create a new node tree with the given values, and the specified root fallback value.
     *
     * @param values The values to be contained in this node tree
     * @param defaultValue The fallback value for any completely undefined nodes
     * @return The newly created node tree
     * @since 2.0.0
     */
    public static NodeTree of(final Map<String, Integer> values, final int defaultValue) {
        final NodeTree newTree = new NodeTree(defaultValue);
        for (Map.Entry<String, Integer> value : values.entrySet()) {
            final String[] parts = splitPerm(value.getKey());
            Node currentNode = newTree.rootNode;
            for (String part : parts) {
                if (currentNode.children.containsKey(part)) {
                    currentNode = currentNode.children.get(part);
                } else {
                    Node newNode = new Node();
                    currentNode.putChild(part, newNode);
                    currentNode = newNode;
                }
            }
            currentNode.value = value.getValue();
        }
        return newTree;
    }

    /**
     * Returns the value assigned to a specific node, or the nearest parent value in the tree if the node itself is undefined.
     *
     * @param node The path to get the node value at
     * @return The int value for the given node
     * @since 2.0.0
     */
    public int get(final String node) {
        final String[] parts = splitPerm(node);
        Node currentNode = this.rootNode;
        int lastUndefinedVal = this.rootNode.value;
        for (final String part : parts) {
            if (!currentNode.children.containsKey(part)) {
                break;
            }
            currentNode = currentNode.children.get(part);
            if (Math.abs(currentNode.value) >= Math.abs(lastUndefinedVal)) {
                lastUndefinedVal = currentNode.value;
            }
        }
        return lastUndefinedVal;
    }

    /**
     * Return whether the node {@code prefix} or any of its children match the predicate {@code test}.
     *
     * @param prefix the prefix to test
     * @param test the test function
     * @return if any values return true
     */
    public boolean anyInPrefixMatching(final String prefix, final IntPredicate test) {
        final String[] parts = splitPerm(prefix);
        Node currentNode = this.rootNode;
        int lastUndefinedVal = this.rootNode.value;

        // Resolve prefix
        for (final String part : parts) {
            if (!currentNode.children.containsKey(part)) {
                return test.test(lastUndefinedVal);
            }
            currentNode = currentNode.children.get(part);
            if (Math.abs(currentNode.value) >= Math.abs(lastUndefinedVal)) {
                lastUndefinedVal = currentNode.value;
            }
        }

        // If there are no children overridden, test on the prefix
        if (currentNode.children.isEmpty()) {
            return test.test(lastUndefinedVal);
        }

        // Now visit all children, stopping on first match
        // search breadth-first
        final ArrayDeque<Node> toVisit = new ArrayDeque<>(currentNode.children.size() * 2);
        toVisit.addAll(currentNode.children.values());

        @Nullable Node current;
        while ((current = toVisit.poll()) != null) {
            // compute the value based on maximum of prefix's value or leaf value
            if (Math.abs(current.value) >= Math.abs(lastUndefinedVal) && test.test(current.value)) {
                return true;
            }

            toVisit.addAll(current.children.values());
        }
        return false;
    }

    /**
     * Convert this node tree into a map of the defined nodes in this tree.
     *
     * @return An immutable map representation of the nodes defined in this tree
     * @since 2.0.0
     */
    public Map<String, Integer> asMap() {
        final Map<String, Integer> ret = new HashMap<>();
        for (final Map.Entry<String, Node> ent : this.rootNode.children.entrySet()) {
            populateMap(ret, ent.getKey(), ent.getValue());
        }
        return Collections.unmodifiableMap(ret);
    }

    private void populateMap(final Map<String, Integer> values, final String prefix, final Node currentNode) {
        if (currentNode.value != 0) {
            values.put(prefix, currentNode.value);
        }
        for (final Map.Entry<String, Node> ent : currentNode.children.entrySet()) {
            populateMap(values, prefix + '.' + ent.getKey(), ent.getValue());
        }
    }

    /**
     * Return a new NodeTree instance with a single changed value.
     *
     * @param node The node path to change the value of
     * @param value The value to change, or UNDEFINED to remove
     * @return The new, modified node tree
     * @since 2.0.0
     */
    public NodeTree withValue(final String node, final int value) {
        final String[] parts = splitPerm(node);
        final Node newRoot = new Node(new HashMap<>(this.rootNode.children));
        Node newPtr = newRoot;
        @Nullable Node currentPtr = this.rootNode;

        newPtr.value = currentPtr.value;
        for (final String part : parts) {
            final @Nullable Node oldChild = currentPtr == null ? null : currentPtr.children.get(part);
            final Node newChild = new Node(oldChild != null ? new HashMap<>(oldChild.children) : new HashMap<>());
            newPtr.children.put(part, newChild);
            currentPtr = oldChild;
            newPtr = newChild;
        }
        newPtr.value = value;
        return new NodeTree(newRoot);
    }

    /**
     * Return a modified new node tree with the specified values set.
     *
     * @param values The values to set
     * @return The new node tree
     * @since 2.0.0
     */
    public NodeTree withAll(Map<String, Integer> values) {
        NodeTree ret = this;
        for (Map.Entry<String, Integer> ent : values.entrySet()) {
            ret = ret.withValue(ent.getKey(), ent.getValue());
        }
        return ret;
    }

    @Override
    public String toString() {
        return "NodeTree{" + this.rootNode + "}";
    }

    private static String[] splitPerm(final String input) {
        requireNonNull(input, "input");
        return SPLIT_REGEX.split(input.toLowerCase(Locale.ROOT), -1);
    }

    static class Node {

        private static final Map<String, Node> EMPTY = Collections.emptyMap();

        Map<String, Node> children;
        int value = 0;

        Node(Map<String, Node> children) {
            this.children = children;
        }

        Node() {
            this.children = EMPTY;
        }

        void putChild(final String path, final Node child) {
            if (this.children == EMPTY) {
                this.children = new HashMap<>();
            }
            this.children.put(path, child);
        }

        @Override
        public String toString() {
            return "<value: " + this.value + ", children=" + this.children + ">";
        }
    }
}
