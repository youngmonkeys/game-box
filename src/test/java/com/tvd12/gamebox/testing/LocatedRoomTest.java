package com.tvd12.gamebox.testing;

import com.tvd12.gamebox.entity.LocatedPlayer;
import com.tvd12.gamebox.entity.LocatedRoom;
import org.testng.annotations.Test;

public class LocatedRoomTest {

    @Test
    public void masterTest() {
        LocatedRoom room = LocatedRoom.builder()
            .name("room")
            .build();

        LocatedPlayer player1 = new LocatedPlayer("player1");
        room.addPlayer(player1);

        LocatedPlayer player2 = new LocatedPlayer("player2");
        room.addPlayer(player2);

        LocatedPlayer player3 = new LocatedPlayer("player3");
        room.addPlayer(player3);

        room.getPlayerManager().setMaster(player2);

        System.out.println("\n\nnext master is: " + room.getPlayerManager().setNewMaster() + "\n\n");
    }

}
