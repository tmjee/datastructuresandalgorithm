package seq;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.String.format;

public class MySkipListSet<E> {

    private final int MAX_LEVELS = 3;

    private final Head<E> HEAD = new Head<>(MAX_LEVELS);
    private final Tail<E> TAIL = new Tail<>(MAX_LEVELS);


    public MySkipListSet() {
        for (int a=0; a<=MAX_LEVELS; a++) {
            HEAD.n[a] = TAIL;
            TAIL.n[a] = null;
        }
    }


    public boolean add(E e) {
        System.out.println("*** add");
        Node<E> p[] = path(e);
        System.out.println(p[0]);
        if (p[0] == HEAD || compare(p[0].v, e) != 0) {
            Node<E> t = new Node<>(e, random(MAX_LEVELS));
            for (int a=0;a<t.n.length;a++) {
                t.n[a] = p[a].n[a];
                p[a].n[a] = t;
            }
            return true;
        }
        return false;
    }


    public Node<E>[] path(E e) {
        System.out.println("path "+e);
        Node<E> p[] = new Node[MAX_LEVELS+1];
        Node<E> c = HEAD;
        for (int a=MAX_LEVELS; a>=0; a--) {
            while(true) {
                Node<E> t = c.n[a];
                if (t == TAIL) {
                    p[a] = c;
                    break;
                } else if (compare(c.n[a].v, e)<=0){
                    c = c.n[a];
                } else {
                    p[a] = c;
                    break;
                }
            }
        }
        return p;
    }

    private int compare(E e1, E e2) {
        return ((Comparable<E>)e1).compareTo(e2);
    }

    private int random(int max) {
        return ThreadLocalRandom.current().nextInt(max+1);
    }


    public void print() {
        Node<E> c = HEAD;
        while( c!= null) {
            for (int a=0; a<=c.n.length; a++) {
                System.out.print(c.v+"("+a+")\t");
            }
            System.out.println();
            c = c.n[0];
        }
    }




    private static class Node<E> {
        final E v;
        final Node<E>[] n;

        private Node(E v, int levels) {
            this.v = v;
            this.n = new Node[levels+1];
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
}
