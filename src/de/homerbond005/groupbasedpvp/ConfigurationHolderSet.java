package de.homerbond005.groupbasedpvp;

import java.util.HashSet;
import java.util.Set;

/**
 * Class for storing the different ConfigurationHolder objects
 * 
 * @author HomerBond005
 * 
 */
public class ConfigurationHolderSet extends HashSet<ConfigurationHolder> {
	private static final long serialVersionUID = -5342016662131482449L;

	/**
	 * Get all ConfigurationHolder objects by the type
	 * 
	 * @param type The type to search
	 * @return A Set containing all found ConfigurationHolder objects
	 */
	public Set<ConfigurationHolder> getByType(ConfigurationType type) {
		Set<ConfigurationHolder> temp = new HashSet<ConfigurationHolder>();
		for (ConfigurationHolder holder : this) {
			if (holder.getType() == type)
				temp.add(holder);
		}
		return temp;
	}

	/**
	 * Get the ConfigurationHolder object for a region with the specific
	 * arguments
	 * 
	 * @param world The name of the world
	 * @param name The name of the region
	 * @return A ConfigurationHolder object if found or null
	 */
	public ConfigurationHolder getExactRegion(String world, String name) {
		for (ConfigurationHolder holder : this) {
			if (holder.getType() == ConfigurationType.REGION)
				if (holder.getWorld().equalsIgnoreCase(world))
					if (holder.getName().equalsIgnoreCase(name))
						return holder;
		}
		return null;
	}

	/**
	 * Get the ConfigurationHolder object for a specific world
	 * 
	 * @param world The name of the world
	 * @return A ConfigurationHolder object if found or null
	 */
	public ConfigurationHolder getExactWorld(String world) {
		for (ConfigurationHolder holder : this) {
			if (holder.getType() == ConfigurationType.WORLD)
				if (holder.getWorld().equalsIgnoreCase(world))
					if (holder.getName().equalsIgnoreCase(world))
						return holder;
		}
		return null;
	}

	/**
	 * Identifies a ConfigurationHolder as world or region
	 * 
	 * @author HomerBond005
	 * 
	 */
	public enum ConfigurationType {
		WORLD, REGION
	}
}
