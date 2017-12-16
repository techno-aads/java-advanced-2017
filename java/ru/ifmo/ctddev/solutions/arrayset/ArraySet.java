package ru.ifmo.ctddev.solutions.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements SortedSet<E>, Iterable<E> {

    public Comparator<? super E> comparator = null;
    private List<E> data = new ArrayList<>();
    private boolean returnComparator = false;

    public ArraySet() {
        this(Collections.emptySortedSet(), null);
    }

    public ArraySet(Collection<? extends E> c) {
        this(c, null);
    }

    @SuppressWarnings("unchecked")
    public ArraySet(Collection<? extends E> collection, Comparator<? super E> comparator) {
        setComparator(comparator);
        TreeSet<E> set = new TreeSet<>(this.comparator);
        for (E i : collection) {
            set.add(i);
        }
        for (E i : set)
            data.add(i);
    }


    private ArraySet(List<E> data, Comparator<? super E> comparator) {
        this.data = data;
        setComparator(comparator);
    }

    @SuppressWarnings("unchecked")
    private void setComparator(Comparator<? super E> comparator) {
        if (comparator == null) {
            this.comparator = (Comparator<? super E>) Comparator.naturalOrder();
            returnComparator = false;
        } else {
            this.comparator = comparator;
            returnComparator = true;
        }
    }

    @Override
    public int size() {
        return data.size();
    }

    private int binarySearch(E e) {
        return Collections.binarySearch(data, e, comparator);
    }

    private boolean inRange(int i) {
        return i >= 0 && i < size();
    }

    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return binarySearch((E) o) >= 0;
    }

    @Override
    public Comparator<? super E> comparator() {
        if (returnComparator)
            return comparator;
        return null;
    }

    public int lowerElementIndex(E e, boolean inclusive) {
        int i = binarySearch(e);
        if (i >= 0) {
            if (!inclusive)
                i--;
            return i;
        }
        return Math.abs(i + 1) - 1;
    }

    public int upperElementIndex(E e, boolean inclusive) {
        int i = binarySearch(e);
        if (i >= 0) {
            if (!inclusive)
                i++;
            return i;
        }
        return Math.abs(i + 1);
    }

    @Override
    public E first() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return data.get(0);
    }

    @Override
    public E last() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return data.get(size() - 1);
    }

    @Override
    public <E> E[] toArray(E[] a) {
        return data.toArray(a);
    }

    @Override
    public Object[] toArray() {
        return data.toArray();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
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
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < size();
            }

            @Override
            public E next() {
                return data.get(i++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        int from = upperElementIndex(fromElement, true);
        int to = lowerElementIndex(toElement, false);
        return new ArraySet<>(data.subList(from, to + 1), comparator());
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        int to = lowerElementIndex(toElement, false);
        return new ArraySet<>(data.subList(0, to + 1), comparator());
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        int from = upperElementIndex(fromElement, true);
        return new ArraySet<>(data.subList(from, data.size()), comparator());
    }

    @Override
    public Spliterator<E> spliterator() {
        return null;
    }
}