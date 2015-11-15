package seq;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

public class SkipListSet<E> extends AbstractSet<E> {

    private final int MAX_LEVEL = 16;
    private final Node HEAD = new Node(null, MAX_LEVEL);
    private final Node TAIL = new Node(null, MAX_LEVEL);


    public SkipListSet() {
        for (int a=0; a<MAX_LEVEL; a++) {
            HEAD.next[a] = TAIL;
            TAIL.next[a] = TAIL;
        }
    }



    @Override
    public boolean contains(Object o) {
        E e = (E)o;
        Node<E>[] p = search(e);
        if (compare(p[0].v,e)==0) {
            return true;
        }
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return null;
    }

    @Override
    public int size() {
        int size=0;
        Node current = HEAD;
        while(current.next[0] != TAIL) {
            size++;
            current = current.next[0];
        }
        return size;
    }

    @Override
    public boolean add(E e) {
        Node<E>[] p = search(e);
        if (compare(p[0].v, e)==0) {
            return false;
        }
        Node<E> n = new Node<E>(e, randomHeight());
        for (int a=0; a<n.next.length; a++) {
            n.next[a] = p[a].next[a];
            p[a].next[a] = n;
        }
        return true;
    }


    // ============ private

    private Node<E>[] search(E e) {
        Node<E>[] path = new Node[MAX_LEVEL+1];
        Node<E> current = HEAD;
        for (int a=MAX_LEVEL; a>=0; a--) {
            if (current != TAIL) {
                int c = compare(current.v, e);
                if (c < 0) { // less than
                    current = current.next[a];
                }
            }
            path[a] = current;
        }
        return path;
    }


    private int compare(E e1, E e2) {
        if (e1 instanceof  Comparable && e2 instanceof  Comparable) {
            return ((Comparable)e1).compareTo((Comparable)e2);
        }
        return 0;
    }

    private int randomHeight() {
        return ThreadLocalRandom.current().nextInt(MAX_LEVEL+1);
    }

    // ============= inner class

    private static class Node<E>  {
        E v;
        Node[] next;

        Node(E v, int maxLevel) {
            this.v = v;
            next = new Node[maxLevel+1];
        }
    }
}
