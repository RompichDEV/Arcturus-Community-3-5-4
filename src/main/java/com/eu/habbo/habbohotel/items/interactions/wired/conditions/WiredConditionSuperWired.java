package com.eu.habbo.habbohotel.items.interactions.wired.conditions;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.DanceType;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboBadge;
import com.eu.habbo.habbohotel.wired.WiredConditionType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredConditionSuperWired extends InteractionWiredCondition {
    public static final WiredConditionType type = WiredConditionType.ACTOR_WEARS_BADGE;

    protected String key = "";

    public WiredConditionSuperWired(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredConditionSuperWired(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        Habbo habbo = room.getHabbo(roomUnit);
        String str = this.key;
        String[] finalText = str.split(":");
        switch (finalText[0]) {
            case "enable":
                if (roomUnit == null)
                    return false;
                return (roomUnit.getEffectId() == Integer.valueOf(finalText[1]).intValue());
            case "noenable":
                if (roomUnit == null)
                    return false;
                return (roomUnit.getEffectId() != Integer.valueOf(finalText[1]).intValue());
            case "handitem":
                if (roomUnit == null)
                    return false;
                return (roomUnit.getHandItem() == Integer.valueOf(finalText[1]).intValue());
            case "nohanditem":
                if (roomUnit == null)
                    return false;
                return (roomUnit.getHandItem() != Integer.valueOf(finalText[1]).intValue());
            case "dance":
                if (roomUnit == null)
                    return false;
                return (roomUnit.getDanceType().getType() == Integer.valueOf(finalText[1]).intValue());
            case "isdance":
                if (roomUnit == null)
                    return false;
                return (roomUnit.getDanceType() != DanceType.NONE);
            case "freeze":
                if (roomUnit == null)
                    return false;
                return !roomUnit.canWalk();
            case "nofreeze":
                if (roomUnit == null)
                    return false;
                return roomUnit.canWalk();
            case "lay":
                if (roomUnit == null)
                    return false;
                return roomUnit.cmdLay;
            case "sit":
                if (roomUnit == null)
                    return false;
                return roomUnit.cmdSit;
            case "badge":
                if (habbo != null)
                    synchronized (habbo.getInventory().getBadgesComponent().getWearingBadges()) {
                        for (HabboBadge badge : habbo.getInventory().getBadgesComponent().getWearingBadges()) {
                            if (badge.getCode().equalsIgnoreCase(finalText[1]))
                                return true;
                        }
                    }
                break;
            case "nobadge":
                if (habbo != null) {
                    synchronized (habbo.getInventory().getBadgesComponent().getWearingBadges()) {
                        for (HabboBadge b : habbo.getInventory().getBadgesComponent().getWearingBadges()) {
                            if (b.getCode().equalsIgnoreCase(finalText[1]))
                                return false;
                        }
                    }
                    return true;
                }
                break;
            case "duckets":
                if (habbo != null)
                    return (habbo.getHabboInfo().getCurrencyAmount(0) >= Integer.valueOf(finalText[1]).intValue());
                break;
            case "noduckets":
                if (habbo != null)
                    return (habbo.getHabboInfo().getCurrencyAmount(0) < Integer.valueOf(finalText[1]).intValue());
                break;
            case "diamants":
                if (habbo != null)
                    return (habbo.getHabboInfo().getCurrencyAmount(5) >= Integer.valueOf(finalText[1]).intValue());
                break;
            case "nodiamants":
                if (habbo != null)
                    return (habbo.getHabboInfo().getCurrencyAmount(5) < Integer.valueOf(finalText[1]).intValue());
                break;
            case "creditos":
            case "credits":
                if (habbo != null)
                    return (habbo.getHabboInfo().getCredits() >= Integer.valueOf(finalText[1]).intValue());
                break;
            case "nocreditos":
            case "nocredits":
                if (habbo != null)
                    return (habbo.getHabboInfo().getCredits() > Integer.valueOf(finalText[1]).intValue());
                break;
            case "points":
                if (habbo != null &&
                        (habbo.getHabboStats()).cache.containsKey("points_game"))
                    return ((habbo.getHabboStats()).cache.get("points_game").hashCode() >= Integer.valueOf(finalText[1]).intValue());
                break;
            case "nopoints":
                if (habbo != null &&
                        (habbo.getHabboStats()).cache.containsKey("points_game"))
                    return ((habbo.getHabboStats()).cache.get("points_game").hashCode() < Integer.valueOf(finalText[1]).intValue());
                break;
        }
        return false;
    }

    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.key));
    }

    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        String wiredData = set.getString("wired_data");
        if (wiredData.startsWith("{")) {
            JsonData data = (JsonData)WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
            this.key = data.key;
        } else {
            this.key = wiredData;
        }
    }

    public void onPickUp() {
        this.key = "";
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
        message.appendString(this.key);
        message.appendInt(Integer.valueOf(0));
        message.appendInt(Integer.valueOf(0));
        message.appendInt(Integer.valueOf((getType()).code));
        message.appendInt(Integer.valueOf(0));
        message.appendInt(Integer.valueOf(0));
    }

    public boolean saveData(WiredSettings settings) {
        this.key = settings.getStringParam();
        return true;
    }

    static class JsonData {
        String key;

        public JsonData(String key) {
            this.key = key;
        }
    }
}