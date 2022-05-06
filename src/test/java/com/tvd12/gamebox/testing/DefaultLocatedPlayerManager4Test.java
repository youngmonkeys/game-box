package com.tvd12.gamebox.testing;

import com.tvd12.gamebox.entity.LocatedPlayer;
import com.tvd12.gamebox.manager.DefaultLocatedPlayerManager;
import com.tvd12.test.assertion.Asserts;
import org.testng.annotations.Test;

public class DefaultLocatedPlayerManager4Test {

    @Test
    public void test() {
        // given
        DefaultLocatedPlayerManager manager = new DefaultLocatedPlayerManager();
        LocatedPlayer binh5 = new LocatedPlayer("binh5"); // CHECK 5
        LocatedPlayer havm = new LocatedPlayer("havm"); // ALL_IN 1
        LocatedPlayer binh4 = new LocatedPlayer("binh4"); //CALL 4
        LocatedPlayer huongntm2 = new LocatedPlayer("huongntm2"); // ALL_IN 3
        LocatedPlayer huongntm3 = new LocatedPlayer("huongntm3"); // CALL 2

        manager.addPlayer(binh5, 0);
        manager.addPlayer(havm, 1);
        manager.addPlayer(huongntm3, 2);
        manager.addPlayer(huongntm2, 3);
        manager.addPlayer(binh4, 4);

        // when
        binh5.setLocation(0);
        havm.setLocation(1);
        huongntm3.setLocation(2);
        huongntm2.setLocation(3);
        binh4.setLocation(4);

        // then
        Asserts.assertEquals(manager.leftOf(huongntm2), huongntm3);
        Asserts.assertEquals(manager.leftOf(binh5, p -> p != huongntm2 && p != havm), binh4);
        Asserts.assertEquals(manager.leftOf(huongntm3, p -> p != huongntm2 && p != havm), binh5);
        Asserts.assertEquals(manager.leftOf(binh4, p -> p != huongntm2 && p != havm), huongntm3);

        Asserts.assertEquals(manager.rightOf(huongntm2), binh4);
        Asserts.assertEquals(manager.rightOf(binh5, p -> p != huongntm2 && p != havm), huongntm3);
        Asserts.assertEquals(manager.rightOf(huongntm3, p -> p != huongntm2 && p != havm), binh4);
        Asserts.assertEquals(manager.rightOf(binh4, p -> p != binh5 && p != havm), huongntm3);
    }
}
