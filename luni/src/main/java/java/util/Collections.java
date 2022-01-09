/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * {@code Collections} contains static methods which operate on
 * {@code Collection} classes.
 *
 * @since 1.2
 */
public class Collections {

    private static final Iterator<?> EMPTY_ITERATOR = new Iterator<Object>() {
        @Override public boolean hasNext() {
            return false;
        }

        @Override public Object next() {
            throw new NoSuchElementException();
        }

        @Override public void remove() {
            throw new IllegalStateException();
        }
    };

    private static final Enumeration<?> EMPTY_ENUMERATION = new Enumeration<Object>() {
        @Override public boolean hasMoreElements() {
            return false;
        }

        @Override public Object nextElement() {
            throw new NoSuchElementException();
        }
    };

    private static final class CopiesList<E> extends AbstractList<E> implements Serializable {
        private static final long serialVersionUID = 2739099268398711800L;
        private final int n;
        private final E element;

        CopiesList(int length, E object) {
            if (length < 0) {
                throw new IllegalArgumentException("length < 0: " + length);
            }
            n = length;
            element = object;
        }

        @Override public boolean contains(Object object) {
            return element == null ? object == null : element.equals(object);
        }

        @Override public int size() {
            return n;
        }

        @Override public E get(int location) {
            if (location >= 0 && location < n) {
                return element;
            }
            throw new IndexOutOfBoundsException();
        }
    }

    @SuppressWarnings("unchecked")
    private static final class EmptyList extends AbstractList
            implements RandomAccess, Serializable {
        private static final long serialVersionUID = 8842843931221139166L;

        @Override public boolean contains(Object object) {
            return false;
        }

        @Override public int size() {
            return 0;
        }

        @Override public Object get(int location) {
            throw new IndexOutOfBoundsException();
        }

        @Override public Iterator iterator() {
            return EMPTY_ITERATOR;
        }

        private Object readResolve() {
            return Collections.EMPTY_LIST;
        }
    }

    @SuppressWarnings("unchecked")
    private static final class EmptySet extends AbstractSet implements Serializable {
        private static final long serialVersionUID = 1582296315990362920L;

        @Override public boolean contains(Object object) {
            return false;
        }

        @Override public int size() {
            return 0;
        }

        @Override public Iterator iterator() {
            return EMPTY_ITERATOR;
        }

        private Object readResolve() {
            return Collections.EMPTY_SET;
        }
    }

    @SuppressWarnings("unchecked")
    private static final class EmptyMap extends AbstractMap implements Serializable {
        private static final long serialVersionUID = 6428348081105594320L;

        @Override public boolean containsKey(Object key) {
            return false;
        }

        @Override public boolean containsValue(Object value) {
            return false;
        }

        @Override public Set entrySet() {
            return EMPTY_SET;
        }

        @Override public Object get(Object key) {
            return null;
        }

        @Override public Set keySet() {
            return EMPTY_SET;
        }

        @Override public Collection values() {
            return EMPTY_LIST;
        }

        private Object readResolve() {
            return Collections.EMPTY_MAP;
        }
    }

    /**
     * An empty immutable instance of {@link List}.
     */
    @SuppressWarnings("unchecked")
    public static final List EMPTY_LIST = new EmptyList();

    /**
     * An empty immutable instance of {@link Set}.
     */
    @SuppressWarnings("unchecked")
    public static final Set EMPTY_SET = new EmptySet();

    /**
     * An empty immutable instance of {@link Map}.
     */
    @SuppressWarnings("unchecked")
    public static final Map EMPTY_MAP = new EmptyMap();

    /**
     * This class is a singleton so that equals() and hashCode() work properly.
     */
    private static final class ReverseComparator<T> implements Comparator<T>, Serializable {
        private static final ReverseComparator<Object> INSTANCE = new ReverseComparator<Object>();

        private static final long serialVersionUID = 7207038068494060240L;

        @SuppressWarnings("unchecked")
        @Override public int compare(T o1, T o2) {
            Comparable<T> c2 = (Comparable<T>) o2;
            return c2.compareTo(o1);
        }

        private Object readResolve() throws ObjectStreamException {
            return INSTANCE;
        }
    }

    private static final class ReverseComparator2<T> implements Comparator<T>, Serializable {
        private static final long serialVersionUID = 4374092139857L;
        private final Comparator<T> cmp;

        ReverseComparator2(Comparator<T> comparator) {
            this.cmp = comparator;
        }

        @Override public int compare(T o1, T o2) {
            return cmp.compare(o2, o1);
        }

        @Override public boolean equals(Object o) {
            return o instanceof ReverseComparator2
                    && ((ReverseComparator2) o).cmp.equals(cmp);
        }

        @Override public int hashCode() {
            return ~cmp.hashCode();
        }
    }

    private static final class SingletonSet<E> extends AbstractSet<E> implements Serializable {
        private static final long serialVersionUID = 3193687207550431679L;
        final E element;

        SingletonSet(E object) {
            element = object;
        }

        @Override public boolean contains(Object object) {
            return element == null ? object == null : element.equals(object);
        }

        @Override public int size() {
            return 1;
        }

