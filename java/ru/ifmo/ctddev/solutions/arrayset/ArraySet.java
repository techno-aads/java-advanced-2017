package ru.ifmo.ctddev.solutions.arrayset;

import java.util.*;

public class ArraySet<E> implements SortedSet<E> {

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

    protected  ArraySet(ArraySet<E> parent, int from, int to) throws IllegalArgumentException, NullPointerException, IndexOutOfBoundsException {
        this.comparator = parent.comparator;
        this.elements = parent.elements.subList(from, to);
    }

    private List<E> addCollection(List<E> elements, Comparator<? super E> comparator) {
        elements.sort(comparator);

        List<E> uniqueElements = new ArrayList<>();
        Iterator<E> iterator = elements.iterator();
        if (iterator.hasNext()) {
            E prev = iterator.next();
            uniqueElements.add(prev);
            while (iterator.hasNext()) {
                E current = iterator.next();
                if (comparator.compare(prev, current) != 0) {
                    uniqueElements.add(current);
                }
                prev = current;
            }
        }

        return uniqueElements;
    }

    @Override
    public Comparator<? super E> comparator() {
        return this.comparator.equals(NATURAL_ORDER_COMPARATOR) ? null : comparator;
    }

    @Override
    public SortedSet<E> headSet(E toElement) throws IndexOutOfBoundsException {
        int to = search(toElement);

        return new ArraySet<>(this.elements.subList(0, to), comparator);
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) throws IndexOutOfBoundsException {
        int to = search(toElement);
        int from = search(fromElement);
        return new ArraySet<>(this.elements.subList(from, to), comparator);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) throws IndexOutOfBoundsException {
        int from = search(fromElement);

        return new ArraySet<>(this.elements.subList(from, elements.size()), comparator);
    }

    @Override
    public Object[] toArray() {

        return elements.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return (T[]) toArray();
    }

    @Override
    public Iterator<E> iterator() {

        return Collections.unmodifiableList(elements).iterator();
    }

    @Override
    public boolean contains(Object o) {
        E temp = (E) o;
        return Collections.binarySearch(elements, temp, comparator) >= 0;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e: c) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
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

    @Override
    public boolean isEmpty() {
        return size() == 0;
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
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    protected int search(E element) {
        int position = Collections.binarySearch(elements, element, comparator);
        if (position < 0) {
            position = -position - 1;
        }
        return position;
    }

}
