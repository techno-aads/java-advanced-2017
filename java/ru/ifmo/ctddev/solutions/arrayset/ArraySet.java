package ru.ifmo.ctddev.solutions.arrayset;

import java.util.*;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class ArraySet<E>  implements SortedSet<E>, NavigableSet<E> {

    private final int start;
    private final int end;
    private final E[] array;
    private final Comparator<? super E> comparator;

    public ArraySet() {
        this((E[]) new Object[0], null);
    }

    public ArraySet(Collection<E> elements) {
        this(elements, null);
    }

    public ArraySet(Collection<E> elements, Comparator<? super E> comparator) {
        this((E[]) elements.toArray(), comparator);
    }

    private ArraySet(E[] elements, Comparator<? super E> comparator) {
        this(elements, comparator, 0, elements.length, false);
    }

    private ArraySet(E[] elements, Comparator<? super E> comparator, int start, int end, boolean sortedAndUnique) {
        this.comparator = comparator;
        if (!sortedAndUnique) {
            if (comparator != null) {
                Arrays.sort(elements, comparator);
            } else {
                Arrays.sort(elements);
            }
            this.array = removeDuplicates(elements);
            this.start = 0;
            this.end = array.length;
        } else {
            this.start = start;
            this.end = end;
            this.array = elements;
        }
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public ArraySet<E> subSet(E fromElement, E toElement) {
        return new ArraySet<>(array, comparator, insertionIndex(fromElement), insertionIndex(toElement), true);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return new ArraySet<>(array, comparator, 0, insertionIndex(toElement), true);
    }

    @Override
    public ArraySet<E> tailSet(E fromElement) {
        return new ArraySet<>(array, comparator, insertionIndex(fromElement), array.length, true);
    }

    @Override
    public E first() {
        if (array.length > 0) {
            return array[0];
        }
        throw new NoSuchElementException();
    }

    @Override
    public E last() {
        if (array.length > 0) {
            return array[array.length - 1];
        }
        throw new NoSuchElementException();
    }

    @Override
    public int size() {
        return end - start;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        int index = binarySearch((E) o);
        return index >= start && index <= end;
    }

    @Override
    public E lower(E e) {
        if (!isEmpty() && greater(e, array[end - 1])) {
            return array[end - 1];
        }

        int index = binarySearch(e);
        if (index >= 0) {
            if (index > start) {
                return array[index - 1];
            } else {
                return null;
            }
        } else {
            index = -index - 1;
            if (index > start) {
                return array[index - 1];
            } else {
                return null;
            }
        }
    }

    @Override
    public E floor(E e) {
        if (!isEmpty() && greater(e, array[end - 1])) {
            return array[end - 1];
        }

        int index = insertionIndex(e);

        if (index >= start && index < end && equals(array[index], e)) {
            return array[index];
        }

        if (index > start && index < end && !equals(array[index], e)) {
            return array[index - 1];
        }

        return null;
    }

    @Override
    public E ceiling(E e) {
        if (!isEmpty() && lower(e, array[start])) {
            return array[start];
        }

        int index = binarySearch(e);

        if (index >= 0) {
            if (index < end) {
                return array[index];
            } else {
                return null;
            }
        } else {
            index = -index - 1;
            if (index < end) {
                return array[index];
            } else {
                return null;
            }
        }
    }

    @Override
    public E higher(E e) {
        if (!isEmpty() && lower(e, array[start])) {
            return array[start];
        }

        int index = binarySearch(e);

        if (index >= 0) {
            if (index < end - 1) {
                return array[index + 1];
            } else {
                return null;
            }
        } else {
            index = -index - 1;
            if (index < end) {
                return array[index];
            } else {
                return null;
            }
        }
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
    public Iterator<E> iterator() {
        return new ArraySetIterator();
    }

    @Override
    public NavigableSet<E> descendingSet() {
        Comparator<? super E> c;

        if (comparator == null) {
            c = (o1, o2) -> ((Comparable<E>) o2).compareTo(o1);
        } else {
            c = (o1, o2) -> comparator.compare(o2, o1);
        }
        return new ArraySet<>(Arrays.copyOfRange(array, start, end), c);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new ArraySetDescIterator();
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        int start = insertionIndex(fromElement, fromInclusive ? 0 : 1);
        int end = insertionIndex(toElement, toInclusive ? 1 : 0);

        if (start > end){
            end = start;
        }

        return new ArraySet<>(array, comparator, start, end, true);
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return new ArraySet<>(array, comparator, start, insertionIndex(toElement, inclusive ? 1 : 0), true);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return new ArraySet<>(array, comparator, insertionIndex(fromElement, inclusive ? 0 : 1), end, true);
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOfRange(array, start, end);
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return (T[]) toArray();
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
    public boolean containsAll(Collection<?> c) {
        return Stream.of(array).allMatch(this::contains);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return Arrays.toString(toArray());
    }

    private boolean equals(E a, E b) {
        if (comparator != null) {
            return comparator.compare(a, b) == 0;
        }
        return Objects.equals(a, b);
    }

    private boolean greater(E a, E b) {
        if (comparator != null) {
            return comparator.compare(a, b) > 0;
        }
        return ((Comparable<E>) a).compareTo(b) > 0;
    }

    private boolean lower(E a, E b) {
        if (comparator != null) {
            return comparator.compare(a, b) < 0;
        }
        return ((Comparable<E>) a).compareTo(b) < 0;
    }

    private int binarySearch(E element) {
        if (comparator != null) {
            return Arrays.binarySearch(array, element, comparator);
        }
        return Arrays.binarySearch(array, element);
    }

    private int insertionIndex(E element) {
        return insertionIndex(element, 0);
    }

    private int insertionIndex(E element, int add) {
        int index = binarySearch(element);

        if (index >= 0) {
            index += add;
        } else {
            index = -index - 1;
        }

        index = index >= 0 ? index : -index - 1;

        if (index < start) {
            return start;
        }
        if (index > end) {
            return end;
        }
        return index;
    }

    private E[] removeDuplicates(E[] input) {
        if (input.length < 2) {
            return input;
        }

        E last = input[0];
        E[] tmp = (E[]) new Object[input.length];
        tmp[0] = input[0];


        int j = 1;
        for (int i = 1; i < input.length; i++) {
            if (!equals(input[i], last)) {
                tmp[j++] = input[i];
                last = input[i];
            }
        }
        return Arrays.copyOfRange(tmp, 0, j);
    }

    private class ArraySetIterator implements Iterator<E> {

        private int index = start;

        @Override
        public boolean hasNext() {
            return index < end;
        }

        @Override
        public E next() {
            return array[index++];
        }
    }

    private class ArraySetDescIterator implements Iterator<E> {

        private int index = end - 1;

        @Override
        public boolean hasNext() {
            return index >= start;
        }

        @Override
        public E next() {
            return array[index--];
        }
    }
}
