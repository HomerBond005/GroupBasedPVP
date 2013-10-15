package de.homerbond005.groupbasedpvp;

import java.util.Map;
import java.util.Set;

import de.homerbond005.groupbasedpvp.ConfigurationHolderSet.ConfigurationType;

/**
 * Class to store a ConfigurationHolder for a region
 * 
 * @author HomerBond005
 * 
 */
public class RegionConfigurationHolder extends ConfigurationHolder {

	/**
	 * Construct a new ConfigurationHolder
	 * 
	 * @param world The name of the world
	 * @param name The name of the region
	 * @param conf A map with the group name as key and the disallowed groups as
	 *        value
	 */
	public RegionConfigurationHolder(String world, String name,
			Map<String, Set<String>> conf) {
		super(world, name, conf);
	}

	@Override
	public ConfigurationType getType() {
		return ConfigurationType.REGION;
	}

}
