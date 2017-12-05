package ru.ifmo.ctddev.solutions.arrayset;

import com.sun.istack.internal.Nullable;

import java.util.*;

public class ArraySet<E> implements SortedSet<E> {
    ArraySet<E> EMPTY = new ArraySet<>();

    /**
     * Point to the first element of set in <code>elements</code>
     */
    private int first;

    /**
     * Point to the first element of set in <code>elements</code>
     */
    private int last;

    /**
     * Contains the elements of set starting from <code>first</code> index to <code>last</code>
     */
    private E[] elements;

    private Comparator<? super E> comparator;

    @SuppressWarnings("WeakerAccess")
    public ArraySet() {
        // nothing to do
    }

    public ArraySet(Collection<E> collection) {
        this(collection, null);
    }

    @SuppressWarnings("WeakerAccess")
    public ArraySet(Collection<E> collection, @Nullable Comparator<? super E> comparator) {
        this.comparator = comparator;

        @SuppressWarnings("unchecked")
        E[] temp = (E[]) collection.toArray();
        Arrays.sort(temp, comparator);

        //noinspection unchecked
        elements = (E[]) new Object[collection.size()];
        for (E e : temp) {
            if (!contains(e)) {
                elements[last++] = e;
            }
        }
        elements = Arrays.copyOfRange(elements, first, last);
    }

    private ArraySet(E[] elements, int first, int last, Comparator<? super E> comparator) {
        this.elements = elements;
        this.first = first;
        this.last = last;
        this.comparator = comparator;
    }

    public int size() {
        return last - first;
    }

    public boolean isEmpty() {
        return Objects.isNull(elements) || size() <= 0;
    }

    public boolean contains(Object o) {
        //noinspection unchecked
        int foundIndex = Arrays.binarySearch(elements, first, last, (E) o, comparator);
        return foundIndex >= first && foundIndex < last;
    }

    public Iterator<E> iterator() {
        return Arrays.asList(elements).subList(first, last).iterator();
    }

    public Object[] toArray() {
        if (isEmpty()) {
            return new Object[0];
        }
        return Arrays.copyOfRange(elements, first, last);
    }

    public <T> T[] toArray(T[] a) {
        if (isEmpty()) {
            //noinspection unchecked
            return a;
        }
        //noinspection unchecked
        return (T[]) Arrays.copyOfRange(elements, first, last);
    }

    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean containsAll(Collection<?> c) {
        Objects.requireNonNull(c);
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        if (isEmpty()) {
            return EMPTY;
        }
        int start = indexOfFirstEqualOrGreater(fromElement);
        int end = indexOfFirstEqualOrGreater(toElement);

        if (start > end) {
            throw new IllegalArgumentException("From elements is greater than to element");
        }

        return createSubset(start, end);
    }

    @Override
    public ArraySet<E> headSet(E toElement) {
        if (isEmpty()) {
            return EMPTY;
        }
        int index = indexOfFirstEqualOrGreater(toElement);
        return createSubset(first, index);
    }

    @Override
    public ArraySet<E> tailSet(E fromElement) {
        if (isEmpty()) {
            return EMPTY;
        }
        int index = indexOfFirstEqualOrGreater(fromElement);

        return createSubset(index, last);
    }

    private int indexOfFirstEqualOrGreater(E element) {
        int index = Arrays.binarySearch(elements, first, last, element, comparator);
        if (index < 0) {
            index = -(index + 1);
        }
        return index;
    }

    private ArraySet<E> createSubset(int indexFrom, int indexTo) {
        return new ArraySet<>(elements, indexFrom, indexTo, comparator);
    }

    @Override
    public E first() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return elements[first];
    }

    @Override
    public E last() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return elements[last - 1];
    }
}