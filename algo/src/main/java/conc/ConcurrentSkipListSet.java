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

    private final int MAX_LEVELS = 64;

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
            if (!c.d && (!(c instanceof Marker))) {
                size++;
            }
            c = c.n.get(0);
        }
        return size;
    }

    public void print() {
        Node<E> c = HEAD;
        while( c!= null) {
            for (int a=0; a<c.n.length(); a++) {
                System.out.print(format("%s/%s(%s)\t", c.v==null?"<n>":c.v, c.d ?"y":"n", a));
            }
            System.out.println();
            c = c.n.get(0);
        }
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
            addMarker(p[0]);
            return true;
        }
        return false;
    }


    private void addMarker(Node<E> n) {
        Marker<E> m = new Marker<>(null, n.n.length()-1);

        for (int a=0; a<m.n.length(); a++) {
            Node<E> x = n.n.get(a);
            m.n.compareAndSet(a, null, x);
            n.n.compareAndSet(a, x, m);
        }
    }



    @Override
    public boolean add(E e) {
        boolean retry = false;
        do {
            Node<E> p[] = path(e);
            if (p[0] == HEAD || compare(p[0].v, e) != 0) {
                Node<E> t = new Node<>(e, random(MAX_LEVELS));

                Node<E> _n = p[0].n.get(0);
                t.n.set(0, _n);
                retry = (!p[0].n.compareAndSet(0, _n, t));

                if (!retry) {
                    for (int a = 1; a < t.n.length(); a++) {
                        boolean retry2 = false;

                        do {
                            _n = p[a].n.get(a);

                            t.n.set(a, _n);
                            retry2 = (!p[a].n.compareAndSet(a, _n, t));
                            if (retry2) {
                                p = path(e);
                            }
                        } while(retry2);
                    }
                    return true;
                }
            }
        } while(retry);
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
                } else if (t instanceof Marker) {
                    t = t.n.get(a);
                } else if (t.d) {
                    Node<E> m = t.n.get(a);
                    if (!(m instanceof Marker)) {
                        addMarker(t);
                    } else {
                        c.n.compareAndSet(a, t, m.n.get(a));
                    }
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
        ThreadLocalRandom r= ThreadLocalRandom.current();
        int level = 0;
        while ((r.nextInt(10) >= 5) && (level < max)) {
            level++;
        }
        return level;
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

    private final static class Marker<E> extends Node<E> {
        private Marker(E v, int levels) {
            super(v, levels);
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
