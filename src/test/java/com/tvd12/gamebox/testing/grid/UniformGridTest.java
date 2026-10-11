package com.tvd12.gamebox.testing.grid;

import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.grid.SynchronizedUniformGrid;
import com.tvd12.gamebox.grid.UniformGrid;
import com.tvd12.gamebox.math.Vec3;
import com.tvd12.test.assertion.Asserts;
import com.tvd12.test.util.RandomUtil;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UniformGridTest {

    @Test
    public void createWithInvalidCellSizeTest() {
        // given
        // when
        Throwable e = Asserts.assertThrows(() -> new UniformGrid<MMOPlayer>(0));

        // then
        Asserts.assertEquals(IllegalArgumentException.class, e.getClass());
    }

    @Test
    public void insertAndRemoveTest() {
        // given
        UniformGrid<MMOPlayer> grid = new UniformGrid<>(10);
        MMOPlayer player = newPlayer("player", 5, 0, 5);

        // when
        boolean inserted = grid.insert(player);
        boolean insertedAgain = grid.insert(player);
        boolean removed = grid.remove(player);
        boolean removedAgain = grid.remove(player);

        // then
        Asserts.assertTrue(inserted);
        Asserts.assertFalse(insertedAgain);
        Asserts.assertTrue(removed);
        Asserts.assertFalse(removedAgain);
        Asserts.assertFalse(grid.contains(player));
        Asserts.assertEquals(grid.size(), 0);
        Asserts.assertEquals(grid.getCellCount(), 0);
    }

    @Test
    public void removeAfterPositionChangedTest() {
        // given
        UniformGrid<MMOPlayer> grid = new UniformGrid<>(10);
        MMOPlayer player = newPlayer("player", 5, 0, 5);
        grid.insert(player);
        player.setPosition(new Vec3(500, 0, 500));

        // when
        boolean removed = grid.remove(player);

        // then
        Asserts.assertTrue(removed);
        Asserts.assertEquals(grid.getCellCount(), 0);
    }

    @Test
    public void searchUsesRealDistanceTest() {
        // given
        UniformGrid<MMOPlayer> grid = new UniformGrid<>(10);
        MMOPlayer center = newPlayer("center", 0, 0, 0);
        MMOPlayer inside = newPlayer("inside", 6, 0, 6);
        MMOPlayer corner = newPlayer("corner", 9, 0, 9);
        MMOPlayer negative = newPlayer("negative", -5, 0, -5);
        grid.insert(center);
        grid.insert(inside);
        grid.insert(corner);
        grid.insert(negative);

        // when
        List<MMOPlayer> matches = grid.search(center, 10);

        // then
        Asserts.assertEquals(
            names(matches),
            new HashSet<>(Arrays.asList("center", "inside", "negative"))
        );
    }

    @Test
    public void updateMovesItemToNewCellTest() {
        // given
        UniformGrid<MMOPlayer> grid = new UniformGrid<>(10);
        MMOPlayer player = newPlayer("player", 5, 0, 5);
        grid.insert(player);

        // when
        player.setPosition(new Vec3(105, 0, 105));
        boolean updated = grid.update(player);

        // then
        Asserts.assertTrue(updated);
        Asserts.assertEquals(grid.getCellCount(), 1);
        Asserts.assertEquals(
            grid.search(new Vec3(5, 0, 5), 20),
            Collections.emptyList(),
            false
        );
        Asserts.assertEquals(
            names(grid.search(new Vec3(100, 0, 100), 20)),
            Collections.singleton("player"),
            false
        );
        Asserts.assertFalse(grid.update(newPlayer("unknown", 0, 0, 0)));
    }

    @Test
    public void isItemRemainingAtSameCellTest() {
        // given
        UniformGrid<MMOPlayer> grid = new UniformGrid<>(10);
        MMOPlayer player = newPlayer("player", 5, 0, 5);
        grid.insert(player);

        // when
        // then
        Asserts.assertTrue(grid.isItemRemainingAtSameCell(player, new Vec3(9, 0, 1)));
        Asserts.assertFalse(grid.isItemRemainingAtSameCell(player, new Vec3(11, 0, 1)));
        Asserts.assertFalse(grid.isItemRemainingAtSameCell(player, new Vec3(-1, 0, 1)));
    }

    @Test
    public void searchWithHugeRangeTest() {
        // given
        UniformGrid<MMOPlayer> grid = new SynchronizedUniformGrid<>(1);
        MMOPlayer player1 = newPlayer("player1", 0, 0, 0);
        MMOPlayer player2 = newPlayer("player2", 5000, 0, 5000);
        grid.insert(player1);
        grid.insert(player2);

        // when
        List<MMOPlayer> matches = grid.search(new Vec3(), 10000);

        // then
        Asserts.assertEquals(
            names(matches),
            new HashSet<>(Arrays.asList("player1", "player2"))
        );
    }

    @Test
    public void searchMatchesBruteForceTest() {
        // given
        UniformGrid<MMOPlayer> grid = new SynchronizedUniformGrid<>(7);
        List<MMOPlayer> players = new ArrayList<>();
        for (int i = 0; i < 200; ++i) {
            MMOPlayer player = newPlayer(
                "player" + i,
                RandomUtil.randomFloat(-100, 100),
                RandomUtil.randomFloat(-100, 100),
                RandomUtil.randomFloat(-100, 100)
            );
            players.add(player);
            grid.insert(player);
        }
        float range = 25;

        for (MMOPlayer player : players) {
            // when
            List<MMOPlayer> matches = grid.search(player, range);

            // then
            Set<String> expected = players.stream()
                .filter(it -> player.getPosition().distanceSquare(it.getPosition())
                    <= range * range)
                .map(MMOPlayer::getName)
                .collect(Collectors.toSet());
            Asserts.assertEquals(names(matches), expected);
        }
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
