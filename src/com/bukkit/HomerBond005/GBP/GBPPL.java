package com.bukkit.HomerBond005.GBP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.util.config.Configuration;
import org.yaml.snakeyaml.Yaml;

import ru.tehkode.permissions.PermissionGroup;

import java.util.HashMap;

@SuppressWarnings("deprecation")
public class GBPPL extends EntityListener{
	static String mainDir = "plugins/GroupBasedPVP";
	static File penalties = new File (mainDir + File.separator + "penalties.yml");
	static Configuration bukkitpenalties = new Configuration(penalties);
	static FileInputStream penaltiesinput = null;
	public static GBP plugin;
	Yaml yaml = new Yaml();
	public GBPPL(GBP gbp) {
		plugin = gbp;
	}
	@SuppressWarnings({ "static-access", "unchecked" })
	public void onEntityDamage(EntityDamageEvent event){
		int gift = 0;
		int penalty = 0;
		try {
			penaltiesinput = new FileInputStream(penalties);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if(!(penalties.exists())){
			try{
				penalties.createNewFile();
				bukkitpenalties.setProperty("HealthAttackedPlayer", "0");
				bukkitpenalties.setProperty("HealthAttackingPlayer", "-5");
				bukkitpenalties.save();
				System.out.println("[GroupBasedPVP]: penalties.yml created.");
			}catch(IOException e){
				e.printStackTrace();
			}
			gift = 0;
			penalty = -5;
		}else{
			HashMap<Object,Object> yamlobj = (HashMap<Object,Object>)yaml.load(penaltiesinput);
			try{
				penaltiesinput.close();
			}catch (IOException e1){
				e1.printStackTrace();
			}
			penalty = Integer.parseInt(yamlobj.get("HealthAttackingPlayer").toString());
			gift = Integer.parseInt(yamlobj.get("HealthAttackedPlayer").toString());
		}
		Player player = null;
		Player damager = null;
		/*if(event.getCause() == DamageCause.PROJECTILE){
			
		}else{*/
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
				return;
			}
		//}
		//Check for permissions
		if(plugin.hasPermission(damager, "GroupBasedPVP.pvp.everyone")){
			return;
		}
		if(plugin.hasPermission(player, "GroupBasedPVP.pvp.protect")){
			damager.sendMessage(ChatColor.RED + "The player " + player.getDisplayName() + " can't be attacked by anyone.");
			event.setCancelled(true);
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
			return;
		}
		if(plugin.hasPermission(damager, "GroupBasedPVP.pvp.disallow")){
			damager.sendMessage(ChatColor.RED + "You are not allowed to attack anyone.");
			event.setCancelled(true);
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
			return;
		}
		//Check for config
		int lengthofdamager;
		if(plugin.permBukkit){
			lengthofdamager = plugin.getGroups(damager).length;
		}else{
			lengthofdamager = plugin.getGroups(damager).length;
		}
		for(int i = 0; i < lengthofdamager; i++){
			String damagergroup;
			if(plugin.permBukkit == true){
				damagergroup = plugin.getGroups(damager)[i].toString().substring(11, plugin.getGroups(damager)[i].toString().length()-1);
			}else{
				damagergroup = ((PermissionGroup)plugin.getGroups(damager)[i]).getName();
			}
			String[] disallowedGroups = null;
			try{
				disallowedGroups = plugin.configyaml().get(damagergroup).toString().split(", ");
			}catch(NullPointerException excep){
				try{
					disallowedGroups[0] = plugin.configyaml().get(damagergroup).toString();
				}catch(NullPointerException excep2){
					return;
				}
			}
			for(int w = 0; w < disallowedGroups.length; w++){
				if(disallowedGroups[w].toCharArray()[0] == '*'){
					damager.sendMessage(ChatColor.RED + "The group " + damagergroup + " is not allowed to attack anyone!"); 
					event.setCancelled(true);
					System.out.println(damager.getDisplayName() + " tried to attack " + player.getDisplayName() + ", but isn't allowed.");
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
					return;
				}
				if(plugin.inGroup(player.getDisplayName(), disallowedGroups[w])){
					damager.sendMessage(ChatColor.RED + "The group " + damagergroup + " is not allowed to attack the group " + disallowedGroups[w] + "!"); 
					event.setCancelled(true);
					System.out.println(damager.getDisplayName() + " tried to attack " + player.getDisplayName() + ", but isn't allowed.");
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
					return;
				}
			}
		}
		event.setCancelled(false);
	}
}
