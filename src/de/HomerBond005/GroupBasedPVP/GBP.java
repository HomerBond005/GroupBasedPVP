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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import com.platymuus.bukkit.permissions.*;
import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.util.CalculableType;

public class GBP extends JavaPlugin{
	static String mainDir = "plugins/GroupBasedPVP";
	static File groupsconfig = new File (mainDir + File.separator + "config.yml");
	static File penalties = new File (mainDir + File.separator + "penalties.yml");
	static FileConfiguration bukkitpenalties = YamlConfiguration.loadConfiguration(penalties);
	static FileConfiguration bukkitconfig = YamlConfiguration.loadConfiguration(groupsconfig);
	static FileInputStream configinput = null;
	static FileInputStream penaltiesinput = null;
	private final GBPPL playerlistener = new GBPPL(this);
	Boolean PermissionsPlugin = false;
    PluginManager pm;
    PermissionManager pexmanager;
    PermissionsPlugin pbplugin;
    int permSys;
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
    	}else{
    		System.out.println("[GroupBasedPVP]: Please install PermissionsBukkit or PermissionsEx or bPermissions!");
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
				bukkitconfig.set("AttackerGroup", "AttackedGroup");
				bukkitconfig.save(groupsconfig);
				System.out.println("[GroupBasedPVP]: config.yml created.");
			}catch(IOException e){
				e.printStackTrace();
			}
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
		try {
			configinput = new FileInputStream(groupsconfig);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			penaltiesinput = new FileInputStream(penalties);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Yaml yaml = new Yaml();
		@SuppressWarnings("unchecked")
		HashMap<Object,Object> yamlobj = (HashMap<Object,Object>)yaml.load(penaltiesinput);
		try{
			penaltiesinput.close();
		}catch (IOException e1){
			e1.printStackTrace();
		}
		try{
			yamlobj.get("CannotBeAttacked").toString();
		}catch(NullPointerException e){
			try{
				bukkitpenalties.load(penalties);
			}catch(Exception e1){
			}
			bukkitpenalties.set("CannotBeAttacked", "The player %p can't be attacked by anyone.");
			bukkitpenalties.set("NoPermAttackAnyone", "You are not allowed to attack anyone.");
			bukkitpenalties.set("GroupNoPermAttackAnyone", "The group %g is not allowed to attack anyone!");
			bukkitpenalties.set("Group1NoPermAttackGroup2", "The group %g1 is not allowed to attack the group %g2!");
			try {
				bukkitpenalties.save(groupsconfig);
			} catch (IOException e1) {
			}
		}
		System.out.println("[GroupBasedPVP]: config.yml loaded.");
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
			List<Group> groups = playerinfo.getGroups();
			List<String> groupnames = new ArrayList<String>();
			for(Group group : groups){
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
		}else{
			return false;
		}
	}
}
