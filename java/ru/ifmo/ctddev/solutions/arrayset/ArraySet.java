package ru.ifmo.ctddev.solutions.arrayset;

import java.util.*;

//fixme: change class signature
public class ArraySet<T> implements SortedSet<T> {

    private final List<T> data;
    private final Comparator<? super T> comparator;
    private boolean standardComparator;

    public ArraySet(Collection<T> collection, Comparator<? super T> comparator) {
        this.comparator = comparator;
        this.standardComparator = false;

        List<T> buffer = new ArrayList<>(collection);
        List<T> unique = new ArrayList<>();

        Collections.sort(buffer, comparator);

        if (collection.size() > 0) {
            unique.add(buffer.get(0));
            for (int i = 1; i < buffer.size(); i++) {
                if (comparator.compare(buffer.get(i), buffer.get(i - 1)) != 0) {
                    unique.add(buffer.get(i));
                }
            }
        }
        this.data = new ArrayList<>(unique);

    }

    private ArraySet(List<T> data, Comparator<? super T> comparator, boolean standardComparator) {
        this.data = data;
        this.comparator = comparator;
        this.standardComparator = standardComparator;
    }

    public ArraySet(Collection<T> collection) {
        this(collection,
                new Comparator<T>() {
                    @Override
                    public int compare(T first, T second) {
                        return ((Comparable<? super T>) first).compareTo(second);
                    }
                });
        this.standardComparator = true;
    }

    public ArraySet() {
        this.data = new ArrayList<>();
        this.comparator = null;
        this.standardComparator = false;
    }


    private class ArrayIterator<T> implements Iterator<T> {

        private int index;

        ArrayIterator() {
            this.index = -1;
        }

        @Override
        public boolean hasNext() {
            return index < data.size() - 1;

        }

        @SuppressWarnings("unchecked")
        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return (T) data.get(++index);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Unsupported operation");
        }
    }

    @Override
    public Comparator<? super T> comparator() {
        if (standardComparator) {
            return null;
        }
        return comparator;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return subSet(findPosition(fromElement), findPosition(toElement));
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return subSet(0, findPosition(toElement));
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return subSet(findPosition(fromElement), size());
    }

    private SortedSet<T> subSet(int fromPos, int toPos) {
        return new ArraySet<>(data.subList(fromPos, toPos), comparator, standardComparator);
    }

    private int findPosition(T element) {
        int pos = Collections.binarySearch(data, element, comparator);

        if (pos < 0) {
            pos = -pos - 1;
        }
        return pos;
    }

    @Override
    public T first() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }

        return data.get(0);
    }

    @Override
    public T last() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return data.get(data.size() - 1);
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(data, (T) o, comparator) >= 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayIterator<>();
    }

    @Override
    public Object[] toArray() {
        return data.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] t1s) {
        return data.toArray(t1s);
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException("Set is immutable");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Set is immutable");
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        for (Object e : collection) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        throw new UnsupportedOperationException("Set is immutable");
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException("Set is immutable");
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException("Set is immutable");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Set is immutable");
    }
}
