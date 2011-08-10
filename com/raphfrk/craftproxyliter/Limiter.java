package com.raphfrk.craftproxyliter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Limiter {

	private final int limit;
	public final AtomicInteger remaining = new AtomicInteger();
	private final AtomicLong startTime = new AtomicLong();
	
	Limiter(int limit) {
		this.limit = limit * 1000 / 20 / 8;  // bytes per 50ms
	}
	
	public void pass(int size) {
		remaining.addAndGet(-size);
	}
	
	public void limit(int size) {
		
		if (limit <= 0) {
			return;
		}
		
		remaining.addAndGet(-size);
		
		long r;
		while ((r = remaining.get()) < 0) {
			
			long currentTime = System.currentTimeMillis();
			long timeToEnd = startTime.get() + 50 - currentTime;
			if (timeToEnd < 0) {
				r = remaining.addAndGet(limit);
				startTime.set(currentTime);
				if (r > limit * 2) {
					remaining.set(limit*2);
				}
			}

			if (r < 0 ) {
				try {
					if (timeToEnd > 0) {
						Thread.sleep(timeToEnd);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}
	
}
