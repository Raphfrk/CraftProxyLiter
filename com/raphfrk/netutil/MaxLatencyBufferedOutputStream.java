/*******************************************************************************
 * Copyright (C) 2012 Raphfrk
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package com.raphfrk.netutil;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import com.raphfrk.protocol.KillableThread;

public class MaxLatencyBufferedOutputStream extends BufferedOutputStream {

	private final long maxLatency;
	private final AtomicLong nextFlush = new AtomicLong(0L);
	private final ReentrantLock lock = new ReentrantLock();
	private final Object syncObj = new Object();
	private final LocalTimer timer = new LocalTimer();

	public MaxLatencyBufferedOutputStream(OutputStream out) {
		this(out, 512, 50);
	}

	public MaxLatencyBufferedOutputStream(OutputStream out, int size, long maxLatency) {
		super(out, size);
		this.maxLatency = maxLatency;
		timer.start();
	}

	private void pingFlush() {
		if(nextFlush.compareAndSet(0, System.currentTimeMillis() + maxLatency)) {
			synchronized(syncObj) {
				//System.out.println("Flush request " + System.currentTimeMillis());
				//System.out.println("Notifying");
				syncObj.notifyAll();
			}
		}
	}

	private void clearFlush() {
		nextFlush.set(0);
	}

	@Override
	public void write(int b) throws IOException {
		lock.lock();
		try {
			super.write(b);
		} finally {
			lock.unlock();
		}
		pingFlush();
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		lock.lock();
		try {
			super.write(b, off, len);
		} finally {
			lock.unlock();
		}
		pingFlush();
	}

	public void flush() throws IOException {
		clearFlush();

		lock.lock();
		try {
			//System.out.println("Flush happening " + System.currentTimeMillis());
			super.flush();
		} finally {
			lock.unlock();
		}
	}

	public void close() throws IOException {

		while(timer.isAlive()) {
			timer.interrupt();
			try {
				timer.join(50);
			} catch (InterruptedException ie) {
				try {
					Thread.currentThread().interrupt();
					Thread.sleep(50);
				} catch (InterruptedException ie2) {
					Thread.currentThread().interrupt();
				}
			}

		}

	}

	private class LocalTimer extends KillableThread {

		public void run() {

			while(!killed()) {

				long nextFlushLocal = nextFlush.get();

				long currentTime = System.currentTimeMillis();
				
				//System.out.println("next flush local: " + nextFlushLocal + " currentTime " + currentTime);

				if(nextFlushLocal != 0 && nextFlushLocal < currentTime + 2) {

					//if(lock.tryLock()) {
					lock.lock(); {
						try {
							flush();
						} catch (IOException e) {
						} finally {
							lock.unlock();
						}
					} 

				}

				synchronized(syncObj) {
					nextFlushLocal = nextFlush.get();
					currentTime = System.currentTimeMillis();

					long delay = Math.max(2L, Math.min(maxLatency, nextFlushLocal - currentTime));
					//System.out.println("Timer sleeping for " + delay + " at " + System.currentTimeMillis());
					try {
						if(nextFlushLocal == 0 ) {
							//System.out.println("Waiting 500");
							syncObj.wait(500);
						} else {
							//System.out.println("Waiting " + delay);
							syncObj.wait(delay);
						} 
						//System.out.println("Timer wake up" + System.currentTimeMillis());
					} catch (InterruptedException ie) {
						kill();
						continue;
					}
				}
			}
		}
	}
}
