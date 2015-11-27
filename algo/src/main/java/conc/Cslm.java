package conc;

import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Created by tmjee on 25/11/15.
 */
public class Cslm<E> extends AbstractSet<E> {

    private final HeadIndex<E> head;

    public Cslm() {
        head = new HeadIndex<E>()
    }




    @Override
    public Iterator<E> iterator() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }




    public Node<E> findPredecessor() {

    }





    static final class Node<E> {
        final E v;
        volatile Node<E> n;

        static AtomicReferenceFieldUpdater<Node, Node> updater =
                AtomicReferenceFieldUpdater.<Node, Node>newUpdater(Node.class, Node.class, "n");

        Node(E v, Node<E> n) {
            this.v = v;
            this.n = n;
        }

        Node(Node<E> n) { // maker node
            this(null, n);
        }

        boolean casN(Node<E> expected, Node<E> update) {
            return updater.compareAndSet(this, expected, update);
        }

        boolean isMarker() {
            return (v == null);
        }

        boolean appendMarkerOnThisNode(Node<E> expected) {
            return updater.compareAndSet(this, expected, new Node<E>(expected));
        }

        void helpDeleteThisNode(Node<E> predecessor, Node<E> successor) {
            if (predecessor.n == this && n == successor) {
                if (successor == null || (!successor.isMarker())) {
                    appendMarkerOnThisNode(successor);
                } else {
                    predecessor.casN(this, successor.n);
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

