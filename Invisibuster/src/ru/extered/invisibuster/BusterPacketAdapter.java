package ru.extered.invisibuster;

import java.util.ArrayList;
import net.minecraft.server.v1_7_R1.WatchableObject;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

public class BusterPacketAdapter extends PacketAdapter {

	private final static Byte invisibleFlag = 0x20;
	private final static Byte drinkingFlag = 0x30;
	private final static int EFFECTS_ID = 0;

	public BusterPacketAdapter(AdapterParameteters params) {
		super(params);
	}

	private static Byte getPacketFlag(WatchableObject data) {
		return (data.a() == 0) ? (Byte) data.b() : null;
	}

	@SuppressWarnings("unchecked")
	public void onPacketSending(PacketEvent event) {
		Player player = event.getPlayer();

		if (!PermissionsEx.getUser(player).has("ru.extered.ib"))
		{
			return;
		}

		if (!PermissionsEx.getUser(player).has("ru.extered.ib.override") && !player.hasPotionEffect(PotionEffectType.NIGHT_VISION))
		{
			return;
		}

		if (InvisiBuster.turnedOff.contains(player))
			return;

		PacketContainer packet = event.getPacket().deepClone();
		StructureModifier<Object> mod = packet.getModifier();

		if (mod.size() == 0) {
			return;
		}

		int eID = (Integer) mod.read(0);

		if (player.getEntityId() == eID) {
			return;
		}

		for (int i = 0; i < mod.size(); i++) {
			if (mod.read(i) instanceof ArrayList) {
				try {
					ArrayList<WatchableObject> list = (ArrayList<WatchableObject>) mod.read(i);
					Byte entFlag;
					if (list.get(EFFECTS_ID) instanceof WatchableObject) {

						entFlag = getPacketFlag(list.get(EFFECTS_ID));
						if (entFlag == null) {
							continue;
						}
						
						if (((entFlag & invisibleFlag) == invisibleFlag) && (entFlag <= drinkingFlag)) {
							byte visibleMask = (byte) (entFlag - (entFlag & invisibleFlag));
							WatchableObject modified = new WatchableObject(list.get(EFFECTS_ID).c(), list.get(EFFECTS_ID).a(), visibleMask);
							list.set(EFFECTS_ID, modified);
							mod.write(i, list);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		event.setPacket(packet);
	}
}
