package com.eu.habbo.habbohotel.items.interactions.wired.conditions;

import com.eu.habbo.habbohotel.games.Game;
import com.eu.habbo.habbohotel.games.GameState;
import com.eu.habbo.habbohotel.games.freeze.FreezeGame;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredConditionType;
import com.eu.habbo.messages.ClientMessage;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredConditionNotFreezeGameActive extends InteractionWiredCondition {
    public static final WiredConditionType type = WiredConditionType.NOT_ACTOR_IN_GROUP;

    public WiredConditionNotFreezeGameActive(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredConditionNotFreezeGameActive(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    public WiredConditionType getType() {
        return type;
    }

    @Override
    public boolean saveData(WiredSettings settings) {
        return true;
    }

    public void serializeWiredData(ServerMessage message, Room room) {
        message.appendBoolean(Boolean.valueOf(false));
        message.appendInt(Integer.valueOf(5));
        message.appendInt(Integer.valueOf(0));
        message.appendInt(Integer.valueOf(getBaseItem().getSpriteId()));
        message.appendInt(Integer.valueOf(getId()));
        message.appendString("");
        message.appendInt(Integer.valueOf(0));
        message.appendInt(Integer.valueOf(0));
        message.appendInt(Integer.valueOf((getType()).code));
        message.appendInt(Integer.valueOf(0));
        message.appendInt(Integer.valueOf(0));
    }


    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        Game game = room.getGame(FreezeGame.class);
        return (game == null || !game.state.equals(GameState.RUNNING));
    }

    public String getWiredData() {
        return "";
    }

    public void loadWiredData(ResultSet set, Room room) throws SQLException {}

    public void onPickUp() {}
}