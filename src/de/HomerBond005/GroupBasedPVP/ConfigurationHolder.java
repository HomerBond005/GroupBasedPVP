package de.HomerBond005.GroupBasedPVP;

import java.util.Map;
import java.util.Set;

import de.HomerBond005.GroupBasedPVP.ConfigurationHolderSet.ConfigurationType;

/**
 * Class to store the configuration of a region / world
 * 
 * @author HomerBond005
 * 
 */
public abstract class ConfigurationHolder {
	private final String world;
	private final String name;
	private Map<String, Set<String>> conf;

	/**
	 * Create a new object for saving the configuration of a region / world
	 * 
	 * @param world The name of the world
	 * @param name The name of the region/world
	 * @param conf A map with the group name as key and the disallowed groups as
	 *        value
	 */
	public ConfigurationHolder(String world, String name,
			Map<String, Set<String>> conf) {
		this.world = world;
		this.name = name;
		this.conf = conf;
	}

	/**
	 * Set the new configuration for this configuration part
	 * 
	 * @param conf A map with the group name as key and the disallowed groups as
	 *        value
	 */
	public void setConfiguration(Map<String, Set<String>> conf) {
		this.conf = conf;
	}

	/**
	 * Get the configuration for this configuration part
	 * 
	 * @return A map with the group name as key and the disallowed groups as
	 *         value
	 */
	public Map<String, Set<String>> getConfiguration() {
		return conf;
	}

	/**
	 * Get the world where this configuration part matches
	 * 
	 * @return The name of the world
	 */
	public String getWorld() {
		return world;
	}

	/**
	 * Get the name of the region/world where this configuration part matches
	 * 
	 * @return The name of the region/world
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the type of configuration
	 * 
	 * @return A ConfigurationType enum
	 */
	public abstract ConfigurationType getType();
}
