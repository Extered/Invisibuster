package ru.extered.invisibuster;

import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ru.tehkode.permissions.bukkit.PermissionsEx;

public class InvisiListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void PlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		event.setCancelled(true);
		if (event.getMessage().toLowerCase().trim().equals("/ib") && PermissionsEx.getUser(player).has("ru.extered.ib.toggle"))
		{
			if (InvisiBuster.turnedOff.contains(player))
			{
				InvisiBuster.turnedOff.remove(player);
			}
			else
			{
				InvisiBuster.turnedOff.add(player);
			}
			InvisiBuster.instance.updateNearbyEntities(player);
			String state = InvisiBuster.turnedOff.contains(player) ? "off" : "on";
			player.sendMessage(ChatColor.AQUA + "[IB] Turned " + state);
			return;
		}
		else if (event.getMessage().toLowerCase().trim().equals("/ibr") && PermissionsEx.getUser(player).has("ru.extered.ib.update"))
		{
			InvisiBuster.instance.updateNearbyEntities(player);
			player.sendMessage(ChatColor.AQUA + "[IB] Updated!");
			return;
		}
		event.setCancelled(false);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPotionSplashEvent(PotionSplashEvent event)
	{
		Iterator<PotionEffect> effects = event.getPotion().getEffects().iterator();

		boolean hasInvisible = false; 
		while (effects.hasNext())
		{
			PotionEffectType type = effects.next().getType();
			if (type.equals(PotionEffectType.NIGHT_VISION))
			{
				hasInvisible = true;
				break;
			}
		}

		if (!hasInvisible)
			return;

		Iterator<LivingEntity> affected = event.getAffectedEntities().iterator();
		while (affected.hasNext())
		{
			LivingEntity entity = affected.next();
			if (entity instanceof Player)
			{
				InvisiBuster.instance.notifyNearbyPlayers((Player)entity);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event)
	{
		int type = event.getItem().getData().getData();
		if ((event.getItem().getType().equals(Material.POTION) && (type == 38 || type == 70)) || (event.getItem().getType().equals(Material.MILK_BUCKET)))
		{
			InvisiBuster.instance.notifyNearbyPlayers(event.getPlayer());
		}
	}
}
