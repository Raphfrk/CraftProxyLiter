package com.raphfrk.craftproxyliter;

public class Globals {
	
	static private String password = null;
	
	public synchronized static String getPassword() {
		return password;
	}
	
	public synchronized static void setPassword(String password) {
		Globals.password = password;
	}
	
	static private String localAlias = "";
	
	public synchronized static String getLocalAlias() {
		return localAlias;
	}
	
	public synchronized static void setLocalAlias(String localAlias) {
		Globals.localAlias = localAlias;
	}
	
	static Long seed = null;
	public synchronized static Long getSeed() {
		return seed;
	}
	
	public synchronized static void setSeed(Long seed) {
		Globals.seed = seed;
	}
	
	static private boolean flood = true;
	
	public synchronized static boolean isFlood() {
		return flood;
	}
	
	public synchronized static void setFlood( boolean newFlood ) {
		flood = newFlood;
	}
	
	static private boolean quiet = false;

	public synchronized static boolean isQuiet() {
		return quiet;
	}
	
	public synchronized static void setQuiet( boolean newQuiet ) {
		quiet = newQuiet;
	}
	
	static private long monitor = -1;

	public synchronized static long monitorBandwidth() {
		return monitor;
	}
	
	public synchronized static void setMonitor( long newMonitor ) {
		monitor = newMonitor;
	}
	
	static private int compressionLevel = 1;
	
	public synchronized static int getCompressionLevel() {
		return compressionLevel;
	}
	
	public synchronized static void setCompressionLevel( int newCompressionLevel ) {
		compressionLevel = newCompressionLevel;
	}
	
	static private long latency = 20;

	public synchronized static long getBufferLatency() {
		return latency;
	}
	
	public synchronized static void setBufferLatency( long newLatency ) {
		latency = newLatency;
	}
	
	static private boolean debug = false;

	public synchronized static boolean isDebug() {
		return debug;
	}
	
	public synchronized static void setDebug( boolean newDebug) {
		debug = newDebug;
	}
	
	static private boolean hell = false;

	public synchronized static boolean isHell() {
		return hell;
	}
	
	public synchronized static void setHell( boolean newHell ) {
		hell = newHell;
	}
	
	static private boolean verbose = false;

	public synchronized static boolean isVerbose() {
		return verbose;
	}
	
	public synchronized static void setVerbose( boolean newVerbose ) {
		verbose = newVerbose;
	}
	
	static private boolean info = false;

	public synchronized static boolean isInfo() {
		return info;
	}
	
	public synchronized static void setInfo( boolean newInfo ) {
		info = newInfo;
	}
	
	static private boolean authenticate = true;
	
	public synchronized static boolean isAuth() {
		return authenticate;
	}
	
	public synchronized static void setAuth( boolean newAuth ) {
		authenticate = newAuth;
	}
	
	static private boolean varyLocalhost = false;
	
	public synchronized static boolean varyLocalhost() {
		return varyLocalhost;
	}
	
	public synchronized static void setVaryLocalhost( boolean varyLocalhost ) {
		Globals.varyLocalhost = varyLocalhost;
	}
	
	static private boolean compressInfo = false;
	
	public synchronized static boolean compressInfo() {
		return compressInfo;
	}
	
	public synchronized static void setCompressInfo( boolean compressInfo) {
		Globals.compressInfo = compressInfo;
	}
	
	static private boolean localCache = false;
	
	public synchronized static boolean localCache() {
		return localCache;
	}
	
	public synchronized static void setlocalCache( boolean localCache ) {
		Globals.localCache = localCache;
	}
	
	static boolean bridgingConnection = false;
	
	static public boolean bridgingConnection() {
		return bridgingConnection;
	}
	
	static public void setBridgingConnection(boolean bridgingConnection) {
		Globals.bridgingConnection = bridgingConnection;
	}
	
	static private int defaultPlayerId = 456789012;
		
	public synchronized static int getDefaultPlayerId() {
		
		return defaultPlayerId;
		
	}
	
	static private int cacheLimit = 48*1024*1024;
	
	public synchronized static int getCacheLimit() {
		return cacheLimit;
	}
	
	public synchronized static void setCacheLimit( int newCacheLimit ) {
		cacheLimit = newCacheLimit;
	}
	
	static private int blockSize = 512;
	
	public synchronized static int getBlockSize() {
		return blockSize;
	}
	
	public synchronized static void setBlockSize( int newBlockSize ) {
		blockSize = newBlockSize;
	}
	
	
	static private int fakeVersion = 111111;
	
	public synchronized static int getFakeVersion() {
		return fakeVersion + clientVersion*2;
	}
	
	public synchronized static void setFakeVersion( int newFakeVersion ) {
		fakeVersion = newFakeVersion;
	}
	
	static private int clientVersion = 11;
	
	public synchronized static int getClientVersion() {
		return clientVersion;
	}
	
	public synchronized static void setClientVersion( int newClientVersion ) {
		clientVersion = newClientVersion;
	}
	
	static private int delay = 5500;
	
	public synchronized static int getDelay() {
		return delay;
	}
	
	public synchronized static void setDelay( int delay ) {
		Globals.delay = delay;
	}
	
	static private int limiter = 20;
	
	public synchronized static int getLimiter() {
		return limiter;
	}
	
	public synchronized static void setLimiter( int limiter ) {
		Globals.limiter = limiter;
	}
	
	static private int fairness = 0;
	
	public synchronized static int getFairness() {
		return fairness;
	}
	
	public synchronized static void setFairness( int fairness ) {
		Globals.fairness = fairness;
	}
	
	static private long window = 0;
	
	public synchronized static long getWindow() {
		return window;
	}
	
	public synchronized static void setWindow( long window ) {
		Globals.window = window;
	}
	
	static private long threshold = 0;
	
	public synchronized static long getThreshold() {
		return threshold;
	}
	
	public synchronized static void setThreshold( long threshold ) {
		Globals.threshold = threshold;
	}
	
	static private Byte dimension = null;
	
	public synchronized static Byte getDimension() {
		return dimension;
	}
	
	public synchronized static void setDimension( Byte dimension ) {
		Globals.dimension = dimension;
	}
	
	static boolean allowRedirect = true;
	static public Boolean allowRedirect() {
		return allowRedirect;
	}
	
	static public void setAllowRedirect(boolean allowRedirect) {
		Globals.allowRedirect = allowRedirect;
	}
	
	
}
