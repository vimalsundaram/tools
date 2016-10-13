package com.ticketing.TicketingSystem;

import java.util.NavigableSet;

public interface IFinderStrategy<T> {
	boolean hasNext() ;
	T next();
	void setEntries(T[] entries);
}
