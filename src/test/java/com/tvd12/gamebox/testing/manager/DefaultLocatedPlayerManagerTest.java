package com.tvd12.gamebox.testing.manager;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.tvd12.gamebox.entity.LocatedPlayer;
import com.tvd12.gamebox.exception.LocationNotAvailableException;
import com.tvd12.gamebox.exception.PlayerExistsException;
import com.tvd12.gamebox.manager.DefaultLocatedPlayerManager;
import com.tvd12.test.assertion.Asserts;

public class DefaultLocatedPlayerManagerTest {

    @Test
    public void test() {
        // given
        DefaultLocatedPlayerManager sut = new DefaultLocatedPlayerManager();
        
        LocatedPlayer master = LocatedPlayer.builder()
            .name("master")
            .build();
        
        LocatedPlayer user = LocatedPlayer.builder()
            .name("user")
            .build();
        
        // when
        sut.addPlayer(master, 0);
        sut.addPlayer(user, 1);
        
        sut.setMaster(master);
        sut.setSpeakinger(user);
        
        // then
        Asserts.assertEquals(sut.getMaster(), master);
        Asserts.assertEquals(sut.getPlayer(0), master);
        Asserts.assertEquals(sut.getSpeakinger(), user);
        Asserts.assertNull(sut.nextOf(null));
        Asserts.assertEquals(sut.getPlayerNames(), Arrays.asList("user", "master"), false);
        Asserts.assertEquals(sut.getPlayerCount(), 2);
        Asserts.assertTrue(sut.containsPlayer("master"));
        Asserts.assertFalse(sut.isEmpty());
        sut.removePlayer(0);
        sut.removePlayer(0);
        sut.removePlayer(1);
    }
    
    @Test
    public void addPlayerFailedDueToLocationTest() {
     // given
        DefaultLocatedPlayerManager sut = new DefaultLocatedPlayerManager();
        
        LocatedPlayer player1 = LocatedPlayer.builder()
            .name("player1")
            .build();
        LocatedPlayer player2 = LocatedPlayer.builder()
            .name("player2")
            .build();
        
        sut.addPlayer(player1, 0);
        
        // when
        Throwable e = Asserts.assertThrows(() -> sut.addPlayer(player2, 0));
        
        // then
        Asserts.assertEqualsType(e, LocationNotAvailableException.class);
    }
    
    @Test
    public void addPlayerFailedDueToPlayerTest() {
        // given
           DefaultLocatedPlayerManager sut = new DefaultLocatedPlayerManager();
           
           LocatedPlayer player1 = LocatedPlayer.builder()
               .name("player1")
               .build();
           
           sut.addPlayer(player1, 0);
           
           // when
           Throwable e = Asserts.assertThrows(() -> sut.addPlayer(player1, 1));
           
           // then
           Asserts.assertEqualsType(e, PlayerExistsException.class);
       }
    
    @Test
    public void nextOfTest() {
        // given
        DefaultLocatedPlayerManager sut = new DefaultLocatedPlayerManager();
        
        LocatedPlayer player1 = LocatedPlayer.builder()
            .name("player1")
            .build();
        LocatedPlayer player2 = LocatedPlayer.builder()
            .name("player2")
            .build();
        LocatedPlayer player3 = LocatedPlayer.builder()
            .name("player3")
            .build();
        
        // when
        // then
        Asserts.assertNull(sut.nextOf(player1));
        Asserts.assertNull(sut.leftOf(player1));
        Asserts.assertNull(sut.rightOf(player1));
        
        sut.addPlayer(player1, 0);
        Asserts.assertNull(sut.nextOf(player1));
        Asserts.assertNull(sut.leftOf(player1));
        Asserts.assertNull(sut.rightOf(player1));
        
        sut.addPlayer(player2, 2);
        sut.addPlayer(player3, 10);
        Asserts.assertEquals(sut.nextOf(player2), player1);
        Asserts.assertNull(sut.leftOf(player2, it -> it.getName().equals("player2")));
        Asserts.assertNull(sut.rightOf(player2, it -> it.getName().equals("player2")));
    }
}
