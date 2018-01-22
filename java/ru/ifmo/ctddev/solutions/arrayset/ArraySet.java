package ru.ifmo.ctddev.solutions.arrayset;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Nikita Sokeran
 */
public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {
    private static final String IMMUTABLE_EXCEPTION_MESSAGE = "This is immutable Collection";
    private static final String NO_SUCH_ELEMENT_EXCEPTION_MESSAGE = "No such element";

    private final List<T> list;
    private final Comparator<? super T> comparator;

    public ArraySet(final Collection<? extends T> collection) {
        this.comparator = null;
        TreeSet<T> treeSet = new TreeSet<>(collection);
        this.list = new ArrayList<>(treeSet);
    }

    public ArraySet() {
        comparator = null;
        list = new ArrayList<>();
    }

    public ArraySet(final Collection<? extends T> collection,
                    final Comparator<? super T> comparator) {
        this.comparator = comparator;
        TreeSet<T> treeSet = new TreeSet<>(comparator);
        treeSet.addAll(collection);
        this.list = new ArrayList<>(treeSet);
    }

    private ArraySet(final List<T> list, final Comparator<? super T> comparator,
                     final int from, final int to) {
        this.comparator = comparator;
        this.list = list.subList(from, to);
    }

    private boolean compareElements(final T e1, final T e2) {
        if (comparator == null) {
            return e1.equals(e2);
        }
        return comparator.compare(e1, e2) == 0;
    }

    @Override
    public T lower(final T t) {
        int position = lowerPosition(t);
        if (position < 0) {
            return null;
        }
        return compareElements(t, list.get(position))
                ? null
                : list.get(position);
    }

    private int lowerPosition(final T t) {
        int position = binarySearch(t);
        if (position < 0) {
            return -position - 2;
        } else if (position == list.size() || position == 0) {
            return -1;
        } else {
            return position - 1;
        }
    }

    @Override
    public T floor(final T t) {
        int position = floorPosition(t);
        return position == -1 ? null : list.get(position);
    }

    private int floorPosition(final T t) {
        int position = binarySearch(t);
        if (position < 0) {
            return -position - 2;
        } else if (position == list.size()) {
            return -1;
        } else {
            return position;
        }
    }

    @Override
    public T ceiling(final T t) {
        int position = ceilingPosition(t);
        return position == -1 ? null : (T) list.get(position);
    }

    private int ceilingPosition(final T t) {
        int position = binarySearch(t);
        if (position < 0) {
            int insertionPoint = -position - 1;
            return (insertionPoint >= list.size()) ? -1 : insertionPoint;
        } else if (position == list.size()) {
            return -1;
        } else {
            return position;
        }
    }

    @Override
    public T higher(final T t) {
        int position = higherPosition(t);
        if (position == -1) {
            return null;
        }
        return compareElements(t, list.get(position)) ? null
                : list.get(position);
    }

    private int higherPosition(final T t) {
        int position = binarySearch(t);
        if (position < 0) {
            int insertionPoint = -position - 1;
            return (insertionPoint >= list.size()) ? -1 : insertionPoint;
        } else if (position >= list.size() - 1) {
            return -1;
        } else {
            return position + 1;
        }
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException(IMMUTABLE_EXCEPTION_MESSAGE);
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException(IMMUTABLE_EXCEPTION_MESSAGE);
    }

    @Override
    public Iterator<T> iterator() {

        return new ArraySetIterator(list.size() - 1,
                ArraySetIteratorType.FORWARD);
    }

    @Override
    @SuppressWarnings("unchecked")
    public NavigableSet<T> descendingSet() {
        Comparator<? super T> newComparator = comparator == null
                ? (o1, o2) -> -((Comparable<? super T>) o1).compareTo(o2)
                : comparator.reversed();
        return new ArraySet<>(this, newComparator);
    }

    @Override
    public Iterator<T> descendingIterator() {
        return new ArraySetIterator(-1, ArraySetIteratorType.BACKWARD);
    }

    @Override
    public NavigableSet<T> subSet(final T fromElement,
                                  final boolean fromInclusive,
                                  final T toElement, final boolean toInclusive) {
        int fromPosition = fromInclusive ? ceilingPosition(fromElement)
                : higherPosition(fromElement);
        int toPosition =
                toInclusive ? floorPosition(toElement) : lowerPosition(toElement);
        if (toPosition < 0 || fromPosition < 0) {
            return new ArraySet<>();
        }
        if (fromPosition > toPosition) {
            return new ArraySet<>();
        }
        return new ArraySet<>(list, comparator, fromPosition,
                ++toPosition);
    }

    @Override
    public NavigableSet<T> headSet(final T toElement, final boolean inclusive) {
        int position =
                inclusive ? floorPosition(toElement) : lowerPosition(toElement);
        return new ArraySet<>(list, comparator, 0, position + 1);
    }

    @Override
    public NavigableSet<T> tailSet(final T fromElement, final boolean inclusive) {
        int position =
                inclusive ? ceilingPosition(fromElement) : higherPosition(fromElement);
        if (position < 0) {
            return new ArraySet<>();
        }
        return new ArraySet<>(list, comparator, position, list.size());
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<T> subSet(final T fromElement, final T toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> headSet(final T toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(final T fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public T first() {
        if (isEmpty()) {
            throw new NoSuchElementException(NO_SUCH_ELEMENT_EXCEPTION_MESSAGE);
        }
        return list.get(0);
    }

    @Override
    public T last() {
        if (isEmpty()) {
            throw new NoSuchElementException(NO_SUCH_ELEMENT_EXCEPTION_MESSAGE);
        }
        return list.get(list.size() - 1);
    }

    @Override
    public int size() {
        return list.size();
    }

    private int binarySearch(final T key) {
        return Collections.binarySearch(list, key, comparator);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(final Object o1) {
        T element = (T) o1;
        int position = binarySearch(element);
        return position >= 0
                && compareElements(element, list.get(position));
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    private enum ArraySetIteratorType {
        FORWARD,
        BACKWARD
    }

    private class ArraySetIterator implements Iterator<T> {

        private final int last;
        private int current;
        private final ArraySetIteratorType iteratorType;

        ArraySetIterator(final int last, final ArraySetIteratorType iteratorType) {
            this.last = last;
            this.iteratorType = iteratorType;
            if (iteratorType == ArraySetIteratorType.FORWARD) {
                current = -1;
            } else {
                current = list.size();
            }
        }

        @Override
        public boolean hasNext() {
            return current != last;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T next() {
            if (hasNext()) {
                if (iteratorType == ArraySetIteratorType.FORWARD) {
                    return list.get(++current);
                } else {
                    return list.get(--current);
                }
            } else {
                throw new NoSuchElementException(NO_SUCH_ELEMENT_EXCEPTION_MESSAGE);
            }
        }
    }
}
