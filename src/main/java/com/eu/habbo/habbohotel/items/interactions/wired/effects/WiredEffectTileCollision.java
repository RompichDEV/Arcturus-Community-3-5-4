package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ClientMessage;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import com.eu.habbo.threading.runnables.WiredCollissionRunnable;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class WiredEffectTileCollision extends InteractionWiredEffect {
    public static final WiredEffectType type = WiredEffectType.CHASE;

    private THashSet<HabboItem> items;

    public WiredEffectTileCollision(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
        this.items = new THashSet();
    }

    public WiredEffectTileCollision(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
        this.items = new THashSet();
    }

    @Override
    public boolean saveData(WiredSettings settings, GameClient gameClient) throws WiredSaveException {
        int itemsCount = settings.getFurniIds().length;

        if(itemsCount > Emulator.getConfig().getInt("hotel.wired.furni.selection.count")) {
            throw new WiredSaveException("Too many furni selected");
        }

        List<HabboItem> newItems = new ArrayList<>();

        for (int i = 0; i < itemsCount; i++) {
            int itemId = settings.getFurniIds()[i];
            HabboItem it = Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId()).getHabboItem(itemId);

            if(it == null)
                throw new WiredSaveException(String.format("Item %s not found", itemId));

            newItems.add(it);
        }

        int delay = settings.getDelay();

        if(delay > Emulator.getConfig().getInt("hotel.wired.max_delay", 20))
            throw new WiredSaveException("Delay too long");

        this.items.clear();
        this.items.addAll(newItems);
        this.setDelay(delay);

        return true;
    }

    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        THashSet<HabboItem> items = new THashSet();
        TObjectHashIterator<HabboItem> tObjectHashIterator;
        for (tObjectHashIterator = this.items.iterator(); tObjectHashIterator.hasNext(); ) {
            HabboItem item = tObjectHashIterator.next();
            if (Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId()).getHabboItem(item.getId()) == null)
                items.add(item);
        }
        for (tObjectHashIterator = items.iterator(); tObjectHashIterator.hasNext(); ) {
            HabboItem item = tObjectHashIterator.next();
            this.items.remove(item);
        }
        for (tObjectHashIterator = this.items.iterator(); tObjectHashIterator.hasNext(); ) {
            HabboItem item = tObjectHashIterator.next();
            if (item == null)
                continue;
            RoomUnit target = null;
            RoomLayout layout = room.getLayout();
            if (layout == null)
                break;
            RoomTile itemTile = layout.getTile(item.getX(), item.getY());
            Collection<RoomUnit> roomUnitsAtItemTile = room.getRoomUnitsAt(itemTile);
            if (roomUnitsAtItemTile.size() > 0)
                for (RoomUnit roomUnitAtItemTile : roomUnitsAtItemTile) {
                    target = roomUnitAtItemTile;
                    Emulator.getThreading().run((Runnable)new WiredCollissionRunnable(target, room, new Object[] { item }));
                }
        }
        return true;
    }

    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(
                getDelay(), (List<Integer>)this.items
                .stream().map(HabboItem::getId).collect(Collectors.toList())));
    }

    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        this.items = new THashSet();
        String wiredData = set.getString("wired_data");
        if (wiredData.startsWith("{")) {
            JsonData data = (JsonData)WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
            setDelay(data.delay);
            for (Integer id : data.itemIds) {
                HabboItem item = room.getHabboItem(id.intValue());
                if (item != null)
                    this.items.add(item);
            }
        } else {
            String[] wiredDataOld = wiredData.split("\t");
            if (wiredDataOld.length >= 1)
                setDelay(Integer.parseInt(wiredDataOld[0]));
            if (wiredDataOld.length == 2)
                if (wiredDataOld[1].contains(";"))
                    for (String string : wiredDataOld[1].split(";")) {
                        HabboItem item = room.getHabboItem(Integer.parseInt(string));
                        if (item != null)
                            this.items.add(item);
                    }
        }
    }

    public void onPickUp() {
        this.items.clear();
        setDelay(0);
    }

    public WiredEffectType getType() {
        return type;
    }

    public void serializeWiredData(ServerMessage message, Room room) {
        THashSet<HabboItem> items = new THashSet();
        TObjectHashIterator<HabboItem> tObjectHashIterator;
        for (tObjectHashIterator = this.items.iterator(); tObjectHashIterator.hasNext(); ) {
            HabboItem item = tObjectHashIterator.next();
            if (item.getRoomId() != getRoomId() || Emulator.getGameEnvironment().getRoomManager().getRoom(getRoomId()).getHabboItem(item.getId()) == null)
                items.add(item);
        }
        for (tObjectHashIterator = items.iterator(); tObjectHashIterator.hasNext(); ) {
            HabboItem item = tObjectHashIterator.next();
            this.items.remove(item);
        }
        message.appendBoolean(Boolean.valueOf(false));
        message.appendInt(Integer.valueOf(WiredHandler.MAXIMUM_FURNI_SELECTION));
        message.appendInt(Integer.valueOf(this.items.size()));
        for (tObjectHashIterator = this.items.iterator(); tObjectHashIterator.hasNext(); ) {
            HabboItem item = tObjectHashIterator.next();
            message.appendInt(Integer.valueOf(item.getId()));
        }
        message.appendInt(Integer.valueOf(getBaseItem().getSpriteId()));
        message.appendInt(Integer.valueOf(getId()));
        message.appendString("");
        message.appendInt(Integer.valueOf(0));
        message.appendInt(Integer.valueOf(0));
        message.appendInt(Integer.valueOf((getType()).code));
        message.appendInt(Integer.valueOf(getDelay()));
        message.appendInt(Integer.valueOf(0));
    }


    protected long requiredCooldown() {
        return 495L;
    }

    static class JsonData {
        int delay;

        List<Integer> itemIds;

        public JsonData(int delay, List<Integer> itemIds) {
            this.delay = delay;
            this.itemIds = itemIds;
        }
    }
}