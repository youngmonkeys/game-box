package com.tvd12.gamebox.testing.exception;

import org.testng.annotations.Test;

import com.tvd12.gamebox.exception.RoomNotExistsException;
import com.tvd12.test.assertion.Asserts;

public class RoomNotExistsExceptionTest {

    @Test
    public void test() {
        // given
        RoomNotExistsException e = new RoomNotExistsException("room not existed");
        
        // when
        // then
        Asserts.assertEquals(e.getMessage(), "room not existed");
    }
}
