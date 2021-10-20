package com.tvd12.gamebox.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class ReadOnlyCollection<E> {
	
	private final Collection<E> collection;
	
	public ReadOnlyCollection(Collection<E> collection) {
		this.collection = collection;
	}
	
	public E getFirst() {
		Iterator<E> iterator = collection.iterator();
		if (iterator.hasNext()) {
			return iterator.next();
		}
		return null;
	}
	
	public int size() {
		return collection.size();
	}
	
	public void forEach(Consumer<E> var) {
		collection.forEach(var);
	}
	
	public List<E> copyToList() {
		return new ArrayList<>(collection);
	}
}
