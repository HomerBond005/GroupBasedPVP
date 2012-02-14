package com.bukkit.HomerBond005.GBP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.entity.CraftArrow;
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
	String stringCannotBeAttacked = "";
	String stringNoPermAttackAnyone = "";
	String stringGroupNoPermAttackAnyone = "";
	String stringGroup1NoPermAttackGroup2 = "";
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
				bukkitpenalties.setProperty("CannotBeAttacked", "The player %p can't be attacked by anyone.");
				bukkitpenalties.setProperty("NoPermAttackAnyone", "You are not allowed to attack anyone.");
				bukkitpenalties.setProperty("GroupNoPermAttackAnyone", "The group %g is not allowed to attack anyone!");
				bukkitpenalties.setProperty("Group1NoPermAttackGroup2", "The group %g1 is not allowed to attack the group %g2!");
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
			stringCannotBeAttacked = yamlobj.get("CannotBeAttacked").toString();
			stringNoPermAttackAnyone = yamlobj.get("NoPermAttackAnyone").toString();
			stringGroupNoPermAttackAnyone = yamlobj.get("NoPermAttackAnyone").toString();
			stringGroup1NoPermAttackGroup2 = yamlobj.get("Group1NoPermAttackGroup2").toString();
			
		}
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
		bukkitpenalties.load();
		if(plugin.hasPermission(damager, "GroupBasedPVP.pvp.everyone")){
			return;
		}
		if(plugin.hasPermission(player, "GroupBasedPVP.pvp.protect")){
			damager.sendMessage(ChatColor.RED + stringCannotBeAttacked.replaceAll("%p", player.getDisplayName()));
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
			damager.sendMessage(ChatColor.RED + stringNoPermAttackAnyone);
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
					damager.sendMessage(ChatColor.RED + stringGroupNoPermAttackAnyone.replaceAll("%g", damagergroup)); 
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
					damager.sendMessage(ChatColor.RED + stringGroup1NoPermAttackGroup2.replaceAll("%g1", damagergroup).replaceAll("%g2", disallowedGroups[w])); 
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
