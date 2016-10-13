package com.ticketing.TicketingSystem;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class DumpStatistics implements Runnable {
	private final Integer totalSeats;
	private final Seat[] seats;
	private final PriorityBlockingQueue<SeatHold> seatHoldQueue;
	private final AvailableSeatCounter availableSeatCounter;
	private final Map<String, SeatHold> reservationMap;
	private final AtomicBoolean runDumper = new AtomicBoolean();
	private final CountDownLatch startLatch;
	private final Lock dumpLock;
	private final Condition dumpCond;


	public DumpStatistics(PriorityBlockingQueue<SeatHold> seatHoldQueue, 
						  AvailableSeatCounter availableSeatCounter, 
						  Map<String, SeatHold> reservationMap,
						  Seat[] seats, CountDownLatch startLatch,
						  Lock dumpLock, Condition dumpCond) {
		this.seatHoldQueue = seatHoldQueue;
		this.availableSeatCounter = availableSeatCounter;
		this.reservationMap = reservationMap;
		this.runDumper.set(true);
		this.seats = seats;
		this.totalSeats = seats.length;
		this.startLatch = startLatch;
		this.dumpLock = dumpLock;
		this.dumpCond = dumpCond;

		System.out.println( "DumpStatisticsThread " + Thread.currentThread().getId());
	}

	public void dumpStatistics() {		
		System.out.println("***********Time : "+ System.currentTimeMillis()/1000);
		System.out.println("***********TotalSeats : "+ totalSeats);
		System.out.println("***********AvailableSeats : "+ availableSeatCounter.getAvailableSeats());
		System.out.println("***********ReservationMap entries : "+ reservationMap.size());
		System.out.println("***********SeatHoldQueue : "+ seatHoldQueue.size());
		System.out.println("***********Seats Hold Queue" + Arrays.toString(seatHoldQueue.toArray()));
		System.out.println("***********Seats " + Arrays.toString(seats));
		System.out.println("***********END***********");
	}

	@Override
	public void run() {
    	startLatch.countDown();

		while(runDumper.get()) {
			try {
				dumpLock.lock();
				dumpCond.await();
				//Thread.sleep(30000);

			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				dumpLock.unlock();
			}

			dumpStatistics();
		}
	}
	public void stopRunning() {
		this.runDumper.set(false);
		System.out.println("DumpStatisticsThread " + Thread.currentThread().getId() +  " Shutting down thread " + Thread.currentThread().getId() + " seatHoldQueue size " + seatHoldQueue.size());
	}

}
