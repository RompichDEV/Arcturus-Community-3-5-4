package com.eu.habbo.habbohotel.items.interactions.wired.conditions;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.wired.conditions.WiredConditionHabboWearsBadge;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredConditionType;
import com.eu.habbo.messages.ClientMessage;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.iterator.hash.TObjectHashIterator;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredConditionHabboNotOwnsFurni extends WiredConditionHabboWearsBadge {
    protected String furni;

    public WiredConditionHabboNotOwnsFurni(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
        this.furni = "";
    }

    public WiredConditionHabboNotOwnsFurni(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
        this.furni = "";
    }

    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        Habbo habbo = room.getHabbo(roomUnit);
        if (habbo != null)
            for (TObjectHashIterator<HabboItem> tObjectHashIterator = habbo.getInventory().getItemsComponent().getItemsAsValueCollection().iterator(); tObjectHashIterator.hasNext(); ) {
                HabboItem item = tObjectHashIterator.next();
                if (item.getBaseItem().getName().equals(this.furni))
                    return false;
            }
        return true;
    }

    public String getWiredData() {
        return this.furni;
    }

    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        this.furni = set.getString("wired_data");
    }

    public void onPickUp() {
        this.furni = "";
    }

    public WiredConditionType getType() {
        return type;
    }

    public void serializeWiredData(ServerMessage message, Room room) {
        message.appendBoolean(Boolean.valueOf(false));
        message.appendInt(Integer.valueOf(5));
        message.appendInt(Integer.valueOf(0));
        message.appendInt(Integer.valueOf(getBaseItem().getSpriteId()));
        message.appendInt(Integer.valueOf(getId()));
        message.appendString(this.furni);
        message.appendInt(Integer.valueOf(0));
        message.appendInt(Integer.valueOf(0));
        message.appendInt(Integer.valueOf((getType()).code));
        message.appendInt(Integer.valueOf(0));
        message.appendInt(Integer.valueOf(0));
    }

    public boolean saveData(ClientMessage packet) {
        packet.readInt();
        this.furni = packet.readString();
        return true;
    }
}
