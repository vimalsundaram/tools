package com.ticketing.TicketingSystem;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
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
		System.out.println("=========BEGIN=========");
		System.out.println("=========Time : "+ System.currentTimeMillis()/1000);
		System.out.println("=========SeatHoldQueue : "+ seatHoldQueue.size());
		System.out.println("=========SeatHoldQueue Seat count : " + seatHoldQueue.stream().mapToInt(sh -> sh.getSeats().size()).sum());
		System.out.println("=========SeatHoldQueue Seat Entries " + Arrays.toString(seatHoldQueue.toArray()));
		System.out.println("                               =========");
		System.out.println("=========ReservationMap  : "+ reservationMap.size());
		System.out.println("=========ReservationMap Seat count : " + reservationMap.values().stream().mapToInt(sh->sh.getSeats().size()).sum());
		reservationMap.forEach((k, v) -> System.out.println("=========ReservationMap Entries : " + v.toString()));
		System.out.println("                               =========");
		System.out.println("=========AvailableSeats : "+ availableSeatCounter.getAvailableSeats());
		System.out.println("=========TotalSeats : "+ totalSeats);
		System.out.println("=========Seats Status" + Arrays.toString(seats));
		System.out.println("=========END=========");
	}

	@Override
	public void run() {
    	startLatch.countDown();

		while(runDumper.get()) {
			try {
				dumpLock.lock();
				dumpCond.await();
				Thread.sleep(500);

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
