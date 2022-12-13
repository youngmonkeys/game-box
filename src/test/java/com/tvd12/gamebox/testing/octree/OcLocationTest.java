package com.tvd12.gamebox.testing.octree;

import com.tvd12.gamebox.octree.OcLocation;
import com.tvd12.test.assertion.Asserts;
import com.tvd12.test.util.RandomUtil;
import org.testng.annotations.Test;

public class OcLocationTest {
    
    @Test
    public void nullTest() {
        // given
        int index1 = RandomUtil.randomInt(-100, 0);
        int index2 = RandomUtil.randomInt(8, 100);
        
        // when
        OcLocation ocLocation1 = OcLocation.of(index1);
        OcLocation ocLocation2 = OcLocation.of(index2);
        
        // then
        Asserts.assertNull(ocLocation1);
        Asserts.assertNull(ocLocation2);
    }
}
