/*
 * Copyright HomerBond005
 * 
 *  Published under CC BY-NC-ND 3.0
 *  http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package de.HomerBond005.GroupBasedPVP;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import com.platymuus.bukkit.permissions.*;
import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.util.CalculableType;
import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;

public class GBP extends JavaPlugin{
	private static String mainDir = "plugins/GroupBasedPVP";
	private static File penalties = new File (mainDir + File.separator + "penalties.yml");
	private static FileConfiguration bukkitpenalties = YamlConfiguration.loadConfiguration(penalties);
	private final GBPPL playerlistener = new GBPPL(this);
	private Metrics metrics;
    private PluginManager pm;
    private PermissionManager pexmanager;
    private PermissionsPlugin pbplugin;
    private GroupManager groupmanager;
    private int permSys;
    private boolean logConsole;
    private Logger log;
    private Updater updater;
    
    @Override
	public void onEnable(){
		log = getLogger();
		pm = getServer().getPluginManager();
		if(setupPermissions() == false){
			return;
		}
	    pm.registerEvents(playerlistener, this);
		if(!(penalties.exists())){
			try{
				penalties.createNewFile();
				log.log(Level.INFO, "penalties.yml created.");
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		if(!new File(getDataFolder(), "plugin.yml").exists()){
			getConfig().set("AttackerGroup", "AttackedGroup");
			saveConfig();
			log.log(Level.INFO, "config.yml created.");
		}
		reloadConfig();
		reloadPenalties();
		bukkitpenalties.options().copyDefaults(true);
		bukkitpenalties.addDefault("HealthAttackedPlayer", "0");
		bukkitpenalties.addDefault("HealthAttackingPlayer", "-5");
		bukkitpenalties.addDefault("CannotBeAttacked", "The player %p can't be attacked by anyone.");
		bukkitpenalties.addDefault("NoPermAttackAnyone", "You are not allowed to attack anyone.");
		bukkitpenalties.addDefault("GroupNoPermAttackAnyone", "The group %g is not allowed to attack anyone!");
		bukkitpenalties.addDefault("Group1NoPermAttackGroup2", "The group %g1 is not allowed to attack the group %g2!");
		bukkitpenalties.addDefault("logInConsole", true);
		savePenalties();
		logConsole = bukkitpenalties.getBoolean("logInConsole", true);
		log.log(Level.INFO, "config.yml and penalties.yml loaded.");
		try {
			metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			log.log(Level.WARNING, "Error while enabling Metrics.");
		}
		updater = new Updater(this);
		getServer().getPluginManager().registerEvents(updater, this);
		log.log(Level.INFO, "is enabled.");
	}
    
    @Override
	public void onDisable(){
		log.log(Level.INFO, "is disabled.");
	}
    
    private boolean setupPermissions(){
    	if(pm.getPlugin("PermissionsBukkit") != null){
    		pbplugin = (PermissionsPlugin)pm.getPlugin("PermissionsBukkit");
    		permSys = 1;
    		log.log(Level.INFO, "using PermissionsBukkit.");
    	}else if(pm.getPlugin("PermissionsEx") != null){
    		pexmanager = PermissionsEx.getPermissionManager();
    		permSys = 2;
    		log.log(Level.INFO, "using PermissionsEx.");
    	}else if(pm.getPlugin("bPermissions") != null){
			permSys = 3;
			log.log(Level.INFO, "using bPermissions.");
    	}else if(pm.getPlugin("GroupManager") != null){
    		groupmanager = (GroupManager) pm.getPlugin("GroupManager");
			permSys = 4;
			log.log(Level.INFO, "using GroupManager.");
    	}else{
    		log.log(Level.WARNING, "Please install PermissionsBukkit or PermissionsEx or bPermissions or GroupManager!");
    		pm.disablePlugin(this);
    		return false;
    	}
		return true;
    }
    
	public String[] getGroups(Player player){
		if(permSys == 1){
			PermissionInfo playerinfo = pbplugin.getPlayerInfo(player.getName());
			if(playerinfo == null)
				return new String[0];
			List<com.platymuus.bukkit.permissions.Group> groups = playerinfo.getGroups();
			List<String> groupnames = new ArrayList<String>();
			for(com.platymuus.bukkit.permissions.Group group : groups){
				groupnames.add(group.getName());
			}
			return groupnames.toArray(new String[groupnames.size()]);
		}else if(permSys == 2){
			PermissionGroup[] groups = pexmanager.getUser(player).getGroups(player.getWorld().getName());
			String[] groupnames = new String[groups.length];
			for(int i = 0; i < groups.length; i++){
				groupnames[i] = groups[i].getName();
			}
			return groupnames;
		}else if(permSys == 3){
			return ApiLayer.getGroups(player.getWorld().getName(), CalculableType.USER, player.getName());
		}else if(permSys == 4){
			AnjoPermissionsHandler holder = groupmanager.getWorldsHolder().getWorldPermissionsByPlayerName(player.getName());
			if (holder == null) {
	            return new String[0];
	        }
	        return holder.getGroups(player.getName());
		}else{
			return new String[0];
		}
	}
	
	public boolean inGroup(Player player, String comparingGroup){
		String[] groups = getGroups(player);
		for(String group : groups){
			if(group.equalsIgnoreCase(comparingGroup))
				return true;
		}
		return false;
	}
	
	public Map<String, String> configyaml(){
		reloadConfig();
		Map<String, String> tempmap = new HashMap<String, String>();
		Set<String> keys = getConfig().getKeys(false);
		for(String key : keys){
			tempmap.put(key, getConfig().getString(key));
		}
		return tempmap;
	}
	
	public boolean hasPermission(Player player, String permission){
		if(permSys == 1){
			return player.hasPermission(permission);
		}else if(permSys == 2){
			return pexmanager.has(player, permission);
		}else if(permSys == 3){
			return ApiLayer.hasPermission(player.getWorld().getName(), CalculableType.USER, player.getName(), permission);
		}else if(permSys == 4){
			AnjoPermissionsHandler holder = groupmanager.getWorldsHolder().getWorldPermissionsByPlayerName(player.getName());
			if (holder == null) {
	            return false;
	        }
	        return holder.permission(player.getName(), permission);
		}else{
			return false;
		}
	}
	
	public void printConsoleMsg(String msg){
		if(logConsole)
			log.log(Level.INFO, msg);
	}
	
	public void addDefault(FileConfiguration conf, String path, String value){
		if(!conf.isSet(path))
			conf.set(path, value);
	}
	
	public void reloadPenalties(){
		bukkitpenalties = YamlConfiguration.loadConfiguration(penalties);
	}
	
	public void savePenalties(){
		try {
			bukkitpenalties.save(penalties);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String[] getPenalties(){
		try {
			bukkitpenalties.load(penalties);
		}catch(Exception e1){
		}
		String[] temparr = new String[6];
		temparr[0] = bukkitpenalties.getString("HealthAttackingPlayer", "-5");
		temparr[1] = bukkitpenalties.getString("HealthAttackedPlayer", "0");
		temparr[2] = bukkitpenalties.getString("CannotBeAttacked", "The player %p can't be attacked by anyone.");
		temparr[3] = bukkitpenalties.getString("NoPermAttackAnyone", "You are not allowed to attack anyone.");
		temparr[4] = bukkitpenalties.getString("GroupNoPermAttackAnyone", "The group %g is not allowed to attack anyone!");
		temparr[5] = bukkitpenalties.getString("Group1NoPermAttackGroup2", "The group %g1 is not allowed to attack the group %g2!");
		return temparr;
	}
}
