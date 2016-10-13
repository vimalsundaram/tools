package com.ticketing.TicketingSystem;

import java.util.concurrent.atomic.AtomicInteger;

public final class AvailableSeatCounter {
	private final Seat[] seats;
	private final AtomicInteger numSeatsAvailable ;	

	public AvailableSeatCounter(Seat[] seats) {
		this.seats = seats;
		this.numSeatsAvailable = new AtomicInteger();
		adjustNumAvailableSeats(seats.length);
	}

	public void adjustNumAvailableSeats(int count) {
		if (Math.abs(count) > seats.length) 
			throw new IllegalStateException("Cannot increment/decrement above range 0 to " + seats.length + ". Count " + count);

		int numSeats = numSeatsAvailable.get();
		if (numSeats + count > seats.length || numSeats + count < 0) {
			throw new IllegalStateException("Cannot increment/decrement more than 0 to " + seats.length + ". Count " + count + " numSeats " + numSeats);
		} else {
			numSeatsAvailable.addAndGet(count);
		}
	}
	
	public int getAvailableSeats() {
		return numSeatsAvailable.get();
	}
}
