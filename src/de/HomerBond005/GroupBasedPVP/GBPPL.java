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

public class GBPPL implements Listener{
	private String stringCannotBeAttacked = "";
	private String stringNoPermAttackAnyone = "";
	private String stringGroupNoPermAttackAnyone = "";
	private String stringGroup1NoPermAttackGroup2 = "";
	private int gift = 0;
	private int penalty = 0;
	private static GBP plugin;
	
	public GBPPL(GBP gbp){
		plugin = gbp;
	}
	
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
		loadConfig();
		if(plugin.hasPermission(damager, "GroupBasedPVP.pvp.everyone")){
			return;
		}
		if(handleDamagerGroups(damager, player))
			event.setCancelled(true);
	}
	
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
		loadConfig();
		if(plugin.hasPermission(damager, "GroupBasedPVP.pvp.everyone")){
			return;
		}
		for(Player player : players){
			if(handleDamagerGroups(damager, player))
				event.setCancelled(true);
		}
	}
	private void loadConfig(){
		String[] loaded = plugin.getPenalties();
		penalty = Integer.parseInt(loaded[0]);
		gift = Integer.parseInt(loaded[1]);
		stringCannotBeAttacked = loaded[2];
		stringNoPermAttackAnyone = loaded[3];
		stringGroupNoPermAttackAnyone = loaded[4];
		stringGroup1NoPermAttackGroup2 = loaded[5];
	}
	private boolean handleDamagerGroups(Player damager, Player player){
		if(plugin.hasPermission(damager, "GroupBasedPVP.pvp.disallow")){
			if(stringNoPermAttackAnyone.length() != 0)
				damager.sendMessage(ChatColor.RED + stringNoPermAttackAnyone);
			try{
				damager.setHealth(damager.getHealth() + penalty);
			}catch(IllegalArgumentException exe){
				damager.setHealth(0);
			}
			try{
				player.setHealth(player.getHealth() + gift);
			}catch(IllegalArgumentException exe){
				player.setHealth(20);
			}
			return true;
		}
		if(plugin.hasPermission(player, "GroupBasedPVP.pvp.protect")){
			if(stringCannotBeAttacked.length() != 0)
				damager.sendMessage(ChatColor.RED + stringCannotBeAttacked.replaceAll("%p", player.getDisplayName()));
			try{
				damager.setHealth(damager.getHealth() + penalty);
			}catch(IllegalArgumentException exe){
				damager.setHealth(0);
			}
			try{
				player.setHealth(player.getHealth() + gift);
			}catch(IllegalArgumentException exe){
				player.setHealth(20);
			}
			return true;
		}
		int lengthofdamager;
		lengthofdamager = plugin.getGroups(damager).length;
		for(int i = 0; i < lengthofdamager; i++){
			String damagergroup = plugin.getGroups(damager)[i];
			String[] disallowedGroups = null;
			try{
				disallowedGroups = plugin.configyaml().get(damagergroup).toString().split(", ");
			}catch(NullPointerException excep){
				try{
					disallowedGroups[0] = plugin.configyaml().get(damagergroup).toString();
				}catch(NullPointerException excep2){
					return false;
				}
			}
			for(int w = 0; w < disallowedGroups.length; w++){
				if(disallowedGroups[w].toCharArray()[0] == '*'){
					if(stringGroupNoPermAttackAnyone.length() != 0)
						damager.sendMessage(ChatColor.RED + stringGroupNoPermAttackAnyone.replaceAll("%g", damagergroup)); 
					plugin.printConsoleMsg(damager.getDisplayName() + "[" + damagergroup + "] tried to attack " + player.getDisplayName() + ", but isn't allowed to attack anyone.");
					try{
						damager.setHealth(damager.getHealth() + penalty);
					}catch(IllegalArgumentException exe){
						damager.setHealth(0);
					}
					try{
						player.setHealth(player.getHealth() + gift);
					}catch(IllegalArgumentException exe){
						player.setHealth(20);
					}
					return true;
				}
				if(plugin.inGroup(player, disallowedGroups[w])){
					if(stringGroup1NoPermAttackGroup2.length() != 0)
						damager.sendMessage(ChatColor.RED + stringGroup1NoPermAttackGroup2.replaceAll("%g1", damagergroup).replaceAll("%g2", disallowedGroups[w]));
					plugin.printConsoleMsg(damager.getDisplayName() + "[" + damagergroup + "] tried to attack " + player.getDisplayName() + "[" + disallowedGroups[w] + "], but isn't allowed.");
					try{
						damager.setHealth(damager.getHealth() + penalty);
					}catch(IllegalArgumentException exe){
						damager.setHealth(0);
					}
					try{
						player.setHealth(player.getHealth() + gift);
					}catch(IllegalArgumentException exe){
						player.setHealth(20);
					}
					return true;
				}
			}
		}
		return false;
	}
}