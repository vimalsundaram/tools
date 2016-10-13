package com.ticketing.TicketingSystem;

public class DefaultFinderStrategy implements IFinderStrategy<Seat> {
	Seat[] seats;

	@Override
	public boolean hasNext() {
		for(Seat s: seats) {
			if (s.isAvailable()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Seat next() {
		for(Seat s: seats) {
			if (s.isAvailable()) {
				return s; 
			}
		}
		return null;
	}
	
	@Override
	public void setEntries(Seat[] objects) {
		this.seats = objects;
	}

}
