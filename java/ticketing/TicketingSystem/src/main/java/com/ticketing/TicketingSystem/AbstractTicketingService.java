package com.ticketing.TicketingSystem;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AbstractTicketingService implements TicketService {
	protected static final Properties props = loadProperties();

	private static int rows;
	private static int cols;
	private static Seat[] seats;
	private static AvailableSeatCounter availableSeatCounter;
	protected  PriorityBlockingQueue<SeatHold> seatHoldQueue;
	protected final Map<String, SeatHold> reservationMap;

    private final Lock holdQLock = new ReentrantLock();
    private final Condition holdQCond = holdQLock.newCondition();
    
	private final Lock dumpLock  = new ReentrantLock();;
	private final Condition dumpCond = dumpLock.newCondition();

	private final IFinderStrategy<Seat> seatFinder;
	
	private final SeatTimeExpirer holdExpirer;  
	private final Thread holdExpirerThread; 
	private final CountDownLatch startLatch = new CountDownLatch(2);
	
	protected static final int HOLD_PERIOD = Integer.parseInt(props.getProperty("HOLD_PERIOD"));
	
	private final DumpStatistics dumpStatistics;
	private final Thread statisticsThread;
		
	private static void initSeats(Properties props) {
		int count = 0;
		rows = Integer.parseInt(props.getProperty("NUMBER_OF_ROWS"));
		cols = Integer.parseInt(props.getProperty("NUMBER_OF_COLS"));

		seats = new Seat[rows * cols];
		for(int r = 1; r <= rows; r++) {
			for(int c = 1; c <= cols; c++) {
				String category = props.getProperty("SEAT."+r+"."+c);
				
				if (null != category) {
					seats[count++] = new Seat(r, c, Seat.CATEGORY.valueOf(category));
				} else {
					seats[count++] = new Seat(r, c, Seat.CATEGORY.STANDARD);
				}
			}
		}
		
		assert(seats.length==rows*cols);
		System.out.println("Thread " + Thread.currentThread().getId()  + " Seats size " + seats.length);
	}
	
	public AbstractTicketingService() throws InterruptedException, TicketException {
		initSeats(props);
		
		availableSeatCounter = new AvailableSeatCounter(seats);
		reservationMap = new ConcurrentHashMap<String, SeatHold>();
		seatHoldQueue = new PriorityBlockingQueue<SeatHold>();
		
		try {
			this.seatFinder = FinderFactory.getInstance(FinderFactory.POLICY.BEST_AVAILABLE);
		} catch (TicketException e) {
			throw e;
		}
		this.seatFinder.setEntries(seats);
		
		dumpStatistics = new DumpStatistics(seatHoldQueue, availableSeatCounter, 
				reservationMap, seats, startLatch, dumpLock, dumpCond);
		statisticsThread = new Thread(dumpStatistics);
		statisticsThread.start();

		holdExpirer = new SeatTimeExpirer(seatHoldQueue, startLatch, 
						availableSeatCounter, holdQLock, holdQCond, dumpLock, dumpCond);
		holdExpirerThread = new Thread(holdExpirer);
		holdExpirerThread.start();
		
		startLatch.await();
		System.out.println("MainThread " + Thread.currentThread().getId() + " holdExpirerThread " + holdExpirerThread.getId() +
				" numSeatsAvailable " + availableSeatCounter.getAvailableSeats());
	}
	
	public int getRows() {
		return rows;
	}

	public int getCols() {
		return cols;
	}

	public int getTotalSeats() {
		return seats.length;
	}
	
	private String generateReservationId() {
		return UUID.randomUUID().toString();	
	}
	
	/**
	* The number of seats in the venue that are neither held nor reserved
	*
	* @return the number of tickets available in the venue
	*/
	public int numSeatsAvailable()  {
		return availableSeatCounter.getAvailableSeats();
	}
	
	/**
	* Find and hold the best available seats for a customer
	*
	* @param numSeats the number of seats to find and hold
	* @param customerEmail unique identifier for the customer
	* @return a SeatHold object identifying the specific seats and related
	* information
	 * @throws TicketException 
	*/
	public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
		if (numSeats <= 0 || customerEmail == null || customerEmail.isEmpty())
			return null;
		
		SeatHold sh = new SeatHold(numSeats, customerEmail, HOLD_PERIOD);
		int counter = numSeats;
		System.out.println("MainThread " + Thread.currentThread().getId() + " REQUEST seats " + numSeats + " seats for customer " 
		                    + customerEmail + " numSeatsAvailable " + numSeatsAvailable() );
		while (true) { 
			Seat s = null;
			while (counter > 0 && numSeatsAvailable() >= 1 && ((s = seatFinder.next()) != null)) {
				if (s != null)  {
					sh.addSeat(s);	
					counter--;
				}
			}
			
			if (counter <= 0)
				break;
			
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
		}
	
		seatHoldQueue.offer(sh);
		availableSeatCounter.adjustNumAvailableSeats(-numSeats);

		if (sh.getSeats().size() > 0) {
			// Notify other thread on hold queue entries.
			try { 
				holdQLock.lock();
				holdQCond.signal();
			} finally {
				holdQLock.unlock();
			}
			System.out.println("MainThread " + Thread.currentThread().getId() + " FOUND seats " + 
					            numSeats  + " numSeatsAvailable " + numSeatsAvailable() + 
					            " reservationMap sz " + 
					            reservationMap.size() + " seatHoldQueue sz " + seatHoldQueue.size() + " " + sh.toString());
		}
		
		return sh;
	}

	/**
	* Commit seats held for a specific customer
	*
	* @param seatHoldId the seat hold identifier
	* @param customerEmail the email address of the customer to which the
	* seat hold is assigned
	* @return a reservation confirmation code
	*/
	public String reserveSeats(int seatHoldId, String customerEmail) {
		if ( seatHoldId <= 0 )
			return null;
		
		String reservation = null; 
		
		Iterator<SeatHold> iter = seatHoldQueue.iterator();
		SeatHold sh = null;

		while(iter.hasNext()) {
			sh = iter.next();
			if (sh.getSeatHoldId() == seatHoldId && 
				sh.getCustomerEmail().equals(customerEmail)) {
				iter.remove();
				sh.confirmReservation();
				reservation = generateReservationId();
				reservationMap.put(reservation, sh);
				System.out.println("MainThread after Reservation " + Thread.currentThread().getId() +" Seat Hold Size " 
						+ sh.getSeats().size() + " SeatHold " + sh.toString());	
				break;
			}
		}
		

		return reservation;
	}	

	public void shutdown() {
		System.out.println("Shutdown MainThread " + Thread.currentThread().getId() + 
				" reservationMap " + reservationMap.size() + " seatHoldQueue " + seatHoldQueue.size()  + " availableSeatCount " + availableSeatCounter.getAvailableSeats());

		try {
			Thread.sleep(5000);
			dumpLock.lock();
			dumpCond.signal();
			holdQLock.lock();
			holdQCond.signalAll();
			seatHoldQueue.clear();
			holdExpirer.stopRunning();
			holdExpirerThread.join(1000);
			dumpStatistics.stopRunning();
			statisticsThread.join(1000);
			reservationMap.clear();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			holdQLock.unlock();
			System.out.println("MainThread " + Thread.currentThread().getId() + " Shutdown complete. total seats " + seats.length + " numSeatsAvailable " + numSeatsAvailable());
		}
	}	
	
	
	public static Properties loadProperties() {
    	Properties prop = new Properties();
    	InputStream input = null;
    	
    	try {
    		input = new FileInputStream("src/main/resources/venue.properties");
    		prop.load(input);
    	} catch (IOException ex) {
    		ex.printStackTrace();
    	} finally {
    		if (input != null) {
    			try {
    				input.close();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
    	}

    	return prop;
	}
	
}
