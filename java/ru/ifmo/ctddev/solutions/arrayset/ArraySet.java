package ru.ifmo.ctddev.solutions.arrayset;

import com.sun.istack.internal.Nullable;

import java.util.*;

import static java.util.Collections.*;
import static java.util.Collections.binarySearch;
import static java.util.Collections.emptyList;

public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E>
{
    protected static final Comparator NATURAL_ORDER_COMPARATOR = Comparator.naturalOrder();
    protected final Comparator<? super E> comparator;
    protected final List<E> elementsData;

    public ArraySet()
    {
        this(emptyList());
    }

    @SuppressWarnings("unchecked")
    public ArraySet(Collection<E> collection) {
        comparator = NATURAL_ORDER_COMPARATOR;
        elementsData = addCollection(new ArrayList<>(collection), comparator);
    }

    @SuppressWarnings("unchecked")
    public ArraySet(SortedSet<E> sortedSet) {
        if (sortedSet.comparator() == null) {
            comparator = NATURAL_ORDER_COMPARATOR;
        } else {
            comparator = sortedSet.comparator();
        }
        this.elementsData = new ArrayList<>(sortedSet);
    }

    @SuppressWarnings("unchecked")
    public ArraySet(Collection<E> collection, @Nullable Comparator<? super E> comparator)
    {
        if (comparator == null)
        {
            this.comparator = NATURAL_ORDER_COMPARATOR;
        }
        else
        {
            this.comparator = comparator;
        }
        elementsData = addCollection(new ArrayList<>(collection), this.comparator);
    }

    private ArraySet(List<E> array, Comparator<? super E> comparator)
    {
        this.elementsData = array;
        this.comparator = comparator;
    }

    private List<E> addCollection(List<E> elements, Comparator<? super E> comparator)
    {
		ArrayList<E> inputData = new ArrayList<>(elements);

        SortedSet set = new TreeSet<E>(comparator);
        set.addAll(inputData);
        
        return new ArrayList<E> (set);
    }

    @Override
    public E lower(E e)
    {
        int position = binarySearch(elementsData, e, comparator);
        int actualPosition = position < 0 ? (-position -1) : position;
        return actualPosition - 1 < 0 ? null : elementsData.get(actualPosition - 1);
    }

    @Override
    public E floor(E e)
    {
        int position = binarySearch(elementsData, e, comparator);
        int actualPosition = position < 0 ? (-position -2) : position;
        return actualPosition < 0 ? null : elementsData.get(actualPosition);
    }

    @Override
    public E ceiling(E e)
    {
        int position = binarySearch(elementsData, e, comparator);
        int actualPosition = position < 0 ? (-position -1) : position;
        return actualPosition == size() ? null : elementsData.get(actualPosition);
    }

    @Override
    public E higher(E e)
    {
        int position = binarySearch(elementsData, e, comparator);
        int actualPosition = position < 0 ? (-position -1) : position + 1;
        return actualPosition == size() ? null : elementsData.get(actualPosition);
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
        return Collections.unmodifiableList(elementsData).iterator();
    }

    @Override
    public NavigableSet<E> descendingSet()
    {
        List<E> newArray = new ArrayList<>(elementsData);
        reverse(newArray);
        return new ArraySet<>(newArray, reverseOrder(comparator));
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new DescendingIterator<>(elementsData);
    }

    @Override
	public NavigableSet<E> subSet(E e, boolean b, E e1, boolean b1) throws IllegalArgumentException
    {
        int fromPosition = binarySearch(elementsData, e, comparator);
        int fromIndex = fromPosition < 0 ? (-fromPosition -1) : fromPosition;

        int toPosition = binarySearch(elementsData, e1, comparator);
        int toIndex = toPosition < 0 ? (-toPosition -1) : toPosition;

        if (fromIndex > toIndex)
        {
            throw new IllegalArgumentException("fromElement > toElement");
        }

        if (fromIndex == toIndex && !(b && b1)
                && (fromPosition >= 0
                && toPosition >= 0))
        {
            return new ArraySet<>(emptyList(), comparator);
        }

        if (!b && fromPosition >= 0)
        {
            fromIndex++;
        }
        if (b1 && toPosition >= 0)
        {
            toIndex++;
        }
        return new ArraySet<>(elementsData.subList(fromIndex, toIndex), comparator);
    }

    @Override
    public NavigableSet<E> headSet(E e, boolean b)
    {
        int position = binarySearch(elementsData, e, comparator);
        int actualPosition = position < 0 ? (-position -1) : b ? position + 1 : position;
        return new ArraySet<>(elementsData.subList(0, actualPosition), comparator);
    }

    @Override
    public NavigableSet<E> tailSet(E e, boolean b)
    {
        int position = binarySearch(elementsData, e, comparator);
        int actualPosition = position < 0 ? (-position -1) : !b ? position + 1 : position;
        return new ArraySet<>(elementsData.subList(actualPosition, size()), comparator);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(elementsData, (E) o, comparator) >= 0;
    }

    @Override
    public Comparator<? super E> comparator()
    {
        return this.comparator.equals(NATURAL_ORDER_COMPARATOR) ? null : comparator;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement)
    {
        int from = search(fromElement);
        int to = search(toElement);
        return new ArraySet<>(elementsData.subList(from, to), comparator);
    }

    @Override
    public SortedSet<E> headSet(E toElement)
    {
        int to = search(toElement);
        return new ArraySet<>(elementsData.subList(0, to), comparator);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement)
    {
        int from = search(fromElement);
        return new ArraySet<>(elementsData.subList(from, elementsData.size()), comparator);
    }

    @Override
    public E first()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException();
        }
        return elementsData.get(0);
    }

    @Override
    public E last()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException();
        }
        return elementsData.get(size() - 1);
    }

    @Override
    public int size() {
        return elementsData.size();
    }

    private int search(E element) {
        int position = Collections.binarySearch(elementsData, element, comparator);
        if (position < 0)
        {
            position = - position - 1;
        }
        return position;
    }

    private static class DescendingIterator<E> implements Iterator<E>
    {
        private final ListIterator<E> iterator;

        DescendingIterator(List<E> list) {
            this.iterator = list.listIterator(list.size());
        }

        @Override
        public boolean hasNext() {
            return iterator.hasPrevious();
        }

        @Override
        public E next() {
            return iterator.previous();
        }
    }
}