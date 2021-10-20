package com.tvd12.gamebox.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ReadOnlyList<E> {
	
	private final List<E> list;
	
	public ReadOnlyList(List<E> list) {
		this.list = list;
	}
	
	public E get(int id) {
		return list.get(id);
	}
	
	public int size() {
		return list.size();
	}
	
	public void forEach(Consumer<E> var) {
		list.forEach(var);
	}
	
	public List<E> copyToList() {
		return new ArrayList<>(list);
	}
}
