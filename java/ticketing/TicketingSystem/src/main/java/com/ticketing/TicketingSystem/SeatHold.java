package com.ticketing.TicketingSystem;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import com.ticketing.TicketingSystem.Seat.STATUS;

public class SeatHold implements Comparable<SeatHold> {
	private static final AtomicInteger COUNTER = new AtomicInteger(0);
	private int holdPeriod;  
	private final int seatHoldId;
	private final String customerEmail;
	private long bookingTime; 
	private long holdTime; 
	private Set<Seat> seats;
	
	public SeatHold(int numSeats, String customerEmail, int holdPeriod) {
		this.seatHoldId = COUNTER.incrementAndGet();
		this.customerEmail = customerEmail;
		this.bookingTime = System.currentTimeMillis();
		this.setHoldPeriod(holdPeriod);
		this.holdTime = this.bookingTime + holdPeriod;
		this.seats = new HashSet<Seat>(numSeats);
	}
	public int getSeatHoldId() {
		return seatHoldId;
	}
	
	public String getCustomerEmail() {
		return customerEmail;
	}
	
	public long getBookingTime() {
		return bookingTime;
	}
	
	public long getHoldTime() {
		return holdTime;
	}
	
	public Set<Seat> getSeats() {
		return seats;
	}
	
	public void addSeat(Seat s) {
		seats.add(s);
		s.setSeatHold(this);
	}
	
	public void clearSeats() {
		seats.forEach(s -> s.setSeatHold(null));
		seats.clear();
	}

	public void setSeats(Set<Seat> seats) {
		this.seats = seats;
		seats.forEach(s -> s.setSeatHold(this));
	}
	
	private boolean tryLockSeats() throws InterruptedException {
		boolean locked = true;
		for(Seat s: seats) {
			locked &= s.getLock().tryLock(200, TimeUnit.MILLISECONDS);
			if (!locked) {
				unlockSeats();
			}
		}
	
		return locked;
	}
	
	private void unlockSeats() {
		seats.forEach(s -> s.getLock().unlock());
	}
	
	public int confirmHold() {
		int count = 0;
		try {
			if (tryLockSeats()) {
				for(Seat s: seats) {
					if (s.isAvailable()) { 
						s.setStatus(STATUS.HOLD);
						count++;
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			unlockSeats();
		}
		return count;
	}
	

	
	public int confirmReservation() {
		int count = 0;

		try {
			if (tryLockSeats()) {
				for(Seat s: seats) {
					if (s.isHeld()) { 
						s.setStatus(STATUS.RESERVED);
						count++;
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			unlockSeats();
		}
		return count;
	}
	
	public int releaseHold() {
		int count = 0;
		try {
			if (tryLockSeats()) {
				for(Seat s: seats) {
					if (s.isHeld()) { 
						s.setStatus(STATUS.AVAILABLE);
						count++;
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			unlockSeats();
		}
		return count;
	}

	public int releaseReservation() {
		int count = 0;

		try {
			if (tryLockSeats()) {
				for(Seat s: seats) {
					if (s.isReserved()) { 
						s.setStatus(STATUS.AVAILABLE);
						count++;
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			unlockSeats();
		}
		return count;

	}

	public long getExpiryTime(long now) {
		return (holdTime - now);
	}
	
	@Override
	public int compareTo(SeatHold o) {
			long now = System.currentTimeMillis();
			if (this.getExpiryTime(now) - o.getExpiryTime(now) < 0) {
				return -1;
			} else if (this.getExpiryTime(now) - o.getExpiryTime(now) > 0) {
				return 1;	
			} else {
				return 0;
			}
	}
	
	public boolean equals(Object obj) {
		if (null == obj)
			return false;
		
		if (! (obj instanceof SeatHold) )
			return false; 
		
		SeatHold other = (SeatHold) obj;
		
		if (this.seatHoldId != other.seatHoldId)
			return false;
		
		if (this.customerEmail != other.customerEmail)
			return false;

		return true;
	}
	
	public int hashCode() {
		return seatHoldId * 37 + customerEmail.hashCode();
	}
	
	@Override
	public String toString() {
		return "SeatHold [seatHoldId=" + seatHoldId + ", customerEmail=" + customerEmail + ", bookingTime="
				+ bookingTime + ", holdTime=" + holdTime +  ", holdPeriod=" +(holdTime-bookingTime) +", seats=" + seats + "]";
	}

	
	public int getHoldPeriod() {
		return holdPeriod;
	}
	public void setHoldPeriod(int holdPeriod) {
		this.holdPeriod = holdPeriod;
	}


	static class SeatHoldPredicate implements Predicate<SeatHold> {  
		SeatHold seatHold;  
		
		public boolean test(SeatHold sh) {  
			if (seatHold.getSeatHoldId() == sh.getSeatHoldId()
					&& seatHold.getCustomerEmail().equals(sh.getCustomerEmail())) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	public boolean isHeld() {
		boolean isHeld = true;
		for(Seat s: seats) {
			isHeld &= s.isHeld();
		}
		return isHeld;
	}

	public boolean isReserved() {
		boolean isReserved = true;
		for(Seat s: seats) {
			isReserved &= s.isReserved();
		}
		return isReserved;
	}

}
