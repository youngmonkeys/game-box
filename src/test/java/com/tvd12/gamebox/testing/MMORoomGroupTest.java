package com.tvd12.gamebox.testing;

import com.tvd12.gamebox.entity.MMORoomGroup;
import com.tvd12.test.assertion.Asserts;
import com.tvd12.test.reflect.FieldUtil;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MMORoomGroupTest {
	
	@Test
	public void createMMORoomGroupTest() throws InterruptedException {
		// given
		MMORoomGroup sut = MMORoomGroup.builder().timeTickMillies(100).build();
		
		// when
		Set<Thread> threads = Thread.getAllStackTraces().keySet();
		List<String> threadNames = threads.stream().map(Thread::getName).collect(Collectors.toList());
		
		// then
		Asserts.assertNotNull(FieldUtil.getFieldValue(sut, "timeTickMillis"));
		Asserts.assertNotNull(FieldUtil.getFieldValue(sut, "roomsBuffer"));
		Asserts.assertNotNull(FieldUtil.getFieldValue(sut, "roomManager"));
		
		Asserts.assertEquals(threadNames.contains("game-box-mmo-room-group-1"), true);
	}
}
