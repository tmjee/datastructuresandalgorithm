package seq;

import org.junit.Test;

public class MySkipListSetTest {


    @Test
    public void test() throws Exception {


        MySkipListSet<Integer> s = new MySkipListSet<>();
        s.add(1);
        s.add(2);

        s.print();
    }

}
