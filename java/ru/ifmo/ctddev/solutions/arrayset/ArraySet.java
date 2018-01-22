package ru.ifmo.ctddev.solutions.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E>, Iterable<E> {

    public Comparator<? super E> comparator = null;
    private List<E> data = new ArrayList<>();
    private boolean returnComparator = false;

    public ArraySet() {
        this(Collections.emptySortedSet(), null);
    }

    @SuppressWarnings("unchecked")
    public ArraySet(Collection<? extends E> collection) {
        setComparator(comparator);
        TreeSet<E> set = new TreeSet<>();
        set.addAll(collection);
        data.addAll(set);
    }
    @SuppressWarnings("unchecked")
    public ArraySet(Collection<? extends E> collection, Comparator<? super E> comparator) {
        setComparator(comparator);
        TreeSet<E> set = new TreeSet<>(this.comparator);
        set.addAll(collection);
        data.addAll(set);
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
    public E lower(E e) {
        int tmp_index = binarySearch(e);
        if (tmp_index < 0) {
            tmp_index = -tmp_index - 1;
        }
        tmp_index = tmp_index - 1;
        if (tmp_index >= 0 && tmp_index < data.size()) {
            return data.get(tmp_index);
        }
        return null;
    }

    @Override
    public E floor(E e) {
        int tmp_index = binarySearch(e);
        if (tmp_index < 0) {
            tmp_index = -tmp_index - 2;
        }
        if (tmp_index >= 0 && tmp_index < data.size()) {
            return data.get(tmp_index);
        }
        return null;
    }

    @Override
    public E ceiling(E e) {
        int tmp_index = binarySearch(e);
        if (tmp_index < 0) {
            tmp_index = -tmp_index - 1;
        }
        if (tmp_index >= 0 && tmp_index < data.size()) {
            return data.get(tmp_index);
        }
        return null;
    }

    @Override
    public E higher(E e) {
        int tmp_index = binarySearch(e);
        if (tmp_index < 0) {
            tmp_index = -tmp_index - 1;
        } else {
            tmp_index += 1;
        }
        if (tmp_index >= 0 && tmp_index < data.size()) {
            return data.get(tmp_index);
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
    public NavigableSet<E> descendingSet() {
        ArraySet<E> descendingSet = new ArraySet<>(new ArrayList<>(data), Collections.reverseOrder(comparator));
        Collections.reverse(descendingSet.data);
        return descendingSet;
    }

    @Override
    public Iterator<E> descendingIterator() {
        ArrayList<E> resArrayList = new ArrayList<>(data);
        Collections.reverse(resArrayList);
        return Collections.unmodifiableList(resArrayList).iterator();
    }

    private ArraySet<E> getSubSet(int newFromIndex, int newToIndex) {
        if (newFromIndex == 0 && newToIndex == size()) {
            return this;
        } else if (newFromIndex < newToIndex) {
            return new ArraySet<E>(data.subList(newFromIndex, newToIndex), comparator);
        } else {
            return new ArraySet<E>();
        }
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return tailSet(fromElement, fromInclusive).headSet(toElement, toInclusive);
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        int tmpIndex = binarySearch(toElement);

        if (tmpIndex >= 0) {
            if (inclusive) {
                tmpIndex += 1;
            }
        } else {
            tmpIndex = -tmpIndex - 1;
        }
        return getSubSet(0, tmpIndex);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        int tmpIndex = binarySearch(fromElement);

        if (tmpIndex >= 0) {
            if (!inclusive) {
                tmpIndex += 1;
            }
        } else {
            tmpIndex = -tmpIndex - 1;
        }
        return getSubSet(tmpIndex, size());
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
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
}