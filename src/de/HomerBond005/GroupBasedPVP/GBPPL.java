/*
 * Copyright HomerBond005
 * 
 *  Published under CC BY-NC-ND 3.0
 *  http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package de.HomerBond005.GroupBasedPVP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.entity.CraftArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.yaml.snakeyaml.Yaml;

public class GBPPL implements Listener{
	static String mainDir = "plugins/GroupBasedPVP";
	static File penalties = new File (mainDir + File.separator + "penalties.yml");
	static FileConfiguration bukkitpenalties = YamlConfiguration.loadConfiguration(penalties);
	static FileInputStream penaltiesinput = null;
	String stringCannotBeAttacked = "";
	String stringNoPermAttackAnyone = "";
	String stringGroupNoPermAttackAnyone = "";
	String stringGroup1NoPermAttackGroup2 = "";
	int gift = 0;
	int penalty = 0;
	public static GBP plugin;
	Yaml yaml = new Yaml();
	public GBPPL(GBP gbp){
		plugin = gbp;
	}
	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityDamage(EntityDamageEvent event){
		Player player = null;
		Player damager = null;
		EntityDamageByEntityEvent e;
		try{
			e = (EntityDamageByEntityEvent) event;
		}catch(ClassCastException classevent){
			return;
		}
		try{
			player = (Player) event.getEntity();
		}catch(ClassCastException classevent){
			return;
		}
		try{
			damager = (Player) e.getDamager();
		}catch(ClassCastException classevent){
			try{
				damager = (Player)((CraftArrow)e.getDamager()).getShooter();
			}catch(Exception exce){
					return;
			}
		}
		//Same as in potion listener
		loadConfig();
		if(plugin.hasPermission(damager, "GroupBasedPVP.pvp.everyone")){
			return;
		}
		//End of same as in potion listener
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
		//Same as in other listener
		loadConfig();
		if(plugin.hasPermission(damager, "GroupBasedPVP.pvp.everyone")){
			return;
		}
		//End of same as in other listener
		for(Player player : players){
			if(handleDamagerGroups(damager, player))
				event.setCancelled(true);
		}
	}
	@SuppressWarnings("unchecked")
	private void loadConfig(){
		try{
			penaltiesinput = new FileInputStream(penalties);
		}catch (FileNotFoundException e){
			e.printStackTrace();
		}
		if(!(penalties.exists())){
			try{
				penalties.createNewFile();
				bukkitpenalties.set("HealthAttackedPlayer", "0");
				bukkitpenalties.set("HealthAttackingPlayer", "-5");
				bukkitpenalties.set("CannotBeAttacked", "The player %p can't be attacked by anyone.");
				bukkitpenalties.set("NoPermAttackAnyone", "You are not allowed to attack anyone.");
				bukkitpenalties.set("GroupNoPermAttackAnyone", "The group %g is not allowed to attack anyone!");
				bukkitpenalties.set("Group1NoPermAttackGroup2", "The group %g1 is not allowed to attack the group %g2!");
				bukkitpenalties.save(penalties);
				System.out.println("[GroupBasedPVP]: penalties.yml created.");
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		HashMap<Object,Object> yamlobj = (HashMap<Object,Object>)yaml.load(penaltiesinput);
		try{
			penaltiesinput.close();
		}catch (IOException e1){
			e1.printStackTrace();
		}
		penalty = Integer.parseInt(yamlobj.get("HealthAttackingPlayer").toString());
		gift = Integer.parseInt(yamlobj.get("HealthAttackedPlayer").toString());
		stringCannotBeAttacked = yamlobj.get("CannotBeAttacked").toString();
		stringNoPermAttackAnyone = yamlobj.get("NoPermAttackAnyone").toString();
		stringGroupNoPermAttackAnyone = yamlobj.get("GroupNoPermAttackAnyone").toString();
		stringGroup1NoPermAttackGroup2 = yamlobj.get("Group1NoPermAttackGroup2").toString();
		try {
			bukkitpenalties.load(penalties);
		}catch(Exception e1){
		}
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
					System.out.println(damager.getDisplayName() + "[" + damagergroup + "] tried to attack " + player.getDisplayName() + ", but isn't allowed to attack anyone.");
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
					System.out.println(damager.getDisplayName() + "[" + damagergroup + "] tried to attack " + player.getDisplayName() + " [" + disallowedGroups[w] + "], but isn't allowed.");
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
