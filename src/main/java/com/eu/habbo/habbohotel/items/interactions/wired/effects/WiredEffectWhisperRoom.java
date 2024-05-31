package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ClientMessage;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserWhisperComposer;
import gnu.trove.procedure.TObjectProcedure;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WiredEffectWhisperRoom extends InteractionWiredEffect {
    public static final WiredEffectType type = WiredEffectType.SHOW_MESSAGE;

    protected String message = "";

    public WiredEffectWhisperRoom(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredEffectWhisperRoom(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    public void serializeWiredData(ServerMessage message, Room room) {
        message.appendBoolean(Boolean.FALSE);
        message.appendInt(0);
        message.appendInt(0);
        message.appendInt(getBaseItem().getSpriteId());
        message.appendInt(getId());
        message.appendString(this.message);
        message.appendInt(0);
        message.appendInt(0);
        message.appendInt(type.code);
        message.appendInt(getDelay());
        if (requiresTriggeringUser()) {
            final List<Integer> invalidTriggers = new ArrayList<>();
            room.getRoomSpecialTypes().getTriggers(getX(), getY()).forEach(new TObjectProcedure<InteractionWiredTrigger>() {
                public boolean execute(InteractionWiredTrigger object) {
                    if (!object.isTriggeredByRoomUnit())
                        invalidTriggers.add(object.getBaseItem().getSpriteId());
                    return true;
                }
            });
            message.appendInt(invalidTriggers.size());
            for (Integer i : invalidTriggers)
                message.appendInt(i);
        } else {
            message.appendInt(0);
        }
    }

    @Override
    public boolean saveData(WiredSettings settings, GameClient gameClient) throws WiredSaveException {
        String message = settings.getStringParam();

        if(gameClient.getHabbo() == null || !gameClient.getHabbo().hasPermission(Permission.ACC_SUPERWIRED)) {
            message = Emulator.getGameEnvironment().getWordFilter().filter(message, null);
            message = message.substring(0, Math.min(message.length(), Emulator.getConfig().getInt("hotel.wired.message.max_length", 100)));
        }

        int delay = settings.getDelay();

        if(delay > Emulator.getConfig().getInt("hotel.wired.max_delay", 20))
            throw new WiredSaveException("Delay too long");

        this.message = message;
        this.setDelay(delay);
        return true;
    }

    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        if (this.message.length() > 0 &&
                roomUnit != null) {
            String msg = "";
            for (Habbo habbo : room.getHabbos()) {
                if (habbo != null) {
                    msg = this.message.replace("%user%", habbo.getHabboInfo().getUsername()).replace("%online_count%", Emulator.getGameEnvironment().getHabboManager().getOnlineCount() + "").replace("%room_count%", Emulator.getGameEnvironment().getRoomManager().getActiveRooms().size() + "");
                    habbo.getClient().sendResponse((MessageComposer)new RoomUserWhisperComposer(new RoomChatMessage(msg, habbo, habbo, RoomChatMessageBubbles.WIRED)));
                    if (habbo.getRoomUnit().isIdle())
                        habbo.getRoomUnit().getRoom().unIdle(habbo);
                }
            }
        }
        return false;
    }

    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.message, getDelay()));
    }

    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        String wiredData = set.getString("wired_data");
        if (wiredData.startsWith("{")) {
            JsonData data = (JsonData)WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
            setDelay(data.delay);
            this.message = data.message;
        } else {
            this.message = "";
            if ((wiredData.split("\t")).length >= 2) {
                setDelay(Integer.valueOf(wiredData.split("\t")[0]));
                this.message = wiredData.split("\t")[1];
            }
            needsUpdate(true);
        }
    }

    public void onPickUp() {
        this.message = "";
        setDelay(0);
    }

    public WiredEffectType getType() {
        return type;
    }

    public boolean requiresTriggeringUser() {
        return true;
    }

    static class JsonData {
        String message;

        int delay;

        public JsonData(String message, int delay) {
            this.message = message;
            this.delay = delay;
        }
    }
}