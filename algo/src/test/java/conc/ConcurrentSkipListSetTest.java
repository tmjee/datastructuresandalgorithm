package conc;


import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConcurrentSkipListSetTest {

    private ConcurrentSkipListSet<Integer> s;

    @Before
    public void before() {
       s = new ConcurrentSkipListSet<>();
    }


    @Test
    public void test_add() throws Exception {
        assertTrue(s.add(1));
        assertTrue(s.add(2));
        assertFalse(s.add(2));
    }

    @Test
    public void test_size() throws Exception {
        s.add(1);
        s.add(2);
        s.add(2);

        assertEquals(s.size(), 2);
    }

    @Test
    public void test_contains() throws Exception {
        s.add(3);
        s.add(2);
        s.add(1);


       assertTrue(s.contains(1));
      assertTrue(s.contains(2));
        assertTrue(s.contains(3));
        assertFalse(s.contains(4));
    }

    @Test
    public void test_remove() throws Exception {
       s.add(3);
        s.add(2);
        s.add(1);
        s.add(4);
        s.add(5);

        assertEquals(s.size(), 5);
        assertTrue(s.remove(2));
        assertFalse(s.remove(6));
        assertEquals(s.size(), 4);

        assertTrue(s.remove(1));
        assertEquals(s.size(), 3);

        assertTrue(s.contains(3));
        assertTrue(s.contains(4));
        assertTrue(s.contains(5));
        assertFalse(s.contains(2));
        assertFalse(s.contains(1));
        assertFalse(s.contains(6));

    }


    @Test
    public void test_iterator() throws Exception {
        s.add(3);
        s.add(2);
        s.add(1);
        s.add(4);
        s.add(5);

        Iterator<Integer> i = s.iterator();

        assertTrue(i.hasNext());
        assertTrue(i.hasNext());
        assertEquals(i.next(), Integer.valueOf(1));
        assertEquals(i.next(), Integer.valueOf(2));
        assertTrue(i.hasNext());
        assertTrue(i.hasNext());
        assertEquals(i.next(), Integer.valueOf(3));
        assertTrue(i.hasNext());
        assertEquals(i.next(), Integer.valueOf(4));
        assertTrue(i.hasNext());
        assertEquals(i.next(), Integer.valueOf(5));
        assertFalse(i.hasNext());
        assertFalse(i.hasNext());
    }


    @Test
    public void test_iteratorRemove() throws Exception {

        s.add(3);
        s.add(2);
        s.add(1);
        s.add(4);
        s.add(5);

        Iterator<Integer> i = s.iterator();
        assertTrue(i.hasNext());
        assertEquals(i.next(), Integer.valueOf(1));
        i.remove();
        assertTrue(i.hasNext());
        assertEquals(i.next(), Integer.valueOf(2));
        assertTrue(i.hasNext());
        assertEquals(i.next(), Integer.valueOf(3));
        i.remove();
        assertTrue(i.hasNext());
        assertEquals(i.next(), Integer.valueOf(4));
        assertTrue(i.hasNext());
        assertEquals(i.next(), Integer.valueOf(5));
        i.remove();
        assertFalse(i.hasNext());

        assertEquals(s.size(), 2);

        i= s.iterator();
        assertTrue(i.hasNext());
        assertEquals(i.next(), Integer.valueOf(2));
        assertTrue(i.hasNext());
        assertEquals(i.next(), Integer.valueOf(4));
        assertFalse(i.hasNext());
    }

}
