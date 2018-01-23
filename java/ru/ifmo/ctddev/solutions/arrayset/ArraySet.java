package ru.ifmo.ctddev.solutions.arrayset;
import java.util.*;

//https://habrahabr.ru/post/232963/

import java.lang.reflect.Array;
import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E>{

    List<E> m_Data;
    List<E> m_DataReversed;
    Comparator<? super E> m_Comparator;
    boolean m_DefaultOrder = false;

    public ArraySet(){
        m_Data = new ArrayList<>();
        m_DataReversed = new ArrayList<>();
        m_Comparator = (Comparator<E>) (o1, o2) -> 0;
        m_DefaultOrder = true;
    }


    public ArraySet(Collection<E> collection, Comparator<? super E> comparator){

        this.m_Comparator = comparator;

        ArrayList<E> inputData = new ArrayList<>(collection);

        SortedSet set = new TreeSet<E>(comparator);
        set.addAll(inputData);

        m_Data = new ArrayList<E> (set);

        m_DataReversed = new ArrayList<>(m_Data);
        m_DataReversed.sort(comparator.reversed());

    }

    public ArraySet(Collection<E> collection){
        this(collection, (o1, o2) -> ((Comparable<E>)o1).compareTo(o2));
        m_DefaultOrder = true;
    }

    public ArraySet(List<E> data, List<E> dataReversed, Comparator<? super E> comparator, boolean defaultOrder ){
        this.m_Data = data;
        this.m_DataReversed = dataReversed;
        this.m_Comparator = comparator;
        this.m_DefaultOrder = defaultOrder;
    }

    // if is not present, value would have been inserted
    // at position next to nearest. So the function returns (-(pos_nearest)-1)
    private int indexOf(E item){
        return Collections.binarySearch(m_Data, item, m_Comparator);
    }

    //Returns the greatest element in this set strictly less than the given element, or null if there is no such element.
    // < item
    @Override
    public E lower(E item) {

        int index = indexOf(item);
        if((index != 0)&&(index != -1)){
            if(index < 0){
                index = -(index + 1);
            }
            return m_Data.get(index - 1);
        }else{
            return null;
        }
    }

    //Returns the greatest element in this set less than or equal to the given element, or null if there is no such element.
    //  <= item
    @Override
    public E floor(E item) {

        int index = indexOf(item);
        if(index >= 0){
            return m_Data.get(index);
        }else{
            index = -(index + 1);
            if(index == 0){
                return null;
            }else{
                return m_Data.get(index - 1);
            }
        }
    }

    //Returns the least element in this set greater than or equal to the given element, or null if there is no such element.
    // >=  item
    @Override
    public E ceiling(E item) {

        int index = indexOf(item);
        if(index >= 0){
            return m_Data.get(index);
        }else{
            index = -(index + 1);

            if(index == m_Data.size()){
                return null;
            }else{
                return m_Data.get(index);
            }
        }
    }

    //Returns the least element in this set strictly greater than the given element, or null if there is no such element.
    // > item
    @Override
    public E higher(E item) {

        int index = indexOf(item);
        if(index >= 0){
            if(index == m_Data.size() - 1){
                return null;
            }else{
                return m_Data.get(index + 1);
            }
        }else{
            index = -(index + 1);
            if(index == m_Data.size()){
                return null;
            }else{
                return m_Data.get(index);
            }
        }
    }

    /**Returns a view of the portion of this set whose elements are greater than (or equal to, if inclusive is true) fromElement.
     * The returned set is backed by this set, so changes in the returned set are reflected in this set, and vice-versa.
     * The returned set supports all optional set operations that this set supports.
     *
     * >=
     */
    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        int index = indexOf(fromElement);

        if(index < 0){
            index = -(index + 1);
        }else{
            if(!inclusive){
                index += 1;
            }
        }

        return new ArraySet<E>(m_Data.subList(index, size()), m_DataReversed.subList(0, size() - index),  this.m_Comparator, m_DefaultOrder);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    /**Returns a view of the portion of this set whose elements are strictly less than toElement.
     * The returned set is backed by this set, so changes in the returned set are reflected in this set, and vice-versa.
     * The returned set supports all optional set operations that this set supports.
     *
     *  <
     *
     * */
    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        int index = indexOf(toElement);

        if(index < 0){
            index = -(index + 1);
        }else{
            if(inclusive){
                index += 1;
            }
        }

        return new ArraySet<E>(m_Data.subList(0, index), m_DataReversed.subList(size() - index, size()), this.m_Comparator, this.m_DefaultOrder);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    /**Returns a view of the portion of this set whose elements range from fromElement to toElement.
     *  If fromElement and toElement are equal, the returned set is empty unless fromInclusive and toInclusive are both true.
     *  The returned set is backed by this set, so changes in the returned set are reflected in this set, and vice-versa.
     *  The returned set supports all optional set operations that this set supports.*/
    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) throws IllegalArgumentException {
        int fromPos = indexOf(fromElement);
        int fromIndex = fromPos < 0 ? (-fromPos -1) : fromPos;

        int toPos = indexOf(toElement);
        int toIndex = toPos < 0 ? (-toPos -1) : toPos;

        if (fromIndex > toIndex)
        {
            throw new IllegalArgumentException("fromElement > toElement");
        }

        return headSet(toElement, toInclusive).tailSet(fromElement, fromInclusive);
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {

        return subSet(fromElement, true, toElement, false);
    }

    /**Returns an iterator over the elements in this set, in descending order.*/
    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }


    @Override
    public int size() {
        return m_Data.size();
    }

    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(m_Data, (E) o, m_Comparator) >= 0;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            Iterator<E> it = m_Data.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public E next() {
                return it.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<E>(this.m_DataReversed, this.m_Data, Collections.reverseOrder(m_Comparator), false);
    }


    @Override
    public Comparator<? super E> comparator() {
        if(m_DefaultOrder){
            return null;
        }else{
            return m_Comparator;
        }
    }

    @Override
    public E first() {
        if (m_Data.size() != 0)
            return m_Data.get(0);
        else
            throw new NoSuchElementException();
    }

    @Override
    public E last() {
        if (m_Data.size() != 0)
            return m_Data.get(m_Data.size() - 1);
        else
            throw new NoSuchElementException();
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException("Not Available");
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException("Not Available");
    }

}
