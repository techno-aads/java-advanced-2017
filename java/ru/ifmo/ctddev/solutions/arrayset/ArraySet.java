import java.lang.reflect.Array;
import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {
	
    List<E> list;
    List<E> revlist;
    Comparator<? super E> comparator;
    boolean value = false;

    public ArraySet() {
    	list = new ArrayList<>();
    	revlist = new ArrayList<>();
        comparator = (Comparator<E>) (o1, o2) -> 0;
        value = true;
    }
    
    public ArraySet(Collection<E> collection0, Comparator<? super E> comparator0) {
        comparator = comparator0;
        ArrayList<E> arraylist_in = new ArrayList<>(collection0);
        ArrayList<E> arraylist_t = new ArrayList<>(arraylist_in.size());
        arraylist_in.sort(comparator0);
        
        if(arraylist_in.size() != 0) {
        	arraylist_t.add(arraylist_in.get(0));
        }
        
        for(int i = 1; i < arraylist_in.size(); i++) {
            if (comparator0.compare(arraylist_in.get(i), arraylist_in.get(i - 1)) != 0) {
            	arraylist_t.add(arraylist_in.get(i));
            }
        }
        
        arraylist_t.trimToSize();
        list = arraylist_t;
        revlist = new ArrayList<>(list);
        revlist.sort(comparator0.reversed());
    }
    
    public ArraySet(Collection<E> collection) {
        this(collection, (obj1, obj2) -> ((Comparable<E>)obj1).compareTo(obj2));
        value = true;
    }

    public ArraySet(List<E> list0, List<E> revlist0, Comparator<? super E> comparator0, boolean value0) {
    	list = list0;
        revlist = revlist0;
        comparator = comparator0;
        value = value0;
    }

    private int indexOf(E a) {
    	int Vreturn = Collections.binarySearch(list, a, comparator);
    	return Vreturn;
    }
    
    @Override
    public E ceiling(E a) {
        int i = indexOf(a);
        E Vreturn;
        if(i >= 0) {
        	Vreturn = list.get(i);
        }
        else {
            i = -(i + 1);
            if(i == list.size()) {
            	Vreturn = null;
            }
            else {
            	Vreturn = list.get(i);
            }
        }
        return Vreturn;
    }
    
    @Override
    public E floor(E a) {
        int i = indexOf(a);
        E Vreturn;
        if(i >= 0) {
        	Vreturn = list.get(i);
        }
        else{
            i = -(i + 1);
            if(i == 0) {
            	Vreturn = null;
            }
            else {
            	Vreturn = list.get(i - 1);
            }
        }
        return Vreturn;
    }
    
    @Override
    public E lower(E a) {
        int i = indexOf(a);
        E Vreturn;
        if((i != 0) && (i != -1)) {
            if(i < 0) {
                i = -(i + 1);
            }
            Vreturn = list.get(i - 1);
        }
        else {
        	Vreturn = null;
        }
        return Vreturn;
    }
    
    @Override
    public E higher(E a) {
        int i = indexOf(a);
        E Vreturn;
        if(i >= 0) {
            if(i == list.size() - 1) {
            	Vreturn = null;
            }
            else {
            	Vreturn = list.get(i + 1);
            }
        }
        else {
            i = -(i + 1);
            if(i == list.size()) {
            	Vreturn = null;
            }
            else {
            	Vreturn = list.get(i);
            }
        }
        return Vreturn;
    }
    
    @Override
    public SortedSet<E> tailSet(E from_elem) {
    	SortedSet<E> Vreturn = tailSet(from_elem, true);
        return Vreturn;
    }

    @Override
    public SortedSet<E> headSet(E to_elem) {
    	SortedSet<E> Vreturn = headSet(to_elem, false);
    	return Vreturn;
    }
    
    @Override
    public SortedSet<E> subSet(E from_elem, E to_elem) {
    	SortedSet<E> Vreturn = subSet(from_elem, true, to_elem, false);
        return Vreturn;
    }
    
    @Override
    public NavigableSet<E> tailSet(E from_elem, boolean flag_incl) {
        int i = indexOf(from_elem);
        if(i < 0) {
            i = -(i + 1);
        }
        else{
            if(flag_incl == false) {
                i += 1;
            }
        }
        return new ArraySet<E>(list.subList(i, size()), revlist.subList(0, size() - i), comparator, value);
    }

    @Override
    public NavigableSet<E> headSet(E to_elem, boolean flag_incl) {
        int i = indexOf(to_elem);
        if(i < 0) {
            i = -(i + 1);
        }
        else {
        	if(flag_incl == true) {
                i += 1;
            }
        }
        return new ArraySet<E>(list.subList(0, i), revlist.subList(size() - i, size()), comparator, value);
    }

    @Override
    public NavigableSet<E> subSet(E from_elem, boolean flag_incl_from, E to_elem, boolean flag_incl_to) {
    	NavigableSet<E> Vreturn = headSet(to_elem, flag_incl_to).tailSet(from_elem, flag_incl_from);
        return Vreturn;
    }
    
    @Override
    public NavigableSet<E> descendingSet() {
    	return new ArraySet<E>(revlist, list, Collections.reverseOrder(comparator), false);
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    public int size() {
    	int Vreturn = list.size();
        return Vreturn;
    }

    @Override
    public boolean isEmpty() {
    	boolean Vreturn = list.isEmpty();
        return Vreturn;
    }

    @Override
    public boolean contains(Object obj) {
        boolean Vreturn = true;
        if (Collections.binarySearch(list, (E) obj, comparator) < 0) {
        	Vreturn = false;
        }
        return Vreturn;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            Iterator<E> iter = list.iterator();

            @Override
            public boolean hasNext() {
            	boolean Vreturn = iter.hasNext();
                return Vreturn;
            }

            @Override
            public E next() {
            	E Vreturn = iter.next();
            	return Vreturn;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException("Unsupported operation");
    }
    
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Unsupported operation");
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    public Iterator<E> descendingIterator() {
    	Iterator<E> Vreturn = descendingSet().iterator();
    	return Vreturn;
    }

    @Override
    public Comparator<? super E> comparator() {
    	Comparator<? super E> Vreturn;
        if(value == true) {
        	Vreturn = null;
        }
        else {
        	Vreturn = comparator;
        }
        return Vreturn;
    }

    @Override
    public Object[] toArray() {
    	Object[] Vreturn = list.toArray();
    	return Vreturn;
    }

    @Override
    public <T> T[] toArray(T[] t) {
        T[] array = (T[])(new Object[list.size()]);
        Object[] obj = list.toArray();
        for(int i = 0; i < list.size(); i++) {
        	array[i] = (T) obj[i];
        }
        return array;
    }

    @Override
    public E first() {
    	E Vreturn;
        if (list.size() != 0) {
        	Vreturn = list.get(0);
        	return Vreturn;
            //return data.get(0);
        }
        else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public E last() {
    	E Vreturn;
        if (list.size() != 0) {
        	Vreturn = list.get(list.size() - 1);
        	return Vreturn;
        }
        else {
            throw new NoSuchElementException();
        }
    }

    public String toString() {
    	String Vreturn = list.toString();
    	return Vreturn;
    }
    
}
