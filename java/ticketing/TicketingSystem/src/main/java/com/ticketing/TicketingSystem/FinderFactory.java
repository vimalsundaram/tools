package com.ticketing.TicketingSystem;

public final class FinderFactory {
	public static enum POLICY {
		BEST_AVAILABLE;
	}
	
	public static IFinderStrategy<Seat> getInstance(POLICY policy) throws TicketException {
		if (POLICY.BEST_AVAILABLE == policy) {
			return new BestAvailableFinderStrategy();
		} else {
			throw new TicketException("Cannot instantiate a Strategy for finder");
		}
	}
}