        @Override public Iterator<E> iterator() {
            return new Iterator<E>() {
                boolean hasNext = true;

                @Override public boolean hasNext() {
                    return hasNext;
                }

                @Override public E next() {
                    if (hasNext) {
                        hasNext = false;
                        return element;
                    }
                    throw new NoSuchElementException();
                }

                @Override public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    private static final class SingletonList<E> extends AbstractList<E> implements Serializable {
        private static final long serialVersionUID = 3093736618740652951L;

        final E element;

        SingletonList(E object) {
            element = object;
        }

        @Override public boolean contains(Object object) {
            return element == null ? object == null : element.equals(object);
        }

        @Override public E get(int location) {
            if (location == 0) {
                return element;
            }
            throw new IndexOutOfBoundsException();
        }

        @Override public int size() {
            return 1;
        }
    }

    private static final class SingletonMap<K, V> extends AbstractMap<K, V>
            implements Serializable {
        private static final long serialVersionUID = -6979724477215052911L;

        final K k;
        final V v;

        SingletonMap(K key, V value) {
            k = key;
            v = value;
        }

        @Override public boolean containsKey(Object key) {
            return k == null ? key == null : k.equals(key);
        }

        @Override public boolean containsValue(Object value) {
            return v == null ? value == null : v.equals(value);
        }

        @Override public V get(Object key) {
            if (containsKey(key)) {
                return v;
            }
            return null;
        }

        @Override public int size() {
            return 1;
        }

        @Override public Set<Map.Entry<K, V>> entrySet() {
            return new AbstractSet<Map.Entry<K, V>>() {
                @Override public boolean contains(Object object) {
                    if (object instanceof Map.Entry) {
                        Map.Entry<?, ?> entry = (Map.Entry<?, ?>) object;
                        return containsKey(entry.getKey())
                                && containsValue(entry.getValue());
                    }
                    return false;
                }

                @Override public int size() {
                    return 1;
                }

                @Override public Iterator<Map.Entry<K, V>> iterator() {
                    return new Iterator<Map.Entry<K, V>>() {
                        boolean hasNext = true;

                        @Override public boolean hasNext() {
                            return hasNext;
                        }

                        @Override public Map.Entry<K, V> next() {
                            if (!hasNext) {
                                throw new NoSuchElementException();
                            }

                            hasNext = false;
                            return new MapEntry<K, V>(k, v) {
                                @Override public V setValue(V value) {
                                    throw new UnsupportedOperationException();
                                }
                            };
                        }

                        @Override public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
            };
        }
    }

    static class SynchronizedCollection<E> implements Collection<E>, Serializable {
        private static final long serialVersionUID = 3053995032091335093L;
        final Collection<E> c;
        final Object mutex;

        SynchronizedCollection(Collection<E> collection) {
            c = collection;
            mutex = this;
        }

        SynchronizedCollection(Collection<E> collection, Object mutex) {
            c = collection;
            this.mutex = mutex;
        }

        @Override public boolean add(E object) {
            synchronized (mutex) {
                return c.add(object);
            }
        }

        @Override public boolean addAll(Collection<? extends E> collection) {
            synchronized (mutex) {
                return c.addAll(collection);
            }
        }

        @Override public void clear() {
            synchronized (mutex) {
                c.clear();
            }
        }

        @Override public boolean contains(Object object) {
            synchronized (mutex) {
                return c.contains(object);
            }
        }

        @Override public boolean containsAll(Collection<?> collection) {
            synchronized (mutex) {
                return c.containsAll(collection);
            }
        }

        @Override public boolean isEmpty() {
            synchronized (mutex) {
                return c.isEmpty();
            }
        }

        @Override public Iterator<E> iterator() {
            synchronized (mutex) {
                return c.iterator();
            }
        }

        @Override public boolean remove(Object object) {
            synchronized (mutex) {
                return c.remove(object);
            }
        }

        @Override public boolean removeAll(Collection<?> collection) {
            synchronized (mutex) {
                return c.removeAll(collection);
            }
        }

        @Override public boolean retainAll(Collection<?> collection) {
            synchronized (mutex) {
                return c.retainAll(collection);
            }
        }

        @Override public int size() {
            synchronized (mutex) {
                return c.size();
            }
        }

        @Override public java.lang.Object[] toArray() {
            synchronized (mutex) {
                return c.toArray();
            }
        }

        @Override public String toString() {
            synchronized (mutex) {
                return c.toString();
            }
        }

        @Override public <T> T[] toArray(T[] array) {
            synchronized (mutex) {
                return c.toArray(array);
            }
        }

        private void writeObject(ObjectOutputStream stream) throws IOException {
            synchronized (mutex) {
                stream.defaultWriteObject();
            }
        }

        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            synchronized (mutex) {
                return c.removeIf(filter);
            }
        }

        @Override
        public void forEach(Consumer<? super E> action) {
            synchronized (mutex) {
                c.forEach(action);
            }
        }
    }

    static class SynchronizedRandomAccessList<E> extends SynchronizedList<E>
            implements RandomAccess {
        private static final long serialVersionUID = 1530674583602358482L;

        SynchronizedRandomAccessList(List<E> l) {
            super(l);
        }

        SynchronizedRandomAccessList(List<E> l, Object mutex) {
            super(l, mutex);
        }

        @Override public List<E> subList(int start, int end) {
            synchronized (mutex) {
                return new SynchronizedRandomAccessList<E>(list.subList(start, end), mutex);
            }
        }

        /**
         * Replaces this SynchronizedRandomAccessList with a SynchronizedList so
         * that JREs before 1.4 can deserialize this object without any
         * problems. This is necessary since RandomAccess API was introduced
         * only in 1.4.
         * <p>
         *
         * @return SynchronizedList
         *
         * @see SynchronizedList#readResolve()
         */
        private Object writeReplace() {
            return new SynchronizedList<E>(list);
        }
    }

    static class SynchronizedList<E> extends SynchronizedCollection<E> implements List<E> {
        private static final long serialVersionUID = -7754090372962971524L;
        final List<E> list;

        SynchronizedList(List<E> l) {
            super(l);
            list = l;
        }

        SynchronizedList(List<E> l, Object mutex) {
            super(l, mutex);
            list = l;
        }

        @Override public void add(int location, E object) {
            synchronized (mutex) {
                list.add(location, object);
            }
        }

        @Override public boolean addAll(int location, Collection<? extends E> collection) {
            synchronized (mutex) {
                return list.addAll(location, collection);
            }
        }

        @Override public boolean equals(Object object) {
            synchronized (mutex) {
                return list.equals(object);
            }
        }

        @Override public E get(int location) {
            synchronized (mutex) {
                return list.get(location);
            }
        }

        @Override public int hashCode() {
            synchronized (mutex) {
                return list.hashCode();
            }
        }

        @Override public int indexOf(Object object) {
            final int size;
            final Object[] array;
            synchronized (mutex) {
                size = list.size();
                array = new Object[size];
                list.toArray(array);
            }
            if (object != null) {
                for (int i = 0; i < size; i++) {
                    if (object.equals(array[i])) {
                        return i;
                    }
                }
            } else {
                for (int i = 0; i < size; i++) {
                    if (array[i] == null) {
                        return i;
                    }
                }
            }
            return -1;
        }

        @Override public int lastIndexOf(Object object) {
            final int size;
            final Object[] array;
            synchronized (mutex) {
                size = list.size();
                array = new Object[size];
                list.toArray(array);
            }
            if (object != null) {
                for (int i = size - 1; i >= 0; i--) {
                    if (object.equals(array[i])) {
                        return i;
                    }
                }
            } else {
                for (int i = size - 1; i >= 0; i--) {
                    if (array[i] == null) {
                        return i;
                    }
                }
            }
            return -1;
        }

        @Override public ListIterator<E> listIterator() {
            synchronized (mutex) {
                return list.listIterator();
            }
        }

        @Override public ListIterator<E> listIterator(int location) {
            synchronized (mutex) {
                return list.listIterator(location);
            }
        }

        @Override public E remove(int location) {
            synchronized (mutex) {
                return list.remove(location);
            }
        }

        @Override public E set(int location, E object) {
            synchronized (mutex) {
                return list.set(location, object);
            }
        }

        @Override
        public void replaceAll(UnaryOperator<E> operator) {
            synchronized (mutex) {
                list.replaceAll(operator);
            }
        }

        @Override
        public void sort(Comparator<? super E> c) {
            synchronized (mutex) {
                list.sort(c);
            }
        }

        @Override public List<E> subList(int start, int end) {
            synchronized (mutex) {
                return new SynchronizedList<E>(list.subList(start, end), mutex);
            }
        }

        private void writeObject(ObjectOutputStream stream) throws IOException {
            synchronized (mutex) {
                stream.defaultWriteObject();
            }
        }

        /**
         * Resolves SynchronizedList instances to SynchronizedRandomAccessList
         * instances if the underlying list is a Random Access list.
         * <p>
         * This is necessary since SynchronizedRandomAccessList instances are
         * replaced with SynchronizedList instances during serialization for
         * compliance with JREs before 1.4.
         * <p>
         *
         * @return a SynchronizedList instance if the underlying list implements
         *         RandomAccess interface, or this same object if not.
         *
         * @see SynchronizedRandomAccessList#writeReplace()
         */
        private Object readResolve() {
            if (list instanceof RandomAccess) {
                return new SynchronizedRandomAccessList<E>(list, mutex);
            }
            return this;
        }
    }

    static class SynchronizedMap<K, V> implements Map<K, V>, Serializable {
        private static final long serialVersionUID = 1978198479659022715L;

        private final Map<K, V> m;

        final Object mutex;

        SynchronizedMap(Map<K, V> map) {
            m = map;
            mutex = this;
        }

        SynchronizedMap(Map<K, V> map, Object mutex) {
            m = map;
            this.mutex = mutex;
        }

        @Override public void clear() {
            synchronized (mutex) {
                m.clear();
            }
        }

        @Override public boolean containsKey(Object key) {
            synchronized (mutex) {
                return m.containsKey(key);
            }
        }

        @Override public boolean containsValue(Object value) {
            synchronized (mutex) {
                return m.containsValue(value);
            }
        }

        @Override public Set<Map.Entry<K, V>> entrySet() {
            synchronized (mutex) {
                return new SynchronizedSet<Map.Entry<K, V>>(m.entrySet(), mutex);
            }
        }

        @Override public boolean equals(Object object) {
            synchronized (mutex) {
                return m.equals(object);
            }
        }

        @Override public V get(Object key) {
            synchronized (mutex) {
                return m.get(key);
            }
        }

        @Override public int hashCode() {
            synchronized (mutex) {
                return m.hashCode();
            }
        }

        @Override public boolean isEmpty() {
            synchronized (mutex) {
                return m.isEmpty();
            }
        }

        @Override public Set<K> keySet() {
            synchronized (mutex) {
                return new SynchronizedSet<K>(m.keySet(), mutex);
            }
        }

        @Override public V put(K key, V value) {
            synchronized (mutex) {
                return m.put(key, value);
            }
        }

        @Override public void putAll(Map<? extends K, ? extends V> map) {
            synchronized (mutex) {
                m.putAll(map);
            }
        }

        @Override public V remove(Object key) {
            synchronized (mutex) {
                return m.remove(key);
            }
        }

        @Override public int size() {
            synchronized (mutex) {
                return m.size();
            }
        }

        @Override public Collection<V> values() {
            synchronized (mutex) {
                return new SynchronizedCollection<V>(m.values(), mutex);
            }
        }

        @Override public String toString() {
            synchronized (mutex) {
                return m.toString();
            }
        }

        private void writeObject(ObjectOutputStream stream) throws IOException {
            synchronized (mutex) {
                stream.defaultWriteObject();
            }
        }
    }

    static class SynchronizedSet<E> extends SynchronizedCollection<E> implements Set<E> {
        private static final long serialVersionUID = 487447009682186044L;

        SynchronizedSet(Set<E> set) {
            super(set);
        }

        SynchronizedSet(Set<E> set, Object mutex) {
            super(set, mutex);
        }

        @Override public boolean equals(Object object) {
            synchronized (mutex) {
                return c.equals(object);
            }
        }

        @Override public int hashCode() {
            synchronized (mutex) {
                return c.hashCode();
            }
        }

        private void writeObject(ObjectOutputStream stream) throws IOException {
            synchronized (mutex) {
                stream.defaultWriteObject();
            }
        }
    }

    static class SynchronizedSortedMap<K, V> extends SynchronizedMap<K, V>
            implements SortedMap<K, V> {
        private static final long serialVersionUID = -8798146769416483793L;

        private final SortedMap<K, V> sm;

        SynchronizedSortedMap(SortedMap<K, V> map) {
            super(map);
            sm = map;
        }

        SynchronizedSortedMap(SortedMap<K, V> map, Object mutex) {
            super(map, mutex);
            sm = map;
        }

        @Override public Comparator<? super K> comparator() {
            synchronized (mutex) {
                return sm.comparator();
            }
        }

        @Override public K firstKey() {
            synchronized (mutex) {
                return sm.firstKey();
            }
        }

        @Override public SortedMap<K, V> headMap(K endKey) {
            synchronized (mutex) {
                return new SynchronizedSortedMap<K, V>(sm.headMap(endKey),
                        mutex);
            }
        }

        @Override public K lastKey() {
            synchronized (mutex) {
                return sm.lastKey();
            }
        }

        @Override public SortedMap<K, V> subMap(K startKey, K endKey) {
            synchronized (mutex) {
                return new SynchronizedSortedMap<K, V>(sm.subMap(startKey,
                        endKey), mutex);
            }
        }

        @Override public SortedMap<K, V> tailMap(K startKey) {
            synchronized (mutex) {
                return new SynchronizedSortedMap<K, V>(sm.tailMap(startKey),
                        mutex);
            }
        }

        private void writeObject(ObjectOutputStream stream) throws IOException {
            synchronized (mutex) {
                stream.defaultWriteObject();
            }
        }
    }

    static class SynchronizedSortedSet<E> extends SynchronizedSet<E> implements SortedSet<E> {
        private static final long serialVersionUID = 8695801310862127406L;

        private final SortedSet<E> ss;

        SynchronizedSortedSet(SortedSet<E> set) {
            super(set);
            ss = set;
        }

        SynchronizedSortedSet(SortedSet<E> set, Object mutex) {
            super(set, mutex);
            ss = set;
        }

        @Override public Comparator<? super E> comparator() {
            synchronized (mutex) {
                return ss.comparator();
            }
        }

        @Override public E first() {
            synchronized (mutex) {
                return ss.first();
            }
        }

        @Override public SortedSet<E> headSet(E end) {
            synchronized (mutex) {
                return new SynchronizedSortedSet<E>(ss.headSet(end), mutex);
            }
        }

        @Override public E last() {
            synchronized (mutex) {
                return ss.last();
            }
        }

        @Override public SortedSet<E> subSet(E start, E end) {
            synchronized (mutex) {
                return new SynchronizedSortedSet<E>(ss.subSet(start, end),
                        mutex);
            }
        }

        @Override public SortedSet<E> tailSet(E start) {
            synchronized (mutex) {
                return new SynchronizedSortedSet<E>(ss.tailSet(start), mutex);
            }
        }

        private void writeObject(ObjectOutputStream stream) throws IOException {
            synchronized (mutex) {
                stream.defaultWriteObject();
            }
        }
    }

    private static class UnmodifiableCollection<E> implements Collection<E>, Serializable {
        private static final long serialVersionUID = 1820017752578914078L;

        final Collection<E> c;

        UnmodifiableCollection(Collection<E> collection) {
            c = collection;
        }

        @Override public boolean add(E object) {
            throw new UnsupportedOperationException();
        }

        @Override public boolean addAll(Collection<? extends E> collection) {
            throw new UnsupportedOperationException();
        }

        @Override public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override public boolean contains(Object object) {
            return c.contains(object);
        }

        @Override public boolean containsAll(Collection<?> collection) {
            return c.containsAll(collection);
        }

        @Override public boolean isEmpty() {
            return c.isEmpty();
        }

        @Override public Iterator<E> iterator() {
            return new Iterator<E>() {
                Iterator<E> iterator = c.iterator();

                @Override public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override public E next() {
                    return iterator.next();
                }

                @Override public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override public boolean remove(Object object) {
            throw new UnsupportedOperationException();
        }

        @Override public boolean removeAll(Collection<?> collection) {
            throw new UnsupportedOperationException();
        }

        @Override public boolean retainAll(Collection<?> collection) {
            throw new UnsupportedOperationException();
        }

        @Override public int size() {
            return c.size();
        }

        @Override public Object[] toArray() {
            return c.toArray();
        }

        @Override public <T> T[] toArray(T[] array) {
            return c.toArray(array);
        }

        @Override public String toString() {
            return c.toString();
        }

        // Override default methods in Collection
        @Override
        public void forEach(Consumer<? super E> action) {
            c.forEach(action);
        }
        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            throw new UnsupportedOperationException();
        }
        @SuppressWarnings("unchecked")
        @Override
        public Spliterator<E> spliterator() {
            return (Spliterator<E>)c.spliterator();
        }
        @SuppressWarnings("unchecked")
        @Override
        public Stream<E> stream() {
            return (Stream<E>)c.stream();
        }
        @SuppressWarnings("unchecked")
        @Override
        public Stream<E> parallelStream() {
            return (Stream<E>)c.parallelStream();
        }
    }

    private static class UnmodifiableRandomAccessList<E> extends UnmodifiableList<E>
            implements RandomAccess {
        private static final long serialVersionUID = -2542308836966382001L;

        UnmodifiableRandomAccessList(List<E> l) {
            super(l);
        }

        @Override public List<E> subList(int start, int end) {
            return new UnmodifiableRandomAccessList<E>(list.subList(start, end));
        }

        /**
         * Replaces this UnmodifiableRandomAccessList with an UnmodifiableList
         * so that JREs before 1.4 can deserialize this object without any
         * problems. This is necessary since RandomAccess API was introduced
         * only in 1.4.
         * <p>
         *
         * @return UnmodifiableList
         *
         * @see UnmodifiableList#readResolve()
         */
        private Object writeReplace() {
            return new UnmodifiableList<E>(list);
        }
    }

    private static class UnmodifiableList<E> extends UnmodifiableCollection<E>
            implements List<E> {
        private static final long serialVersionUID = -283967356065247728L;

        final List<E> list;

        UnmodifiableList(List<E> l) {
            super(l);
            list = l;
        }

        @Override public void add(int location, E object) {
            throw new UnsupportedOperationException();
        }

        @Override public boolean addAll(int location, Collection<? extends E> collection) {
            throw new UnsupportedOperationException();
        }

        @Override public boolean equals(Object object) {
            return list.equals(object);
        }

        @Override public E get(int location) {
            return list.get(location);
        }

        @Override public int hashCode() {
            return list.hashCode();
        }

        @Override public int indexOf(Object object) {
            return list.indexOf(object);
        }

        @Override public int lastIndexOf(Object object) {
            return list.lastIndexOf(object);
        }

        @Override public ListIterator<E> listIterator() {
            return listIterator(0);
        }

        @Override public ListIterator<E> listIterator(final int location) {
            return new ListIterator<E>() {
                ListIterator<E> iterator = list.listIterator(location);

                @Override public void add(E object) {
                    throw new UnsupportedOperationException();
                }

                @Override public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override public boolean hasPrevious() {
                    return iterator.hasPrevious();
                }

                @Override public E next() {
                    return iterator.next();
                }

                @Override public int nextIndex() {
                    return iterator.nextIndex();
                }

                @Override public E previous() {
                    return iterator.previous();
                }

                @Override public int previousIndex() {
                    return iterator.previousIndex();
                }

                @Override public void remove() {
                    throw new UnsupportedOperationException();
                }

                @Override public void set(E object) {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override public E remove(int location) {
            throw new UnsupportedOperationException();
        }

        @Override public E set(int location, E object) {
            throw new UnsupportedOperationException();
        }

        @Override public List<E> subList(int start, int end) {
            return new UnmodifiableList<E>(list.subList(start, end));
        }

        /**
         * Resolves UnmodifiableList instances to UnmodifiableRandomAccessList
         * instances if the underlying list is a Random Access list.
         * <p>
         * This is necessary since UnmodifiableRandomAccessList instances are
         * replaced with UnmodifiableList instances during serialization for
         * compliance with JREs before 1.4.
         * <p>
         *
         * @return an UnmodifiableList instance if the underlying list
         *         implements RandomAccess interface, or this same object if
         *         not.
         *
         * @see UnmodifiableRandomAccessList#writeReplace()
         */
        private Object readResolve() {
            if (list instanceof RandomAccess) {
                return new UnmodifiableRandomAccessList<E>(list);
            }
            return this;
        }
    }

    private static class UnmodifiableMap<K, V> implements Map<K, V>,
            Serializable {
        private static final long serialVersionUID = -1034234728574286014L;

        private final Map<K, V> m;

        protected static class UnmodifiableEntrySet<K, V> extends
                UnmodifiableSet<Map.Entry<K, V>> {
            private static final long serialVersionUID = 7854390611657943733L;

            private static class UnmodifiableMapEntry<K, V> implements
                    Map.Entry<K, V> {
                Map.Entry<K, V> mapEntry;

                UnmodifiableMapEntry(Map.Entry<K, V> entry) {
                    mapEntry = entry;
                }

                @Override public boolean equals(Object object) {
                    return mapEntry.equals(object);
                }

                @Override public K getKey() {
                    return mapEntry.getKey();
                }

                @Override public V getValue() {
                    return mapEntry.getValue();
                }

                @Override public int hashCode() {
                    return mapEntry.hashCode();
                }

                @Override public V setValue(V object) {
                    throw new UnsupportedOperationException();
                }

                @Override public String toString() {
                    return mapEntry.toString();
                }
            }

            UnmodifiableEntrySet(Set<Map.Entry<K, V>> set) {
                super(set);
            }

            static <K, V> Consumer<Map.Entry<? extends K, ? extends V>> entryConsumer(
                Consumer<? super Entry<K, V>> action) {
                return e -> action.accept(new UnmodifiableMapEntry<K, V>((Entry<K, V>) e));
            }

            public void forEach(Consumer<? super Entry<K, V>> action) {
                Objects.requireNonNull(action);
                c.forEach(entryConsumer(action));
            }

            static final class UnmodifiableEntrySetSpliterator<K, V>
                implements Spliterator<Entry<K,V>> {
                final Spliterator<Map.Entry<K, V>> s;

                UnmodifiableEntrySetSpliterator(Spliterator<Entry<K, V>> s) {
                    this.s = s;
                }

                @Override
                public boolean tryAdvance(Consumer<? super Entry<K, V>> action) {
                    Objects.requireNonNull(action);
                    return s.tryAdvance(entryConsumer(action));
                }

                @Override
                public void forEachRemaining(Consumer<? super Entry<K, V>> action) {
                    Objects.requireNonNull(action);
                    s.forEachRemaining(entryConsumer(action));
                }

                @Override
                public Spliterator<Entry<K, V>> trySplit() {
                    Spliterator<Entry<K, V>> split = s.trySplit();
                    return split == null
                        ? null
                        : new UnmodifiableEntrySetSpliterator<>(split);
                }

                @Override
                public long estimateSize() {
                    return s.estimateSize();
                }

                @Override
                public long getExactSizeIfKnown() {
                    return s.getExactSizeIfKnown();
                }

                @Override
                public int characteristics() {
                    return s.characteristics();
                }

                @Override
                public boolean hasCharacteristics(int characteristics) {
                    return s.hasCharacteristics(characteristics);
                }

                @Override
                public Comparator<? super Entry<K, V>> getComparator() {
                    return s.getComparator();
                }
            }

            @SuppressWarnings("unchecked")
            public Spliterator<Entry<K,V>> spliterator() {
                return new UnmodifiableEntrySetSpliterator<>(
                    (Spliterator<Map.Entry<K, V>>) c.spliterator());
            }

            @Override
            public Stream<Entry<K,V>> stream() {
                return StreamSupport.stream(spliterator(), false);
            }

            @Override
            public Stream<Entry<K,V>> parallelStream() {
                return StreamSupport.stream(spliterator(), true);
            }

            @Override public Iterator<Map.Entry<K, V>> iterator() {
                return new Iterator<Map.Entry<K, V>>() {
                    Iterator<Map.Entry<K, V>> iterator = c.iterator();

                    @Override public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override public Map.Entry<K, V> next() {
                        return new UnmodifiableMapEntry<K, V>(iterator.next());
                    }

                    @Override public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            @Override public Object[] toArray() {
                int length = c.size();
                Object[] result = new Object[length];
                Iterator<?> it = iterator();
                for (int i = 0; i < length; i++) {
                    result[i] = it.next();
                }
                return result;
            }

            @SuppressWarnings("unchecked")
            @Override public <T> T[] toArray(T[] contents) {
                int size = c.size(), index = 0;
                Iterator<Map.Entry<K, V>> it = iterator();
                if (size > contents.length) {
                    Class<?> ct = contents.getClass().getComponentType();
                    contents = (T[]) Array.newInstance(ct, size);
                }
                while (index < size) {
                    contents[index++] = (T) it.next();
                }
                if (index < contents.length) {
                    contents[index] = null;
                }
                return contents;
            }
        }

        UnmodifiableMap(Map<K, V> map) {
            m = map;
        }

        @Override public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override public boolean containsKey(Object key) {
            return m.containsKey(key);
        }

        @Override public boolean containsValue(Object value) {
            return m.containsValue(value);
        }

        @Override public Set<Map.Entry<K, V>> entrySet() {
            return new UnmodifiableEntrySet<K, V>(m.entrySet());
        }

        @Override public boolean equals(Object object) {
            return m.equals(object);
        }

        @Override public V get(Object key) {
            return m.get(key);
        }

        @Override public int hashCode() {
            return m.hashCode();
        }

        @Override public boolean isEmpty() {
            return m.isEmpty();
        }

        @Override public Set<K> keySet() {
            return new UnmodifiableSet<K>(m.keySet());
        }

        @Override public V put(K key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override public void putAll(Map<? extends K, ? extends V> map) {
            throw new UnsupportedOperationException();
        }

        @Override public V remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override public int size() {
            return m.size();
        }

        @Override public Collection<V> values() {
            return new UnmodifiableCollection<V>(m.values());
        }

        @Override public String toString() {
            return m.toString();
        }
    }

    private static class UnmodifiableSet<E> extends UnmodifiableCollection<E>
            implements Set<E> {
        private static final long serialVersionUID = -9215047833775013803L;

        UnmodifiableSet(Set<E> set) {
            super(set);
        }

        @Override public boolean equals(Object object) {
            return c.equals(object);
        }

        @Override public int hashCode() {
            return c.hashCode();
        }
    }

    private static class UnmodifiableSortedMap<K, V> extends
            UnmodifiableMap<K, V> implements SortedMap<K, V> {
        private static final long serialVersionUID = -8806743815996713206L;

        private final SortedMap<K, V> sm;

        UnmodifiableSortedMap(SortedMap<K, V> map) {
            super(map);
            sm = map;
        }

        @Override public Comparator<? super K> comparator() {
            return sm.comparator();
        }

        @Override public K firstKey() {
            return sm.firstKey();
        }

        @Override public SortedMap<K, V> headMap(K before) {
            return new UnmodifiableSortedMap<K, V>(sm.headMap(before));
        }

        @Override public K lastKey() {
            return sm.lastKey();
        }

        @Override public SortedMap<K, V> subMap(K start, K end) {
            return new UnmodifiableSortedMap<K, V>(sm.subMap(start, end));
        }

        @Override public SortedMap<K, V> tailMap(K after) {
            return new UnmodifiableSortedMap<K, V>(sm.tailMap(after));
        }
    }

    private static class UnmodifiableSortedSet<E> extends UnmodifiableSet<E>
            implements SortedSet<E> {
        private static final long serialVersionUID = -4929149591599911165L;

        private final SortedSet<E> ss;

        UnmodifiableSortedSet(SortedSet<E> set) {
            super(set);
            ss = set;
        }

        @Override public Comparator<? super E> comparator() {
            return ss.comparator();
        }

        @Override public E first() {
            return ss.first();
        }

        @Override public SortedSet<E> headSet(E before) {
            return new UnmodifiableSortedSet<E>(ss.headSet(before));
        }

        @Override public E last() {
            return ss.last();
        }

        @Override public SortedSet<E> subSet(E start, E end) {
            return new UnmodifiableSortedSet<E>(ss.subSet(start, end));
        }

        @Override public SortedSet<E> tailSet(E after) {
            return new UnmodifiableSortedSet<E>(ss.tailSet(after));
        }
    }

    private Collections() {}

    /**
     * Performs a binary search for the specified element in the specified
     * sorted list. The list needs to be already sorted in natural sorting
     * order. Searching in an unsorted array has an undefined result. It's also
     * undefined which element is found if there are multiple occurrences of the
     * same element.
     *
     * @param list
     *            the sorted list to search.
     * @param object
     *            the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is the {@code -index - 1} where the element would be inserted
     * @throws ClassCastException
     *             if an element in the List or the search element does not
     *             implement Comparable, or cannot be compared to each other.
     */
    @SuppressWarnings("unchecked")
    public static <T> int binarySearch(List<? extends Comparable<? super T>> list, T object) {
        if (list == null) {
            throw new NullPointerException("list == null");
        }
        if (list.isEmpty()) {
            return -1;
        }


        if (!(list instanceof RandomAccess)) {
            ListIterator<? extends Comparable<? super T>> it = list.listIterator();
            while (it.hasNext()) {
                final int result = it.next().compareTo(object);
                if (result == 0) {
                    return it.previousIndex();
                } else if (result > 0) {
                    return -it.previousIndex() - 1;
                }
            }
            return -list.size() - 1;
        }

        int low = 0, mid = list.size(), high = mid - 1, result = 1;
        while (low <= high) {
            mid = (low + high) >>> 1;
            result = list.get(mid).compareTo(object);
            if (result < 0) {
                low = mid + 1;
            } else if (result == 0) {
                return mid;
            } else {
                high = mid - 1;
            }
        }
        return -mid - (result > 0 ? 1 : 2);
    }

    /**
     * Performs a binary search for the specified element in the specified
     * sorted list using the specified comparator. The list needs to be already
     * sorted according to the comparator passed. Searching in an unsorted array
     * has an undefined result. It's also undefined which element is found if
     * there are multiple occurrences of the same element.
     *
     * @param list
     *            the sorted List to search.
     * @param object
     *            the element to find.
     * @param comparator
     *            the comparator. If the comparator is {@code null} then the
     *            search uses the objects' natural ordering.
     * @return the non-negative index of the element, or a negative index which
     *         is the {@code -index - 1} where the element would be inserted.
     * @throws ClassCastException
     *             when an element in the list and the searched element cannot
     *             be compared to each other using the comparator.
     */
    @SuppressWarnings("unchecked")
    public static <T> int binarySearch(List<? extends T> list, T object,
            Comparator<? super T> comparator) {
        if (comparator == null) {
            return Collections.binarySearch(
                    (List<? extends Comparable<? super T>>) list, object);
        }
        if (!(list instanceof RandomAccess)) {
            ListIterator<? extends T> it = list.listIterator();
            while (it.hasNext()) {
                final int result = comparator.compare(it.next(), object);
                if (result == 0) {
                    return it.previousIndex();
                } else if (result > 0) {
                    return -it.previousIndex() - 1;
                }
            }
            return -list.size() - 1;
        }

        int low = 0, mid = list.size(), high = mid - 1, result = 1;
        while (low <= high) {
            mid = (low + high) >>> 1;
            result = comparator.compare(list.get(mid), object);
            if (result < 0) {
                low = mid + 1;
            } else if (result == 0) {
                return mid;
            } else {
                high = mid - 1;
            }
        }
        return -mid - (result > 0 ? 1 : 2);
    }

    /**
     * Copies the elements from the source list to the destination list. At the
     * end both lists will have the same objects at the same index. If the
     * destination array is larger than the source list, the elements in the
     * destination list with {@code index >= source.size()} will be unchanged.
     *
     * @param destination
     *            the list whose elements are set from the source list.
     * @param source
     *            the list with the elements to be copied into the destination.
     * @throws IndexOutOfBoundsException
     *             when the destination list is smaller than the source list.
     * @throws UnsupportedOperationException
     *             when replacing an element in the destination list is not
     *             supported.
     */
    public static <T> void copy(List<? super T> destination, List<? extends T> source) {
        if (destination.size() < source.size()) {
            throw new IndexOutOfBoundsException("destination.size() < source.size(): " +
                    destination.size() + " < " + source.size());
        }
        Iterator<? extends T> srcIt = source.iterator();
        ListIterator<? super T> destIt = destination.listIterator();
        while (srcIt.hasNext()) {
            try {
                destIt.next();
            } catch (NoSuchElementException e) {
                // TODO: AssertionError?
                throw new IndexOutOfBoundsException("Source size " + source.size() +
                        " does not fit into destination");
            }
            destIt.set(srcIt.next());
        }
    }

    /**
     * Returns an {@code Enumeration} on the specified collection.
     *
     * @param collection
     *            the collection to enumerate.
     * @return an Enumeration.
     */
    public static <T> Enumeration<T> enumeration(Collection<T> collection) {
        final Collection<T> c = collection;
        return new Enumeration<T>() {
            Iterator<T> it = c.iterator();

            @Override public boolean hasMoreElements() {
                return it.hasNext();
            }

            @Override public T nextElement() {
                return it.next();
            }
        };
    }

    /**
     * Fills the specified list with the specified element.
     *
     * @param list
     *            the list to fill.
     * @param object
     *            the element to fill the list with.
     * @throws UnsupportedOperationException
     *             when replacing an element in the List is not supported.
     */
    public static <T> void fill(List<? super T> list, T object) {
        ListIterator<? super T> it = list.listIterator();
        while (it.hasNext()) {
            it.next();
            it.set(object);
        }
    }

    /**
     * Searches the specified collection for the maximum element.
     *
     * @param collection
     *            the collection to search.
     * @return the maximum element in the Collection.
     * @throws ClassCastException
     *             when an element in the collection does not implement
     *             {@code Comparable} or elements cannot be compared to each
     *             other.
     */
    public static <T extends Object & Comparable<? super T>> T max(
            Collection<? extends T> collection) {
        Iterator<? extends T> it = collection.iterator();
        T max = it.next();
        while (it.hasNext()) {
            T next = it.next();
            if (max.compareTo(next) < 0) {
                max = next;
            }
        }
        return max;
    }

    /**
     * Searches the specified collection for the maximum element using the
     * specified comparator.
     *
     * @param collection
     *            the collection to search.
     * @param comparator
     *            the comparator.
     * @return the maximum element in the Collection.
     * @throws ClassCastException
     *             when elements in the collection cannot be compared to each
     *             other using the {@code Comparator}.
     */
    public static <T> T max(Collection<? extends T> collection,
            Comparator<? super T> comparator) {
        if (comparator == null) {
            @SuppressWarnings("unchecked") // null comparator? T is comparable
            T result = (T) max((Collection<Comparable>) collection);
            return result;
        }

        Iterator<? extends T> it = collection.iterator();
        T max = it.next();
        while (it.hasNext()) {
            T next = it.next();
            if (comparator.compare(max, next) < 0) {
                max = next;
            }
        }
        return max;
    }

    /**
     * Searches the specified collection for the minimum element.
     *
     * @param collection
     *            the collection to search.
     * @return the minimum element in the collection.
     * @throws ClassCastException
     *             when an element in the collection does not implement
     *             {@code Comparable} or elements cannot be compared to each
     *             other.
     */
    public static <T extends Object & Comparable<? super T>> T min(
            Collection<? extends T> collection) {
        Iterator<? extends T> it = collection.iterator();
        T min = it.next();
        while (it.hasNext()) {
            T next = it.next();
            if (min.compareTo(next) > 0) {
                min = next;
            }
        }
        return min;
    }

    /**
     * Searches the specified collection for the minimum element using the
     * specified comparator.
     *
     * @param collection
     *            the collection to search.
     * @param comparator
     *            the comparator.
     * @return the minimum element in the collection.
     * @throws ClassCastException
     *             when elements in the collection cannot be compared to each
     *             other using the {@code Comparator}.
     */
    public static <T> T min(Collection<? extends T> collection,
            Comparator<? super T> comparator) {
        if (comparator == null) {
            @SuppressWarnings("unchecked") // null comparator? T is comparable
            T result = (T) min((Collection<Comparable>) collection);
            return result;
        }

        Iterator<? extends T> it = collection.iterator();
        T min = it.next();
        while (it.hasNext()) {
            T next = it.next();
            if (comparator.compare(min, next) > 0) {
                min = next;
            }
        }
        return min;
    }

    /**
     * Returns a list containing the specified number of the specified element.
     * The list cannot be modified. The list is serializable.
     *
     * @param length
     *            the size of the returned list.
     * @param object
     *            the element to be added {@code length} times to a list.
     * @return a list containing {@code length} copies of the element.
     * @throws IllegalArgumentException
     *             when {@code length < 0}.
     */
    public static <T> List<T> nCopies(final int length, T object) {
        return new CopiesList<T>(length, object);
    }

    /**
     * Modifies the specified {@code List} by reversing the order of the
     * elements.
     *
     * @param list
     *            the list to reverse.
     * @throws UnsupportedOperationException
     *             when replacing an element in the List is not supported.
     */
    @SuppressWarnings("unchecked")
    public static void reverse(List<?> list) {
        int size = list.size();
        ListIterator<Object> front = (ListIterator<Object>) list.listIterator();
        ListIterator<Object> back = (ListIterator<Object>) list
                .listIterator(size);
        for (int i = 0; i < size / 2; i++) {
            Object frontNext = front.next();
            Object backPrev = back.previous();
            front.set(backPrev);
            back.set(frontNext);
        }
    }

    /**
     * A comparator which reverses the natural order of the elements. The
     * {@code Comparator} that's returned is {@link Serializable}.
     *
     * @return a {@code Comparator} instance.
     */
    @SuppressWarnings("unchecked")
    public static <T> Comparator<T> reverseOrder() {
        return (Comparator) ReverseComparator.INSTANCE;
    }

    /**
     * Returns a {@link Comparator} that reverses the order of the
     * {@code Comparator} passed. If the {@code Comparator} passed is
     * {@code null}, then this method is equivalent to {@link #reverseOrder()}.
     * <p>
     * The {@code Comparator} that's returned is {@link Serializable} if the
     * {@code Comparator} passed is serializable or {@code null}.
     *
     * @param c
     *            the {@code Comparator} to reverse or {@code null}.
     * @return a {@code Comparator} instance.
     * @since 1.5
     */
    public static <T> Comparator<T> reverseOrder(Comparator<T> c) {
        if (c == null) {
            return reverseOrder();
        }
        if (c instanceof ReverseComparator2) {
            return ((ReverseComparator2<T>) c).cmp;
        }
        return new ReverseComparator2<T>(c);
    }

    /**
     * Moves every element of the list to a random new position in the list.
     *
     * @param list
     *            the List to shuffle.
     *
     * @throws UnsupportedOperationException
     *             when replacing an element in the List is not supported.
     */
    public static void shuffle(List<?> list) {
        shuffle(list, new Random());
    }

    /**
     * Moves every element of the list to a random new position in the list
     * using the specified random number generator.
     *
     * @param list
     *            the list to shuffle.
     * @param random
     *            the random number generator.
     * @throws UnsupportedOperationException
     *             when replacing an element in the list is not supported.
     */
    public static void shuffle(List<?> list, Random random) {
        @SuppressWarnings("unchecked") // we won't put foreign objects in
        final List<Object> objectList = (List<Object>) list;

        if (list instanceof RandomAccess) {
            for (int i = objectList.size() - 1; i > 0; i--) {
                int index = random.nextInt(i + 1);
                objectList.set(index, objectList.set(i, objectList.get(index)));
            }
        } else {
            Object[] array = objectList.toArray();
            for (int i = array.length - 1; i > 0; i--) {
                int index = random.nextInt(i + 1);
                Object temp = array[i];
                array[i] = array[index];
                array[index] = temp;
            }

            int i = 0;
            ListIterator<Object> it = objectList.listIterator();
            while (it.hasNext()) {
                it.next();
                it.set(array[i++]);
            }
        }
    }

    /**
     * Returns a set containing the specified element. The set cannot be
     * modified. The set is serializable.
     *
     * @param object
     *            the element.
     * @return a set containing the element.
     */
    public static <E> Set<E> singleton(E object) {
        return new SingletonSet<E>(object);
    }

    /**
     * Returns a list containing the specified element. The list cannot be
     * modified. The list is serializable.
     *
     * @param object
     *            the element.
     * @return a list containing the element.
     */
    public static <E> List<E> singletonList(E object) {
        return new SingletonList<E>(object);
    }

    /**
     * Returns a Map containing the specified key and value. The map cannot be
     * modified. The map is serializable.
     *
     * @param key
     *            the key.
     * @param value
     *            the value.
     * @return a Map containing the key and value.
     */
    public static <K, V> Map<K, V> singletonMap(K key, V value) {
        return new SingletonMap<K, V>(key, value);
    }

    /**
     * Sorts the given list in ascending natural order. The algorithm is
     * stable which means equal elements don't get reordered.
     *
     * @throws ClassCastException if any element does not implement {@code Comparable},
     *     or if {@code compareTo} throws for any pair of elements.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Comparable<? super T>> void sort(List<T> list) {
        // Note that we can't use instanceof here since ArrayList isn't final and
        // subclasses might make arbitrary use of array and modCount.
        if (list.getClass() == ArrayList.class) {
            ArrayList<T> arrayList = (ArrayList<T>) list;
            Object[] array = arrayList.array;
            int end = arrayList.size();
            Arrays.sort(array, 0, end);
            arrayList.modCount++;
        } else {
            Object[] array = list.toArray();
            Arrays.sort(array);
            int i = 0;
            ListIterator<T> it = list.listIterator();
            while (it.hasNext()) {
                it.next();
                it.set((T) array[i++]);
            }
        }
    }

    /**
     * Sorts the given list using the given comparator. The algorithm is
     * stable which means equal elements don't get reordered.
     *
     * @throws ClassCastException if any element does not implement {@code Comparable},
     *     or if {@code compareTo} throws for any pair of elements.
     */
    @SuppressWarnings("unchecked")
    public static <T> void sort(List<T> list, Comparator<? super T> comparator) {
        if (list.getClass() == ArrayList.class) {
            ArrayList<T> arrayList = (ArrayList<T>) list;
            T[] array = (T[]) arrayList.array;
            int end = arrayList.size();
            Arrays.sort(array, 0, end, comparator);
            arrayList.modCount++;
        } else {
            T[] array = list.toArray((T[]) new Object[list.size()]);
            Arrays.sort(array, comparator);
            int i = 0;
            ListIterator<T> it = list.listIterator();
            while (it.hasNext()) {
                it.next();
                it.set(array[i++]);
            }
        }
    }

    /**
     * Swaps the elements of list {@code list} at indices {@code index1} and
     * {@code index2}.
     *
     * @param list
     *            the list to manipulate.
     * @param index1
     *            position of the first element to swap with the element in
     *            index2.
     * @param index2
     *            position of the other element.
     *
     * @throws IndexOutOfBoundsException
     *             if index1 or index2 is out of range of this list.
     * @since 1.4
     */
    @SuppressWarnings("unchecked")
    public static void swap(List<?> list, int index1, int index2) {
        if (list == null) {
            throw new NullPointerException("list == null");
        }
        final int size = list.size();
        if (index1 < 0 || index1 >= size || index2 < 0 || index2 >= size) {
            throw new IndexOutOfBoundsException();
        }
        if (index1 == index2) {
            return;
        }
        List<Object> rawList = (List<Object>) list;
        rawList.set(index2, rawList.set(index1, rawList.get(index2)));
    }

    /**
     * Replaces all occurrences of Object {@code obj} in {@code list} with
     * {@code newObj}. If the {@code obj} is {@code null}, then all
     * occurrences of {@code null} are replaced with {@code newObj}.
     *
     * @param list
     *            the list to modify.
     * @param obj
     *            the object to find and replace occurrences of.
     * @param obj2
     *            the object to replace all occurrences of {@code obj} in
     *            {@code list}.
     * @return true, if at least one occurrence of {@code obj} has been found in
     *         {@code list}.
     * @throws UnsupportedOperationException
     *             if the list does not support setting elements.
     */
    public static <T> boolean replaceAll(List<T> list, T obj, T obj2) {
        int index;
        boolean found = false;

        while ((index = list.indexOf(obj)) > -1) {
            found = true;
            list.set(index, obj2);
        }
        return found;
    }

    /**
     * Rotates the elements in {@code list} by the distance {@code dist}
     * <p>
     * e.g. for a given list with elements [1, 2, 3, 4, 5, 6, 7, 8, 9, 0],
     * calling rotate(list, 3) or rotate(list, -7) would modify the list to look
     * like this: [8, 9, 0, 1, 2, 3, 4, 5, 6, 7]
     *
     * @param lst
     *            the list whose elements are to be rotated.
     * @param dist
     *            is the distance the list is rotated. This can be any valid
     *            integer. Negative values rotate the list backwards.
     */
    @SuppressWarnings("unchecked")
    public static void rotate(List<?> lst, int dist) {
        List<Object> list = (List<Object>) lst;
        int size = list.size();

        // Can't sensibly rotate an empty collection
        if (size == 0) {
            return;
        }

        // normalize the distance
        int normdist;
        if (dist > 0) {
            normdist = dist % size;
        } else {
            normdist = size - ((dist % size) * (-1));
        }

        if (normdist == 0 || normdist == size) {
            return;
        }

        if (list instanceof RandomAccess) {
            // make sure each element gets juggled
            // with the element in the position it is supposed to go to
            Object temp = list.get(0);
            int index = 0, beginIndex = 0;
            for (int i = 0; i < size; i++) {
                index = (index + normdist) % size;
                temp = list.set(index, temp);
                if (index == beginIndex) {
                    index = ++beginIndex;
                    temp = list.get(beginIndex);
                }
            }
        } else {
            int divideIndex = (size - normdist) % size;
            List<Object> sublist1 = list.subList(0, divideIndex);
            List<Object> sublist2 = list.subList(divideIndex, size);
            reverse(sublist1);
            reverse(sublist2);
            reverse(list);
        }
    }

    /**
     * Searches the {@code list} for {@code sublist} and returns the beginning
     * index of the first occurrence.
     * <p>
     * -1 is returned if the {@code sublist} does not exist in {@code list}.
     *
     * @param list
     *            the List to search {@code sublist} in.
     * @param sublist
     *            the List to search in {@code list}.
     * @return the beginning index of the first occurrence of {@code sublist} in
     *         {@code list}, or -1.
     */
    public static int indexOfSubList(List<?> list, List<?> sublist) {
        int size = list.size();
        int sublistSize = sublist.size();

        if (sublistSize > size) {
            return -1;
        }

        if (sublistSize == 0) {
            return 0;
        }

        // find the first element of sublist in the list to get a head start
        Object firstObj = sublist.get(0);
        int index = list.indexOf(firstObj);
        if (index == -1) {
            return -1;
        }

        while (index < size && (size - index >= sublistSize)) {
            ListIterator<?> listIt = list.listIterator(index);

            if ((firstObj == null) ? listIt.next() == null : firstObj
                    .equals(listIt.next())) {

                // iterate through the elements in sublist to see
                // if they are included in the same order in the list
                ListIterator<?> sublistIt = sublist.listIterator(1);
                boolean difFound = false;
                while (sublistIt.hasNext()) {
                    Object element = sublistIt.next();
                    if (!listIt.hasNext()) {
                        return -1;
                    }
                    if ((element == null) ? listIt.next() != null : !element
                            .equals(listIt.next())) {
                        difFound = true;
                        break;
                    }
                }
                // All elements of sublist are found in main list
                // starting from index.
                if (!difFound) {
                    return index;
                }
            }
            // This was not the sequence we were looking for,
            // continue search for the firstObj in main list
            // at the position after index.
            index++;
        }
        return -1;
    }

    /**
     * Searches the {@code list} for {@code sublist} and returns the beginning
     * index of the last occurrence.
     * <p>
     * -1 is returned if the {@code sublist} does not exist in {@code list}.
     *
     * @param list
     *            the list to search {@code sublist} in.
     * @param sublist
     *            the list to search in {@code list}.
     * @return the beginning index of the last occurrence of {@code sublist} in
     *         {@code list}, or -1.
     */
    public static int lastIndexOfSubList(List<?> list, List<?> sublist) {
        int sublistSize = sublist.size();
        int size = list.size();

        if (sublistSize > size) {
            return -1;
        }

        if (sublistSize == 0) {
            return size;
        }

        // find the last element of sublist in the list to get a head start
        Object lastObj = sublist.get(sublistSize - 1);
        int index = list.lastIndexOf(lastObj);

        while ((index > -1) && (index + 1 >= sublistSize)) {
            ListIterator<?> listIt = list.listIterator(index + 1);

            if ((lastObj == null) ? listIt.previous() == null : lastObj
                    .equals(listIt.previous())) {
                // iterate through the elements in sublist to see
                // if they are included in the same order in the list
                ListIterator<?> sublistIt = sublist
                        .listIterator(sublistSize - 1);
                boolean difFound = false;
                while (sublistIt.hasPrevious()) {
                    Object element = sublistIt.previous();
                    if (!listIt.hasPrevious()) {
                        return -1;
                    }
                    if ((element == null) ? listIt.previous() != null
                            : !element.equals(listIt.previous())) {
                        difFound = true;
                        break;
                    }
                }
                // All elements of sublist are found in main list
                // starting from listIt.nextIndex().
                if (!difFound) {
                    return listIt.nextIndex();
                }
            }
            // This was not the sequence we were looking for,
            // continue search for the lastObj in main list
            // at the position before index.
            index--;
        }
        return -1;
    }

    /**
     * Returns an {@code ArrayList} with all the elements in the {@code
     * enumeration}. The elements in the returned {@code ArrayList} are in the
     * same order as in the {@code enumeration}.
     *
     * @param enumeration
     *            the source {@link Enumeration}.
     * @return an {@code ArrayList} from {@code enumeration}.
     */
    public static <T> ArrayList<T> list(Enumeration<T> enumeration) {
        ArrayList<T> list = new ArrayList<T>();
        while (enumeration.hasMoreElements()) {
            list.add(enumeration.nextElement());
        }
        return list;
    }

    /**
     * Returns a wrapper on the specified collection which synchronizes all
     * access to the collection.
     *
     * @param collection
     *            the Collection to wrap in a synchronized collection.
     * @return a synchronized Collection.
     */
    public static <T> Collection<T> synchronizedCollection(
            Collection<T> collection) {
        if (collection == null) {
            throw new NullPointerException("collection == null");
        }
        return new SynchronizedCollection<T>(collection);
    }

    /**
     * Returns a wrapper on the specified List which synchronizes all access to
     * the List.
     *
     * @param list
     *            the List to wrap in a synchronized list.
     * @return a synchronized List.
     */
    public static <T> List<T> synchronizedList(List<T> list) {
        if (list == null) {
            throw new NullPointerException("list == null");
        }
        if (list instanceof RandomAccess) {
            return new SynchronizedRandomAccessList<T>(list);
        }
        return new SynchronizedList<T>(list);
    }

    /**
     * Returns a wrapper on the specified map which synchronizes all access to
     * the map.
     *
     * @param map
     *            the map to wrap in a synchronized map.
     * @return a synchronized Map.
     */
    public static <K, V> Map<K, V> synchronizedMap(Map<K, V> map) {
        if (map == null) {
            throw new NullPointerException("map == null");
        }
        return new SynchronizedMap<K, V>(map);
    }

    /**
     * Returns a wrapper on the specified set which synchronizes all access to
     * the set.
     *
     * @param set
     *            the set to wrap in a synchronized set.
     * @return a synchronized set.
     */
    public static <E> Set<E> synchronizedSet(Set<E> set) {
        if (set == null) {
            throw new NullPointerException("set == null");
        }
        return new SynchronizedSet<E>(set);
    }

    /**
     * Returns a wrapper on the specified sorted map which synchronizes all
     * access to the sorted map.
     *
     * @param map
     *            the sorted map to wrap in a synchronized sorted map.
     * @return a synchronized sorted map.
     */
    public static <K, V> SortedMap<K, V> synchronizedSortedMap(
            SortedMap<K, V> map) {
        if (map == null) {
            throw new NullPointerException("map == null");
        }
        return new SynchronizedSortedMap<K, V>(map);
    }

    /**
     * Returns a synchronized (thread-safe) navigable map backed by the
     * specified navigable map.  In order to guarantee serial access, it is
     * critical that <strong>all</strong> access to the backing navigable map is
     * accomplished through the returned navigable map (or its views).<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * navigable map when traversing any of its collection views, or the
     * collections views of any of its {@code subMap}, {@code headMap} or
     * {@code tailMap} views, via {@link Iterator}, {@link Spliterator} or
     * {@link Stream}:
     * <pre>
     *  NavigableMap m = Collections.synchronizedNavigableMap(new TreeMap());
     *      ...
     *  Set s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized (m) {  // Synchronizing on m, not s!
     *      Iterator i = s.iterator(); // Must be in synchronized block
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * or:
     * <pre>
     *  NavigableMap m = Collections.synchronizedNavigableMap(new TreeMap());
     *  NavigableMap m2 = m.subMap(foo, true, bar, false);
     *      ...
     *  Set s2 = m2.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized (m) {  // Synchronizing on m, not m2 or s2!
     *      Iterator i = s.iterator(); // Must be in synchronized block
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned navigable map will be serializable if the specified
     * navigable map is serializable.
     *
     * @param <K> the class of the map keys
     * @param <V> the class of the map values
     * @param  m the navigable map to be "wrapped" in a synchronized navigable
     *              map
     * @return a synchronized view of the specified navigable map.
     * @since 1.8
     */
    public static <K,V> NavigableMap<K,V> synchronizedNavigableMap(NavigableMap<K,V> m) {
        return new SynchronizedNavigableMap<>(m);
    }

    /**
     * A synchronized NavigableMap.
     *
     * @serial include
     */
    static class SynchronizedNavigableMap<K,V>
        extends SynchronizedSortedMap<K,V>
        implements NavigableMap<K,V>
    {
        private static final long serialVersionUID = 699392247599746807L;

        private final NavigableMap<K,V> nm;

        SynchronizedNavigableMap(NavigableMap<K,V> m) {
            super(m);
            nm = m;
        }
        SynchronizedNavigableMap(NavigableMap<K,V> m, Object mutex) {
            super(m, mutex);
            nm = m;
        }

        public Entry<K, V> lowerEntry(K key)
        { synchronized (mutex) { return nm.lowerEntry(key); } }
        public K lowerKey(K key)
        { synchronized (mutex) { return nm.lowerKey(key); } }
        public Entry<K, V> floorEntry(K key)
        { synchronized (mutex) { return nm.floorEntry(key); } }
        public K floorKey(K key)
        { synchronized (mutex) { return nm.floorKey(key); } }
        public Entry<K, V> ceilingEntry(K key)
        { synchronized (mutex) { return nm.ceilingEntry(key); } }
        public K ceilingKey(K key)
        { synchronized (mutex) { return nm.ceilingKey(key); } }
        public Entry<K, V> higherEntry(K key)
        { synchronized (mutex) { return nm.higherEntry(key); } }
        public K higherKey(K key)
        { synchronized (mutex) { return nm.higherKey(key); } }
        public Entry<K, V> firstEntry()
        { synchronized (mutex) { return nm.firstEntry(); } }
        public Entry<K, V> lastEntry()
        { synchronized (mutex) { return nm.lastEntry(); } }
        public Entry<K, V> pollFirstEntry()
        { synchronized (mutex) { return nm.pollFirstEntry(); } }
        public Entry<K, V> pollLastEntry()
        { synchronized (mutex) { return nm.pollLastEntry(); } }

        public NavigableMap<K, V> descendingMap() {
            synchronized (mutex) {
                return
                    new SynchronizedNavigableMap<>(nm.descendingMap(), mutex);
            }
        }

        public NavigableSet<K> keySet() {
            return navigableKeySet();
        }

        public NavigableSet<K> navigableKeySet() {
            synchronized (mutex) {
                return new SynchronizedNavigableSet<>(nm.navigableKeySet(), mutex);
            }
        }

        public NavigableSet<K> descendingKeySet() {
            synchronized (mutex) {
                return new SynchronizedNavigableSet<>(nm.descendingKeySet(), mutex);
            }
        }


        public SortedMap<K,V> subMap(K fromKey, K toKey) {
            synchronized (mutex) {
                return new SynchronizedNavigableMap<>(
                    nm.subMap(fromKey, true, toKey, false), mutex);
            }
        }
        public SortedMap<K,V> headMap(K toKey) {
            synchronized (mutex) {
                return new SynchronizedNavigableMap<>(nm.headMap(toKey, false), mutex);
            }
        }
        public SortedMap<K,V> tailMap(K fromKey) {
            synchronized (mutex) {
                return new SynchronizedNavigableMap<>(nm.tailMap(fromKey, true),mutex);
            }
        }

        public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
            synchronized (mutex) {
                return new SynchronizedNavigableMap<>(
                    nm.subMap(fromKey, fromInclusive, toKey, toInclusive), mutex);
            }
        }

        public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
            synchronized (mutex) {
                return new SynchronizedNavigableMap<>(
                    nm.headMap(toKey, inclusive), mutex);
            }
        }

        public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
            synchronized (mutex) {
                return new SynchronizedNavigableMap<>(
                    nm.tailMap(fromKey, inclusive), mutex);
            }
        }
    }

    /**
     * Returns a wrapper on the specified sorted set which synchronizes all
     * access to the sorted set.
     *
     * @param set
     *            the sorted set to wrap in a synchronized sorted set.
     * @return a synchronized sorted set.
     */
    public static <E> SortedSet<E> synchronizedSortedSet(SortedSet<E> set) {
        if (set == null) {
            throw new NullPointerException("set == null");
        }
        return new SynchronizedSortedSet<E>(set);
    }

    /**
     * Returns a synchronized (thread-safe) navigable set backed by the
     * specified navigable set.  In order to guarantee serial access, it is
     * critical that <strong>all</strong> access to the backing navigable set is
     * accomplished through the returned navigable set (or its views).<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * navigable set when traversing it, or any of its {@code subSet},
     * {@code headSet}, or {@code tailSet} views, via {@link Iterator},
     * {@link Spliterator} or {@link Stream}:
     * <pre>
     *  NavigableSet s = Collections.synchronizedNavigableSet(new TreeSet());
     *      ...
     *  synchronized (s) {
     *      Iterator i = s.iterator(); // Must be in the synchronized block
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * or:
     * <pre>
     *  NavigableSet s = Collections.synchronizedNavigableSet(new TreeSet());
     *  NavigableSet s2 = s.headSet(foo, true);
     *      ...
     *  synchronized (s) {  // Note: s, not s2!!!
     *      Iterator i = s2.iterator(); // Must be in the synchronized block
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned navigable set will be serializable if the specified
     * navigable set is serializable.
     *
     * @param  <T> the class of the objects in the set
     * @param  s the navigable set to be "wrapped" in a synchronized navigable
     * set
     * @return a synchronized view of the specified navigable set
     * @since 1.8
     */
    public static <T> NavigableSet<T> synchronizedNavigableSet(NavigableSet<T> s) {
        return new SynchronizedNavigableSet<>(s);
    }

    /**
     * @serial include
     */
    static class SynchronizedNavigableSet<E>
        extends SynchronizedSortedSet<E>
        implements NavigableSet<E>
    {
        private static final long serialVersionUID = -5505529816273629798L;

        private final NavigableSet<E> ns;

        SynchronizedNavigableSet(NavigableSet<E> s) {
            super(s);
            ns = s;
        }

        SynchronizedNavigableSet(NavigableSet<E> s, Object mutex) {
            super(s, mutex);
            ns = s;
        }
        public E lower(E e)      { synchronized (mutex) {return ns.lower(e);} }
        public E floor(E e)      { synchronized (mutex) {return ns.floor(e);} }
        public E ceiling(E e)  { synchronized (mutex) {return ns.ceiling(e);} }
        public E higher(E e)    { synchronized (mutex) {return ns.higher(e);} }
        public E pollFirst()  { synchronized (mutex) {return ns.pollFirst();} }
        public E pollLast()    { synchronized (mutex) {return ns.pollLast();} }

        public NavigableSet<E> descendingSet() {
            synchronized (mutex) {
                return new SynchronizedNavigableSet<>(ns.descendingSet(), mutex);
            }
        }

        public Iterator<E> descendingIterator()
        { synchronized (mutex) { return descendingSet().iterator(); } }

        public NavigableSet<E> subSet(E fromElement, E toElement) {
            synchronized (mutex) {
                return new SynchronizedNavigableSet<>(ns.subSet(fromElement, true, toElement, false), mutex);
            }
        }
        public NavigableSet<E> headSet(E toElement) {
            synchronized (mutex) {
                return new SynchronizedNavigableSet<>(ns.headSet(toElement, false), mutex);
            }
        }
        public NavigableSet<E> tailSet(E fromElement) {
            synchronized (mutex) {
                return new SynchronizedNavigableSet<>(ns.tailSet(fromElement, true), mutex);
            }
        }

        public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
            synchronized (mutex) {
                return new SynchronizedNavigableSet<>(ns.subSet(fromElement, fromInclusive, toElement, toInclusive), mutex);
            }
        }

        public NavigableSet<E> headSet(E toElement, boolean inclusive) {
            synchronized (mutex) {
                return new SynchronizedNavigableSet<>(ns.headSet(toElement, inclusive), mutex);
            }
        }

        public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
            synchronized (mutex) {
                return new SynchronizedNavigableSet<>(ns.tailSet(fromElement, inclusive), mutex);
            }
        }
    }

    /**
     * Returns a wrapper on the specified collection which throws an
     * {@code UnsupportedOperationException} whenever an attempt is made to
     * modify the collection.
     *
     * @param collection
     *            the collection to wrap in an unmodifiable collection.
     * @return an unmodifiable collection.
     */
    @SuppressWarnings("unchecked")
    public static <E> Collection<E> unmodifiableCollection(
            Collection<? extends E> collection) {
        if (collection == null) {
            throw new NullPointerException("collection == null");
        }
        return new UnmodifiableCollection<E>((Collection<E>) collection);
    }

    /**
     * Returns a wrapper on the specified list which throws an
     * {@code UnsupportedOperationException} whenever an attempt is made to
     * modify the list.
     *
     * @param list
     *            the list to wrap in an unmodifiable list.
     * @return an unmodifiable List.
     */
    @SuppressWarnings("unchecked")
    public static <E> List<E> unmodifiableList(List<? extends E> list) {
        if (list == null) {
            throw new NullPointerException("list == null");
        }
        if (list instanceof RandomAccess) {
            return new UnmodifiableRandomAccessList<E>((List<E>) list);
        }
        return new UnmodifiableList<E>((List<E>) list);
    }

    /**
     * Returns a wrapper on the specified map which throws an
     * {@code UnsupportedOperationException} whenever an attempt is made to
     * modify the map.
     *
     * @param map
     *            the map to wrap in an unmodifiable map.
     * @return a unmodifiable map.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> unmodifiableMap(
            Map<? extends K, ? extends V> map) {
        if (map == null) {
            throw new NullPointerException("map == null");
        }
        return new UnmodifiableMap<K, V>((Map<K, V>) map);
    }

    /**
     * Returns a wrapper on the specified set which throws an
     * {@code UnsupportedOperationException} whenever an attempt is made to
     * modify the set.
     *
     * @param set
     *            the set to wrap in an unmodifiable set.
     * @return a unmodifiable set
     */
    @SuppressWarnings("unchecked")
    public static <E> Set<E> unmodifiableSet(Set<? extends E> set) {
        if (set == null) {
            throw new NullPointerException("set == null");
        }
        return new UnmodifiableSet<E>((Set<E>) set);
    }

    /**
     * Returns a wrapper on the specified sorted map which throws an
     * {@code UnsupportedOperationException} whenever an attempt is made to
     * modify the sorted map.
     *
     * @param map
     *            the sorted map to wrap in an unmodifiable sorted map.
     * @return a unmodifiable sorted map
     */
    @SuppressWarnings("unchecked")
    public static <K, V> SortedMap<K, V> unmodifiableSortedMap(
            SortedMap<K, ? extends V> map) {
        if (map == null) {
            throw new NullPointerException("map == null");
        }
        return new UnmodifiableSortedMap<K, V>((SortedMap<K, V>) map);
    }

    /**
     * Returns a wrapper on the specified sorted set which throws an
     * {@code UnsupportedOperationException} whenever an attempt is made to
     * modify the sorted set.
     *
     * @param set
     *            the sorted set to wrap in an unmodifiable sorted set.
     * @return a unmodifiable sorted set.
     */
    public static <E> SortedSet<E> unmodifiableSortedSet(SortedSet<E> set) {
        if (set == null) {
            throw new NullPointerException("set == null");
        }
        return new UnmodifiableSortedSet<E>(set);
    }

    /**
     * Returns an <a href="Collection.html#unmodview">unmodifiable view</a> of the
     * specified navigable map. Query operations on the returned navigable map "read
     * through" to the specified navigable map.  Attempts to modify the returned
     * navigable map, whether direct, via its collection views, or via its
     * {@code subMap}, {@code headMap}, or {@code tailMap} views, result in
     * an {@code UnsupportedOperationException}.<p>
     *
     * The returned navigable map will be serializable if the specified
     * navigable map is serializable.
     *
     * @param <K> the class of the map keys
     * @param <V> the class of the map values
     * @param m the navigable map for which an unmodifiable view is to be
     *        returned
     * @return an unmodifiable view of the specified navigable map
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    public static <K,V> NavigableMap<K,V> unmodifiableNavigableMap(NavigableMap<K, ? extends V> m) {
        return new UnmodifiableNavigableMap<K, V>((NavigableMap<K, V>) m);
    }

    /**
     * @serial include
     */
    static class UnmodifiableNavigableMap<K,V>
        extends UnmodifiableSortedMap<K,V>
        implements NavigableMap<K,V>, Serializable {
        private static final long serialVersionUID = -4858195264774772197L;

        /**
         * A class for the {@link EMPTY_NAVIGABLE_MAP} which needs readResolve
         * to preserve singleton property.
         *
         * @param <K> type of keys, if there were any, and of bounds
         * @param <V> type of values, if there were any
         */
        private static class EmptyNavigableMap<K,V> extends UnmodifiableNavigableMap<K,V>
            implements Serializable {

            private static final long serialVersionUID = -2239321462712562324L;

            EmptyNavigableMap()                       { super(new TreeMap<>()); }

            @Override
            public NavigableSet<K> navigableKeySet()
            { return emptyNavigableSet(); }

            private Object readResolve()        { return EMPTY_NAVIGABLE_MAP; }
        }

        /**
         * Singleton for {@link emptyNavigableMap()} which is also immutable.
         */
        private static final EmptyNavigableMap<?,?> EMPTY_NAVIGABLE_MAP =
            new EmptyNavigableMap<>();

        /**
         * The instance we wrap and protect.
         */
        private final NavigableMap<K, V> nm;

        UnmodifiableNavigableMap(NavigableMap<K, V> m)
        {super(m); nm = m;}

        public K lowerKey(K key)                   { return nm.lowerKey(key); }
        public K floorKey(K key)                   { return nm.floorKey(key); }
        public K ceilingKey(K key)               { return nm.ceilingKey(key); }
        public K higherKey(K key)                 { return nm.higherKey(key); }

        @SuppressWarnings("unchecked")
        public Entry<K, V> lowerEntry(K key) {
            Entry<K,V> lower = (Entry<K, V>) nm.lowerEntry(key);
            return (null != lower)
                ? new UnmodifiableEntrySet.UnmodifiableMapEntry<>(lower)
                : null;
        }

        @SuppressWarnings("unchecked")
        public Entry<K, V> floorEntry(K key) {
            Entry<K,V> floor = (Entry<K, V>) nm.floorEntry(key);
            return (null != floor)
                ? new UnmodifiableEntrySet.UnmodifiableMapEntry<>(floor)
                : null;
        }

        @SuppressWarnings("unchecked")
        public Entry<K, V> ceilingEntry(K key) {
            Entry<K,V> ceiling = (Entry<K, V>) nm.ceilingEntry(key);
            return (null != ceiling)
                ? new UnmodifiableEntrySet.UnmodifiableMapEntry<>(ceiling)
                : null;
        }


        @SuppressWarnings("unchecked")
        public Entry<K, V> higherEntry(K key) {
            Entry<K,V> higher = (Entry<K, V>) nm.higherEntry(key);
            return (null != higher)
                ? new UnmodifiableEntrySet.UnmodifiableMapEntry<>(higher)
                : null;
        }

        @SuppressWarnings("unchecked")
        public Entry<K, V> firstEntry() {
            Entry<K,V> first = (Entry<K, V>) nm.firstEntry();
            return (null != first)
                ? new UnmodifiableEntrySet.UnmodifiableMapEntry<>(first)
                : null;
        }

        @SuppressWarnings("unchecked")
        public Entry<K, V> lastEntry() {
            Entry<K,V> last = (Entry<K, V>) nm.lastEntry();
            return (null != last)
                ? new UnmodifiableEntrySet.UnmodifiableMapEntry<>(last)
                : null;
        }

        public Entry<K, V> pollFirstEntry()
        { throw new UnsupportedOperationException(); }
        public Entry<K, V> pollLastEntry()
        { throw new UnsupportedOperationException(); }
        public NavigableMap<K, V> descendingMap()
        { return unmodifiableNavigableMap(nm.descendingMap()); }
        public NavigableSet<K> navigableKeySet()
        { return unmodifiableNavigableSet(nm.navigableKeySet()); }
        public NavigableSet<K> descendingKeySet()
        { return unmodifiableNavigableSet(nm.descendingKeySet()); }

        public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
            return unmodifiableNavigableMap(
                nm.subMap(fromKey, fromInclusive, toKey, toInclusive));
        }

        public NavigableMap<K, V> headMap(K toKey, boolean inclusive)
        { return unmodifiableNavigableMap(nm.headMap(toKey, inclusive)); }
        public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive)
        { return unmodifiableNavigableMap(nm.tailMap(fromKey, inclusive)); }
    }

    /**
     * Returns an <a href="Collection.html#unmodview">unmodifiable view</a> of the
     * specified navigable set. Query operations on the returned navigable set "read
     * through" to the specified navigable set.  Attempts to modify the returned
     * navigable set, whether direct, via its iterator, or via its
     * {@code subSet}, {@code headSet}, or {@code tailSet} views, result in
     * an {@code UnsupportedOperationException}.<p>
     *
     * The returned navigable set will be serializable if the specified
     * navigable set is serializable.
     *
     * @param  <T> the class of the objects in the set
     * @param s the navigable set for which an unmodifiable view is to be
     *        returned
     * @return an unmodifiable view of the specified navigable set
     * @since 1.8
     */
    public static <T> NavigableSet<T> unmodifiableNavigableSet(NavigableSet<T> s) {
        return new UnmodifiableNavigableSet<>(s);
    }

    /**
     * Wraps a navigable set and disables all of the mutative operations.
     *
     * @param <E> type of elements
     * @serial include
     */
    static class UnmodifiableNavigableSet<E>
        extends UnmodifiableSortedSet<E>
        implements NavigableSet<E>, Serializable {

        private static final long serialVersionUID = -6027448201786391929L;

        /**
         * A singleton empty unmodifiable navigable set used for
         * {@link #emptyNavigableSet()}.
         *
         * @param <E> type of elements, if there were any, and bounds
         */
        private static class EmptyNavigableSet<E> extends UnmodifiableNavigableSet<E>
            implements Serializable {
            private static final long serialVersionUID = -6291252904449939134L;

            public EmptyNavigableSet() {
                super(new TreeSet<>());
            }

            private Object readResolve()        { return EMPTY_NAVIGABLE_SET; }
        }

        @SuppressWarnings("rawtypes")
        private static final NavigableSet<?> EMPTY_NAVIGABLE_SET =
            new EmptyNavigableSet<>();

        /**
         * The instance we are protecting.
         */
        private final NavigableSet<E> ns;

        UnmodifiableNavigableSet(NavigableSet<E> s)         {super(s); ns = s;}

        public E lower(E e)                             { return ns.lower(e); }
        public E floor(E e)                             { return ns.floor(e); }
        public E ceiling(E e)                         { return ns.ceiling(e); }
        public E higher(E e)                           { return ns.higher(e); }
        public E pollFirst()     { throw new UnsupportedOperationException(); }
        public E pollLast()      { throw new UnsupportedOperationException(); }
        public NavigableSet<E> descendingSet()
        { return new UnmodifiableNavigableSet<>(ns.descendingSet()); }
        public Iterator<E> descendingIterator()
        { return descendingSet().iterator(); }

        public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
            return new UnmodifiableNavigableSet<>(
                ns.subSet(fromElement, fromInclusive, toElement, toInclusive));
        }

        public NavigableSet<E> headSet(E toElement, boolean inclusive) {
            return new UnmodifiableNavigableSet<>(
                ns.headSet(toElement, inclusive));
        }

        public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
            return new UnmodifiableNavigableSet<>(
                ns.tailSet(fromElement, inclusive));
        }
    }

    /**
     * Returns the number of elements in the {@code Collection} that match the
     * {@code Object} passed. If the {@code Object} is {@code null}, then the
     * number of {@code null} elements is returned.
     *
     * @param c
     *            the {@code Collection} to search.
     * @param o
     *            the {@code Object} to search for.
     * @return the number of matching elements.
     * @throws NullPointerException
     *             if the {@code Collection} parameter is {@code null}.
     * @since 1.5
     */
    public static int frequency(Collection<?> c, Object o) {
        if (c == null) {
            throw new NullPointerException("c == null");
        }
        if (c.isEmpty()) {
            return 0;
        }
        int result = 0;
        Iterator<?> itr = c.iterator();
        while (itr.hasNext()) {
            Object e = itr.next();
            if (o == null ? e == null : o.equals(e)) {
                result++;
            }
        }
        return result;
    }

    /**
     * Returns a type-safe empty, immutable {@link List}.
     *
     * @return an empty {@link List}.
     * @since 1.5
     * @see #EMPTY_LIST
     */
    @SuppressWarnings("unchecked")
    public static final <T> List<T> emptyList() {
        return EMPTY_LIST;
    }

    /**
     * Returns a type-safe empty, immutable {@link Set}.
     *
     * @return an empty {@link Set}.
     * @since 1.5
     * @see #EMPTY_SET
     */
    @SuppressWarnings("unchecked")
    public static final <T> Set<T> emptySet() {
        return EMPTY_SET;
    }

    /**
     * Returns an empty sorted set (immutable).  This set is serializable.
     *
     * <p>This example illustrates the type-safe way to obtain an empty
     * sorted set:
     * <pre> {@code
     *     SortedSet<String> s = Collections.emptySortedSet();
     * }</pre>
     *
     * @implNote Implementations of this method need not create a separate
     * {@code SortedSet} object for each call.
     *
     * @param <E> type of elements, if there were any, in the set
     * @return the empty sorted set
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    public static <E> SortedSet<E> emptySortedSet() {
        return (SortedSet<E>) UnmodifiableNavigableSet.EMPTY_NAVIGABLE_SET;
    }

    /**
     * Returns an empty navigable set (immutable).  This set is serializable.
     *
     * <p>This example illustrates the type-safe way to obtain an empty
     * navigable set:
     * <pre> {@code
     *     NavigableSet<String> s = Collections.emptyNavigableSet();
     * }</pre>
     *
     * @implNote Implementations of this method need not
     * create a separate {@code NavigableSet} object for each call.
     *
     * @param <E> type of elements, if there were any, in the set
     * @return the empty navigable set
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    public static <E> NavigableSet<E> emptyNavigableSet() {
        return (NavigableSet<E>) UnmodifiableNavigableSet.EMPTY_NAVIGABLE_SET;
    }

    /**
     * Returns a type-safe empty, immutable {@link Map}.
     *
     * @return an empty {@link Map}.
     * @since 1.5
     * @see #EMPTY_MAP
     */
    @SuppressWarnings("unchecked")
    public static final <K, V> Map<K, V> emptyMap() {
        return EMPTY_MAP;
    }

    /**
     * Returns an empty sorted map (immutable).  This map is serializable.
     *
     * <p>This example illustrates the type-safe way to obtain an empty map:
     * <pre> {@code
     *     SortedMap<String, Date> s = Collections.emptySortedMap();
     * }</pre>
     *
     * @implNote Implementations of this method need not create a separate
     * {@code SortedMap} object for each call.
     *
     * @param <K> the class of the map keys
     * @param <V> the class of the map values
     * @return an empty sorted map
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    public static final <K,V> SortedMap<K,V> emptySortedMap() {
        return (SortedMap<K,V>) UnmodifiableNavigableMap.EMPTY_NAVIGABLE_MAP;
    }

    /**
     * Returns an empty navigable map (immutable).  This map is serializable.
     *
     * <p>This example illustrates the type-safe way to obtain an empty map:
     * <pre> {@code
     *     NavigableMap<String, Date> s = Collections.emptyNavigableMap();
     * }</pre>
     *
     * @implNote Implementations of this method need not create a separate
     * {@code NavigableMap} object for each call.
     *
     * @param <K> the class of the map keys
     * @param <V> the class of the map values
     * @return an empty navigable map
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    public static final <K,V> NavigableMap<K,V> emptyNavigableMap() {
        return (NavigableMap<K,V>) UnmodifiableNavigableMap.EMPTY_NAVIGABLE_MAP;
    }

    /**
     * Returns an enumeration containing no elements.
     * @since 1.7
     */
    @SuppressWarnings("unchecked")
    public static <T> Enumeration<T> emptyEnumeration() {
        return (Enumeration<T>) EMPTY_ENUMERATION;
    }

    /**
     * Returns an iterator containing no elements.
     * @since 1.7
     */
    @SuppressWarnings("unchecked")
    public static <T> Iterator<T> emptyIterator() {
        return (Iterator<T>) EMPTY_ITERATOR;
    }

    /**
     * Returns a list iterator containing no elements.
     * @since 1.7
     */
    public static <T> ListIterator<T> emptyListIterator() {
        return Collections.<T>emptyList().listIterator();
    }

    /**
     * Returns a dynamically typesafe view of the specified collection. Trying
     * to insert an element of the wrong type into this collection throws a
     * {@code ClassCastException}. At creation time the types in {@code c} are
     * not checked for correct type.
     *
     * @param c
     *            the collection to be wrapped in a typesafe collection.
     * @param type
     *            the type of the elements permitted to insert.
     * @return a typesafe collection.
     */
    public static <E> Collection<E> checkedCollection(Collection<E> c,
            Class<E> type) {
        return new CheckedCollection<E>(c, type);
    }

    /**
     * Returns a dynamically typesafe view of the specified queue.
     * Any attempt to insert an element of the wrong type will result in
     * an immediate {@link ClassCastException}.  Assuming a queue contains
     * no incorrectly typed elements prior to the time a dynamically typesafe
     * view is generated, and that all subsequent access to the queue
     * takes place through the view, it is <i>guaranteed</i> that the
     * queue cannot contain an incorrectly typed element.
     *
     * <p>A discussion of the use of dynamically typesafe views may be
     * found in the documentation for the {@link #checkedCollection
     * checkedCollection} method.
     *
     * <p>The returned queue will be serializable if the specified queue
     * is serializable.
     *
     * <p>Since {@code null} is considered to be a value of any reference
     * type, the returned queue permits insertion of {@code null} elements
     * whenever the backing queue does.
     *
     * @param <E> the class of the objects in the queue
     * @param queue the queue for which a dynamically typesafe view is to be
     *             returned
     * @param type the type of element that {@code queue} is permitted to hold
     * @return a dynamically typesafe view of the specified queue
     * @since 1.8
     */
    public static <E> Queue<E> checkedQueue(Queue<E> queue, Class<E> type) {
        return new CheckedQueue<>(queue, type);
    }

    /**
     * @serial include
     */
    static class CheckedQueue<E>
        extends CheckedCollection<E>
        implements Queue<E>, Serializable
    {
        private static final long serialVersionUID = 1433151992604707767L;
        final Queue<E> queue;

        CheckedQueue(Queue<E> queue, Class<E> elementType) {
            super(queue, elementType);
            this.queue = queue;
        }

        public E element()              {return queue.element();}
        public boolean equals(Object o) {return o == this || c.equals(o);}
        public int hashCode()           {return c.hashCode();}
        public E peek()                 {return queue.peek();}
        public E poll()                 {return queue.poll();}
        public E remove()               {return queue.remove();}
        public boolean offer(E e)       {return queue.offer(checkType(e, type));}
    }

    /**
     * Returns a dynamically typesafe view of the specified map. Trying to
     * insert an element of the wrong type into this map throws a
     * {@code ClassCastException}. At creation time the types in {@code m} are
     * not checked for correct type.
     *
     * @param m
     *            the map to be wrapped in a typesafe map.
     * @param keyType
     *            the type of the keys permitted to insert.
     * @param valueType
     *            the type of the values permitted to insert.
     * @return a typesafe map.
     */
    public static <K, V> Map<K, V> checkedMap(Map<K, V> m, Class<K> keyType,
            Class<V> valueType) {
        return new CheckedMap<K, V>(m, keyType, valueType);
    }

    /**
     * Returns a dynamically typesafe view of the specified list. Trying to
     * insert an element of the wrong type into this list throws a
     * {@code ClassCastException}. At creation time the types in {@code list}
     * are not checked for correct type.
     *
     * @param list
     *            the list to be wrapped in a typesafe list.
     * @param type
     *            the type of the elements permitted to insert.
     * @return a typesafe list.
     */
    public static <E> List<E> checkedList(List<E> list, Class<E> type) {
        if (list instanceof RandomAccess) {
            return new CheckedRandomAccessList<E>(list, type);
        }
        return new CheckedList<E>(list, type);
    }

    /**
     * Returns a dynamically typesafe view of the specified set. Trying to
     * insert an element of the wrong type into this set throws a
     * {@code ClassCastException}. At creation time the types in {@code s} are
     * not checked for correct type.
     *
     * @param s
     *            the set to be wrapped in a typesafe set.
     * @param type
     *            the type of the elements permitted to insert.
     * @return a typesafe set.
     */
    public static <E> Set<E> checkedSet(Set<E> s, Class<E> type) {
        return new CheckedSet<E>(s, type);
    }

    /**
     * Returns a dynamically typesafe view of the specified sorted map. Trying
     * to insert an element of the wrong type into this sorted map throws a
     * {@code ClassCastException}. At creation time the types in {@code m} are
     * not checked for correct type.
     *
     * @param m
     *            the sorted map to be wrapped in a typesafe sorted map.
     * @param keyType
     *            the type of the keys permitted to insert.
     * @param valueType
     *            the type of the values permitted to insert.
     * @return a typesafe sorted map.
     */
    public static <K, V> SortedMap<K, V> checkedSortedMap(SortedMap<K, V> m,
            Class<K> keyType, Class<V> valueType) {
        return new CheckedSortedMap<K, V>(m, keyType, valueType);
    }

    /**
     * Returns a dynamically typesafe view of the specified navigable map.
     * Any attempt to insert a mapping whose key or value have the wrong
     * type will result in an immediate {@link ClassCastException}.
     * Similarly, any attempt to modify the value currently associated with
     * a key will result in an immediate {@link ClassCastException},
     * whether the modification is attempted directly through the map
     * itself, or through a {@link Map.Entry} instance obtained from the
     * map's {@link Map#entrySet() entry set} view.
     *
     * <p>Assuming a map contains no incorrectly typed keys or values
     * prior to the time a dynamically typesafe view is generated, and
     * that all subsequent access to the map takes place through the view
     * (or one of its collection views), it is <em>guaranteed</em> that the
     * map cannot contain an incorrectly typed key or value.
     *
     * <p>A discussion of the use of dynamically typesafe views may be
     * found in the documentation for the {@link #checkedCollection
     * checkedCollection} method.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * <p>Since {@code null} is considered to be a value of any reference
     * type, the returned map permits insertion of null keys or values
     * whenever the backing map does.
     *
     * @param <K> type of map keys
     * @param <V> type of map values
     * @param m the map for which a dynamically typesafe view is to be
     *          returned
     * @param keyType the type of key that {@code m} is permitted to hold
     * @param valueType the type of value that {@code m} is permitted to hold
     * @return a dynamically typesafe view of the specified map
     * @since 1.8
     */
    public static <K,V> NavigableMap<K,V> checkedNavigableMap(NavigableMap<K, V> m,
                                                              Class<K> keyType,
                                                              Class<V> valueType) {
        return new CheckedNavigableMap<>(m, keyType, valueType);
    }

    /**
     * @serial include
     */
    static class CheckedNavigableMap<K,V> extends CheckedSortedMap<K,V>
        implements NavigableMap<K,V>, Serializable
    {
        private static final long serialVersionUID = -4852462692372534096L;

        private final NavigableMap<K, V> nm;

        CheckedNavigableMap(NavigableMap<K, V> m,
                            Class<K> keyType, Class<V> valueType) {
            super(m, keyType, valueType);
            nm = m;
        }

        public Comparator<? super K> comparator()   { return nm.comparator(); }
        public K firstKey()                           { return nm.firstKey(); }
        public K lastKey()                             { return nm.lastKey(); }

        public Entry<K, V> lowerEntry(K key) {
            Entry<K,V> lower = nm.lowerEntry(key);
            return (null != lower)
                ? new CheckedMap.CheckedEntry<>(lower, valueType)
                : null;
        }

        public K lowerKey(K key)                   { return nm.lowerKey(key); }

        public Entry<K, V> floorEntry(K key) {
            Entry<K,V> floor = nm.floorEntry(key);
            return (null != floor)
                ? new CheckedMap.CheckedEntry<>(floor, valueType)
                : null;
        }

        public K floorKey(K key)                   { return nm.floorKey(key); }

        public Entry<K, V> ceilingEntry(K key) {
            Entry<K,V> ceiling = nm.ceilingEntry(key);
            return (null != ceiling)
                ? new CheckedMap.CheckedEntry<>(ceiling, valueType)
                : null;
        }

        public K ceilingKey(K key)               { return nm.ceilingKey(key); }

        public Entry<K, V> higherEntry(K key) {
            Entry<K,V> higher = nm.higherEntry(key);
            return (null != higher)
                ? new CheckedMap.CheckedEntry<>(higher, valueType)
                : null;
        }

        public K higherKey(K key)                 { return nm.higherKey(key); }

        public Entry<K, V> firstEntry() {
            Entry<K,V> first = nm.firstEntry();
            return (null != first)
                ? new CheckedMap.CheckedEntry<>(first, valueType)
                : null;
        }

        public Entry<K, V> lastEntry() {
            Entry<K,V> last = nm.lastEntry();
            return (null != last)
                ? new CheckedMap.CheckedEntry<>(last, valueType)
                : null;
        }

        public Entry<K, V> pollFirstEntry() {
            Entry<K,V> entry = nm.pollFirstEntry();
            return (null == entry)
                ? null
                : new CheckedMap.CheckedEntry<>(entry, valueType);
        }

        public Entry<K, V> pollLastEntry() {
            Entry<K,V> entry = nm.pollLastEntry();
            return (null == entry)
                ? null
                : new CheckedMap.CheckedEntry<>(entry, valueType);
        }

        public NavigableMap<K, V> descendingMap() {
            return checkedNavigableMap(nm.descendingMap(), keyType, valueType);
        }

        public NavigableSet<K> keySet() {
            return navigableKeySet();
        }

        public NavigableSet<K> navigableKeySet() {
            return checkedNavigableSet(nm.navigableKeySet(), keyType);
        }

        public NavigableSet<K> descendingKeySet() {
            return checkedNavigableSet(nm.descendingKeySet(), keyType);
        }

        @Override
        public NavigableMap<K,V> subMap(K fromKey, K toKey) {
            return checkedNavigableMap(nm.subMap(fromKey, true, toKey, false),
                keyType, valueType);
        }

        @Override
        public NavigableMap<K,V> headMap(K toKey) {
            return checkedNavigableMap(nm.headMap(toKey, false), keyType, valueType);
        }

        @Override
        public NavigableMap<K,V> tailMap(K fromKey) {
            return checkedNavigableMap(nm.tailMap(fromKey, true), keyType, valueType);
        }

        public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
            return checkedNavigableMap(nm.subMap(fromKey, fromInclusive, toKey, toInclusive), keyType, valueType);
        }

        public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
            return checkedNavigableMap(nm.headMap(toKey, inclusive), keyType, valueType);
        }

