package ru.ifmo.ctddev.solutions.arrayset;

import java.util.*;
import java.util.function.Predicate;

public class ArraySet<E extends Comparable<? super E>> extends AbstractSet<E> implements SortedSet<E> {

    private List<E> data;
    private Comparator<E> comparator = null;

    public ArraySet() {
        this(new ArrayList<>(), null);
    }

    public ArraySet(Collection<E> collection) {
        this(new ArrayList<>(collection),  null);
    }

    public ArraySet(Collection<E> collection, Comparator<E> comparator) {
        SortedSet<E> set = new TreeSet<>(comparator);
        set.addAll(collection);
        this.data = new ArrayList<>(set);
        this.comparator = comparator;
    }

    public ArraySet(ArraySet<E> parent, int from, int to) throws IllegalArgumentException {
        this.comparator = parent.comparator;
        this.data = parent.data.subList(from, to);
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        int size = size();
        if (size == 0)
            return Collections.emptySortedSet();

        int indexTo = getElementIndex(toElement);
        if (indexTo > size)
            return Collections.emptySortedSet();

        return new ArraySet<>(this, 0, indexTo);
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        if (size() == 0)
            return Collections.emptySortedSet();

        int indexFrom = getElementIndex(fromElement);
        int indexTo = getElementIndex(toElement);

        if (indexFrom > indexTo)
            return Collections.emptySortedSet();

        return new ArraySet<>(this, indexFrom, indexTo);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        int size = size();
        if (size == 0)
            return Collections.emptySortedSet();

        int indexFrom = getElementIndex(fromElement);

        if (indexFrom <= size)
            return new ArraySet<>(this, indexFrom, size);

        return Collections.emptySortedSet();
    }

    @Override
    public E first() {
        if (data.size() == 0)
            throw new NoSuchElementException();

        return data.get(0);
    }

    @Override
    public E last() {
        int size = data.size();

        if (size == 0)
            throw new NoSuchElementException();

        return data.get(size - 1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains (Object object) {
        return Collections.binarySearch(data, (E) object, comparator) >= 0;
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableList(data).iterator();
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        throw new UnsupportedOperationException();
    }

    private int getElementIndex(E element) {
        int index = Collections.binarySearch(data, element, comparator);
        if (index < 0) {
            index = -(index + 1);
        }
        return index;
    }
}
