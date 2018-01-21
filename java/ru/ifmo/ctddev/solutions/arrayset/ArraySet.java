package ru.ifmo.ctddev.ovsyannikov.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements SortedSet<E>
{

    // ----------------------------------------------------------------------------------------------------*
    //                            ATTRIBUTES
    // ----------------------------------------------------------------------------------------------------*

    protected List<E> array;
    protected Comparator<E> elementComprator;
    protected static final Comparator COMPARATOR = Comparator.naturalOrder();

    // ----------------------------------------------------------------------------------------------------*
    //                            CONSTRUCTORS
    // ----------------------------------------------------------------------------------------------------*
    public ArraySet()
    {
        array = new ArrayList<>();
        elementComprator = COMPARATOR;
    }

    public ArraySet(Collection<E> c)
    {
        this(c, COMPARATOR);
    }

    public ArraySet(Collection<E> c, Comparator<E> elComp)
    {
        elementComprator = elComp;
        array = addCollection(new ArrayList<>(c), elComp);
    }

    protected  ArraySet(ArraySet<E> parent, int from, int to) throws IllegalArgumentException
    {
        this.elementComprator = parent.elementComprator;
        this.array = parent.array.subList(from, to);
    }

    // ----------------------------------------------------------------------------------------------------*
    //                            METHODS
    // ----------------------------------------------------------------------------------------------------*

    private List<E> addCollection(List<E> array, Comparator<? super E> elComp)
    {
        List<E> uniqueElements = new ArrayList<>();
        Set<E> unique = new TreeSet<E>(elComp);
        unique.addAll(array);
        uniqueElements.addAll(unique);
        return uniqueElements;
    }

    protected int search(E element)
    {
        int position = Collections.binarySearch(array, element, elementComprator);
        if (position < 0)
        {
            position = -position - 1;
        }
        return position;
    }

    @Override
    public Comparator<? super E> comparator()
    {
        if (this.elementComprator.equals(COMPARATOR))
        {
            return null;
        }
        else
        {
            return elementComprator;
        }
    }

    @Override
    public int size() throws NoSuchElementException
    {
        return array.size();
    }

    @Override
    public SortedSet<E> headSet(E toElement) throws IndexOutOfBoundsException
    {
        int endElem = search(toElement);

        return new ArraySet<>(this, 0, endElem);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) throws IndexOutOfBoundsException
    {
        int startElem = search(fromElement);

        return new ArraySet<>(this, startElem, array.size());
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) throws IndexOutOfBoundsException
    {
        int startElem = search(fromElement);
        int endElem = search(toElement);

        return new ArraySet<>(this, startElem, endElem);
    }

    @Override
    public E first() throws NoSuchElementException
    {
        if(isEmpty())
            throw new NoSuchElementException();
        return array.get(0);
    }

    @Override
    public E last() throws NoSuchElementException
    {
        if(isEmpty())
            throw new NoSuchElementException();
        return array.get(array.size() - 1);
    }

    @Override
    public Iterator<E> iterator()
    {
        return Collections.unmodifiableList(array).iterator();
    }

    @Override
    public boolean contains(Object o)
    {
        E elem = (E) o;
        return Collections.binarySearch(array, elem, elementComprator) >= 0;
    }

}