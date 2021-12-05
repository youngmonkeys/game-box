package com.tvd12.gamebox.testing.constant;

import org.testng.annotations.Test;

import com.tvd12.gamebox.constant.PlayerStatus;
import com.tvd12.test.assertion.Asserts;

public class PlayerStatusTest {

    @Test
    public void test() {
        Asserts.assertEquals(PlayerStatus.VIEWING.getId(), 1);
        Asserts.assertEquals(PlayerStatus.VIEWING.getName(), "VIEWING");
        Asserts.assertEquals(PlayerStatus.valueOf(1), PlayerStatus.VIEWING);
    }
}
