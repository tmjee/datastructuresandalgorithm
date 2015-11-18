package conc;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReferenceArray;

import static java.lang.String.format;

/**
 * make concurrent @see SynchronizedConcurrency paper
 * @author tmjee
 */
public class ConcurrentSkipListSet<E> extends AbstractSet<E> {

    private final int MAX_LEVELS = 3;

    private final Head<E> HEAD = new Head<>(MAX_LEVELS);
    private final Tail<E> TAIL = new Tail<>(MAX_LEVELS);


    public ConcurrentSkipListSet() {
        for (int a=0; a<=MAX_LEVELS; a++) {
            HEAD.n.set(a, TAIL);
            TAIL.n.set(a, null);
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new InternalIterator<>(HEAD, TAIL);
    }


    @Override
    public int size() {
        Node<E> c = HEAD.n.get(0);
        int size = 0;
        while(c != TAIL) {
            if (!c.d) {
                size++;
            }
            c = c.n.get(0);
        }
        return size;
    }

    @Override
    public boolean contains(Object o) {
        E e = (E)o;
        Node<E>[] p = path(e);
        if (p[0] == HEAD)
            return false;
        return (p[0].v.equals(e));
    }


    @Override
    public boolean remove(Object o) {
        E e = (E) o;
        Node<E>[] p = path(e);
        if (p[0] == HEAD)
            return false;
        else if (compare(p[0].v, e)==0) {
            p[0].d = true;
            return true;
        }
        return false;
    }



    @Override
    public boolean add(E e) {
        Node<E> p[] = path(e);
        if (p[0] == HEAD || compare(p[0].v, e) != 0) {
            Node<E> t = new Node<>(e, random(MAX_LEVELS));
            for (int a=0;a<t.n.length();a++) {

                Node<E> _n = p[a].n.get(a);

                t.n.set(a, _n);
                p[a].n.compareAndSet(a, _n, t);
            }
            return true;
        }
        return false;
    }


    public Node<E>[] path(E e) {
        Node<E> p[] = new Node[MAX_LEVELS+1];
        Node<E> c = HEAD;
        for (int a=MAX_LEVELS; a>=0; a--) {
            Node<E> t = c.n.get(a);

            while(true) {
                if (t == TAIL) {
                    p[a] = c;
                    break;
                } else if (t.d) {
                    t = t.n.get(a);
                } else  {
                    int r = compare(t.v, e);
                    if (r == 0) {
                        p[a] = t;
                        c = t;
                        break;
                    } else if (r < 0) {
                        c = t;
                        t = t.n.get(a);
                    } else {
                        p[a] = c;
                        break;
                    }
                }
            }
        }
        return p;
    }

    private int compare(E e1, E e2) {
        if (e1==e2)
            return 0;
        if (e1 == null && e2 != null)
            return -1;
        if (e1 != null && e2 == null)
            return 1;
        return ((Comparable<E>)e1).compareTo(e2);
    }

    private int random(int max) {
        return ThreadLocalRandom.current().nextInt(max+1);
    }



    private static class Node<E> {
        final E v;
        final AtomicReferenceArray<Node<E>> n;
        volatile boolean d;
        volatile Node<E> a;

        private Node(E v, int levels) {
            this.v = v;
            this.n = new AtomicReferenceArray<Node<E>>(new Node[levels+1]);
            d = false;
        }
    }

    private final static class Head<E> extends Node<E> {
        private Head(int levels) {
            super(null,levels);
        }
    }

    private final static class Tail<E> extends Node<E> {
        private Tail(int levels) {
            super(null, levels);
        }
    }



    private final static class InternalIterator<E> implements Iterator<E> {

        private final Head<E> HEAD;
        private final Tail<E> TAIL;
        private volatile Node<E> curr;
        private volatile Node<E> prev;
        private volatile Node<E> next;

        public InternalIterator(Head<E> head, Tail<E> tail) {
            this.HEAD = head;
            this.TAIL = tail;
            this.curr = HEAD;
        }

        @Override
        public boolean hasNext() {
            _next();
            return (next != null);
        }

        @Override
        public E next() {
            _next();
            if (next == null) {
                throw new NoSuchElementException("No such element");
            }
            prev = next;
            next = null;
            return prev.v;
        }

        @Override
        public void remove() {
            if (prev == null) {
                throw new IllegalStateException("next method has not been called");
            }
            prev.d =true;
        }

        private void _next() {
            if (next == null && curr != TAIL) {
                Node<E> c = curr.n.get(0);
                while(c != TAIL && c.d) {
                    c = c.n.get(0);
                }
                if (c != TAIL && (!c.d)) {
                    next = c;
                } else {
                    next = null;
                }
                curr = c;
            }
        }
    }
}
