package com.raphfrk.netutil;

import java.io.EOFException;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicLong;

import com.raphfrk.protocol.KillableThread;

public class MaxLatencyBufferedOutputStream extends FilterOutputStream {

	private final byte[] buffer;
	private final int bufferLength;
	private int position;
	private AtomicLong nextFlushTime;
	private final long maxLatency;
	private final TimerThread timer = new TimerThread();

	public MaxLatencyBufferedOutputStream(OutputStream out) {
		this(out, 512, 50);
	}

	public MaxLatencyBufferedOutputStream(OutputStream out, int size, long maxLatency) {
		super(out);
		buffer = new byte[size];
		position = 0;
		nextFlushTime = new AtomicLong(System.currentTimeMillis());
		this.maxLatency = maxLatency;
		this.bufferLength = buffer.length;
		this.timer.start();
	}

	@Override
	public void close() throws IOException {
		synchronized(this) {
			killTimerWithJoin();
		}
		synchronized(this) {
			out.close();
		}
	}

	@Override
	public void flush() throws IOException {

		boolean notify = position != 0;

		synchronized(this) {
			if(position != 0) {
				out.write(buffer, 0, position);

				position = 0;
				out.flush();
			}
		}
		if(notify) {
			updateFlushTime();
		}
	}

	private void updateFlushTime() {
		synchronized(timer) {
			nextFlushTime.set(System.currentTimeMillis() + maxLatency);
			if(timer.deepSleep) {
				timer.notifyAll();
			}
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		this.write(b, 0, b.length);
	}

	@Override 
	public void write(byte[] b, int offset, int length) throws IOException {
		synchronized(this) {
			if(length + position > bufferLength) {
				if(length < bufferLength || position < (bufferLength >> 1)) {
					int copyLength = bufferLength - position;
					System.arraycopy(b, offset, buffer, position, copyLength);
					position += copyLength;
					length -= copyLength;
					offset += copyLength;
				}
				flush();
			}
			if(length + position > bufferLength) {
				flush();
				out.write(b, offset, length);
			} else {
				System.arraycopy(b, offset, buffer, position, length);
				position += length;
			}
		}
		synchronized(timer) {
			if(timer.deepSleep) {
				updateFlushTime();
			}
		}
	}

	@Override 
	public void write(int b) throws IOException {
		synchronized(this) {
			if(position == bufferLength) {
				flush();
				buffer[0] = (byte)b;
				position = 1;
			} else {
				buffer[position++] = (byte)b;
			}
		}
	}

	@Override
	protected void finalize() {
		killTimer();
	}

	void killTimer() {
		timer.interrupt();
		synchronized(timer) {
			timer.notifyAll();
		}
	}

	public void killTimerWithJoin() {
		while(timer.isAlive()) {
			killTimer();
			try {
				timer.join();
			} catch (InterruptedException e) {
				System.err.println("Interrupted while attempted to kill Timer Thread");
			}
			if(timer.isAlive()) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	private class TimerThread extends KillableThread {

		public boolean deepSleep = false;

		public void run() {

			while(!killed()) {

				long currentTime = System.currentTimeMillis();

				long nextFlushTimeTemp = nextFlushTime.get();

				if(currentTime > nextFlushTimeTemp) {
					try {
						synchronized(timer) {
							flush();
							if(!nextFlushTime.compareAndSet(nextFlushTimeTemp, Long.MAX_VALUE)) {
								nextFlushTime.set(nextFlushTimeTemp + maxLatency);
							}
							timer.wait(maxLatency + 1);
						}
					} catch (SocketException sce) {
						System.err.print("Socket closed");
						kill();
						continue;
					} catch (EOFException eof) {
						System.err.print("EOF in timer thread");
						kill();
						continue;
					} catch (IOException e) {
						System.err.println("IO Exception in TimerThread");
						kill();
						continue;
					} catch (InterruptedException e) {
						kill();
						continue;
					}
				} else if(nextFlushTimeTemp == Long.MAX_VALUE) {
					synchronized(timer) {
						if(nextFlushTime.get() == Long.MAX_VALUE) {
							deepSleep = true;
							try {
								timer.wait(500);
							} catch (InterruptedException e) {
								kill();
								continue;
							}
							deepSleep = false;
						}
					}
				} else {
					try {
						long delay = Math.max(1, 1 + Math.min(maxLatency, nextFlushTimeTemp - currentTime));
						synchronized(timer) {
							timer.wait(delay);
						}
					} catch (InterruptedException e) {
						kill();
						continue;
					}

				}

			}

		}

	}

}
