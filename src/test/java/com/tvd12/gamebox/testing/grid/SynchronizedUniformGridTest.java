package com.tvd12.gamebox.testing.grid;

import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.grid.SynchronizedUniformGrid;
import com.tvd12.gamebox.math.Vec3;
import com.tvd12.test.assertion.Asserts;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class SynchronizedUniformGridTest {

    @Test
    public void insertSearchAndRemoveTest() {
        // given
        SynchronizedUniformGrid<MMOPlayer> grid = new SynchronizedUniformGrid<>(10);
        MMOPlayer player1 = newPlayer("player1", 5, 0, 5);
        MMOPlayer player2 = newPlayer("player2", 8, 0, 8);
        MMOPlayer player3 = newPlayer("player3", 100, 0, 100);

        // when
        boolean inserted1 = grid.insert(player1);
        boolean inserted2 = grid.insert(player2);
        boolean inserted3 = grid.insert(player3);
        boolean insertedAgain = grid.insert(player1);

        // then
        Asserts.assertTrue(inserted1);
        Asserts.assertTrue(inserted2);
        Asserts.assertTrue(inserted3);
        Asserts.assertFalse(insertedAgain);
        Asserts.assertTrue(grid.contains(player1));
        Asserts.assertEquals(grid.size(), 3);
        Asserts.assertEquals(grid.getCellCount(), 2);
        Asserts.assertEquals(
            names(grid.search(player1, 10)),
            new HashSet<>(Arrays.asList("player1", "player2"))
        );
        Asserts.assertEquals(
            names(grid.search(new Vec3(100, 0, 100), 1)),
            Collections.singleton("player3"),
            false
        );

        // when
        boolean removed = grid.remove(player3);
        boolean removedAgain = grid.remove(player3);

        // then
        Asserts.assertTrue(removed);
        Asserts.assertFalse(removedAgain);
        Asserts.assertFalse(grid.contains(player3));
        Asserts.assertEquals(grid.size(), 2);
        Asserts.assertEquals(grid.getCellCount(), 1);
    }

    @Test
    public void updateTest() {
        // given
        SynchronizedUniformGrid<MMOPlayer> grid = new SynchronizedUniformGrid<>(10);
        MMOPlayer player = newPlayer("player", 5, 0, 5);
        grid.insert(player);

        // when
        player.setPosition(new Vec3(105, 0, 105));
        boolean updated = grid.update(player);
        boolean unknownUpdated = grid.update(newPlayer("unknown", 0, 0, 0));

        // then
        Asserts.assertTrue(updated);
        Asserts.assertFalse(unknownUpdated);
        Asserts.assertEquals(grid.getCellCount(), 1);
        Asserts.assertTrue(grid.search(new Vec3(5, 0, 5), 20).isEmpty());
        Asserts.assertEquals(
            names(grid.search(new Vec3(100, 0, 100), 20)),
            Collections.singleton("player"),
            false
        );
    }

    @Test
    public void isItemRemainingAtSameCellTest() {
        // given
        SynchronizedUniformGrid<MMOPlayer> grid = new SynchronizedUniformGrid<>(10);
        MMOPlayer player = newPlayer("player", 5, 0, 5);
        grid.insert(player);

        // when
        // then
        Asserts.assertTrue(grid.isItemRemainingAtSameCell(player, new Vec3(9, 0, 1)));
        Asserts.assertFalse(grid.isItemRemainingAtSameCell(player, new Vec3(11, 0, 1)));
        Asserts.assertFalse(
            grid.isItemRemainingAtSameCell(newPlayer("unknown", 5, 0, 5), new Vec3(5, 0, 5))
        );
    }

    @Test
    public void toStringTest() {
        // given
        SynchronizedUniformGrid<MMOPlayer> grid = new SynchronizedUniformGrid<>(10);
        grid.insert(newPlayer("player1", 5, 0, 5));
        grid.insert(newPlayer("player2", 15, 0, 5));

        // when
        String actual = grid.toString();

        // then
        Asserts.assertEquals(actual, "{cellSize=10.0, cellCount=2, itemCount=2}");
    }

    @Test
    public void concurrentInsertUpdateRemoveTest() throws Exception {
        // given
        SynchronizedUniformGrid<MMOPlayer> grid = new SynchronizedUniformGrid<>(5);
        int threadCount = 8;
        int playerPerThread = 200;
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        List<Thread> threads = new ArrayList<>();
        for (int t = 0; t < threadCount; ++t) {
            final int threadIndex = t;
            Thread thread = new Thread(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < playerPerThread; ++i) {
                        MMOPlayer player = newPlayer(
                            "player" + threadIndex + "_" + i,
                            i,
                            threadIndex,
                            0
                        );
                        grid.insert(player);
                        player.setPosition(new Vec3(i + 50, threadIndex, 0));
                        grid.update(player);
                        grid.search(player, 10);
                        if (i % 2 == 0) {
                            grid.remove(player);
                        }
                    }
                } catch (Throwable e) {
                    error.set(e);
                }
            });
            threads.add(thread);
            thread.start();
        }

        // when
        startLatch.countDown();
        for (Thread thread : threads) {
            thread.join();
        }

        // then
        Asserts.assertNull(error.get());
        Asserts.assertEquals(grid.size(), threadCount * playerPerThread / 2);
        Asserts.assertEquals(
            grid.search(new Vec3(0, 0, 0), 10000).size(),
            threadCount * playerPerThread / 2
        );
    }

    private static MMOPlayer newPlayer(String name, float x, float y, float z) {
        MMOPlayer player = new MMOPlayer(name);
        player.setPosition(new Vec3(x, y, z));
        return player;
    }

    private static Set<String> names(List<MMOPlayer> players) {
        return players.stream()
            .map(MMOPlayer::getName)
            .collect(Collectors.toSet());
    }
}
