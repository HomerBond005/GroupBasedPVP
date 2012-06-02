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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.mcstats.Metrics.Metrics;
import org.yaml.snakeyaml.Yaml;
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
	private static File groupsconfig = new File (mainDir + File.separator + "config.yml");
	private static File penalties = new File (mainDir + File.separator + "penalties.yml");
	private static FileConfiguration bukkitpenalties = YamlConfiguration.loadConfiguration(penalties);
	private static FileConfiguration bukkitconfig = YamlConfiguration.loadConfiguration(groupsconfig);
	private static FileInputStream configinput = null;
	private final GBPPL playerlistener = new GBPPL(this);
	private Metrics metrics;
    private PluginManager pm;
    private PermissionManager pexmanager;
    private PermissionsPlugin pbplugin;
    private GroupManager groupmanager;
    private int permSys;
    private boolean logConsole;
    private boolean setupPermissions(){
    	if(pm.getPlugin("PermissionsBukkit") != null){
    		pbplugin = (PermissionsPlugin)pm.getPlugin("PermissionsBukkit");
    		permSys = 1;
    		System.out.println("[GroupBasedPVP] using PermissionsBukkit.");
    	}else if(pm.getPlugin("PermissionsEx") != null){
    		pexmanager = PermissionsEx.getPermissionManager();
    		permSys = 2;
    		System.out.println("[GroupBasedPVP] using PermissionsEx.");
    	}else if(pm.getPlugin("bPermissions") != null){
			permSys = 3;
			System.out.println("[GroupBasedPVP] using bPermissions.");
    	}else if(pm.getPlugin("GroupManager") != null){
    		groupmanager = (GroupManager) pm.getPlugin("GroupManager");
			permSys = 4;
			System.out.println("[GroupBasedPVP] using GroupManager.");
    	}else{
    		System.err.println("[GroupBasedPVP]: Please install PermissionsBukkit or PermissionsEx or bPermissions or GroupManager!");
    		pm.disablePlugin(this);
    		return false;
    	}
		return true;
    }
	public void onEnable(){
		pm = getServer().getPluginManager();
		if(setupPermissions() == false){
			return;
		}
	    pm.registerEvents(playerlistener, this);
		new File (mainDir).mkdir();
		if(!(groupsconfig.exists())){
			try{
				groupsconfig.createNewFile();
				System.out.println("[GroupBasedPVP]: config.yml created.");
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		if(!(penalties.exists())){
			try{
				penalties.createNewFile();
				System.out.println("[GroupBasedPVP]: penalties.yml created.");
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		try{
			bukkitconfig.load(groupsconfig);
			addDefault(bukkitconfig, "AttackerGroup", "AttackedGroup");
			bukkitconfig.save(groupsconfig);
			bukkitpenalties.load(penalties);
			addDefault(bukkitpenalties, "HealthAttackedPlayer", "0");
			addDefault(bukkitpenalties, "HealthAttackingPlayer", "-5");
			addDefault(bukkitpenalties, "CannotBeAttacked", "The player %p can't be attacked by anyone.");
			addDefault(bukkitpenalties, "NoPermAttackAnyone", "You are not allowed to attack anyone.");
			addDefault(bukkitpenalties, "GroupNoPermAttackAnyone", "The group %g is not allowed to attack anyone!");
			addDefault(bukkitpenalties, "Group1NoPermAttackGroup2", "The group %g1 is not allowed to attack the group %g2!");
			addDefault(bukkitpenalties, "logInConsole", true);
			bukkitpenalties.save(penalties);
		}catch(Exception e){}
		try {
			configinput = new FileInputStream(groupsconfig);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		logConsole = bukkitpenalties.getBoolean("logInConsole", true);
		System.out.println("[GroupBasedPVP]: config.yml and penalties.yml loaded.");
		try {
			metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			System.err.println("[GroupBasedPVP]: Error while enabling Metrics.");
		}
		System.out.println("[GroupBasedPVP] is enabled.");
	}
	public void onDisable(){
		System.out.println("[GroupBasedPVP] is disabled.");
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
	@SuppressWarnings("unchecked")
	public Map<Object, Object> configyaml(){
		Yaml yaml = new Yaml();
		try{
			configinput = new FileInputStream(groupsconfig);
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}
		Map<Object,Object> tempmap = (Map<Object, Object>) yaml.load(configinput);
		try{
			configinput.close();
		}catch (IOException e){
			e.printStackTrace();
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
			System.out.println(msg);
	}
	public void addDefault(FileConfiguration conf, String path, String value){
		if(!conf.isSet(path))
			conf.set(path, value);
	}
	public void addDefault(FileConfiguration conf, String path, boolean value){
		if(!conf.isSet(path))
			conf.set(path, value);
	}
}
