package de.HomerBond005.GroupBasedPVP;

import java.util.Map;
import java.util.Set;
import de.HomerBond005.GroupBasedPVP.ConfigurationHolderSet.ConfigurationType;

public class ConfigurationHolder{
	private String world;
	private String name;
	private ConfigurationType type;
	private Map<String, Set<String>> conf;
	
	/**
	 * Create a new object for saving the configuration of a region / world
	 * @param world The name of the world
	 * @param name The name of the region/world
	 * @param type The type of ConfigurationHolder
	 */
	public ConfigurationHolder(String world, String name, ConfigurationType type){
		this.world = world;
		this.name = name;
		this.type = type;
	}
	
	/**
	 * Create a new object for saving the configuration of a region / world
	 * @param world The name of the world
	 * @param name The name of the region/world
	 * @param type The type of ConfigurationHolder
	 * @param conf A map with the group name as key and the disallowed groups as value
	 */
	public ConfigurationHolder(String world, String name, ConfigurationType type, Map<String, Set<String>> conf){
		this.world = world;
		this.name = name;
		this.type = type;
		this.conf = conf;
	}
	
	/**
	 * Set the new configuration for this configuration part
	 * @param conf A map with the group name as key and the disallowed groups as value
	 */
	public void setConfiguration(Map<String, Set<String>> conf){
		this.conf = conf;
	}
	
	public Map<String, Set<String>> getConfiguration(){
		return conf;
	}
	
	/**
	 * Get the world where this configuration part matches
	 * @return The name of the world
	 */
	public String getWorld(){
		return world;
	}
	
	/**
	 * Get the name of the region/world where this configuration part matches
	 * @return The name of the region/world
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Get the type of configuration
	 * @return A ConfigurationType enum
	 */
	public ConfigurationType getType(){
		return type;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof ConfigurationHolder))
			return false;
		ConfigurationHolder other = (ConfigurationHolder) obj;
		if(type == other.getType())
			if(world.equalsIgnoreCase(other.getWorld()))
				if(name.equalsIgnoreCase(other.getName()))
					return true;
		return false;
	}
	
	/**
	 * Check if a group is allowed to attack another group in this configuration part
	 * @param attackerGroup The name of the group that is attacking
	 * @param attackedGroup The name of the group that is attacked
	 * @return Is the group allowed to attack the other group?
	 */
	public boolean isAllowed(String attackerGroup, String attackedGroup){
		if(!conf.containsKey(attackerGroup))
			return true;
		if(conf.get(attackerGroup).contains(attackedGroup))
			return false;
		return true;
	}
}
