package com.bukkit.HomerBond005.GBP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.yaml.snakeyaml.Yaml;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;

import com.platymuus.bukkit.permissions.*;

@SuppressWarnings("deprecation")
public class GBP extends JavaPlugin{
	static String mainDir = "plugins/GroupBasedPVP";
	static File groupsconfig = new File (mainDir + File.separator + "config.yml");
	static File penalties = new File (mainDir + File.separator + "penalties.yml");
	static Configuration bukkitpenalties = new Configuration(penalties);
	static Configuration bukkitconfig = new Configuration(groupsconfig);
	static FileInputStream configinput = null;
	static FileInputStream penaltiesinput = null;
	private final GBPPL playerlistener = new GBPPL(this);
	Boolean PermissionsPlugin = false;
    PluginManager pm;
    static PermissionManager pexmanager;
    static PermissionsPlugin pbplugin;
    static boolean permBukkit;
    private boolean setupPermissions(){
    	if(pm.getPlugin("PermissionsBukkit") != null){
    		pbplugin = (PermissionsPlugin)pm.getPlugin("PermissionsBukkit");
    		permBukkit = true;
    		System.out.println("[GroupBasedPVP] using PermissionsBukkit.");
    	}else if(pm.getPlugin("PermissionsEx") != null){
    		pexmanager = new PermissionManager(new Configuration(new File("plugins/PermissionsEx/config.yml")));
    		permBukkit = false;
    		System.out.println("[GroupBasedPVP] using PermissionsEx.");
    	}else{
    		System.out.println("[GroupBasedPVP]: Please install PermissionsBukkit or PermissionsEx!");
    		pm.disablePlugin(this);
    	}
		return true;
    }
	public void onEnable(){
		pm = getServer().getPluginManager();
		if(setupPermissions() == false){
			return;
		}
	    pm.registerEvent(Event.Type.ENTITY_DAMAGE, playerlistener,
				Event.Priority.High, this);
		new File (mainDir).mkdir();
		if(!(groupsconfig.exists())){
			try{
				groupsconfig.createNewFile();
				bukkitconfig.setProperty("AttackerGroup", "AttackedGroup");
				bukkitconfig.save();
				System.out.println("[GroupBasedPVP]: config.yml created.");
			}catch(IOException e){
				e.printStackTrace();
			}
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
		System.out.println("[GroupBasedPVP]: config.yml loaded.");
		System.out.println("[GroupBasedPVP] is enabled.");
	}
	public void onDisable(){
		System.out.println("[GroupBasedPVP] is disabled.");
	}
	public Object[] getGroups(Player player){
		if(permBukkit == true){
			Object[] groups = pbplugin.getPlayerInfo(player.getDisplayName()).getGroups().toArray();
			return groups;
		}else{
			return pexmanager.getUser(player).getGroups(player.getWorld().getName());
		}
	}
	public static boolean inGroup(String playername, String comparingGroup){
		if(permBukkit == true){
			Object[] tempthing = pbplugin.getPlayerInfo(playername).getGroups().toArray();//contains("Group{name=" + comparingGroup + "}")){
			for(int f = 0; f < tempthing.length; f++){
				System.out.println(tempthing[f]);
				if(tempthing[f].toString().equalsIgnoreCase("Group{name=" + comparingGroup + "}")){
					return true;
				}
			}
		}else{
			for(PermissionGroup group : pexmanager.getUser(playername).getGroups()){
				if(group.getName() == comparingGroup){
					return true;
				}
			}
		}
		return false;
	}
	@SuppressWarnings("unchecked")
	public Map<Object, Object> configyaml(){
		Yaml yaml = new Yaml();
		try{
			configinput = new FileInputStream(mainDir + File.separator + "config.yml");
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
}
