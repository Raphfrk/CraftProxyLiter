package com.raphfrk.compression;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.raphfrk.craftproxyliter.Globals;
import com.raphfrk.craftproxyliter.PassthroughConnection;

public class HashStore {

	final File cacheDir;

	final AtomicInteger fileId = new AtomicInteger();
	final AtomicLong fileUse = new AtomicLong();

	static FileCompare fileCompare = new FileCompare();

	final ConcurrentHashMap<Long,Integer> FAT = new ConcurrentHashMap<Long,Integer>();
	final ConcurrentLinkedQueue<Long> FATList = new ConcurrentLinkedQueue<Long>();

	final ConcurrentLinkedQueue<Long> pending = new ConcurrentLinkedQueue<Long>();
	final AtomicInteger pendingCount = new AtomicInteger(0);
	final ReentrantLock pendingLock = new ReentrantLock();

	final ConcurrentHashMap<Long,Reference<byte[]>> cache = new ConcurrentHashMap<Long,Reference<byte[]>>();


	public HashStore(File cacheDir) {
		this.cacheDir = cacheDir;

		if(!cacheDir.isDirectory()) {
			if(cacheDir.isFile()) {
				return;
			} else {
				cacheDir.mkdirs();
			}
		}

		File[] files = cacheDir.listFiles();
		Arrays.sort(files, fileCompare);

		if(files.length > 0) {
			fileId.set(getIntFromName(files[files.length-1]) + 1);
		}

		prune(files);

		readFAT();

	}

	boolean addHash(long hash, byte[] block) {

		cache.put(hash, new HardReference<byte[]>(block));
		pending.add(hash);
		int count = pendingCount.incrementAndGet();

		if(count > 512) {
			return flushPending();
		} else {
			return true;
		}

	}

	byte[] getHash(PassthroughConnection ptc, long hash) {

		if(FAT.containsKey(hash) && !ptc.connectionInfo.hashesSent.containsKey(hash)) {
			Integer id = FAT.get(hash);
			//System.out.println("FAT hit + load " + id);
			if(id != null) {
				byte[] block = loadFile(ptc, id, hash);
				if(block == null) {
					ptc.printLogMessage("FAT error, unable to find hash in file number " + id);
				} else {
					return block;
				}
			}
		}
		
		//System.out.println("Hash sent?" + ptc.connectionInfo.hashesSent.containsKey(hash));

		if(cache.containsKey(hash)) {
			//System.out.println("Cache hit");
			Reference<byte[]> ref = cache.get(hash);
			if(ref != null) {
				byte[] block = ref.get();
				if(block != null) {
					return block;
				}
			}
		}

		if(FAT.containsKey(hash)) {
			//System.out.println("FAT hit");
			Integer id = FAT.get(hash);
			if(id != null) {
				byte[] block = loadFile(ptc, id, hash);
				if(block == null) {
					ptc.printLogMessage("FAT error, unable to find hash in file number " + id);
				} else {
					return block;
				}
			}
		}

		//System.out.println("Cache/FAT miss");

		return null;

	}

	ConcurrentLinkedQueue<byte[]> hardLoop = new ConcurrentLinkedQueue<byte[]>();
	AtomicInteger hardLoopSize = new AtomicInteger(0);

	ConcurrentHashMap<Integer,Object> fileLocks = new ConcurrentHashMap<Integer,Object>();

	byte[] loadFile(PassthroughConnection ptc, int id, long requestedHash) {

		/*Object fileLock = fileLocks.get(id);
		if(fileLock == null) {
			fileLock = new Object();
			Object oldLock = fileLocks.putIfAbsent(id, fileLock);
			if(oldLock != null) {
				fileLock = oldLock;
			}
		}*/
		
		//synchronized(fileLock) {
			Reference<byte[]> ref = cache.get(requestedHash);
			if(ref != null) {
				byte[] block = ref.get();
				if(block != null) {
					return block;
				}
			}
			if(cache.containsKey(requestedHash)) {
				
			}
			System.out.println("Loading file: " + id);

			File f = new File(cacheDir, "CPL" + id);

			FileInputStream fileIn;
			GZIPInputStream gzin;

			try {
				fileIn = new FileInputStream(f);
				gzin = new GZIPInputStream(fileIn);
			} catch (IOException e) {
				return null;
			}

			DataInputStream in = new DataInputStream(gzin);

			byte[] requestedBlock = null;

			ConcurrentLinkedQueue<Long> sendQueue = ptc.connectionInfo.hashesToSend;
			ConcurrentHashMap<Long,Boolean> sentAlready = ptc.connectionInfo.hashesSent;

			boolean eof = false;
			while(!eof) {

				try {
					long hash = in.readLong();
					byte[] block = new byte[2048];
					in.readFully(block);
					if(hash == requestedHash) {
						requestedBlock = block;
					}
					cache.put(hash, new SoftReference<byte[]>(block));
					if(!sentAlready.containsKey(hash)) {
						//System.out.println("Adding hash to send queue: " + Long.toHexString(hash));
						sendQueue.add(hash);
					}
					hardLoop.add(block);
					int loopSize = hardLoopSize.incrementAndGet();

					while(loopSize > 32768) {
						hardLoop.poll();
						loopSize = hardLoopSize.decrementAndGet();
					}
				} catch (EOFException eof1) {
					eof = true;
					continue;
				} catch (IOException ioe) {
					if(in != null) {
						try {
							in.close();
						} catch (IOException ioe2) {
							return null;
						}
					}
					return null;
				}
			}

			return requestedBlock;
		//}

	}
	
