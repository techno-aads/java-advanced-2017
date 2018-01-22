package ru.ifmo.ctddev.solutions.arrayset;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {
    private List<E> list;
    private Comparator<? super E> comparator;

    public ArraySet() {
        list = new ArrayList<>();
    }

    public ArraySet(Collection<? extends E> var1) {
        this();
        addAllElementsToList(var1);
    }

    public ArraySet(Collection<? extends E> collection, Comparator<? super E> comparator) {
        this();
        this.comparator = comparator;
        addAllElementsToList(collection);
    }

    @Override
    public String toString() {
        return Arrays.toString(list.toArray());
    }

    private ArraySet(ArrayList<E> list) {
        this.list = list;
    }

    private ArraySet(List<E> list, Comparator<? super E> comparator) {
        this.list = list;
        this.comparator = comparator;
    }

    private ArraySet(ArrayList<E> list, Comparator<? super E> comparator) {
        this.list = list;
        this.comparator = comparator;
    }

    private E getElementByIndex(int index) {
        return list.get(index);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(list, (E) o, comparator) >= 0;
    }

    private int getValidExistingElementIndex(Integer i) {
        return Math.abs(i + 1);
    }

    private int getValidExistingElementIndexFromBinarySearch(Integer i) {
        return Math.abs(i + 1);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<E> stream() {
        return list.stream();
    }

    @Override
    public Stream<E> parallelStream() {
        return list.parallelStream();
    }

    @Override
    public Comparator<? super E> comparator() {
        return this.comparator;
    }

    @Override
    public E first() {
        if (size() == 0) throw new NoSuchElementException();
        return list.get(0);
    }

    @Override
    public E last() {
        if (size() == 0) throw new NoSuchElementException();
        return list.get(size() - 1);
    }

    @Override
    public Iterator<E> iterator() {
        return new ArraySet.Itr(list.iterator());
    }

    private class Itr implements Iterator<E> {
        Iterator iterator;

        private Itr(Iterator<E> iterator) {
            this.iterator = iterator;
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public E next() {
            return (E) iterator.next();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        return list.toArray(ts);
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        for (Object o : collection) {
            if (!contains(o)) return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        throw new UnsupportedOperationException();
    }

    private boolean addAllElementsToList(Collection<? extends E> collection) {
        try {
            list = new ArrayList<>(collection
                    .stream()
                    .collect(Collectors.toCollection(() -> new TreeSet<E>(comparator))));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E lower(E e) {
        if (list == null || size() == 0) return null;
        Integer i = Collections.binarySearch(list, e, comparator);
        if (i != -1 && i != 0) {
            return i < 0 ? getElementByIndex(getValidExistingElementIndex(i) - 1) : getElementByIndex(i - 1);
        }
        return null;
    }

    @Override
    public E floor(E e) {
        if (list == null || size() == 0) return null;
        Integer i = Collections.binarySearch(list, e, comparator);
        if (i != -1) {
            return i < 0 ? getElementByIndex(getValidExistingElementIndex(i) - 1) : getElementByIndex(i);
        }
        return null;
    }

    @Override
    public E ceiling(E e) {
        if (list == null || size() == 0) return null;
        Integer i = Collections.binarySearch(list, e, comparator);
        if (i > -size() - 1) {
            return i >= 0 ? getElementByIndex(i) : getElementByIndex(getValidExistingElementIndex(i));
        }
        return null;
    }

    @Override
    public E higher(E e) {
        if (list == null || size() == 0) return null;
        Integer i = Collections.binarySearch(list, e, comparator);
        if (i != size() - 1 && i > -size() - 1) {
            return i >= 0 ? getElementByIndex(i + 1) : getElementByIndex(getValidExistingElementIndex(i));
        }
        return null;
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NavigableSet<E> descendingSet() {
        ArrayList<E> descending = new ArrayList<>(size());
        for (int i = size() - 1; i >= 0; i--) {
            descending.add(list.get(i));
        }
        return new ArraySet<>(descending, Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<E> descendingIterator() {
        return this.descendingSet().iterator();
    }

    @Override
    public Spliterator<E> spliterator() {
        return list.spliterator();
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean includeFromElement, E toElement, boolean includeToElement) {
        if (size() == 0) {
            return new ArraySet<>();
        }

        int fromIndex;
        fromIndex = getFromIndex(fromElement, includeFromElement);
        if (fromIndex == size()) {
            return new ArraySet<>();
        }

        int toIndex;
        toIndex = getToIndex(toElement, includeToElement);

        if (fromIndex > toIndex) {
            return new ArraySet<>();
        }

        return new ArraySet<>(new ArrayList<>(list.subList(fromIndex, toIndex)));
    }

    private int getToIndex(E toElement, Boolean includeToElement) {
        int toIndex;
        toIndex = Collections.binarySearch(list, toElement, comparator);
        toIndex = toIndex >= 0 ? toIndex + (includeToElement ? 1 : 0) : getValidExistingElementIndexFromBinarySearch(toIndex);
        return toIndex;
    }

    private int getFromIndex(E fromElement, Boolean includeFromElement) {
        int fromIndex;
        fromIndex = Collections.binarySearch(list, fromElement, comparator);
        fromIndex = fromIndex >= 0 ? fromIndex + (includeFromElement ? 0 : 1) : getValidExistingElementIndexFromBinarySearch(fromIndex);
        return fromIndex;
    }

    @Override
    public NavigableSet<E> headSet(E e, boolean b) {
        if (size() == 0) {
            return new ArraySet<>();
        }
        int toIndex = getToIndex(e, b);
        return new ArraySet<>(list.subList(0, toIndex), comparator);
    }

    @Override
    public SortedSet<E> headSet(E e) {
        return headSet(e, false);
    }

    @Override
    public NavigableSet<E> tailSet(E e, boolean b) {
        if (size() == 0) {
            return new ArraySet<>();
        }
        int fromIndex = getFromIndex(e, b);
        return new ArraySet<>(list.subList(fromIndex, size()), comparator);
    }

    @Override
    public SortedSet<E> tailSet(E e) {
        return tailSet(e, true);
    }

    @Override
    public SortedSet<E> subSet(E e, E e1) {
        return subSet(e, true, e1, false);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEach(Consumer<? super E> consumer) {
        list.forEach(consumer);
    }

    @Override
    public boolean removeIf(Predicate<? super E> predicate) {
        return list.removeIf(predicate);
    }
}
