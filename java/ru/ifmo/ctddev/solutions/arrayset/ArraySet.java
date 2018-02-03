package ru.ifmo.ctddev.solutions.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {
    
    private List<E> elements;
    private List<E> reversedElements;
    private Comparator<? super E> comparator;
    private boolean naturalOrder;
    
    private ArraySet(List<E> elements, List<E> reversedElements, Comparator<? super E> comparator, boolean naturalOrder) {
        this.elements = elements;
        this.reversedElements = reversedElements;
        this.comparator = comparator;
        this.naturalOrder = naturalOrder;
    }
    
    public ArraySet(Collection<E> elements, Comparator<? super E> comparator) {
        this.comparator = comparator;
        
        this.naturalOrder = false;
        
        NavigableSet<E> set = new TreeSet<>(comparator);
        set.addAll(elements);
        
        this.elements = new ArrayList<>(set);
        this.reversedElements = new ArrayList<>(set.descendingSet());
    }
    
    public ArraySet(Collection<E> collection) {
        this(collection, (o1, o2) -> ((Comparable<E>)o1).compareTo(o2));
        naturalOrder = true;
    }
    
    public ArraySet() {
        this(new ArrayList<>(), new ArrayList<>(), (o1, o2) -> 0, false);
    }
    
    /**
     * Returns the greatest element in this set strictly less than the
     * given element, or {@code null} if there is no such element.
     *
     * @param e the value to match
     *
     * @return the greatest element less than {@code e},
     * or {@code null} if there is no such element
     *
     * @throws ClassCastException   if the specified element cannot be
     *                              compared with the elements currently in the set
     * @throws NullPointerException if the specified element is null
     *                              and this set does not permit null elements
     */
    @Override
    public E lower(E e) {
        int index = binarySearch(e);
        if (index < 0) {
            index = -index - 1;
        }
        
        index--;
        
        if (index >= 0 && index < elements.size()) {
            return elements.get(index);
        }
        
        return null;
    }
    
    /**
     * Returns the greatest element in this set less than or equal to
     * the given element, or {@code null} if there is no such element.
     *
     * @param e the value to match
     *
     * @return the greatest element less than or equal to {@code e},
     * or {@code null} if there is no such element
     *
     * @throws ClassCastException   if the specified element cannot be
     *                              compared with the elements currently in the set
     * @throws NullPointerException if the specified element is null
     *                              and this set does not permit null elements
     */
    @Override
    public E floor(E e) {
        int index = binarySearch(e);
        if (index < 0) {
            index = -index - 2;
        }
        
        if (index >= 0) {
            return elements.get(index);
        }
        
        return null;
    }
    
    /**
     * Returns the least element in this set greater than or equal to
     * the given element, or {@code null} if there is no such element.
     *
     * @param e the value to match
     *
     * @return the least element greater than or equal to {@code e},
     * or {@code null} if there is no such element
     *
     * @throws ClassCastException   if the specified element cannot be
     *                              compared with the elements currently in the set
     * @throws NullPointerException if the specified element is null
     *                              and this set does not permit null elements
     */
    @Override
    public E ceiling(E e) {
        int index = binarySearch(e);
        if (index < 0) {
            index = -index - 1;
        }
        
        if (index >= 0 && index < elements.size()) {
            return elements.get(index);
        }
        
        return null;
    }
    
    /**
     * Returns the least element in this set strictly greater than the
     * given element, or {@code null} if there is no such element.
     *
     * @param e the value to match
     *
     * @return the least element greater than {@code e},
     * or {@code null} if there is no such element
     *
     * @throws ClassCastException   if the specified element cannot be
     *                              compared with the elements currently in the set
     * @throws NullPointerException if the specified element is null
     *                              and this set does not permit null elements
     */
    @Override
    public E higher(E e) {
        int index = binarySearch(e);
        if (index >= 0) {
            index++;
        } else {
            index = -index - 1;
        }
        
        if (index >= 0 && index < elements.size()) {
            return elements.get(index);
        }
        
        return null;
    }
    
    /**
     * Retrieves and removes the first (lowest) element,
     * or returns {@code null} if this set is empty.
     *
     * @return the first element, or {@code null} if this set is empty
     */
    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException("Operation is not supported due set's immutability");
    }
    
    /**
     * Retrieves and removes the last (highest) element,
     * or returns {@code null} if this set is empty.
     *
     * @return the last element, or {@code null} if this set is empty
     */
    @Override
    public E pollLast() {
        throw new UnsupportedOperationException("Operation is not supported due set's immutability");
    }
    
    /**
     * Returns a reverse order view of the elements contained in this set.
     * The descending set is backed by this set, so changes to the set are
     * reflected in the descending set, and vice-versa. If either set is
     * modified while an iteration over either set is in progress (except
     * through the iterator's own {@code remove} operation), the results of
     * the iteration are undefined.
     * <p>
     * <p>The returned set has an ordering equivalent to
     * {@link Collections#reverseOrder(Comparator) Collections.reverseOrder}{@code (comparator())}.
     * The expression {@code s.descendingSet().descendingSet()} returns a
     * view of {@code s} essentially equivalent to {@code s}.
     *
     * @return a reverse order view of this set
     */
    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<>(this.reversedElements, this.elements, comparator.reversed(), false);
    }
    
    /**
     * Returns an iterator over the elements in this set, in descending order.
     * Equivalent in effect to {@code descendingSet().iterator()}.
     *
     * @return an iterator over the elements in this set, in descending order
     */
    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }
    
    /**
     * Returns a view of the portion of this set whose elements range from
     * {@code fromElement} to {@code toElement}.  If {@code fromElement} and
     * {@code toElement} are equal, the returned set is empty unless {@code
     * fromInclusive} and {@code toInclusive} are both true.  The returned set
     * is backed by this set, so changes in the returned set are reflected in
     * this set, and vice-versa.  The returned set supports all optional set
     * operations that this set supports.
     * <p>
     * <p>The returned set will throw an {@code IllegalArgumentException}
     * on an attempt to insert an element outside its range.
     *
     * @param fromElement   low endpoint of the returned set
     * @param fromInclusive {@code true} if the low endpoint
     *                      is to be included in the returned view
     * @param toElement     high endpoint of the returned set
     * @param toInclusive   {@code true} if the high endpoint
     *                      is to be included in the returned view
     *
     * @return a view of the portion of this set whose elements range from
     * {@code fromElement}, inclusive, to {@code toElement}, exclusive
     *
     * @throws ClassCastException       if {@code fromElement} and
     *                                  {@code toElement} cannot be compared to one another using this
     *                                  set's comparator (or, if the set has no comparator, using
     *                                  natural ordering).  Implementations may, but are not required
     *                                  to, throw this exception if {@code fromElement} or
     *                                  {@code toElement} cannot be compared to elements currently in
     *                                  the set.
     * @throws NullPointerException     if {@code fromElement} or
     *                                  {@code toElement} is null and this set does
     *                                  not permit null elements
     * @throws IllegalArgumentException if {@code fromElement} is
     *                                  greater than {@code toElement}; or if this set itself
     *                                  has a restricted range, and {@code fromElement} or
     *                                  {@code toElement} lies outside the bounds of the range.
     */
    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) throws IllegalArgumentException {
        E ceiling = ceiling(fromElement);
        E floor = floor(toElement);
        
        if (floor == null || ceiling == null) {
            return emptySet(comparator);
        }
        
        if (comparator.compare(ceiling, fromElement) != 0) {
            fromInclusive = true;
            fromElement = ceiling;
        }
        
        if (comparator.compare(floor, toElement) != 0) {
            toElement = floor;
            toInclusive = true;
        }
        
        int from = binarySearch(fromElement);
        int to = binarySearch(toElement);
        
        if (!fromInclusive) {
            from++;
        }
        
        if (toInclusive) {
            to++;
        }
        
        if (from > to) {
            return emptySet(comparator);
        } else {
            return new ArraySet<>(elements.subList(from, to),
                                  reversedElements.subList(size() - to, size() - from),
                                  comparator,
                                  naturalOrder);
        }
    }
    
    /**
     * Returns a view of the portion of this set whose elements are less than
     * (or equal to, if {@code inclusive} is true) {@code toElement}.  The
     * returned set is backed by this set, so changes in the returned set are
     * reflected in this set, and vice-versa.  The returned set supports all
     * optional set operations that this set supports.
     * <p>
     * <p>The returned set will throw an {@code IllegalArgumentException}
     * on an attempt to insert an element outside its range.
     *
     * @param toElement high endpoint of the returned set
     * @param inclusive {@code true} if the high endpoint
     *                  is to be included in the returned view
     *
     * @return a view of the portion of this set whose elements are less than
     * (or equal to, if {@code inclusive} is true) {@code toElement}
     *
     * @throws ClassCastException       if {@code toElement} is not compatible
     *                                  with this set's comparator (or, if the set has no comparator,
     *                                  if {@code toElement} does not implement {@link Comparable}).
     *                                  Implementations may, but are not required to, throw this
     *                                  exception if {@code toElement} cannot be compared to elements
     *                                  currently in the set.
     * @throws NullPointerException     if {@code toElement} is null and
     *                                  this set does not permit null elements
     * @throws IllegalArgumentException if this set itself has a
     *                                  restricted range, and {@code toElement} lies outside the
     *                                  bounds of the range
     */
    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        if (size() == 0) {
            return this;
        }
        
        return subSet(first(), true, toElement, inclusive);
    }
    
    /**
     * Returns a view of the portion of this set whose elements are greater
     * than (or equal to, if {@code inclusive} is true) {@code fromElement}.
     * The returned set is backed by this set, so changes in the returned set
     * are reflected in this set, and vice-versa.  The returned set supports
     * all optional set operations that this set supports.
     * <p>
     * <p>The returned set will throw an {@code IllegalArgumentException}
     * on an attempt to insert an element outside its range.
     *
     * @param fromElement low endpoint of the returned set
     * @param inclusive   {@code true} if the low endpoint
     *                    is to be included in the returned view
     *
     * @return a view of the portion of this set whose elements are greater
     * than or equal to {@code fromElement}
     *
     * @throws ClassCastException       if {@code fromElement} is not compatible
     *                                  with this set's comparator (or, if the set has no comparator,
     *                                  if {@code fromElement} does not implement {@link Comparable}).
     *                                  Implementations may, but are not required to, throw this
     *                                  exception if {@code fromElement} cannot be compared to elements
     *                                  currently in the set.
     * @throws NullPointerException     if {@code fromElement} is null
     *                                  and this set does not permit null elements
     * @throws IllegalArgumentException if this set itself has a
     *                                  restricted range, and {@code fromElement} lies outside the
     *                                  bounds of the range
     */
    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        if (size() == 0) {
            return this;
        }
        
        return subSet(fromElement, inclusive, last(), true);
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * <p>Equivalent to {@code subSet(fromElement, true, toElement, false)}.
     *
     * @param fromElement
     * @param toElement
     *
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * <p>Equivalent to {@code headSet(toElement, false)}.
     *
     * @param toElement
     *
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * <p>Equivalent to {@code tailSet(fromElement, true)}.
     *
     * @param fromElement
     *
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }
    
    /**
     * Returns an iterator over the elements contained in this collection.
     *
     * @return an iterator over the elements contained in this collection
     */
    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            Iterator<E> iterator = elements.iterator();
            
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }
            
            @Override
            public E next() {
                return iterator.next();
            }
        };
    }
    
    /**
     * Returns the number of elements in this set (its cardinality).  If this
     * set contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of elements in this set (its cardinality)
     */
    @Override
    public int size() {
        return elements.size();
    }
    
    /**
     * Returns <tt>true</tt> if this set contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this set
     * contains an element <tt>e</tt> such that
     * <tt>(o&nbsp;==&nbsp;null&nbsp;?&nbsp;e&nbsp;==&nbsp;null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this set is to be tested
     *
     * @return <tt>true</tt> if this set contains the specified element
     *
     * @throws ClassCastException   if the type of the specified element
     *                              is incompatible with this set
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this
     *                              set does not permit null elements
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    public boolean contains(Object o) {
        return binarySearch((E)o) >= 0;
    }
    
    /**
     * Returns the comparator used to order the elements in this set,
     * or {@code null} if this set uses the {@linkplain Comparable
     * natural ordering} of its elements.
     *
     * @return the comparator used to order the elements in this set,
     * or {@code null} if this set uses the natural ordering
     * of its elements
     */
    @Override
    public Comparator<? super E> comparator() {
        return naturalOrder ? null : comparator;
    }
    
    /**
     * Returns the first (lowest) element currently in this set.
     *
     * @return the first (lowest) element currently in this set
     *
     * @throws NoSuchElementException if this set is empty
     */
    @Override
    public E first() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return elements.get(0);
    }
    
    /**
     * Returns the last (highest) element currently in this set.
     *
     * @return the last (highest) element currently in this set
     *
     * @throws NoSuchElementException if this set is empty
     */
    @Override
    public E last() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return elements.get(size() - 1);
    }
    
    /**
     * Delegated {@link Collections#binarySearch(List, Object, Comparator)}
     */
    public int binarySearch(E e) {
        return Collections.binarySearch(elements, e, comparator);
    }
    
    public static <E> NavigableSet<E> emptySet(Comparator<? super E> comparator) {
        return new ArraySet<>(new ArrayList<>(), comparator);
    }
}