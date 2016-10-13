package com.ticketing.TicketingSystem;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends AbstractTicketingService implements TicketService
{
	private static App app;
	private ExecutorService exec = Executors.newFixedThreadPool(Integer.parseInt(props.getProperty("NUMBER_OF_THREADS")));
	private final Random random = new Random();
	
	private App() throws InterruptedException, TicketException {
		super();
	}

	public void execute() {
		int threads = Integer.parseInt(props.getProperty("NUMBER_OF_THREADS"));
		threads = random.nextInt(threads);
		System.out.println("Invoking " + threads + " threads for booking " );
		for (int i = 0; i < threads; i++) {
			int numSeats = random.nextInt(threads);
			int id = random.nextInt(50000);
			
	        try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	        
			exec.execute(
				new Runnable() {
					public void run() {
						SeatHold sh = null;
						long threadId = Thread.currentThread().getId();
						String customerEmail = "t"+threadId + "_id"+id +"_s"+numSeats +"_test@test.com";         
				        sh = findAndHoldSeats(numSeats, customerEmail);
				        
				        if (null != sh) { 
				        	System.out.println("ExecutorThread " + threadId + " numSeatsAvailable " + numSeatsAvailable() 
				        			+ " numSeats " + numSeats + " reservationMap sz " + 
						            reservationMap.size() + " seatHoldQueue sz " + seatHoldQueue.size() + " " + sh.toString() );
					        
				        	
				        	try {
								Thread.sleep(999);

					        	String reservation = reserveSeats(sh.getSeatHoldId(), customerEmail);
					        	if (reservation != null)
						        	System.out.println("ExecutorThread " + threadId +  " RESERVATION " + reservation + " numSeatsAvailable " 
						        					+ numSeatsAvailable() + " numSeats " + numSeats + 
						        					" reservationMap sz " + reservationMap.size() + " seatHoldQueue sz " + seatHoldQueue.size() 
						        					+ " " + sh.toString() );

							} catch (InterruptedException e) {
								e.printStackTrace();
							}
					        
				        }
					}
				});
		}
		
	}
	
    public static void main( String[] args )
    {
		try {
			app = new App();
			long count = 15L;
			while (count-- > 0) {
				app.execute();
				Thread.sleep(6000);
				System.out.println("Ticketing running pass " + count);
			}
	        app.exec.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {			
			System.out.println("Shutting down the app");
			app.shutdown();
			System.exit(0);
		}
    }
}
