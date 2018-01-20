package ru.ifmo.ctddev.solutions.arrayset;

import java.lang.reflect.Array;
import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E>{

    List<E> data;
    List<E> dataReversed;
    Comparator<? super E> comparator;
    boolean defaultOrder = false;

    public ArraySet(Collection<E> collection, Comparator<? super E> comparator){
        this.comparator = comparator;
        ArrayList<E> inputData = new ArrayList<>(collection);
        ArrayList<E> tempData = new ArrayList<>(inputData.size());
        inputData.sort(comparator);
        if(inputData.size() != 0){
            tempData.add(inputData.get(0));
        }
        for(int i = 1; i< inputData.size();i++){
            if (comparator.compare(inputData.get(i), inputData.get(i - 1)) != 0) {
                tempData.add(inputData.get(i));
            }
        }
        tempData.trimToSize();
        data = tempData;
        dataReversed = new ArrayList<>(data);
        dataReversed.sort(comparator.reversed());
    }

    public ArraySet(Collection<E> collection){
        this(collection, (o1, o2) -> ((Comparable<E>)o1).compareTo(o2));
        defaultOrder = true;
    }

    public ArraySet(){
        data = new ArrayList<>();
        dataReversed = new ArrayList<>();
        comparator = (Comparator<E>) (o1, o2) -> 0;
        defaultOrder = true;
    }

    public ArraySet(List<E> data, List<E> dataReversed, Comparator<? super E> comparator, boolean defaultOrder ){
        this.data = data;
        this.dataReversed = dataReversed;
        this.comparator = comparator;
        this.defaultOrder = defaultOrder;
    }

    private int indexOf(E item){

        return Collections.binarySearch(data, item, comparator);

    }


    // ----------------------------------------

    @Override
    public E lower(E item) {
        int index = indexOf(item);
        if((index != 0)&&(index != -1)){

            if(index < 0){
                index = -(index + 1);
            }

            return data.get(index - 1);

        }else{
            return null;
        }


    }

    @Override
    public E ceiling(E item) {
        int index = indexOf(item);
        if(index >= 0){

            return data.get(index);
        }else{
            index = -(index + 1);

            if(index == data.size()){
                return null;
            }else{
                return data.get(index);
            }

        }


    }

    @Override
    public E higher(E item) {
        int index = indexOf(item);
        if(index >= 0){
            if(index == data.size() - 1){
                return null;
            }else{
                return data.get(index + 1);
            }
        }else{
            index = -(index + 1);
            if(index == data.size()){
                return null;
            }else{
                return data.get(index);
            }
        }
    }

    @Override
    public E floor(E item) {
        int index = indexOf(item);
        if(index >= 0){

            return data.get(index);

        }else{
            index = -(index + 1);
            if(index == 0){
                return null;
            }else{
                return data.get(index - 1);
            }
        }
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

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

        return new ArraySet<E>(data.subList(index, size()), dataReversed.subList(0, size() - index),  this.comparator, defaultOrder);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

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

        return new ArraySet<E>(data.subList(0, index), dataReversed.subList(size() - index, size()), this.comparator, this.defaultOrder);
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return headSet(toElement, toInclusive).tailSet(fromElement, fromInclusive);
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException("nenado");
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException("nenado");
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(data, (E) o, comparator) >= 0;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            Iterator<E> it = data.iterator();

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
    public boolean add(E e) {
        throw new UnsupportedOperationException("nenado");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("nenado");
    }
    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException("nenado");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("nenado");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("nenado");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("nenado");
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<E>(this.dataReversed, this.data, Collections.reverseOrder(comparator), false);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public Comparator<? super E> comparator() {
        if(defaultOrder){
            return null;
        }else{
            return comparator;
        }
    }

    @Override
    public Object[] toArray() {
        return data.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {

        T[] arr = (T[])(new Object[data.size()]);
        Object[] dd = data.toArray();

        for(int i = 0; i<data.size(); i++){
            arr[i] = (T) dd[i];
        }
        return arr;
    }

    @Override
    public E first() {
        if (data.size() != 0)
            return data.get(0);
        else
            throw new NoSuchElementException();
    }

    @Override
    public E last() {
        if (data.size() != 0)
            return data.get(data.size() - 1);
        else
            throw new NoSuchElementException();
    }

    public String toString(){
        return data.toString();
    }
}

