package de.HomerBond005.GroupBasedPVP;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ConfigurationHolderSet implements Set<ConfigurationHolder>{
	private Set<ConfigurationHolder> data;
	
	/**
	 * Create a new Set of ConfigurationHolder objects
	 */
	public ConfigurationHolderSet(){
		data = new HashSet<ConfigurationHolder>();
	}
	
	/**
	 * @see java.util.Set#add(java.lang.Object)
	 */
	@Override
	public boolean add(ConfigurationHolder arg0){
		return data.add(arg0);
	}

	/**
	 * @see java.util.Set#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends ConfigurationHolder> arg0){
		return data.addAll(arg0);
	}

	/**
	 * @see java.util.Set#clear()
	 */
	@Override
	public void clear(){
		data.clear();
	}

	/**
	 * @see java.util.Set#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object arg0){
		return data.contains(arg0);
	}

	/**
	 * @see java.util.Set#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(Collection<?> arg0){
		return data.containsAll(arg0);
	}

	/**
	 * @see java.util.Set#isEmpty()
	 */
	@Override
	public boolean isEmpty(){
		return data.isEmpty();
	}

	/**
	 * @see java.util.Set#iterator()
	 */
	@Override
	public Iterator<ConfigurationHolder> iterator(){
		return data.iterator();
	}

	/**
	 * @see java.util.Set#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object arg0){
		return data.remove(arg0);
	}

	/**
	 * @see java.util.Set#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(Collection<?> arg0){
		return data.removeAll(arg0);
	}

	/**
	 * @see java.util.Set#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(Collection<?> arg0){
		return data.retainAll(arg0);
	}

	/**
	 * @see java.util.Set#size()
	 */
	@Override
	public int size(){
		return data.size();
	}

	/**
	 * @see java.util.Set#toArray()
	 */
	@Override
	public Object[] toArray(){
		return data.toArray();
	}

	/**
	 * @see java.util.Set#toArray(T[])
	 */
	@Override
	public <T> T[] toArray(T[] arg0){
		return data.toArray(arg0);
	}
	
	/**
	 * Get all ConfigurationHolderSet objects with the given name
	 * @param name
	 * @return
	 */
	public Set<ConfigurationHolder> getByName(String name){
		Set<ConfigurationHolder> temp = new HashSet<ConfigurationHolder>();
		for(ConfigurationHolder holder : data){
			if(holder.getName().equalsIgnoreCase(name))
				temp.add(holder);
		}
		return temp;
	}
	
	public Set<ConfigurationHolder> getByType(ConfigurationType type){
		Set<ConfigurationHolder> temp = new HashSet<ConfigurationHolder>();
		for(ConfigurationHolder holder : data){
			if(holder.getType() == type)
				temp.add(holder);
		}
		return temp;
	}
	
	public ConfigurationHolder getExact(String world, String name, ConfigurationType type){
		for(ConfigurationHolder holder : data){
			if(holder.getType() == type)
				if(holder.getWorld().equalsIgnoreCase(world))
					if(holder.getName().equalsIgnoreCase(name))
						return holder;
		}
		return null;
	}
	
	public enum ConfigurationType{
		WORLD,
		REGION
	}
}