        public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
            return checkedNavigableMap(nm.tailMap(fromKey, inclusive), keyType, valueType);
        }
    }

    /**
     * Returns a dynamically typesafe view of the specified sorted set. Trying
     * to insert an element of the wrong type into this sorted set throws a
     * {@code ClassCastException}. At creation time the types in {@code s} are
     * not checked for correct type.
     *
     * @param s
     *            the sorted set to be wrapped in a typesafe sorted set.
     * @param type
     *            the type of the elements permitted to insert.
     * @return a typesafe sorted set.
     */
    public static <E> SortedSet<E> checkedSortedSet(SortedSet<E> s,
            Class<E> type) {
        return new CheckedSortedSet<E>(s, type);
    }

    /**
     * Returns a dynamically typesafe view of the specified navigable set.
     * Any attempt to insert an element of the wrong type will result in an
     * immediate {@link ClassCastException}.  Assuming a navigable set
     * contains no incorrectly typed elements prior to the time a
     * dynamically typesafe view is generated, and that all subsequent
     * access to the navigable set takes place through the view, it is
     * <em>guaranteed</em> that the navigable set cannot contain an incorrectly
     * typed element.
     *
     * <p>A discussion of the use of dynamically typesafe views may be
     * found in the documentation for the {@link #checkedCollection
     * checkedCollection} method.
     *
     * <p>The returned navigable set will be serializable if the specified
     * navigable set is serializable.
     *
     * <p>Since {@code null} is considered to be a value of any reference
     * type, the returned navigable set permits insertion of null elements
     * whenever the backing sorted set does.
     *
     * @param <E> the class of the objects in the set
     * @param s the navigable set for which a dynamically typesafe view is to be
     *          returned
     * @param type the type of element that {@code s} is permitted to hold
     * @return a dynamically typesafe view of the specified navigable set
     * @since 1.8
     */
    public static <E> NavigableSet<E> checkedNavigableSet(NavigableSet<E> s,
                                                          Class<E> type) {
        return new CheckedNavigableSet<>(s, type);
    }

    /**
     * @serial include
     */
    static class CheckedNavigableSet<E> extends CheckedSortedSet<E>
        implements NavigableSet<E>, Serializable
    {
        private static final long serialVersionUID = -5429120189805438922L;

        private final NavigableSet<E> ns;

        CheckedNavigableSet(NavigableSet<E> s, Class<E> type) {
            super(s, type);
            ns = s;
        }

        public E lower(E e)                             { return ns.lower(e); }
        public E floor(E e)                             { return ns.floor(e); }
        public E ceiling(E e)                         { return ns.ceiling(e); }
        public E higher(E e)                           { return ns.higher(e); }
        public E pollFirst()                         { return ns.pollFirst(); }
        public E pollLast()                            {return ns.pollLast(); }
        public NavigableSet<E> descendingSet()
        { return checkedNavigableSet(ns.descendingSet(), type); }
        public Iterator<E> descendingIterator()
        {return checkedNavigableSet(ns.descendingSet(), type).iterator(); }

        public NavigableSet<E> subSet(E fromElement, E toElement) {
            return checkedNavigableSet(ns.subSet(fromElement, true, toElement, false), type);
        }
        public NavigableSet<E> headSet(E toElement) {
            return checkedNavigableSet(ns.headSet(toElement, false), type);
        }
        public NavigableSet<E> tailSet(E fromElement) {
            return checkedNavigableSet(ns.tailSet(fromElement, true), type);
        }

        public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
            return checkedNavigableSet(ns.subSet(fromElement, fromInclusive, toElement, toInclusive), type);
        }

        public NavigableSet<E> headSet(E toElement, boolean inclusive) {
            return checkedNavigableSet(ns.headSet(toElement, inclusive), type);
        }

        public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
            return checkedNavigableSet(ns.tailSet(fromElement, inclusive), type);
        }
    }

    /**
     * Adds all the specified elements to the specified collection.
     *
     * @param c
     *            the collection the elements are to be inserted into.
     * @param a
     *            the elements to insert.
     * @return true if the collection changed during insertion.
     * @throws UnsupportedOperationException
     *             when the method is not supported.
     * @throws NullPointerException
     *             when {@code c} or {@code a} is {@code null}, or {@code a}
     *             contains one or more {@code null} elements and {@code c}
     *             doesn't support {@code null} elements.
     * @throws IllegalArgumentException
     *             if at least one of the elements can't be inserted into the
     *             collection.
     */
    @SafeVarargs
    public static <T> boolean addAll(Collection<? super T> c, T... a) {
        boolean modified = false;
        for (int i = 0; i < a.length; i++) {
            modified |= c.add(a[i]);
        }
        return modified;
    }

    /**
     * Returns whether the specified collections have no elements in common.
     *
     * @param c1
     *            the first collection.
     * @param c2
     *            the second collection.
     * @return {@code true} if the collections have no elements in common,
     *         {@code false} otherwise.
     * @throws NullPointerException
     *             if one of the collections is {@code null}.
     */
    public static boolean disjoint(Collection<?> c1, Collection<?> c2) {
        if ((c1 instanceof Set) && !(c2 instanceof Set)
                || (c2.size()) > c1.size()) {
            Collection<?> tmp = c1;
            c1 = c2;
            c2 = tmp;
        }
        Iterator<?> it = c1.iterator();
        while (it.hasNext()) {
            if (c2.contains(it.next())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if specified object is instance of specified class. Used for a
     * dynamically typesafe view of the collections.
     *
     * @param obj -
     *            object is to be checked
     * @param type -
     *            class of object that should be
     * @return specified object
     */
    static <E> E checkType(E obj, Class<? extends E> type) {
        if (obj != null && !type.isInstance(obj)) {
            throw new ClassCastException("Attempt to insert element of type " + obj.getClass() +
                    " into collection of type " + type);
        }
        return obj;
    }

    /**
     * Returns a set backed by {@code map}.
     *
     * @throws IllegalArgumentException if the map is not empty
     * @since 1.6
     */
    public static <E> Set<E> newSetFromMap(Map<E, Boolean> map) {
        if (map.isEmpty()) {
            return new SetFromMap<E>(map);
        }
        throw new IllegalArgumentException("map not empty");
    }

    /**
     * Returns a last-in, first-out queue as a view of {@code deque}.
     *
     * @since 1.6
     */
    public static <T> Queue<T> asLifoQueue(Deque<T> deque) {
        return new AsLIFOQueue<T>(deque);
    }

    private static class SetFromMap<E> extends AbstractSet<E> implements Serializable {
        private static final long serialVersionUID = 2454657854757543876L;

        // Must be named as is, to pass serialization compatibility test.
        private final Map<E, Boolean> m;

        private transient Set<E> backingSet;

        SetFromMap(final Map<E, Boolean> map) {
            m = map;
            backingSet = map.keySet();
        }

        @Override public boolean equals(Object object) {
            return backingSet.equals(object);
        }

        @Override public int hashCode() {
            return backingSet.hashCode();
        }

        @Override public boolean add(E object) {
            return m.put(object, Boolean.TRUE) == null;
        }

        @Override public void clear() {
            m.clear();
        }

        @Override public String toString() {
            return backingSet.toString();
        }

        @Override public boolean contains(Object object) {
            return backingSet.contains(object);
        }

        @Override public boolean containsAll(Collection<?> collection) {
            return backingSet.containsAll(collection);
        }

        @Override public boolean isEmpty() {
            return m.isEmpty();
        }

        @Override public boolean remove(Object object) {
            return m.remove(object) != null;
        }

        @Override public boolean retainAll(Collection<?> collection) {
            return backingSet.retainAll(collection);
        }

        @Override public Object[] toArray() {
            return backingSet.toArray();
        }

        @Override
        public <T> T[] toArray(T[] contents) {
            return backingSet.toArray(contents);
        }

        @Override public Iterator<E> iterator() {
            return backingSet.iterator();
        }

        @Override public int size() {
            return m.size();
        }

        @SuppressWarnings("unchecked")
        private void readObject(ObjectInputStream stream)
                throws IOException, ClassNotFoundException {
            stream.defaultReadObject();
            backingSet = m.keySet();
        }
    }

    private static class AsLIFOQueue<E> extends AbstractQueue<E> implements Serializable {
        private static final long serialVersionUID = 1802017725587941708L;

        // Must be named as is, to pass serialization compatibility test.
        private final Deque<E> q;

        AsLIFOQueue(final Deque<E> deque) {
            this.q = deque;
        }

        @Override public Iterator<E> iterator() {
            return q.iterator();
        }

        @Override public int size() {
            return q.size();
        }

        @Override public boolean offer(E o) {
            return q.offerFirst(o);
        }

        @Override public E peek() {
            return q.peekFirst();
        }

        @Override public E poll() {
            return q.pollFirst();
        }

        @Override public boolean add(E o) {
            q.push(o);
            return true;
        }

        @Override public void clear() {
            q.clear();
        }

        @Override public E element() {
            return q.getFirst();
        }

        @Override public E remove() {
            return q.pop();
        }

        @Override public boolean contains(Object object) {
            return q.contains(object);
        }

        @Override public boolean containsAll(Collection<?> collection) {
            return q.containsAll(collection);
        }

        @Override public boolean isEmpty() {
            return q.isEmpty();
        }

        @Override public boolean remove(Object object) {
            return q.remove(object);
        }

        @Override public boolean removeAll(Collection<?> collection) {
            return q.removeAll(collection);
        }

        @Override public boolean retainAll(Collection<?> collection) {
            return q.retainAll(collection);
        }

        @Override public Object[] toArray() {
            return q.toArray();
        }

        @Override public <T> T[] toArray(T[] contents) {
            return q.toArray(contents);
        }

        @Override public String toString() {
            return q.toString();
        }
    }

    /**
     * A dynamically typesafe view of a Collection.
     */
    private static class CheckedCollection<E> implements Collection<E>, Serializable {

        private static final long serialVersionUID = 1578914078182001775L;

        final Collection<E> c;

        final Class<E> type;

        public CheckedCollection(Collection<E> c, Class<E> type) {
            if (c == null) {
                throw new NullPointerException("c == null");
            } else if (type == null) {
                throw new NullPointerException("type == null");
            }
            this.c = c;
            this.type = type;
        }

        @Override public int size() {
            return c.size();
        }

        @Override public boolean isEmpty() {
            return c.isEmpty();
        }

        @Override public boolean contains(Object obj) {
            return c.contains(obj);
        }

        @Override public Iterator<E> iterator() {
            Iterator<E> i = c.iterator();
            if (i instanceof ListIterator) {
                i = new CheckedListIterator<E>((ListIterator<E>) i, type);
            }
            return i;
        }

        @Override public Object[] toArray() {
            return c.toArray();
        }

        @Override public <T> T[] toArray(T[] arr) {
            return c.toArray(arr);
        }

        @Override public boolean add(E obj) {
            return c.add(checkType(obj, type));
        }

        @Override public boolean remove(Object obj) {
            return c.remove(obj);
        }

        @Override public boolean containsAll(Collection<?> c1) {
            return c.containsAll(c1);
        }

        @SuppressWarnings("unchecked")
        @Override public boolean addAll(Collection<? extends E> c1) {
            Object[] array = c1.toArray();
            for (Object o : array) {
                checkType(o, type);
            }
            return c.addAll((List<E>) Arrays.asList(array));
        }

        @Override public boolean removeAll(Collection<?> c1) {
            return c.removeAll(c1);
        }

        @Override public boolean retainAll(Collection<?> c1) {
            return c.retainAll(c1);
        }

        @Override public void clear() {
            c.clear();
        }

        @Override public String toString() {
            return c.toString();
        }
    }

    /**
     * Class represents a dynamically typesafe view of the specified
     * ListIterator.
     */
    private static class CheckedListIterator<E> implements ListIterator<E> {

        private final ListIterator<E> i;

        private final Class<E> type;

        /**
         * Constructs a dynamically typesafe view of the specified ListIterator.
         *
         * @param i -
         *            the listIterator for which a dynamically typesafe view to
         *            be constructed.
         */
        public CheckedListIterator(ListIterator<E> i, Class<E> type) {
            this.i = i;
            this.type = type;
        }

        @Override public boolean hasNext() {
            return i.hasNext();
        }

        @Override public E next() {
            return i.next();
        }

        @Override public void remove() {
            i.remove();
        }

        @Override public boolean hasPrevious() {
            return i.hasPrevious();
        }

        @Override public E previous() {
            return i.previous();
        }

        @Override public int nextIndex() {
            return i.nextIndex();
        }

        @Override public int previousIndex() {
            return i.previousIndex();
        }

        @Override public void set(E obj) {
            i.set(checkType(obj, type));
        }

        @Override public void add(E obj) {
            i.add(checkType(obj, type));
        }
    }

    /**
     * Class represents a dynamically typesafe view of a List.
     */
    private static class CheckedList<E> extends CheckedCollection<E> implements List<E> {

        private static final long serialVersionUID = 65247728283967356L;

        final List<E> l;

        public CheckedList(List<E> l, Class<E> type) {
            super(l, type);
            this.l = l;
        }

        @SuppressWarnings("unchecked")
        @Override public boolean addAll(int index, Collection<? extends E> c1) {
            Object[] array = c1.toArray();
            for (Object o : array) {
                checkType(o, type);
            }
            return l.addAll(index, (List<E>) Arrays.asList(array));
        }

        @Override public E get(int index) {
            return l.get(index);
        }

        @Override public E set(int index, E obj) {
            return l.set(index, checkType(obj, type));
        }

        @Override public void add(int index, E obj) {
            l.add(index, checkType(obj, type));
        }

        @Override public E remove(int index) {
            return l.remove(index);
        }

        @Override public int indexOf(Object obj) {
            return l.indexOf(obj);
        }

        @Override public int lastIndexOf(Object obj) {
            return l.lastIndexOf(obj);
        }

        @Override public ListIterator<E> listIterator() {
            return new CheckedListIterator<E>(l.listIterator(), type);
        }

        @Override public ListIterator<E> listIterator(int index) {
            return new CheckedListIterator<E>(l.listIterator(index), type);
        }

        @Override public List<E> subList(int fromIndex, int toIndex) {
            return checkedList(l.subList(fromIndex, toIndex), type);
        }

        @Override public boolean equals(Object obj) {
            return l.equals(obj);
        }

        @Override public int hashCode() {
            return l.hashCode();
        }
    }

    /**
     * A dynamically typesafe view of a RandomAccessList.
     */
    private static class CheckedRandomAccessList<E> extends CheckedList<E> implements RandomAccess {

        private static final long serialVersionUID = 1638200125423088369L;

        public CheckedRandomAccessList(List<E> l, Class<E> type) {
            super(l, type);
        }
    }

    /**
     * A dynamically typesafe view of a Set.
     */
    private static class CheckedSet<E> extends CheckedCollection<E> implements Set<E> {

        private static final long serialVersionUID = 4694047833775013803L;

        public CheckedSet(Set<E> s, Class<E> type) {
            super(s, type);
        }

        @Override public boolean equals(Object obj) {
            return c.equals(obj);
        }

        @Override public int hashCode() {
            return c.hashCode();
        }

    }

    /**
     * A dynamically typesafe view of a Map.
     */
    private static class CheckedMap<K, V> implements Map<K, V>, Serializable {

        private static final long serialVersionUID = 5742860141034234728L;

        final Map<K, V> m;
        final Class<K> keyType;
        final Class<V> valueType;

        private CheckedMap(Map<K, V> m, Class<K> keyType, Class<V> valueType) {
            if (m == null) {
                throw new NullPointerException("m == null");
            } else if (keyType == null) {
                throw new NullPointerException("keyType == null");
            } else if (valueType == null) {
                throw new NullPointerException("valueType == null");
            }
            this.m = m;
            this.keyType = keyType;
            this.valueType = valueType;
        }

        @Override public int size() {
            return m.size();
        }

        @Override public boolean isEmpty() {
            return m.isEmpty();
        }

        @Override public boolean containsKey(Object key) {
            return m.containsKey(key);
        }

        @Override public boolean containsValue(Object value) {
            return m.containsValue(value);
        }

        @Override public V get(Object key) {
            return m.get(key);
        }

        @Override public V put(K key, V value) {
            return m.put(checkType(key, keyType), checkType(value, valueType));
        }

        @Override public V remove(Object key) {
            return m.remove(key);
        }

        @SuppressWarnings("unchecked")
        @Override public void putAll(Map<? extends K, ? extends V> map) {
            int size = map.size();
            if (size == 0) {
                return;
            }
            Map.Entry<? extends K, ? extends V>[] entries = new Map.Entry[size];
            Iterator<? extends Map.Entry<? extends K, ? extends V>> it = map
                    .entrySet().iterator();
            for (int i = 0; i < size; i++) {
                Map.Entry<? extends K, ? extends V> e = it.next();
                checkType(e.getKey(), keyType);
                checkType(e.getValue(), valueType);
                entries[i] = e;
            }
            for (int i = 0; i < size; i++) {
                m.put(entries[i].getKey(), entries[i].getValue());
            }
        }

        @Override public void clear() {
            m.clear();
        }

        @Override public Set<K> keySet() {
            return m.keySet();
        }

        @Override public Collection<V> values() {
            return m.values();
        }

        @Override public Set<Map.Entry<K, V>> entrySet() {
            return new CheckedEntrySet<K, V>(m.entrySet(), valueType);
        }

        @Override public boolean equals(Object obj) {
            return m.equals(obj);
        }

        @Override public int hashCode() {
            return m.hashCode();
        }

        @Override public String toString() {
            return m.toString();
        }

        /**
         * A dynamically typesafe view of a Map.Entry.
         */
        private static class CheckedEntry<K, V> implements Map.Entry<K, V> {
            final Map.Entry<K, V> e;
            final Class<V> valueType;

            public CheckedEntry(Map.Entry<K, V> e, Class<V> valueType) {
                if (e == null) {
                    throw new NullPointerException("e == null");
                }
                this.e = e;
                this.valueType = valueType;
            }

            @Override public K getKey() {
                return e.getKey();
            }

            @Override public V getValue() {
                return e.getValue();
            }

            @Override public V setValue(V obj) {
                return e.setValue(checkType(obj, valueType));
            }

            @Override public boolean equals(Object obj) {
                return e.equals(obj);
            }

            @Override public int hashCode() {
                return e.hashCode();
            }
        }

        /**
         * A dynamically typesafe view of an entry set.
         */
        private static class CheckedEntrySet<K, V> implements Set<Map.Entry<K, V>> {
            final Set<Map.Entry<K, V>> s;
            final Class<V> valueType;

            public CheckedEntrySet(Set<Map.Entry<K, V>> s, Class<V> valueType) {
                this.s = s;
                this.valueType = valueType;
            }

            @Override public Iterator<Map.Entry<K, V>> iterator() {
                return new CheckedEntryIterator<K, V>(s.iterator(), valueType);
            }

            @Override public Object[] toArray() {
                int thisSize = size();
                Object[] array = new Object[thisSize];
                Iterator<?> it = iterator();
                for (int i = 0; i < thisSize; i++) {
                    array[i] = it.next();
                }
                return array;
            }

            @SuppressWarnings("unchecked")
            @Override public <T> T[] toArray(T[] array) {
                int thisSize = size();
                if (array.length < thisSize) {
                    Class<?> ct = array.getClass().getComponentType();
                    array = (T[]) Array.newInstance(ct, thisSize);
                }
                Iterator<?> it = iterator();
                for (int i = 0; i < thisSize; i++) {
                    array[i] = (T) it.next();
                }
                if (thisSize < array.length) {
                    array[thisSize] = null;
                }
                return array;
            }

            @Override public boolean retainAll(Collection<?> c) {
                return s.retainAll(c);
            }

            @Override public boolean removeAll(Collection<?> c) {
                return s.removeAll(c);
            }

            @Override public boolean containsAll(Collection<?> c) {
                return s.containsAll(c);
            }

            @Override public boolean addAll(Collection<? extends Map.Entry<K, V>> c) {
                throw new UnsupportedOperationException();
            }

            @Override public boolean remove(Object o) {
                return s.remove(o);
            }

            @Override public boolean contains(Object o) {
                return s.contains(o);
            }

            @Override public boolean add(Map.Entry<K, V> o) {
                throw new UnsupportedOperationException();
            }

            @Override public boolean isEmpty() {
                return s.isEmpty();
            }

            @Override public void clear() {
                s.clear();
            }

            @Override public int size() {
                return s.size();
            }

            @Override public int hashCode() {
                return s.hashCode();
            }

            @Override public boolean equals(Object object) {
                return s.equals(object);
            }

            /**
             * A dynamically typesafe view of an entry iterator.
             */
            private static class CheckedEntryIterator<K, V> implements Iterator<Map.Entry<K, V>> {
                Iterator<Map.Entry<K, V>> i;
                Class<V> valueType;

                public CheckedEntryIterator(Iterator<Map.Entry<K, V>> i,
                        Class<V> valueType) {
                    this.i = i;
                    this.valueType = valueType;
                }

                @Override public boolean hasNext() {
                    return i.hasNext();
                }

                @Override public void remove() {
                    i.remove();
                }

                @Override public Map.Entry<K, V> next() {
                    return new CheckedEntry<K, V>(i.next(), valueType);
                }
            }
        }
    }

    /**
     * A dynamically typesafe view of a SortedSet.
     */
    private static class CheckedSortedSet<E> extends CheckedSet<E> implements SortedSet<E> {
        private static final long serialVersionUID = 1599911165492914959L;
        private final SortedSet<E> ss;

        public CheckedSortedSet(SortedSet<E> s, Class<E> type) {
            super(s, type);
            this.ss = s;
        }

        @Override public Comparator<? super E> comparator() {
            return ss.comparator();
        }

        @Override public SortedSet<E> subSet(E fromElement, E toElement) {
            return new CheckedSortedSet<E>(ss.subSet(fromElement, toElement),
                    type);
        }

        @Override public SortedSet<E> headSet(E toElement) {
            return new CheckedSortedSet<E>(ss.headSet(toElement), type);
        }

        @Override public SortedSet<E> tailSet(E fromElement) {
            return new CheckedSortedSet<E>(ss.tailSet(fromElement), type);
        }

        @Override public E first() {
            return ss.first();
        }

        @Override public E last() {
            return ss.last();
        }
    }

    /**
     * A dynamically typesafe view of a SortedMap.
     */
    private static class CheckedSortedMap<K, V> extends CheckedMap<K, V>
            implements SortedMap<K, V> {
        private static final long serialVersionUID = 1599671320688067438L;
        final SortedMap<K, V> sm;

        CheckedSortedMap(SortedMap<K, V> m, Class<K> keyType, Class<V> valueType) {
            super(m, keyType, valueType);
            this.sm = m;
        }

        @Override public Comparator<? super K> comparator() {
            return sm.comparator();
        }

        @Override public SortedMap<K, V> subMap(K fromKey, K toKey) {
            return new CheckedSortedMap<K, V>(sm.subMap(fromKey, toKey), keyType, valueType);
        }

        @Override public SortedMap<K, V> headMap(K toKey) {
            return new CheckedSortedMap<K, V>(sm.headMap(toKey), keyType, valueType);
        }

        @Override public SortedMap<K, V> tailMap(K fromKey) {
            return new CheckedSortedMap<K, V>(sm.tailMap(fromKey), keyType, valueType);
        }

        @Override public K firstKey() {
            return sm.firstKey();
        }

        @Override public K lastKey() {
            return sm.lastKey();
        }
    }

    /**
     * Computes a hash code and applies a supplemental hash function to defend
     * against poor quality hash functions. This is critical because HashMap
     * uses power-of-two length hash tables, that otherwise encounter collisions
     * for hash codes that do not differ in lower or upper bits.
     * Routine taken from java.util.concurrent.ConcurrentHashMap.hash(int).
     * @hide
     */
    public static int secondaryHash(Object key) {
        return secondaryHash(key.hashCode());
    }

    /**
     * Computes an identity hash code and applies a supplemental hash function to defend
     * against poor quality hash functions. This is critical because identity hash codes
     * are currently implemented as object addresses, which will have been aligned by the
     * underlying memory allocator causing all hash codes to have the same bottom bits.
     * @hide
     */
    public static int secondaryIdentityHash(Object key) {
        return secondaryHash(System.identityHashCode(key));
    }

    private static int secondaryHash(int h) {
        // Spread bits to regularize both segment and index locations,
        // using variant of single-word Wang/Jenkins hash.
        h += (h <<  15) ^ 0xffffcd7d;
        h ^= (h >>> 10);
        h += (h <<   3);
        h ^= (h >>>  6);
        h += (h <<   2) + (h << 14);
        return h ^ (h >>> 16);
    }

    /**
     * Returns the smallest power of two >= its argument, with several caveats:
     * If the argument is negative but not Integer.MIN_VALUE, the method returns
     * zero. If the argument is > 2^30 or equal to Integer.MIN_VALUE, the method
     * returns Integer.MIN_VALUE. If the argument is zero, the method returns
     * zero.
     * @hide
     */
    public static int roundUpToPowerOfTwo(int i) {
        i--; // If input is a power of two, shift its high-order bit right.

        // "Smear" the high-order bit all the way to the right.
        i |= i >>>  1;
        i |= i >>>  2;
        i |= i >>>  4;
        i |= i >>>  8;
        i |= i >>> 16;

        return i + 1;
    }
}
