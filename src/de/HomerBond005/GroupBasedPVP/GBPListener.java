/*
 * Copyright HomerBond005
 * 
 *  Published under CC BY-NC-ND 3.0
 *  http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package de.HomerBond005.GroupBasedPVP;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.entity.CraftArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.entity.Snowball;

public class GBPListener implements Listener{
	private String stringCannotBeAttacked = "";
	private String stringNoPermAttackAnyone = "";
	private String stringGroupNoPermAttackAnyone = "";
	private String stringGroup1NoPermAttackGroup2 = "";
	private int gift = 0;
	private int penalty = 0;
	private GBP plugin;
	
	public GBPListener(GBP gbp){
		plugin = gbp;
		String[] loaded = gbp.getSettings();
		penalty = Integer.parseInt(loaded[0]);
		gift = Integer.parseInt(loaded[1]);
		stringCannotBeAttacked = loaded[2];
		stringNoPermAttackAnyone = loaded[3];
		stringGroupNoPermAttackAnyone = loaded[4];
		stringGroup1NoPermAttackGroup2 = loaded[5];
	}
	
	/**
	 * Called when an entity gets damage
	 * @param event The event that is called
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityDamage(EntityDamageEvent event){
		Player player = null;
		Player damager = null;
		EntityDamageByEntityEvent e;
		if(event instanceof EntityDamageByEntityEvent)
			e = (EntityDamageByEntityEvent) event;
		else
			return;
		if(event.getEntity() instanceof Player)
			player = (Player) event.getEntity();
		else
			return;
		if(e.getDamager() instanceof Player)
			damager = (Player) e.getDamager();
		else if(e.getDamager() instanceof CraftArrow)
			if(((CraftArrow)e.getDamager()).getShooter() instanceof Player)
				damager = (Player)((CraftArrow)e.getDamager()).getShooter();
			else
				return;
		else if(e.getDamager() instanceof Snowball)
			if(((Snowball)e.getDamager()).getShooter() instanceof Player)
				damager = (Player)((Snowball)e.getDamager()).getShooter();
			else
				return;
		else
			return;
		if(plugin.isPVPCompletelyDisabled()){
			event.setCancelled(true);
			return;
		}
		if(plugin.hasPermission(damager, "GroupBasedPVP.pvp.everyone")){
			return;
		}
		if(handleDamagerGroups(damager, player))
			event.setCancelled(true);
	}
	
	/**
	 * Called when a potion splashes
	 * @param event The event object that is called
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onPotionSplashEvent(PotionSplashEvent event){
		Set<Player> players = new HashSet<Player>();
		for(LivingEntity possplayer : event.getAffectedEntities()){
			if(possplayer instanceof Player)
				players.add((Player) possplayer);
		}
		Player damager;
		if(event.getPotion().getShooter() instanceof Player)
			damager = (Player) event.getPotion().getShooter();
		else
			return;
		if(plugin.hasPermission(damager, "GroupBasedPVP.pvp.everyone")){
			return;
		}
		for(Player player : players){
			if(plugin.isPVPCompletelyDisabled())
				event.setIntensity(player, 0);
			else if(handleDamagerGroups(damager, player))
				event.setIntensity(player, 0);
		}
	}
	
	/**
	 * Check if a player can attack another player
	 * @param damager The player that is attacking
	 * @param damaged The player that is attacked
	 * @return Does the damager has insufficient permissions?
	 */
	private boolean handleDamagerGroups(Player damager, Player damaged){
		if(plugin.hasPermission(damager, "GroupBasedPVP.pvp.disallow")){
			if(stringNoPermAttackAnyone.length() != 0||stringNoPermAttackAnyone.equalsIgnoreCase("false"))
				damager.sendMessage(ChatColor.RED+stringNoPermAttackAnyone);
			try{
				damager.setHealth(damager.getHealth()+penalty);
			}catch(IllegalArgumentException exe){
				damager.setHealth(0);
			}
			try{
				damaged.setHealth(damaged.getHealth()+gift);
			}catch(IllegalArgumentException exe){
				damaged.setHealth(20);
			}
			return true;
		}
		if(plugin.hasPermission(damaged, "GroupBasedPVP.pvp.protect")){
			if(stringCannotBeAttacked.length() != 0||stringCannotBeAttacked.equalsIgnoreCase("false"))
				damager.sendMessage(ChatColor.RED + stringCannotBeAttacked.replaceAll("%p", damaged.getDisplayName()));
			try{
				damager.setHealth(damager.getHealth() + penalty);
			}catch(IllegalArgumentException exe){
				damager.setHealth(0);
			}
			try{
				damaged.setHealth(damaged.getHealth() + gift);
			}catch(IllegalArgumentException exe){
				damaged.setHealth(20);
			}
			return true;
		}
		int lengthofdamager = plugin.getGroups(damager).length;
		for(int i = 0; i < lengthofdamager; i++){
			String damagergroup = plugin.getGroups(damager)[i];
			Set<String> disallowedGroups = plugin.getDisallowedGroupsAtLocationForGroup(damaged.getLocation(), damagergroup);
			for(String disallowedGroup : disallowedGroups){
				boolean illegal = false;
				if(disallowedGroup.toCharArray()[0] == '*'){
					if(stringGroupNoPermAttackAnyone.length() != 0||stringGroupNoPermAttackAnyone.equalsIgnoreCase("false"))
						damager.sendMessage(ChatColor.RED + stringGroupNoPermAttackAnyone.replaceAll("%g", damagergroup)); 
					plugin.printConsoleMsg(damager.getDisplayName() + "[" + damagergroup + "] tried to attack " + damaged.getDisplayName() + ", but isn't allowed to attack anyone.");
					illegal = true;
				}else if(plugin.inGroup(damaged, disallowedGroup)){
					if(stringGroup1NoPermAttackGroup2.length() != 0||stringGroup1NoPermAttackGroup2.equalsIgnoreCase("false"))
						damager.sendMessage(ChatColor.RED + stringGroup1NoPermAttackGroup2.replaceAll("%g1", damagergroup).replaceAll("%g2", disallowedGroup));
					plugin.printConsoleMsg(damager.getDisplayName() + "[" + damagergroup+"] tried to attack "+damaged.getDisplayName()+"["+disallowedGroup+"], but isn't allowed.");
					illegal = true;
				}
				if(illegal == true){
					if(!plugin.hasPermission(damager, "GroupBasedPVP.pvpgroup."+disallowedGroup)){
						try{
							damager.setHealth(damager.getHealth() + penalty);
						}catch(IllegalArgumentException exe){
							damager.setHealth(0);
						}
						try{
							damaged.setHealth(damaged.getHealth() + gift);
						}catch(IllegalArgumentException exe){
							damaged.setHealth(20);
						}
						return true;
					}
				}
			}
		}
		return false;
	}
}