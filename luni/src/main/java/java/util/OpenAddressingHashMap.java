/*
 * Copyright (C) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package java.util;

/** @hide */
public class OpenAddressingHashMap<K, V> extends AbstractMap<K, V> {
    private static final int DEFAULT_CAPACITY = 32;

    private static final Object DELETED_KEY = new Object();
    private static final Object NULL_KEY = new Object();

    private int shrinkThreshold = 0;
    private int enlargeThreshold = 0;
    private final float shrinkFactor = 0.3f;
    private final float enlargeFactor = 0.8f;

    private Object[] hashTable;
    private int size;
    private int numDeletes;
    private int modCount;

    private transient Set<K> keySet;
    private transient Set<Entry<K, V>> entrySet;
    private transient Collection<V> values;

    /**
     * Constructs a new empty {@code HashMap} instance.
     */
    @SuppressWarnings("unchecked")
    public OpenAddressingHashMap() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Constructs a new {@code HashMap} instance with the specified capacity.
     *
     * @param capacity
     *            the initial capacity of this hash map.
     * @throws IllegalArgumentException
     *                when the capacity is less than zero.
     */
    public OpenAddressingHashMap(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity < 0: " + capacity);
        }

        hashTable = new Object[capacity * 2];
        shrinkThreshold = (int) shrinkFactor * capacity;
        enlargeThreshold = (int) enlargeFactor * capacity;
        size = 0;
    }

    /**
     * Constructs a new {@code HashMap} instance with the specified capacity and
     * load factor.
     *
     * @param capacity
     *            the initial capacity of this hash map.
     * @param loadFactor
     *            the initial load factor.
     * @throws IllegalArgumentException
     *                when the capacity is less than zero or the load factor is
     *                less or equal to zero or NaN.
     */
    public OpenAddressingHashMap(int capacity, float loadFactor) {
        this(capacity);
    }

    /**
     * Constructs a new {@code HashMap} instance containing the mappings from
     * the specified map.
     *
     * @param map
     *            the mappings to add.
     */
    public OpenAddressingHashMap(Map<? extends K, ? extends V> map) {

    }

    /**
     * Inserts all of the elements of map into this HashMap in a manner
     * suitable for use by constructors and pseudo-constructors (i.e., clone,
     * readObject). Also used by LinkedHashMap.
     */
    final void constructorPutAll(Map<? extends K, ? extends V> map) {
    }


    /**
     * Returns a shallow copy of this map.
     *
     * @return a shallow copy of this map.
     */
    @SuppressWarnings("unchecked")
    @Override public Object clone() {
        return 0;
    }

    /**
     * This method is called from the pseudo-constructors (clone and readObject)
     * prior to invoking constructorPut/constructorPutAll, which invoke the
     * overridden constructorNewEntry method. Normally it is a VERY bad idea to
     * invoke an overridden method from a pseudo-constructor (Effective Java
     * Item 17). In this case it is unavoidable, and the init method provides a
     * workaround.
     */
    void init() { }

    Iterator<K> newKeyIterator() {
        return new KeyIterator();
    }

    Iterator<V> newValueIterator() {
        return new ValueIterator();
    }

    Iterator<Map.Entry<K, V>> newEntryIterator() {
        return new EntryIterator();
    }

    /**
     * Returns whether this map is empty.
     *
     * @return {@code true} if this map has no elements, {@code false}
     *         otherwise.
     * @see #size()
     */
    @Override public boolean isEmpty() {
        return (size == 0);
    }

    /**
     * Returns the number of elements in this map.
     *
     * @return the number of elements in this map.
     */
    @Override public int size() {
        return size;
    }

    /**
     * Returns the value of the mapping with the specified key.
     *
     * @param key
     *            the key.
     * @return the value of the mapping with the specified key, or {@code null}
     *         if no mapping for the specified key is found.
     */
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        final int index = findIndexInternal(replaceNullKey(key), hashTable);
        return (V) hashTable[index + 1];
    }

    /**
     * Returns whether this map contains the specified key.
     *
     * @param key
     *            the key to search for.
     * @return {@code true} if this map contains the specified key,
     *         {@code false} otherwise.
     */
    @Override public boolean containsKey(Object key) {
        return findIndexInternal(replaceNullKey(key), hashTable) != -1;
    }

    /**
     * Returns whether this map contains the specified value.
     *
     * @param value
     *            the value to search for.
     * @return {@code true} if this map contains the specified value,
     *         {@code false} otherwise.
     */
    @Override public boolean containsValue(Object value) {
        for (int i = 0; i < hashTable.length; i+= 2) {
            final Object key = hashTable[i];
            if (key != null && key != DELETED_KEY) {
                final Object currentValue = hashTable[i + 1];
                if (Objects.equals(value, currentValue)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Maps the specified key to the specified value.
     *
     * @param key
     *            the key.
     * @param value
     *            the value.
     * @return the value of any previous mapping with the specified key or
     *         {@code null} if there was no such mapping.
     */
    @Override public V put(K key, V value) {
        maybeResize(1);
        final int index = findInsertionIndexInternal(replaceNullKey(key), hashTable);

        final Object previousKey = hashTable[index];
        final Object previousValue;
        if (previousKey == DELETED_KEY || previousKey == null) {
            previousValue = null;
            size++;
        } else {
            previousValue = hashTable[index + 1];
        }

        hashTable[index] = key;
        hashTable[index + 1] = value;

        return (V) previousValue;
    }

    /**
     * Give LinkedHashMap a chance to take action when we modify an existing
     * entry.
     */
    void preModify(K key, V value) { }

    /**
     * Copies all the mappings in the specified map to this map. These mappings
     * will replace all mappings that this map had for any of the keys currently
     * in the given map.
     *
     * @param map
     *            the map to copy mappings from.
     */
    @Override public void putAll(Map<? extends K, ? extends V> map) {
        maybeResize(map.size());
        super.putAll(map);
    }

    /**
     * Removes the mapping with the specified key from this map.
     *
     * @param key
     *            the key of the mapping to remove.
     * @return the value of the removed mapping or {@code null} if no mapping
     *         for the specified key was found.
     */
    @Override public V remove(Object key) {
        final int index = findIndexInternal(replaceNullKey(key), hashTable);
        if (index == -1) {
            return null;
        }

        return removeAtIndex(index);
    }

    private V removeAtIndex(int index) {
        Object previous = hashTable[index];
        hashTable[index] = DELETED_KEY;
        numDeletes++;

        return (V) previous;
    }

    /**
     * Subclass overrides this method to unlink entry.
     */
    void postRemove(K key, V value) { }

    /**
     * Removes all mappings from this hash map, leaving it empty.
     *
     * @see #isEmpty
     * @see #size
     */
    @Override public void clear() {
        if (size != 0) {
            Arrays.fill(hashTable, null);
            size = 0;
            numDeletes = 0;
        }
    }

    /**
     * Returns a set of the keys contained in this map. The set is backed by
     * this map so changes to one are reflected by the other. The set does not
     * support adding.
     *
     * @return a set of the keys.
     */
    @Override public Set<K> keySet() {
        Set<K> ks = keySet;
        return (ks != null) ? ks : (keySet = new KeySet());
    }

    /**
     * Returns a collection of the values contained in this map. The collection
     * is backed by this map so changes to one are reflected by the other. The
     * collection supports remove, removeAll, retainAll and clear operations,
     * and it does not support add or addAll operations.
     * <p>
     * This method returns a collection which is the subclass of
     * AbstractCollection. The iterator method of this subclass returns a
     * "wrapper object" over the iterator of map's entrySet(). The {@code size}
     * method wraps the map's size method and the {@code contains} method wraps
     * the map's containsValue method.
     * </p>
     * <p>
     * The collection is created when this method is called for the first time
     * and returned in response to all subsequent calls. This method may return
     * different collections when multiple concurrent calls occur, since no
     * synchronization is performed.
     * </p>
     *
     * @return a collection of the values contained in this map.
     */
    @Override public Collection<V> values() {
        Collection<V> vs = values;
        return (vs != null) ? vs : (values = new Values());
    }

    /**
     * Returns a set containing all of the mappings in this map. Each mapping is
     * an instance of {@link Map.Entry}. As the set is backed by this map,
     * changes in one will be reflected in the other.
     *
     * @return a set of the mappings.
     */
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> es = entrySet;
        return (es != null) ? es : (entrySet = new EntrySet());
    }

    private static Object replaceNullKey(Object key) {
        return key != null ? key : NULL_KEY;
    }

    private void maybeResize(int delta) {
        // TODO: This method uses a very complicated set of heuristics

        // If we have a very large number of empty buckets, we should
        // consider shrinking the map to a smaller size.
        if (shrinkThreshold > 0 && size < shrinkThreshold) {
            maybeShrink();
        }

        // We're still number
        final int bucketCount = (hashTable.length >> 1);
        if (bucketCount >= DEFAULT_CAPACITY && (size + delta) <= enlargeThreshold) {
            return;
        }

        final int neededSize = minBuckets(size + numDeletes + delta, 0, enlargeFactor);
        if (neededSize <= bucketCount) {
            return;
        }

        int resizeTo = minBuckets(size + delta, bucketCount, enlargeFactor);
        if (resizeTo < neededSize) {
            int target = (int) shrinkFactor * (resizeTo << 1);
            if (size + delta >= target) {
                resizeTo = (resizeTo << 1);
            }
        }

        resizeToSize(resizeTo);
    }

    private static int minBuckets(int numElements, int numElementsWanted,
            float enlargeFactor) {
        int size = DEFAULT_CAPACITY;
        while (size < numElementsWanted || numElements >= (size * enlargeFactor)) {
            size = (size << 1);
        }
        return size;
    }

    private void maybeShrink() {
        int numBuckets = hashTable.length >> 1;

        if (size < shrinkThreshold && numBuckets > DEFAULT_CAPACITY) {
            while (numBuckets > DEFAULT_CAPACITY && size()  < numBuckets * shrinkFactor) {
                numBuckets = (numBuckets >> 1);
            }

            resizeToSize(numBuckets);
        }
    }

    private void resizeToSize(int size) {
        Object[] newTable = new Object[size << 1];

        for (int i = 0; i < hashTable.length; i+= 2) {
            final Object key = hashTable[i];
            if (key != null && key != DELETED_KEY) {
                int index = findInsertionIndexInternal(replaceNullKey(key), newTable);

                final Object value = hashTable[i + 1];
                newTable[index] = key;
                newTable[index + 1] = value;
            }
        }

        hashTable = newTable;

        numDeletes = 0;
        shrinkThreshold = (int) shrinkFactor * size;
        enlargeThreshold = (int) enlargeFactor * size;
    }

    private static int findInsertionIndexInternal(Object key, Object[] hashTable) {
        final int hashCode = Collections.secondaryHash(key);
        final int sizeMinusOne = (hashTable.length - 1) >> 1;

        int nextIndex = (hashCode & sizeMinusOne);
        int numProbes = 0;
        int foundDeleted = -1;

        while (true) {
            final int arrayIndex = nextIndex << 1;
            final Object keyAt = hashTable[arrayIndex];
            if (keyAt == null) {
                if (foundDeleted != -1) {
                    return foundDeleted;
                }
                return arrayIndex;
            } else if (keyAt == DELETED_KEY && (foundDeleted == -1)) {
                foundDeleted = arrayIndex;
            } else if (keyAt.equals(key)) {
                return arrayIndex;
            }

            numProbes++;
            nextIndex = (nextIndex + numProbes) & sizeMinusOne;
        }
    }

    private static int findIndexInternal(Object key, Object[] hashTable) {
        final int hashCode = Collections.secondaryHash(key);
        final int sizeMinusOne = (hashTable.length - 1) >> 1;

        int nextIndex = (hashCode & sizeMinusOne);
        int numProbes = 0;
        while (true) {
            final int arrayIndex = nextIndex << 1;
            final Object o = hashTable[arrayIndex];
            if (o == null) {
                return -1;
            } else if (o == key || o.equals(key)) {
                return arrayIndex;
            }

            numProbes++;
            nextIndex = (nextIndex + numProbes) & sizeMinusOne;
        }
    }

    abstract class TableIterator  {
        private int next;
        protected int current;

        TableIterator() {
            next = 0;
            current = -1;
            skipOverEmptyAndDeleted();
        }

        protected boolean hasNextIndex() {
            return (next <= hashTable.length);
        }

        protected int nextIndex() {
            if (!hasNextIndex()) {
                throw new NoSuchElementException();
            }

            current = next;
            next++;
            skipOverEmptyAndDeleted();
            return current;
        }

        protected void removeAtCurrentIndex() {
            if (current == -1) {
                throw new IllegalStateException();
            }

            removeAtIndex(current);
            current = -1;
        }

        private void skipOverEmptyAndDeleted() {
            while ((hashTable[next] == null || hashTable[next] == DELETED_KEY) &&
                    next < hashTable.length) {
                next += 2;
            }
        }
    }

    final class ValueIterator extends TableIterator implements Iterator<V> {
        public boolean hasNext() {
            return hasNextIndex();
        }

        public V next() {
            return (V) hashTable[nextIndex() + 1];
        }

        public void remove() {
            removeAtCurrentIndex();
        }
    }

    final class KeyIterator extends TableIterator implements Iterator<K> {
        public boolean hasNext() {
            return hasNextIndex();
        }

        public K next() {
            int idx = nextIndex();
            Object o = hashTable[idx];
            if (o == NULL_KEY) {
                o = null;
            }

            return (K) o;
        }

        public void remove() {
            removeAtCurrentIndex();
        }
    }

    final class EntryIterator extends TableIterator implements Iterator<Entry<K, V>> {
        public boolean hasNext() {
            return hasNextIndex();
        }

        public Entry<K, V> next() {
            int idx = nextIndex();
            Object o = hashTable[idx];
            if (o == NULL_KEY) {
                o = null;
            }

            return new SimpleEntry<K, V>((K) o, (V) hashTable[idx + 1]);
        }

        public void remove() {
            removeAtCurrentIndex();
        }
    }

    final class KeySet extends AbstractSet<K> {
        public Iterator<K> iterator() {
            return newKeyIterator();
        }
        public int size() {
            return size;
        }
        public boolean isEmpty() {
            return size == 0;
        }
        public boolean contains(Object o) {
            return containsKey(o);
        }
        public boolean remove(Object o) {
            int oldSize = size;
            remove(o);
            return size != oldSize;
        }
        public void clear() {
            clear();
        }
    }

    private final class Values extends AbstractCollection<V> {
        public Iterator<V> iterator() {
            return newValueIterator();
        }
        public int size() {
            return size;
        }
        public boolean isEmpty() {
            return size == 0;
        }
        public boolean contains(Object o) {
            return containsValue(o);
        }
        public void clear() {
            clear();
        }
    }

    final class EntrySet extends AbstractSet<Entry<K, V>> {
        public Iterator<Entry<K, V>> iterator() {
            return newEntryIterator();
        }

        public boolean contains(Object o) {
            if (!(o instanceof Entry))
                return false;
            Entry<?, ?> e = (Entry<?, ?>) o;
            final V value = get(e.getKey());
            return Objects.equals(value, e.getValue());
        }

        public boolean remove(Object o) {
            if (!(o instanceof Entry))
                return false;
            Entry<?, ?> e = (Entry<?, ?>)o;

            // TODO: We could do this with one lookup instead of two.
            final V value = get(e.getKey());
            if (Objects.equals(value, e.getValue())) {
                remove(e.getKey());
                return true;
            }

            return false;
        }

        public int size() {
            return size;
        }

        public boolean isEmpty() {
            return size == 0;
        }

        public void clear() {
            clear();
        }
    }
}
