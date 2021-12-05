package com.tvd12.gamebox.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import com.tvd12.gamebox.handler.MMORoomUpdatedHandler;
import com.tvd12.gamebox.manager.RoomManager;
import com.tvd12.gamebox.manager.SynchronizedRoomManager;
import com.tvd12.gamebox.math.Vec3;

public class MMOIntergrationTest {

    private final MMOVirtualWorld world;
    private final ExecutorService executorService;
    private final List<MMOPlayer> freePlayerList;
    private final List<MMOPlayer> joinedPlayerList;
    private final AtomicInteger totalRooms;
    private final AtomicInteger totalPlayers;
    private final RoomManager<MMORoom> roomManager;
    
    public MMOIntergrationTest() {
        this.world = MMOVirtualWorld.builder()
            .maxRoomCount(1000)
            .roomGroupCount(16)
            .timeTickMillis(100)
            .build();
        this.totalRooms = new AtomicInteger();
        this.totalPlayers = new AtomicInteger();
        this.executorService = Executors.newFixedThreadPool(16);
        this.roomManager = new SynchronizedRoomManager<>();
        this.freePlayerList = Collections.synchronizedList(new ArrayList<>());
        this.joinedPlayerList = Collections.synchronizedList(new ArrayList<>());
    }
    
    public void test() throws Exception {
        long testTime = 5 * 60 * 1000;
        long startTime = System.currentTimeMillis();
        long endtime = startTime + testTime;
        while(true) {
            long currentTime = System.currentTimeMillis();
            if (currentTime > endtime) {
                break;
            }
            Thread.sleep(300);
            addPlayers();
            addPlayerToRooms();
            setPlayerPositions();
            removePlayerFromRooms();
        }
    }
    
    private void addPlayers() {
        int playersToAdd = ThreadLocalRandom.current().nextInt(5);
        for (int i = 0 ; i < playersToAdd ; ++i) {
            MMOPlayer player = MMOPlayer.builder()
                .name("Player#" + totalPlayers.incrementAndGet())
                .build();
            freePlayerList.add(player);
        }
    }
    
    private void addPlayerToRooms() {
        if (freePlayerList.isEmpty()) {
            return;
        }
        int numberOfPlayers = ThreadLocalRandom.current().nextInt(freePlayerList.size());
        Iterator<MMOPlayer> iterator = freePlayerList.iterator();
        for (int i = 0 ; i < numberOfPlayers ; ++i) {
            MMORoom room = findAvailableRooms();
            MMOPlayer player = iterator.next();
            executorService.execute(() -> room.addPlayer(player));
            iterator.remove();
            joinedPlayerList.add(player);
        }
    }
    
    private void setPlayerPositions() {
        for (MMOPlayer player : joinedPlayerList) {
            executorService.execute(() -> {
                Vec3 position = new Vec3(
                    (float)ThreadLocalRandom.current().nextDouble(300),
                    (float)ThreadLocalRandom.current().nextDouble(300),
                    (float)ThreadLocalRandom.current().nextDouble(300)
                );
                player.setPosition(position);
            });
        }
    }
    
    private void removePlayerFromRooms() {
        if (joinedPlayerList.isEmpty()) {
            return;
        }
        int numberOfPlayers = ThreadLocalRandom.current().nextInt(joinedPlayerList.size());
        Iterator<MMOPlayer> iterator = joinedPlayerList.iterator();
        for (int i = 0 ; i < numberOfPlayers ; ++i) {
            MMOPlayer player = iterator.next();
            MMORoom room = roomManager.getRoom(player.getCurrentRoomId());
            if (room == null) {
                continue;
            }
            executorService.execute(() -> {
                room.removePlayer(player);
                if (room.isEmpty()) {
                    roomManager.removeRoom(room);
                    world.removeRoom(room);
                }
            });
            iterator.remove();
        }
    }
    
    private MMORoom findAvailableRooms() {
        MMORoom room = roomManager.getRoom(it -> it.getPlayerManager().getPlayerCount() == 1);
        if (room == null) {
            room = MMORoom.builder()
                .name("Room#" + totalRooms.incrementAndGet())
                .distanceOfInterest(100.0D)
                .addRoomUpdatedHandler(new InternalPositionRoomUpdatedHandler())
                .build();
            roomManager.addRoom(room);
            world.addRoom(room);
        }
        return room;
    }
    
    private static class InternalPositionRoomUpdatedHandler implements MMORoomUpdatedHandler {
        @Override
        public void onRoomUpdated(MMORoom room, List<MMOPlayer> players) {
            players.forEach(player -> {

                // Check if player's position or rotation is updated
                if (player.isStateChanged()) {
                    System.out.println("Player: " + player.getName() + ", near by players: " + player.getNearbyPlayerNames());
                    player.setStateChanged(false);
                }
            });
        }
    }
    
    public static void main(String[] args) throws Exception {
        new MMOIntergrationTest().test();
    }
}
