package com.tvd12.gamebox.entity;

import com.tvd12.gamebox.constant.RoomStatus;
import com.tvd12.test.assertion.Asserts;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RoomTest {

    @Test
    public void test() {
        // given
        Room sut = Room.builder()
            .name("test")
            .build();

        // when
        sut.setPassword("123456");
        sut.setStatus(RoomStatus.FINISHED);

        // then
        Asserts.assertEquals(sut.getPassword(), "123456");
        Asserts.assertEquals(sut.getStatus(), RoomStatus.FINISHED);
        //noinspection EqualsWithItself
        Asserts.assertTrue(sut.equals(sut));
        //noinspection ConstantConditions
        Asserts.assertFalse(sut.equals(null));

        Room me = Room.builder()
            .id(1)
            .name("test")
            .build();
        Room other = Room.builder()
            .id(2)
            .name("other")
            .build();
        Assert.assertEquals(sut, me);
        Asserts.assertNotEquals(me, other);
        Assert.assertEquals(sut.hashCode(), me.hashCode());
        Asserts.assertNotEquals(sut.hashCode(), other.hashCode());
    }
}
