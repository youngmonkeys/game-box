package com.tvd12.gamebox.testing;

import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.math.Vec3;
import com.tvd12.test.assertion.Asserts;
import com.tvd12.test.util.RandomUtil;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MMOPlayerTest {
	
	@Test
	public void setPositionTest() {
		// given
		Vec3 position = TestHelper.randomVec3();
		MMOPlayer sut = new MMOPlayer("a");
		
		// when
		sut.setPosition(position);
		
		// then
		Asserts.assertEquals(position, sut.getPosition());
	}
	
	@Test
	public void setRotationTest() {
		// given
		Vec3 rotation = TestHelper.randomVec3();
		MMOPlayer sut = new MMOPlayer("a");
		
		// when
		sut.setRotation(rotation);
		
		// then
		Asserts.assertEquals(rotation, sut.getRotation());
	}
	
	@Test
	public void addNearbyPlayersTest() {
		// given
		int nearbyPlayerCount = RandomUtil.randomSmallInt();
		MMOPlayer sut = new MMOPlayer("a");
		List<MMOPlayer> nearbyPlayers = new ArrayList<>();
		for (int i = 0; i < nearbyPlayerCount; i++) {
			nearbyPlayers.add(new MMOPlayer("player#" + i));
		}
		
		// when
		nearbyPlayers.forEach(sut::addNearbyPlayer);
		
		// then
		List<String> buffer = new ArrayList<>();
		sut.getNearbyPlayerNames(buffer);
		buffer.sort(String::compareTo);
		Asserts.assertEquals(
				nearbyPlayers.stream().map(MMOPlayer::getName).collect(Collectors.toList()),
				buffer
		);
	}
}
