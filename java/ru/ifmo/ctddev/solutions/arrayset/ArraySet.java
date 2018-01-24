package ru.ifmo.ctddev.solutions.arrayset;

import java.util.*;

//fixme: change class signature
public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {

    private final List<E> elements;
    private final List<E> reverseElements;
    private final Comparator<? super E> comparator;
    private boolean sorted;

    public ArraySet() {
        elements = new ArrayList<>();
        reverseElements = new ArrayList<>();
        comparator = (Comparator<E>) (o1, o2) -> 0;
        sorted = false;
    }

    @SuppressWarnings("unchecked")
    public ArraySet(Collection<E> collection) {
        this(collection, (o1, o2) -> ((Comparable<? super E>) o1).compareTo(o2));
        sorted = false;
    }

    public ArraySet(Collection<E> collection, Comparator<? super E> comparator) {
        List<E> list = new ArrayList<>(collection);
        list.sort(comparator);
        List<E> copyList = new ArrayList<>(list.size());

        if (list.size() > 0) {
            copyList.add(list.get(0));
        }

        for (int i = 1; i < list.size(); ++i) {
            if (comparator.compare(list.get(i), list.get(i - 1)) != 0) {
                copyList.add(list.get(i));
            }
        }
        ((ArrayList) copyList).trimToSize();

        this.elements = copyList;
        this.comparator = comparator;
        this.sorted = true;
        this.reverseElements = new ArrayList<>(copyList);
        Collections.reverse(this.reverseElements);
    }

    public ArraySet(List<E> elements, List<E> reverseElements, Comparator<? super E> comparator, boolean sorted) {
        this.elements = elements;
        this.reverseElements = reverseElements;
        this.comparator = comparator;
        this.sorted = sorted;
    }

    private int find(E e) {
        return Collections.binarySearch(elements, e, comparator);
    }

    @Override
    public E lower(E e) {
        int idx;
        if ((idx = find(e)) >= 0) {
            return (idx > 0) ? elements.get(idx - 1) : null;
        }

        idx = -(idx + 1);
        return (idx == 0) ? null : elements.get(idx - 1);
    }

    @Override
    public E floor(E e) {
        int idx;
        if ((idx = find(e)) >= 0) {
            return elements.get(idx);
        }

        idx = -(idx + 1);
        return (idx == 0) ? null : elements.get(idx - 1);
    }

    @Override
    public E ceiling(E e) {
        int idx = find(e);
        if (idx >= 0) {
            return elements.get(idx);
        }

        idx = -(idx + 1);
        return (idx == elements.size()) ? null : elements.get(idx);
    }

    @Override
    public E higher(E e) {
        int idx;
        if ((idx = find(e)) >= 0) {
            return (idx < elements.size() - 1) ? elements.get(idx + 1) : null;
        }

        idx = -(idx + 1);
        return (idx == elements.size()) ? null : elements.get(idx);
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
        return new Iterator<E>() {
            Iterator<E> it = elements.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public E next() {
                return it.next();
            }
        };
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<>(this.reverseElements, this.elements, Collections.reverseOrder(comparator), sorted);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return headSet(toElement, toInclusive).tailSet(fromElement, fromInclusive);
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        int idx = find(toElement);
        if (idx < 0) {
            idx = (-(idx + 1)) - 1;
        } else if (!inclusive) {
            --idx;
        }
        
        return new ArraySet<>(elements.subList(0, idx + 1), reverseElements.subList(size() - idx - 1, size()), comparator, sorted);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        int idx = find(fromElement);
        if (idx < 0) {
            idx = -(idx + 1);
        } else if (!inclusive) {
            ++idx;
        }

        return new ArraySet<>(elements.subList(idx, elements.size()), reverseElements.subList(0, size() - idx), comparator, sorted);
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public Comparator<? super E> comparator() {
        return sorted ? comparator : null;
    }

    @Override
    public E first() {
        if (elements.size() > 0) {
            return elements.get(0);
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public E last() {
        if (elements.size() > 0) {
            return elements.get(elements.size() - 1);
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return Collections.binarySearch(elements, (E) o, comparator) >= 0;
    }
}