	public boolean flushPending() {
		return flushPending(false);
	}

	public boolean flushPending(boolean forceWrite) {

		if(forceWrite || pendingLock.tryLock()) {
			if(forceWrite) {
				pendingLock.lock();
			}
			try {

				int id = fileId.getAndIncrement();

				File f = new File(cacheDir, "CPL" + id);

				LinkedList<Long> toWrite = new LinkedList<Long>();

				while(!pending.isEmpty()) {
					Long current = pending.poll();
					if(current != null) {
						toWrite.add(current);
					}
				}

				pendingCount.set(0);

				FileOutputStream fileOut;
				GZIPOutputStream gzout;

				try {
					fileOut = new FileOutputStream(f);
					gzout = new GZIPOutputStream(fileOut);
				} catch (IOException e) {
					return false;
				}


				DataOutputStream out = new DataOutputStream(gzout);

				for(Long current : toWrite) {

					if(current != null) {
						Reference<byte[]> ref = cache.get(current);
						byte[] block = ref.get();
						if(block != null && block.length == 2048) {

							try {
								out.writeLong(current);
								out.write(block);
							} catch (IOException ioe) {
								if(out != null) {
									try {
										out.close();
									} catch (IOException ioe2) {
										return false;
									}
								}
								return false;
							}

						}
					}

				}

				try {
					if(out != null) {
						out.close();
					}
				} catch (IOException ioe) {
					return false;
				}

				fileUse.addAndGet(f.length());

				for(Long current : toWrite) {
					if(current != null) {
						Reference<byte[]> ref = cache.get(current);
						byte[] block = ref.get();
						if(block != null) {
							FAT.put(current, id);
							FATList.add(current);
							cache.put(current, new SoftReference<byte[]>(block));
						}
					}
				}

				return true;

			} finally {
				pendingLock.unlock();
			}
		} else {
			//System.out.println("Unable to flush pending");
			return true;
		}

	}

	void prune(File[] files) {

		int size = 0;

		for(File file : files) {
			if(file.isFile() && !file.getName().equals("FAT")) {
				size += file.length();
			}
		}

		int cnt = 0;
		int limit = Globals.getCacheLimit();

		while(size > limit && cnt < files.length) {
			File current = files[cnt++];
			if(current.isFile() && !current.getName().equals("FAT")) {
				size -= current.length();
			}
		}

		fileUse.set(size);

	}

	public boolean writeFAT() {

		//System.out.println("About to write FAT");

		File[] files = cacheDir.listFiles();
		Arrays.sort(files, fileCompare);

		prune(files);

		File file = new File(cacheDir, "FAT");

		FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream(file);
		} catch (IOException e) {
			return false;
		}

		Iterator<Long> itr = FATList.iterator();

		DataOutputStream out = new DataOutputStream(fileOut);

		while(itr.hasNext()) {
			Long current = itr.next();
			Integer id = FAT.get(current);
			try {
				if(id != null && current != null) {
					if(getFile(id).isFile()) {
						out.writeLong(current);
						out.writeInt(id);
					}
				}
			} catch (IOException ioe) {
				if(out != null) {
					try {
						out.close();
					} catch (IOException ioe2) {
						return false;
					}
				}
				return false;
			}
		}

		try {
			if(out != null) {
				out.close();
			}
		} catch (IOException ioe) {
			return false;
		}

		//System.out.println("FAT write completed successfully");

		return true;
	}

	public boolean readFAT() {

		//System.out.println("Reading FAT from disk");

		int entries= 0;

		File file = new File(cacheDir, "FAT");

		if(!file.isFile()) {
			return false;
		}

		FileInputStream fileIn;
		try {
			fileIn = new FileInputStream(file);
		} catch (IOException e) {
			return false;
		}

		DataInputStream in = new DataInputStream(fileIn);

		boolean eof = false;

		while(!eof) {

			try {
				long hash = in.readLong();
				int id = in.readInt();
				FAT.put(hash, id);
				FATList.add(hash);
				entries++;
				//System.out.println("FAT: " + Long.toHexString(hash));
			} catch (EOFException eofe) {
				System.out.println("EOF FAT");
				eof = true;
				continue;
			} catch (IOException ioe) {
				System.out.println("IO exception reading FAT");
			}

		}

		try {
			if(in != null) {
				in.close();
			}
		} catch (IOException ioe) {
			return false;
		}

		//System.out.println("Entries: " + entries);

		return true;


	}

	synchronized File getFile(int id) {
		return new File(cacheDir, "CPL" + id);
	}

	private static class FileCompare implements Comparator<File> {

		public int compare(File f1, File f2) {			
			return getIntFromName(f1) - getIntFromName(f2);
		}

	}

	public static int getIntFromName(File file) {
		try {
			String fileName = file.getName();
			if(fileName == null || fileName.length() < 3) {
				return 0;
			} else if(fileName.equals("FAT")) {
				return -1;
			}
			return Integer.parseInt(file.getName().substring(3));
		} catch (NumberFormatException nfe) {
			return 0;
		}
	}

	public class HardReference<T> extends SoftReference<T> {

		public T hard;

		T getHard() {
			return hard;
		}

		HardReference(T ref) {
			super(ref);
			hard = ref;
		}
	}

}
