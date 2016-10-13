package com.ticketing.TicketingSystem;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends AbstractTicketingService implements TicketService
{
	private static App app;
	private final Random random = new Random();
	private final Properties testProps = loadTestProperties(); 
	
	private int threads = Integer.parseInt(testProps.getProperty("NUMBER_OF_THREADS"));
	private int numOfTimes  = Integer.parseInt(testProps.getProperty("NUMBER_OF_TIMES"));
	private int numOfSeatsPerReq  = Integer.parseInt(testProps.getProperty("NUMBER_OF_SEATS_PER_REQ"));
	private boolean randomizeSeatsPerReq= Boolean.parseBoolean(testProps.getProperty("RANDOMIZE_SEATS_PER_REQ"));
	private int delayBetweenHoldAndRes = Integer.parseInt(testProps.getProperty("DELAY_BETWEEN_HOLD_AND_RESERVATION"));
	private boolean randomizeDelayBetweenHoldAndRes = Boolean.parseBoolean(testProps.getProperty("RANDOMIZE_DELAY_BETWEEN_HOLD_AND_RESERVATION"));
	private int delayBetweenInvocation = Integer.parseInt(testProps.getProperty("DELAY_BETWEEN_EACH_INVOCATION"));

	private ExecutorService holdExecutor = Executors.newFixedThreadPool(threads);
	
	private Map<Integer, SeatHold> holder = new HashMap<Integer, SeatHold>();

	private App() throws InterruptedException, TicketException {
		super();
	}
	
	public static Properties loadTestProperties() {
    	Properties prop = new Properties();
    	InputStream input = null;
    	
    	try {
    		input = new FileInputStream("src/test/resources/test.properties");
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

	public void execute() {
		System.out.println("Running " + numOfTimes + "times with " + threads + " threads for reservation");
		for (int i = 0; i < numOfTimes; i++) {
			System.out.println("Run " + i);

			try {
				Thread.sleep(delayBetweenInvocation);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			int id = random.nextInt(50000);

			holdExecutor.execute(new Runnable() {
				public void run() {
					long threadId = Thread.currentThread().getId();

					int numSeats = 0;
					if (randomizeSeatsPerReq)
						numSeats = random.nextInt(numOfSeatsPerReq);
					else
						numSeats = numOfSeatsPerReq;

					String customerEmail = "t" + threadId + "_id" + id + "_s" + numSeats + "_test@test.com";
					SeatHold sh = findAndHoldSeats(numSeats, customerEmail);

					if (null != sh) {
						holder.put(sh.getSeatHoldId(), sh);
						System.out.println("ExecutorThread " + threadId + "HOLD numSeatsAvailable " + numSeatsAvailable()
								+ " numSeats " + numSeats + " reservationMap sz " + reservationMap.size()
								+ " seatHoldQueue sz " + seatHoldQueue.size() + " " + sh.toString());

						try {
							int resDelay = 0;
							if (randomizeDelayBetweenHoldAndRes)
								resDelay = random.nextInt(delayBetweenHoldAndRes);
							else
								resDelay = delayBetweenHoldAndRes;

							Thread.sleep(resDelay);
							String reservation = reserveSeats(sh.getSeatHoldId(), customerEmail);
							if (reservation != null)
								System.out.println("ExecutorThread " + threadId + " RESERVATION " + reservation
										+ " numSeatsAvailable " + numSeatsAvailable() + " numSeats " + sh.getSeats().size()
										+ " reservationMap sz " + reservationMap.size() + " seatHoldQueue sz "
										+ seatHoldQueue.size() + " " + sh.toString());
	
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
			app.execute();
	        app.holdExecutor.shutdown();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {			
			System.out.println("Shutting down the app");
			app.shutdown();
			System.exit(0);
		}
    }
}
