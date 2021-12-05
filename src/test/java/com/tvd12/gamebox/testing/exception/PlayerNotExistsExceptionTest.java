package com.tvd12.gamebox.testing.exception;

import org.testng.annotations.Test;

import com.tvd12.gamebox.exception.PlayerNotExistsException;
import com.tvd12.test.assertion.Asserts;

public class PlayerNotExistsExceptionTest {

    @Test
    public void test() {
        // given
        PlayerNotExistsException e = new PlayerNotExistsException("player not existed");
        
        // when
        // then
        Asserts.assertEquals(e.getMessage(), "player not existed");
    }
}
