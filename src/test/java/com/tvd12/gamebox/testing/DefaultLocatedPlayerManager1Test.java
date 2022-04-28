package com.tvd12.gamebox.testing;

import com.tvd12.gamebox.entity.LocatedPlayer;
import com.tvd12.gamebox.manager.DefaultLocatedPlayerManager;
import com.tvd12.test.assertion.Asserts;
import org.testng.annotations.Test;

public class DefaultLocatedPlayerManager1Test {

    @Test
    public void test() {
        DefaultLocatedPlayerManager manager = new DefaultLocatedPlayerManager();
        LocatedPlayer a = new LocatedPlayer("a");
        LocatedPlayer b = new LocatedPlayer("b");
        LocatedPlayer c = new LocatedPlayer("c");
        manager.setMaster(a);
        manager.addPlayer(a, 1);
        manager.addPlayer(b, 0);
        manager.addPlayer(c, 2);

        a.setLocation(1);
        b.setLocation(0);
        c.setLocation(2);

        System.out.println(manager);

        System.out.println(manager.nextOf(c, p -> p != b));

    }

    @Test
    public void justOne() {
        DefaultLocatedPlayerManager manager = new DefaultLocatedPlayerManager();
        LocatedPlayer a = new LocatedPlayer("a");
        manager.setMaster(a);
        manager.addPlayer(a, 1);

        a.setLocation(1);

        System.out.println(manager);

        Asserts.assertEquals(manager.nextOf(a), null);

    }

}
