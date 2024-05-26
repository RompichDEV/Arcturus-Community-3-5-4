package com.eu.habbo.habbohotel.items.interactions.wired.conditions;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.wired.WiredConditionOperator;
import com.eu.habbo.habbohotel.wired.WiredConditionType;
import com.eu.habbo.messages.ClientMessage;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredConditionMottoContains extends InteractionWiredCondition {
    public static final WiredConditionType type = WiredConditionType.ACTOR_WEARS_BADGE;

    private String motto = "";

    public WiredConditionMottoContains(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredConditionMottoContains(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        Habbo habbo = room.getHabbo(roomUnit);
        return (habbo != null && habbo.getHabboInfo().getMotto().contains(this.motto));
    }

    public String getWiredData() {
        return this.motto;
    }

    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        this.motto = set.getString("wired_data");
    }

    public void onPickUp() {
        this.motto = "";
    }

    public WiredConditionType getType() {
        return type;
    }

    @Override
    public boolean saveData(WiredSettings settings) {
        settings.getIntParams();
        this.motto = settings.getStringParam();
        return true;
    }

    public void serializeWiredData(ServerMessage message, Room room) {
        message.appendBoolean(Boolean.valueOf(false));
        message.appendInt(Integer.valueOf(5));
        message.appendInt(Integer.valueOf(0));
        message.appendInt(Integer.valueOf(getBaseItem().getSpriteId()));
        message.appendInt(Integer.valueOf(getId()));
        message.appendString(this.motto);
        message.appendInt(Integer.valueOf(0));
        message.appendInt(Integer.valueOf(0));
        message.appendInt(Integer.valueOf((getType()).code));
        message.appendInt(Integer.valueOf(0));
        message.appendInt(Integer.valueOf(0));
    }


    public WiredConditionOperator operator() {
        return WiredConditionOperator.OR;
    }
}