package com.ticketing.TicketingSystem;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import com.ticketing.TicketingSystem.Seat.STATUS;

public class BestAvailableFinderStrategy implements IFinderStrategy<Seat> {
	Set<Seat> seats = new TreeSet<Seat>(new SeatCategoryComparator());

	@Override
	public boolean hasNext() {
		for(Seat s: seats) {
			System.out.println(" s catgory " + s.getSeatCategory());
			if (s.isAvailable()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Seat next() {
		Seat nextSeat = null;
		for(Seat s: seats) {
			try {
				s.getLock().tryLock(10, TimeUnit.MILLISECONDS);
				if (s.isAvailable()) { 
					s.setStatus(STATUS.HOLD);
					nextSeat = s;
					break;
				}
			} catch (InterruptedException e) {
				if (!s.isAvailable())
					s.setStatus(STATUS.AVAILABLE);
				e.printStackTrace();
			} finally {
				try { 
					s.getLock().unlock();
				} catch(IllegalMonitorStateException e) {
					System.out.println(" This thread did not acquire the lock " + Thread.currentThread().getId());
				}
			}
		}
		
		return nextSeat;
	}

	@Override
	public void setEntries(Seat[] seats) {
		for(Seat s: seats) {
			this.seats.add(s);
		}
	}
	
	
}
