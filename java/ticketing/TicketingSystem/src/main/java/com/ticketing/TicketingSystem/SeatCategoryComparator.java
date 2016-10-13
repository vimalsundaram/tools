package com.ticketing.TicketingSystem;

import java.util.Comparator;

public class SeatCategoryComparator implements Comparator<Seat> {

	@Override
	public int compare(Seat o1, Seat o2) {
		int result = 10;
		if (o1.getSeatCategory() == Seat.CATEGORY.BEST &&
			o2.getSeatCategory() == Seat.CATEGORY.BEST) {
			result = -10;
		} 
		
		if (o1.getSeatCategory() == Seat.CATEGORY.BEST &&
			o2.getSeatCategory() == Seat.CATEGORY.STANDARD) {
			result = -5;
		} 

		if (o1.getSeatCategory() == Seat.CATEGORY.STANDARD &&
			o2.getSeatCategory() == Seat.CATEGORY.BEST) {
			result =  5;
		} 

		if (o1.getSeatCategory() == Seat.CATEGORY.STANDARD &&
			o2.getSeatCategory() == Seat.CATEGORY.STANDARD) {
			result = 10;
		}
		
		return result; 

	}

}
