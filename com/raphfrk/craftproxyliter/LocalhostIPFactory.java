package com.raphfrk.craftproxyliter;


public class LocalhostIPFactory {

	static Integer counter = 1;

	static String getNextIP() {



		int high, low;

		synchronized(counter) {

			do{
				do {
					high = (counter>>8) & 0x00FF;
					if(high < 10 || high > 200) {
						counter += 256;
					}
				} while(high < 10);
				low = counter & 0x00FF;
				counter ++;
			} while ( high < 10 || high > 200 || low < 10 || low > 200 );

		}
		return "127.0." + high + "." + low;
	}

}