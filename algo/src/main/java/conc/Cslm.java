package conc;

import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Created by tmjee on 25/11/15.
 */
public class Cslm<E> extends AbstractSet<E> {

    private final HeadIndex<E> head;

    private static AtomicReferenceFieldUpdater<Cslm, HeadIndex> updater =
        AtomicReferenceFieldUpdater.newUpdater(Cslm.class, HeadIndex.class, "head");


    public Cslm() {
        head = new HeadIndex<E>(1, null, null, null);
    }

    boolean casHead(HeadIndex<E> expected, HeadIndex<E> current) {
        return updater.compareAndSet(this, expected, current);
    }


    @Override
    public boolean add(E e) {
        return doPut(e);
    }

    @Override
    public boolean remove(Object o) {
        return doRemove((E)o);
    }

    @Override
    public boolean contains(Object o) {
        return super.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    private boolean doRemove(E e) {
        E v = null;
        Node<E> f = null;
        outer: for(;;) {
            for (Node<E> b = findPredecessor(e), n = b.r; ; ) {
                if (n == null) { //  no next
                    break outer;
                }
                if (b.r != n) { // inconsistent read
                    break ;
                }
                v = n.v;
                f = n.r;
                if (n.isDeleted()) { // n is deleted and may not  be marked yet
                    n.helpDeleteThisNode(b,f);
                    break;
                }
                if (b.isDeleted() || n.isMarker()) { // be is deleted
                    break;
                }
                int c = compare(e, v);
                if (c == 0) {

                }

            }
        }
        return false;
    }


    private boolean doPut(E e) {
        boolean added = false;

        // find and put Node in
        Node<E> z = null;
        outer:for(;;) {
            for (Node<E> b = findPredecessor(e), n = b.r; ;) {
                if (n != null) {
                    E v = n.v;
                    Node<E> f = n.r;

                    if (b.r != n) {
                        break; // inconsistent read
                    }
                    if (n.isDeleted()) { // deleted but might not be marked
                        n.helpDeleteThisNode(b,f);
                        break;
                    }
                    if (b.isDeleted() || n.isMarker()) { // b delete
                        break;
                    }


                    int c = 0;
                    if (compare(e,n.v) == 0) {
                       return false;
                    }
                }

                // new Node
                z = new Node<E>(e,n);
                if(!b.casN(n,z)) {
                    break;
                }
                break outer;
            }
        }

        // build Index
        int rnd = ThreadLocalRandom.current().nextInt(10);
        int level=1, max;
        while(rnd >5) {
            level++;
            rnd = ThreadLocalRandom.current().nextInt(10);
        }

        HeadIndex h = head;
        max = h.level;
        Index<E> idx = null;
        if (level <= max) {
            for (int a=1; a<=level; a++) {
                idx = new Index<E>(z, null, idx);
            }

        } else {
            level = max + 1;

            Index[] idxs = new Index[level + 1];

            for (int a = 1; a <= level; a++) {
                idx = idxs[a] = new Index<E>(z, null, idx);
            }

            for (; ; ) {
                h = head;
                int oldLevel = h.level;
                if (level <= oldLevel) {
                    break;
                }
                HeadIndex<E> hdx = h;
                for (int j = oldLevel + 1; j <= level; j++) {
                    hdx = new HeadIndex<>(j, z, idxs[j], hdx);
                }
                if(casHead(h, hdx)) {
                   h=hdx;
                    idx = idxs[level = oldLevel];
                    break;
                }
            }
        }

        // put index into position
        splice: for (int insertionLevel = level;;) {
            int j = h.level;
            for (Index<E> q = head, r = q.r, t = idx; ; ) {
                if (r == null || q == null) {
                    break splice;
                }
                if (r != null) {
                    Node<E> n=r.n;
                    int c = compare(e, n.v);
                    if (c>0) {
                       q = r;
                        r = q.r;
                        continue;
                    }
                }

                if (insertionLevel == j) {
                    if(!(added=q.link(r, t))) {
                        break;
                    }
                    if (t.n.isDeleted()) {
                        findNode(e);
                        break splice;
                    }
                    if (--insertionLevel == 0) {
                        break splice;
                    }
                }

                if (--j >= insertionLevel && j < level)
                    t = t.d;

                q = q.d;
                r = q.r;
            }
        }
        return added;
    }



    public Node<E> findPredecessor(E e) {
        for (;;) {
            for (Index<E> q = head, r = q.r, d; ; ) {
                if (r != null) {
                    Node<E> n = r.n;
                    if (n == null) { // index deleted
                        if (!q.unlink(r)) {
                            break;
                        }
                        r = q.r;
                        continue;
                    }
                    if (compare(e, n.v)>0) {
                        q = r;
                        r = q.r;
                        continue;
                    }
                }
                if ((d=q.d) == null) {
                    return q.n;
                }
                q = q.d;
                r = q.r;
            }
        }
    }

    public Node<E> findNode(E e) {
        outer:
        for (;;) {
            for (Node<E> b = findPredecessor(e), n = b.r; ; ) {
                if (n == null) {
                    break outer;
                }
                Node<E> f = n.r;
                E v = n.v;

                if (b.r != n) { // inconsistent read
                    break;
                }
                if (b.isDeleted()) {        // n deleted, but may not be marked
                    n.helpDeleteThisNode(b, f);
                    break;
                }
                if (b.isDeleted() && n.isMarker()) { // b deleted and marked
                   break;
                }
                int c = 0;
                if ((c=compare(e, n.v))==0) {
                   return n;
                }
                if (c<0) {
                    break outer;
                }
                b = n;
                n = f;
            }
        }
        return null;
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



    static class Node<E> {
        final E v;
        volatile Node<E> r;
        volatile boolean d;

        static AtomicReferenceFieldUpdater<Node, Node> updater =
                AtomicReferenceFieldUpdater.<Node, Node>newUpdater(Node.class, Node.class, "n");

        Node(E v, Node<E> r) {
            this.v = v;
            this.r = r;
            this.d = false;
        }


        boolean casN(Node<E> expected, Node<E> update) {
            return updater.compareAndSet(this, expected, update);
        }

        boolean isMarker() {
            return false;
        }

        boolean isDeleted() {
            return d;
        }

        boolean appendMarkerOnThisNode(Node<E> expected) {
            return updater.compareAndSet(this, expected, new Marker<E>(expected));
        }

        void helpDeleteThisNode(Node<E> predecessor, Node<E> successor) {
            if (predecessor.r == this && r == successor) {
                if (successor == null || (!successor.isMarker())) {
                    appendMarkerOnThisNode(successor);
                } else {
                    predecessor.casN(this, successor.r);
                }
            }
        }

        E getValidValue() {
            E e = v;
            if (e != null) {
                return e;
            }
            return null;
        }
    }

    static class Marker<E> extends Node<E> {
        Marker(Node<E> r) { // maker node
            super(null, r);
        }
        @Override
        boolean isMarker() {
            return true;
        }
        @Override
        boolean isDeleted() {
            return false;
        }
    }





    static class Index<E> {
        final Node<E> n;
        volatile Index<E> r;
        final Index<E> d;

        static AtomicReferenceFieldUpdater<Index, Index> updater =
                AtomicReferenceFieldUpdater.<Index, Index>newUpdater(Index.class, Index.class, "right");


        Index(Node<E> n, Index<E> r, Index<E> d) {
            this.n = n;
            this.r = r;
            this.d = d;
        }

        boolean casR(Index<E> expected, Index<E> current) {
            return updater.compareAndSet(this, expected, current);
        }

        boolean isNDeleted() {
            return (n == null);
        }

        boolean link(Index<E> successor, Index<E> newSuccessor) {
            Node<E> n = this.n;
            newSuccessor.r = successor;
            return n != null &&  casR(successor, newSuccessor);
        }


        boolean unlink(Index<E> successor) {
            return !isNDeleted() && casR(successor, successor.r);
        }
    }


    static class HeadIndex<E> extends Index<E> {
        final int level;
        HeadIndex(int level, Node<E> n, Index<E> r, Index<E> d) {
            super(n, r, d);
            this.level = level;
        }
    }


    static class ComparableUsingComparator<E> implements Comparable<E> {
        private final E e;
        private final Comparator<E> comparator;

        ComparableUsingComparator(E e, Comparator<E> comparator) {
            this.e =e;
            this.comparator = comparator;
        }

        @Override
        public int compareTo(E o) {
            return comparator.compare(e, o);
        }
    }
}

