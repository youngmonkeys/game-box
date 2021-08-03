package com.tvd12.gamebox.testing;

import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.math.Vec3;
import com.tvd12.test.assertion.Asserts;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;

public class MMOPlayerTest {
	
	@Test
	public void test() {
		MMOPlayer a = new MMOPlayer("a");
		a.setPosition(mock(Vec3.class));
		a.setRotation(mock(Vec3.class));
		
		MMOPlayer b = new MMOPlayer("b");
		b.setPosition(mock(Vec3.class));
		b.setRotation(mock(Vec3.class));
		
		a.addNearbyPlayer(b);
		b.addNearbyPlayer(a);
		
		Asserts.assertEquals(a.getNearbyPlayers().size(), 1);
		Asserts.assertEquals(b.getNearbyPlayers().size(), 1);
		
		b.removeNearByPlayer(a);
		Asserts.assertEquals(b.getNearbyPlayers().size(), 0);
	}
}
