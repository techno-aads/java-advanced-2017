package ru.ifmo.ctddev.solutions.arrayset;

import java.util.*;

public class NavigableArraySet<E> extends ArraySet<E> implements NavigableSet<E> {

    public class DescendingIterator implements Iterator<E> {
        private List<E> elems;
        private int from, to;

        public DescendingIterator(List<E> elems) {
            this.elems = elems;
            from = 0;
            to = elems.size() - 1;
        }

        @Override
        public boolean hasNext() {
            return to > from;
        }

        @Override
        public E next() {
            return elems.get(to--);
        }

    }

    public NavigableArraySet() {
        super();
    }

    public NavigableArraySet(Collection<E> c) {
        super(c);
    }

    public NavigableArraySet(Collection<E> c, Comparator comp) {
        super(c, comp);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new DescendingIterator(elements);
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new NavigableArraySet<>(elements, Collections.reverseOrder(comparator));
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {

        int from = Collections.binarySearch(elements, fromElement, comparator);
        int fromIndex = from < 0 ? (-from -1) : from;
        int to = Collections.binarySearch(elements, toElement, comparator);
        int toIndex = to < 0 ? (-to -1) : to;

        if (fromIndex > toIndex) {
            throw new IllegalArgumentException();
        }
        if (fromIndex == toIndex && !(fromInclusive && toInclusive) && (from >= 0
                && to >= 0)) {
            return new NavigableArraySet<>(Collections.emptyList(), comparator);
        }

        if (!fromInclusive && from >= 0) {
            fromIndex++;
        }
        if (toInclusive && to >= 0) {
            toIndex++;
        }
        return new NavigableArraySet<>(elements.subList(fromIndex, toIndex), comparator);

    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean fromInclusive) {
        int from = Collections.binarySearch(elements, fromElement, comparator);
        from = from < 0 ? (-from - 1) : !fromInclusive ? from + 1 : from;
        return new NavigableArraySet<>(elements.subList(from, size()), comparator);
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean toInclusive) {
        int to = Collections.binarySearch(elements, toElement, comparator);
        to = to < 0 ? (-to - 1) : toInclusive ? to + 1 : to;
        return new NavigableArraySet<>(this.elements.subList(0, to), comparator);
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
    public E ceiling(E e) {
        int pos = Collections.binarySearch(elements, e, comparator);
        pos = pos >= 0 ? pos : castIndex(pos);
        return outOfRange(pos) ? null : elements.get(pos);
    }

    @Override
    public E higher(E e) {

        int pos = Collections.binarySearch(elements, e, comparator);
        pos = pos >= 0 ? pos + 1 : castIndex(pos);
        return outOfRange(pos) ? null : elements.get(pos);
    }

    @Override
    public E floor(E e) {
//        int pos = search(e);
//        return pos < elements.size() && pos > 0 ? elements.get(pos) : null;
        int pos = Collections.binarySearch(elements, e, comparator);
        pos = pos >= 0 ? pos : castIndex(pos) - 1;
        return outOfRange(pos) ? null : elements.get(pos);
    }

    @Override
    public E lower(E e) {
        int pos = search(e);
        return outOfRange(pos - 1) ? null : elements.get(pos - 1);
    }

    private int castIndex(int i) {
        return i < 0 ? (-i - 1) : i;
    }

    private boolean outOfRange(int i) {
        return !(i >= 0 && i < this.elements.size());
    }
}
