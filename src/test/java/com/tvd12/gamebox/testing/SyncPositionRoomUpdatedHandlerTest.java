package com.tvd12.gamebox.testing;

import com.tvd12.ezyfoxserver.support.command.EzyArrayResponse;
import com.tvd12.ezyfoxserver.support.factory.EzyResponseFactory;
import com.tvd12.gamebox.constant.Commands;
import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.entity.MMORoom;
import com.tvd12.gamebox.handler.SyncPositionRoomUpdatedHandler;
import com.tvd12.gamebox.math.Vec3;
import com.tvd12.test.reflect.FieldUtil;
import com.tvd12.test.util.RandomUtil;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.*;

public class SyncPositionRoomUpdatedHandlerTest {

    @SuppressWarnings("unchecked")
    @Test
    public void casePlayerChangeStateTest() {
        // given
        String playerName = RandomUtil.randomShortAlphabetString();

        int nearbyPlayerCount = RandomUtil.randomSmallInt();
        Set<String> nearbyPlayerNames = RandomUtil.randomSet(nearbyPlayerCount, String.class);
        List<String> nearbyPlayerNameList = new ArrayList<>(nearbyPlayerNames);

        Vec3 position = TestHelper.randomVec3();
        Vec3 rotation = TestHelper.randomVec3();
        int clientTimeTick = RandomUtil.randomInt();

        EzyArrayResponse arrayResponse = mock(EzyArrayResponse.class);
        when(arrayResponse.udpTransport()).thenReturn(arrayResponse);
        when(arrayResponse.command(Commands.SYNC_POSITION)).thenReturn(arrayResponse);
        when(arrayResponse.param(playerName)).thenReturn(arrayResponse);
        when(arrayResponse.param(position.toArray())).thenReturn(arrayResponse);
        when(arrayResponse.param(rotation.toArray())).thenReturn(arrayResponse);
        when(arrayResponse.param(clientTimeTick)).thenReturn(arrayResponse);
        when(arrayResponse.usernames(nearbyPlayerNameList)).thenReturn(arrayResponse);
        doNothing().when(arrayResponse).execute();

        EzyResponseFactory responseFactory = mock(EzyResponseFactory.class);
        when(responseFactory.newArrayResponse()).thenReturn(arrayResponse);

        SyncPositionRoomUpdatedHandler sut = new SyncPositionRoomUpdatedHandler();
        sut.setResponseFactory(responseFactory);
        sut = new SyncPositionRoomUpdatedHandler(responseFactory);

        MMORoom room = MMORoom.builder()
            .name(RandomUtil.randomShortAlphabetString())
            .distanceOfInterest(RandomUtil.randomSmallDouble())
            .build();

        MMOPlayer player = new MMOPlayer(playerName);
        player.setPosition(position);
        player.setRotation(rotation);
        player.setClientTimeTick(clientTimeTick);

        Map<String, MMOPlayer> nearbyPlayers = FieldUtil.getFieldValue(player, "nearbyPlayers");
        nearbyPlayerNames.forEach(name -> {
            MMOPlayer nearbyPlayer = new MMOPlayer(name);
            nearbyPlayers.put(name, nearbyPlayer);
            room.getPlayerManager().addPlayer(nearbyPlayer);
        });
        room.getPlayerManager().addPlayer(player);

        List<MMOPlayer> players = FieldUtil.getFieldValue(room, "playerBuffer");
        room.getPlayerManager().getPlayerList(players);

        // when
        sut.onRoomUpdated(room, players);

        // then
        verify(responseFactory, times(1)).newArrayResponse();
        verify(arrayResponse, times(1)).udpTransport();
        verify(arrayResponse, times(1)).command(Commands.SYNC_POSITION);
        verify(arrayResponse, times(1)).param(playerName);
        verify(arrayResponse, times(1)).param(position.toArray());
        verify(arrayResponse, times(1)).param(rotation.toArray());
        verify(arrayResponse, times(1)).usernames(nearbyPlayerNameList);
        verify(arrayResponse, times(1)).execute();
    }

}
