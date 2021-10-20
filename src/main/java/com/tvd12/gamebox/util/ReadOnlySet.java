package com.tvd12.gamebox.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ReadOnlySet<E> {
	
	private final Set<E> set;
	private final Iterator<E> iterator;
	
	public ReadOnlySet(Set<E> set) {
		this.set = set;
		this.iterator = set.iterator();
	}
	
	public boolean hasNext() {
		return iterator.hasNext();
	}
	
	public E next() {
		return iterator.next();
	}
	
	public void forEachRemaining(Consumer<E> var) {
		iterator.forEachRemaining(var);
	}
	
	public int size() {
		return set.size();
	}
	
	public List<E> copyToList() {
		return new ArrayList<>(set);
	}
}
