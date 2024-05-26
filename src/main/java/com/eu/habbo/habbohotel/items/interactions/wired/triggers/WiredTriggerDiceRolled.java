package com.eu.habbo.habbohotel.items.interactions.wired.triggers;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.ClientMessage;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredTriggerDiceRolled extends InteractionWiredTrigger {
    public static final WiredTriggerType type = WiredTriggerType.DICE_ROLLED;

    private String username = "";

    public WiredTriggerDiceRolled(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredTriggerDiceRolled(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        Habbo habbo = room.getHabbo(roomUnit);
        if (habbo != null) {
            if (!this.username.isEmpty())
                return habbo.getHabboInfo().getUsername().equalsIgnoreCase(this.username);
            return true;
        }
        return false;
    }

    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.username));
    }

    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        String wiredData = set.getString("wired_data");
        if (wiredData.startsWith("{")) {
            JsonData data = (JsonData)WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
            this.username = data.username;
        } else {
            this.username = wiredData;
        }
    }

    public void onPickUp() {
        this.username = "";
    }

    public WiredTriggerType getType() {
        return type;
    }

    @Override
    public boolean saveData(WiredSettings settings) {
        settings.getIntParams();
        this.username = settings.getStringParam();
        return true;
    }

    public void serializeWiredData(ServerMessage message, Room room) {
        message.appendBoolean(Boolean.FALSE);
        message.appendInt(5);
        message.appendInt(0);
        message.appendInt(getBaseItem().getSpriteId());
        message.appendInt(getId());
        message.appendString(this.username);
        message.appendInt(0);
        message.appendInt(0);
        message.appendInt(7);
        message.appendInt(0);
        message.appendInt(0);
    }

    public boolean isTriggeredByRoomUnit() {
        return true;
    }

    static class JsonData {
        String username;

        public JsonData(String username) {
            this.username = username;
        }
    }
}
