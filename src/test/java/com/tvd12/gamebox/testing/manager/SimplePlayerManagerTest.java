package com.tvd12.gamebox.testing.manager;

import org.testng.annotations.Test;

import com.tvd12.gamebox.entity.Player;
import com.tvd12.gamebox.manager.SimplePlayerManager;
import com.tvd12.test.assertion.Asserts;

public class SimplePlayerManagerTest {

    @SuppressWarnings("unchecked")
    @Test
    public void test() {
        // given
        SimplePlayerManager<Player> sut = 
            (SimplePlayerManager<Player>) SimplePlayerManager.builder()
            .maxPlayer(100)
            .build();
        
        // when
        // then
        Asserts.assertEquals(sut.getMaxPlayer(), 100);
    }
    
    @Test
    public void defaultConstructorTest() {
        // given
        SimplePlayerManager<Player> sut = new SimplePlayerManager<>();
        
        // when
        // then
        Asserts.assertEquals(sut.getMaxPlayer(), 999999999);
    }
    
    @Test
    public void cConstructorWithMaxPlayerTest() {
        // given
        SimplePlayerManager<Player> sut = new SimplePlayerManager<>(200);
        
        // when
        // then
        Asserts.assertEquals(sut.getMaxPlayer(), 200);
    }
}
