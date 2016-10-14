# Compile make sure to have java 1.8
 mvn clean install

# Check the target from root directory to see the generated classes 
# 20 class files should be there
 ls -l target/classes/com/ticketing/TicketingSystem/ 

# Check the configuration files
 venue.properties - This define the service side parameters
    - configuring the seats is using row value (SEAT.1.1=BEST/STANDARD)
 test.properties - This defines the test side parameters

# To modify the settings use test.properties 
# Run using following command without any command line param
 java -cp /Users/visundaram/tools/java/ticketing/TicketingSystem/target/TicketingSystem-0.0.1-SNAPSHOT.jar com.ticketing.TicketingSystem.App 2>&1 | tee logs/log.txt

#Sample log file
java/ticketing/TicketingSystem/logs/logs.txt
 
#Assumptions.
 - RESERVED seats to AVAILABLE seats is not implemented
 - Testing can be done by modifying the test.properties file.
 - Each call to findAndHoldSeats and reserveSeats will be blocking

Data Structures
Seat
 - can move from AVAILABLE, HOLD, RESERVED status and can be in BEST or STANDARD category.
 - has the row, col and may have SeatHold reference if it is in HOLD or RESERVED status.
 - object has a lock which should be held for changing the status.
 - Seats natural ordering is by the row and col values.
 - Specialized comparator SeatCategoryComparator is implemented for sorting based on category.

SeatHold
 - has booking time, hold time, hold period, Set of Seats, customerEmail, seatHoldId (which is generated using AtomicInteger)
 - has methods to set list of Seats and add Seats 
 - has methods to lock , unlock seats, which is used in methods confirmHold, confirmReservation, releaseHold, releaseReservation 
 - SeatHold natural ordering is by expiry time (holdtime - current time) . Hold time = bookingtime + hold period(configurable)

SeatHoldQueue
 - All the hold requests are stored in PriorityBlockingQueue which uses SeatHold natural ordering. 
 - It is used to fetch the shortest expiried entries first for expiring.
 - The queue is associated with Lock and Condition. 
 - MainThread's findAndHoldSeat signals the start of SeatHold expiry in SeatHoldQueue.
 - ExpiryThread's takes the expired SeatHold from SeatHoldQueue.
 - ExpiryThread's's signal StatisticsThread to dump statitics

ReservationMap
 - will hold the final confirmed reservation in a regular ConcurrentHashMap.
 
AvailableSeatCounter 
 - Internall AbstractTicketingService creates a AvailableSeatCounter, which mainitans the available seats. 
 - This object internally uses AtomicInteger and is shared across three threads. 
 - AbstractTicketingService also has seatHoldQueue(PriorityBlockingQueue) having SeatHold objects. 

# Implementation 
The service has three threads
 - MainThread - implementing the TicketService interface having entire implementation in AbstractTicketingService.
 - ExpirerThread - implementing a runnable interface to expire (SeatTimeExpirer) SeatHold objects after HOLD_PERIOD (configurable).
 - StatisticsThread - implementing a runnable interface to dump all the data structure entiries and seat available.
 -  Once ExpirerThread and StatisticsThread starts then Main threads starts getting the requests. 
 - ExpirierThread will only check if there is any valid entry in the SeatHoldQueue or it will just sleep based
   on the mininal expiry period.

#Checking / Testing / Verifying
 - logs/logs.txt file will have all the logs.  
 - grep "REQUEST" logs/logs.txt - returns requests came.
 - grep "FOUND" logs/logs.txt - returns hold requests honored.
 - grep "RESERVATION" logs/logs.txt - returns reservation requests honored.
 - grep "REQUEST" logs/logs.txt | awk '{sum+=$5} END {print sum}' - will return number of seats hold
 - grep "Shutdown complete" logs/logs.txt - can be checked here.

#Issues
 - Not using a proper logger 
 - Not having proper info messages without dumping all the data.

