package com.tvd12.gamebox.testing;

import com.tvd12.gamebox.entity.LocatedPlayer;
import com.tvd12.gamebox.manager.DefaultLocatedPlayerManager;
import org.testng.annotations.Test;

public class DefaultLocatedPlayerManager2Test {

    @Test
    public void test() {
        DefaultLocatedPlayerManager manager = new DefaultLocatedPlayerManager();
        LocatedPlayer a = new LocatedPlayer("a");
        LocatedPlayer b = new LocatedPlayer("b");
        manager.setMaster(a);
        manager.addPlayer(a, 1);
        manager.addPlayer(b, 2);

        manager.removePlayer(1);
        manager.setNewMaster();

        System.out.println(manager);
    }
}
