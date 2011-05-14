package com.raphfrk.protocol;

import java.util.ArrayList;

public class KillableThread extends Thread {

	private ArrayList<KillableThread> children = new ArrayList<KillableThread>();

	private boolean killed = false;

	public boolean killed() {
		if(Thread.interrupted()) {
			kill();
		}

		return killed;
	}

	protected void kill() {
		for(Thread t : children) {
			t.interrupt();
		}
		killed = true;
	}

	protected void revive() {
		Thread.interrupted();
		killed = false;
	}
	
	boolean joinAll(int delay) throws InterruptedException {
		kill();
		for(Thread t : children) {
			t.join(delay);
			if(t.isAlive()) {
				return false;
			}
		}
		
		return true;
	}

}