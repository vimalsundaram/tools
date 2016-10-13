package com.ticketing.TicketingSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class SeatTimeExpirer implements Runnable {
	private final CountDownLatch startLatch;
	private final PriorityBlockingQueue<SeatHold> seatHoldQueue;
	private final Lock holdQLock;
	private final Condition holdQCond;
	private final Lock dumpLock;
	private final Condition dumpCond;

	private final AvailableSeatCounter availableSeatCounter;
	private final AtomicBoolean runExpirer = new AtomicBoolean();

	public SeatTimeExpirer(PriorityBlockingQueue<SeatHold> seatHoldQueue, 
			CountDownLatch startLatch, AvailableSeatCounter availableSeatCounter, 
			Lock holdQLock, Condition holdQCond, Lock dumpLock, Condition dumpCond) {
		this.startLatch = startLatch;
		this.holdQLock = holdQLock;
		this.holdQCond = holdQCond;
		this.seatHoldQueue = seatHoldQueue;
		this.dumpLock = dumpLock;
		this.dumpCond = dumpCond;

		this.availableSeatCounter = availableSeatCounter;
		this.runExpirer.set(true);
		System.out.println( "ExpirerThread " + Thread.currentThread().getId());
	}

	@Override
	public void run() {
		long count = 0;
    	startLatch.countDown();

		while (runExpirer.get()) { 
			long leastExpiryTime = 0;
			count++;
			    		            
            if (seatHoldQueue.isEmpty()) {
				try {
					holdQLock.lock();
					holdQCond.await(); 
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					holdQLock.unlock();
				}
            }
            
    		System.out.println("ExpirerThread " + Thread.currentThread().getId() + " pass " + count + 
    				" numSeatsAvailable " + availableSeatCounter.getAvailableSeats() + 
    				"  seatHoldQueue size " + seatHoldQueue.size() );

                     
    		SeatHold sh = null;
        	long now = System.currentTimeMillis();
            while((sh = seatHoldQueue.peek()) != null) {
            	leastExpiryTime = sh.getExpiryTime(now);
            	if (leastExpiryTime <= 0) {
            		if (null != sh) {
                		sh = seatHoldQueue.poll();
	            		sh.releaseHold();
	            		availableSeatCounter.adjustNumAvailableSeats(sh.getSeats().size());
	            		System.out.println("ExpirerThread " + Thread.currentThread().getId() + " RETURN seats "  + sh.getSeats().size() +
	            		                   " numSeatsAvailable "+ availableSeatCounter.getAvailableSeats() +  
	            		                   " seatHoldQueue sz " + seatHoldQueue.size() 
	            		                   + " " + sh.toString());
	            		sh = null;
            		}
            	} else {
                	break;
            	}
            }
            
            if (seatHoldQueue.size() > 0) {
				try {
					dumpLock.lock();
					dumpCond.signal(); 
				} finally {
					dumpLock.unlock();
				}

            }
            
            if (leastExpiryTime > 0 ) { 
	            try {
					Thread.sleep(leastExpiryTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }
		}
	}
	
	public void stopRunning() {
		this.runExpirer.set(false);
		System.out.println("ExpirerThread " + Thread.currentThread().getId() +  " Shutting down thread " + Thread.currentThread().getId() + " seatHoldQueue size " + seatHoldQueue.size());
	}

}
