package ru.ifmo.ctddev.solutions.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractCollection<E> implements NavigableSet<E> {

    private List<E> arrayList;
    private Comparator<? super E> comparator;

    public ArraySet() {
        arrayList = new ArrayList<>();
    }

    public ArraySet(Collection<E> sourceArray) {
        this(sourceArray, null);
    }

    public ArraySet(Collection<E> sourceArray, Comparator<? super E> comparator) {

        this.comparator = comparator;

        NavigableSet<E> treeSet = new TreeSet<>(comparator);
        treeSet.addAll(sourceArray);
        this.arrayList = new ArrayList<>(treeSet);
    }

    private ArraySet(List<E> sourceArray, Comparator<? super E> comparator){
        this.arrayList = sourceArray;
        this.comparator = comparator;
    }

    @Override
    public E lower(E e) {
        int index = Collections.binarySearch(arrayList, e, comparator);

        if ( index < 0) {
            index = -(index + 1);
        }

         index--;

        if (index >= 0 && index < arrayList.size()){
            return arrayList.get(index);
        }
        return null;
    }

    @Override
    public E floor(E e) {
        int index = Collections.binarySearch(arrayList,e,comparator);
        if (index < 0){
            index = -(index + 1) - 1;

        }

        if (index >= 0){
            return arrayList.get(index);
        }
        return null;
    }

    @Override
    public E ceiling(E e) {
        int index = Collections.binarySearch(arrayList, e, comparator);

        if (index < 0){
            index = -(index + 1);
        }

        if (index >= 0 && index < arrayList.size()){
            return arrayList.get(index);
        }

        return null;
    }

    @Override
    public E higher(E e) {
        int index = Collections.binarySearch(arrayList, e, comparator);

        if (index >= 0){
            index++;
        }
        else{
            index = - (index+1);
        }

        if (index >= 0 && index < arrayList.size()){
            return arrayList.get(index);
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
        return Collections.unmodifiableList(this.arrayList).iterator();
    }

    @Override
    public NavigableSet<E> descendingSet() {
        ArraySet<E> result = new ArraySet<>( new ArrayList<>(arrayList), Collections.reverseOrder(comparator));
        Collections.reverse(result.arrayList);
        return  result;
    }

    @Override
    public Iterator<E> descendingIterator() {
        ArrayList<E> resArrayList = new ArrayList<>(arrayList);
        Collections.reverse(resArrayList);
        return Collections.unmodifiableList(resArrayList).iterator();
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        int fromIndex = Collections.binarySearch(arrayList, fromElement, comparator);
        int toIndex = Collections.binarySearch(arrayList, toElement, comparator);

        if (fromIndex < 0){
            fromIndex = - (fromIndex+1);
        }
        else{
            if (!fromInclusive){
                fromIndex++;
            }
        }

        if (toIndex < 0){
            toIndex =- (toIndex + 1);
        }
        else{
            if (toInclusive) {
                toIndex++;
            }

        }

        if (fromIndex >= 0 && toIndex <= arrayList.size() && fromIndex <= toIndex) {
            ArraySet<E> result = new ArraySet<>();
            result.comparator = this.comparator;
            result.arrayList = arrayList.subList(fromIndex, toIndex );
            return  result;
        } else {
            return Collections.emptyNavigableSet();
        }

    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        int index = Collections.binarySearch(arrayList, toElement, comparator);

        if (index >= 0) {
            if (inclusive) {
                index++;
            }
        }
        else {
            index = - index  - 1;
        }

        if (index >= 0 && index <= arrayList.size()) {
            return new ArraySet<>(arrayList.subList(0, index),comparator);
        } else {
            return Collections.emptyNavigableSet();
        }
    }

    @Override
    public NavigableSet tailSet(E fromElement, boolean inclusive) {
        int index = Collections.binarySearch(arrayList, fromElement, comparator);

        if (index >= 0 ){
            if (!inclusive){
                index++;
            }
        }
        else{
            index = - (index+1);
        }

        if ( index >= 0 && index < arrayList.size()){
            return new ArraySet<>(arrayList.subList(index, arrayList.size()), comparator);
        }
        else{
            return Collections.emptyNavigableSet();
        }
    }

    @Override
    public NavigableSet subSet(E fromElement, E toElement) {
        return subSet(fromElement,true, toElement, false);
    }

    @Override
    public NavigableSet headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public NavigableSet tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public E last() {
        if (arrayList != null && !arrayList.isEmpty()) {
            return arrayList.get(arrayList.size() - 1);
        }

        throw new NoSuchElementException();
    }

    @Override
    public E first() {
        if (arrayList != null && !arrayList.isEmpty()) {
            return arrayList.get(0);
        }
        throw new NoSuchElementException();
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(arrayList, (E) o, comparator) >= 0;
    }
    @Override
    public int size() {
        return arrayList.size();
    }
}
