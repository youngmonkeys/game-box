package com.tvd12.gamebox.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ReadOnlySet<E> {
	
	private final Set<E> set;
	
	public ReadOnlySet(Set<E> set) {
		this.set = set;
	}
	
	public E getFirst() {
		Iterator<E> iter = set.iterator();
		if (iter.hasNext()) {
			return iter.next();
		}
		return null;
	}
	
	public void forEach(Consumer<E> var) {
		set.forEach(var);
	}
	
	public int size() {
		return set.size();
	}
	
	public List<E> copyToList() {
		return new ArrayList<>(set);
	}
}
