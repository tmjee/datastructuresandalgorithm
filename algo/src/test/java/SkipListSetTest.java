import org.junit.Test;
import seq.SkipListSet;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SkipListSetTest {

    @Test
    public void test() throws Exception {
        Set<Integer> set = new SkipListSet<>();
        System.out.println("add 1="+set.add(1));
        System.out.println("add 2="+set.add(2));

        System.out.println("size="+set.size());

        System.out.println("contains 1 ="+set.contains(1));
        System.out.println("contains 2 ="+set.contains(2));
        System.out.println("contains 3 ="+set.contains(3));


        /*assertEquals(set.size(), 2);
        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
       */
    }
}
