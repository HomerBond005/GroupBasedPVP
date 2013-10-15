package de.homerbond005.groupbasedpvp;

import java.util.Map;
import java.util.Set;

import de.homerbond005.groupbasedpvp.ConfigurationHolderSet.ConfigurationType;

/**
 * Class to store a ConfigurationHolder for a world
 * 
 * @author HomerBond005
 * 
 */
public class WorldConfigurationHolder extends ConfigurationHolder {

	/**
	 * Construct a new ConfigurationHolder
	 * 
	 * @param world The name of the world
	 * @param conf A map with the group name as key and the disallowed groups as
	 *        value
	 */
	public WorldConfigurationHolder(String world, Map<String, Set<String>> conf) {
		super(world, world, conf);
	}

	@Override
	public ConfigurationType getType() {
		return ConfigurationType.WORLD;
	}

}
