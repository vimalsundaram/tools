package com.ticketing.TicketingSystem;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class Seat implements Comparable<Seat> {
	public static enum STATUS {
		AVAILABLE,
		HOLD,
		RESERVED;
	}
	
	public static enum CATEGORY {
		BEST,
		STANDARD;
	}
	
	private int row;
	private int col;
	
	private CATEGORY seatCategory;
	private volatile STATUS seatStatus;

	private Lock lock = new ReentrantLock();
	
	private SeatHold seatHold;
	
	public Seat(int row, int col, CATEGORY category) {
		this.row = row;
		this.col = col;
		this.seatCategory = category;
		this.seatStatus = STATUS.AVAILABLE;	
	}
	
	public int getRow() { return row; }
	public int getCol() { return col;}
	public CATEGORY getSeatCategory() { return seatCategory; }
	public STATUS getStatus() { return seatStatus; }
		
	public void setStatus(STATUS update) {		
		lock.lock();
		switch(this.seatStatus) {
			case AVAILABLE:
				if (update == STATUS.HOLD) {
					this.seatStatus = update;
				}
				break;
				
			case HOLD:
				if (update != STATUS.HOLD) {
					this.seatStatus = update;
				}
				break;
				
			case RESERVED:
				if (update == STATUS.AVAILABLE) {
					this.seatStatus = update;
				}
				break;
				
			default:
				// do nothing
				break;
		}
		lock.unlock();
	}
	

	public Lock getLock() {
		return lock;
	}
	
	public boolean isAvailable() {
		return (this.seatStatus == STATUS.AVAILABLE);
	}
	
	public boolean isHeld() {
		return (this.seatStatus == STATUS.HOLD);
	}
	
	public boolean isReserved() {
		return (this.seatStatus == STATUS.RESERVED);
	}


	
	@Override
	public int compareTo(Seat s) {
		if (this.getRow() == s.getRow())
			return this.getCol() - s.getCol();
		else 
			return this.getRow() - s.getRow();
	}
	
	public boolean equals(Object obj) {
		if (null == obj)
			return false;
		
		if (! (obj instanceof Seat) )
			return false; 
		
		Seat other = (Seat) obj;
		
		if (this.row != other.row)
			return false;
		
		if (this.col != other.col)
			return false;
		
		return true;
	}
	
	public int hashCode() {
		return row * 37 + col;
	}

	@Override
	public String toString() {
		return "Seat [row=" + row + ", col=" + col + ", seatCategory=" + seatCategory + ", seatStatus=" + seatStatus
				+ "]";
	}

	public SeatHold getSeatHold() {
		return seatHold;
	}

	public void setSeatHold(SeatHold seatHold) {
		this.seatHold = seatHold;
	}

	
	
//	private Seat prev;
//	private Seat next;
//	private Seat fwd;
//	private Seat bkwd;
//	
//	public String print( ) {
//		return "Seat [row=" + row + ", col=" + col + "]";
//	}
//	
//
//	@Override
//	public String toString() {
//		return "Seat [row=" + row + ", col=" + col + " prev " + prev.print() +
//			   " next " + next.print() + " fwd " + fwd.print() + " bkwd " + bkwd.print() + " ]";
//	}
//	
//	
//
//	public Seat getPrev() {
//		return prev;
//	}
//
//	public Seat getNext() {
//		return next;
//	}
//
//	public Seat getFwd() {
//		return fwd;
//	}
//
//	public Seat getBkwd() {
//		return bkwd;
//	}
//	
//	public Seat(int row, int col, Seat prev, Seat fwd) {
//	this.row = row;
//	this.col = col;
//	
//	if (col > 1) { 
//		this.prev = prev;
//		prev.next = this;
//	}
//
//	if (row > 1) {
//		this.fwd = fwd;
//		fwd.bkwd = this.fwd;
//	}
//	
//	this.seatStatus = STATUS.AVAILABLE;	
//}
//
//	public void setPrev(Seat prev) {
//		this.prev = prev;
//		this.prev.setNext(this);
//	}
//
//	public void setNext(Seat next) {
//		this.next = next;
//		next.prev = this;
//	}
//
//	public void setBkwd(Seat bkwd) {
//		this.bkwd = bkwd;
//		bkwd.fwd = this;
//	}
//	public void setFwd(Seat fwd) {
//		this.fwd = fwd;
//		this.setBkwd(this);
//	}

}
