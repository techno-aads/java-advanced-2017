package ru.ifmo.ctddev.solutions.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements SortedSet<E> {

    protected static final Comparator NATURAL_ORDER_COMPARATOR = Comparator.naturalOrder();

    protected List<E> elements;
    protected Comparator<E> comparator;

    public ArraySet() {
        elements = new ArrayList<>();
        comparator = NATURAL_ORDER_COMPARATOR;
    }

    public ArraySet(Collection<E> c) {
        this(c, NATURAL_ORDER_COMPARATOR);
    }

    public ArraySet(Collection<E> c, Comparator<E> comp) {
        comparator = comp;
        elements = addCollection(new ArrayList<>(c), comp);
    }

    protected  ArraySet(ArraySet<E> parent, int from, int to) throws IllegalArgumentException {
        this.comparator = parent.comparator;
        this.elements = parent.elements.subList(from, to);
    }

    private List<E> addCollection(List<E> elements, Comparator<? super E> comparator) {
        List<E> uniqueElements = new ArrayList<>();
        Set<E> unique = new TreeSet<E>(comparator);
        unique.addAll(elements);
        uniqueElements.addAll(unique);
        return uniqueElements;
    }

    @Override
    public Comparator<? super E> comparator() {
        return this.comparator.equals(NATURAL_ORDER_COMPARATOR) ? null : comparator;
    }

    @Override
    public SortedSet<E> headSet(E toElement) throws IndexOutOfBoundsException {
        int to = search(toElement);
        return new ArraySet<>(this, 0, to);
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) throws IndexOutOfBoundsException {
        int to = search(toElement);
        int from = search(fromElement);
        return new ArraySet<>(this, from, to);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) throws IndexOutOfBoundsException {
        int from = search(fromElement);
        return new ArraySet<>(this, from, elements.size());
    }

    @Override
    public Iterator<E> iterator() {

        return Collections.unmodifiableList(elements).iterator();
    }
    // this method has realization in AbstractSet but works too slow
    @Override
    public boolean contains(Object o) {
        E temp = (E) o;
        return Collections.binarySearch(elements, temp, comparator) >= 0;
    }

    @Override
    public E first() throws NoSuchElementException  {
        if(isEmpty())
            throw new NoSuchElementException();
        return elements.get(0);
    }

    @Override
    public E last() throws NoSuchElementException {
        if(isEmpty())
            throw new NoSuchElementException();
        return elements.get(elements.size() - 1);
    }

    @Override
    public int size() throws NoSuchElementException {
        return elements.size();
    }

    protected int search(E element) {
        int position = Collections.binarySearch(elements, element, comparator);
        if (position < 0) {
            position = -position - 1;
        }
        return position;
    }

}
