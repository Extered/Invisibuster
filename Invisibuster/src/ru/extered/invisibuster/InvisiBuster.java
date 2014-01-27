package ru.extered.invisibuster;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.v1_7_R1.WatchableObject;

import org.bukkit.craftbukkit.v1_7_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;

public class InvisiBuster extends JavaPlugin {

	private ProtocolManager protocolManager;
	private PacketAdapter pAdapter;

	public static InvisiBuster instance;
	public static List<Player> turnedOff = new ArrayList<Player>();

	public void onLoad()
	{
		instance = this;
	}

	public void onEnable() {
		protocolManager = ProtocolLibrary.getProtocolManager();

		PacketAdapter.AdapterParameteters params = new PacketAdapter.AdapterParameteters();
		params.plugin(this);
		params.serverSide();
		params.types(PacketType.Play.Server.ENTITY_METADATA);
		params.listenerPriority(ListenerPriority.NORMAL);
		pAdapter = new BusterPacketAdapter(params);

		protocolManager.addPacketListener(pAdapter);

		getServer().getPluginManager().registerEvents(new InvisiListener(), instance);


	}

	public void onDisable() {
		ProtocolLibrary.getProtocolManager().removePacketListeners(this);
	}

	public void sendEntityUpdate(Player receiver, CraftEntity entity)
	{
		ArrayList<WatchableObject> list = new ArrayList<WatchableObject>();

		Byte flags = entity.getHandle().getDataWatcher().getByte(0);

		WatchableObject state = new WatchableObject(0, 0, flags);
		list.add(state);

		PacketContainer flagContainer = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
		flagContainer.getModifier().write(0, entity.getEntityId());
		flagContainer.getModifier().write(1, list);

		try {
			protocolManager.sendServerPacket(receiver, flagContainer, true);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

	}
	
	public void updateNearbyEntities(Player receiver)
	{
		int radius = instance.getServer().getViewDistance() * 16;
		List<Entity> ents = receiver.getNearbyEntities(radius, radius, radius);

		for (Entity e : ents) {
			if (e instanceof CraftPlayer)
			{
				sendEntityUpdate(receiver, (CraftEntity)e);
			}
		}
	}
	
	public void notifyNearbyPlayers(Player center)
	{
		int radius = instance.getServer().getViewDistance() * 16;
		List<Entity> ents = center.getNearbyEntities(radius, radius, radius);

		for (Entity e : ents) {
			if (e instanceof CraftPlayer)
			{
				updateNearbyEntities((CraftPlayer)e);
			}
		}
	}
}
