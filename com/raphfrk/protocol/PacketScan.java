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
package com.raphfrk.protocol;


public class PacketScan {
	
	public static final int maxPacketSize = 90*1024;
	
	static final Packet packetScan(byte[] buffer, int start, int dataLength, int mask, Packet packet) {
		return packetScan(buffer, start, dataLength, mask, packet, false);
	}

	static final Packet packetScan(byte[] buffer, int start, int dataLength, int mask, Packet packet, boolean debug) {

		if (debug) {
			System.out.println("Start scan");
			System.err.println("Start scan");
			System.out.println("Start " + start);
			System.err.println("Start " + start);
			System.out.println("dataLength " + dataLength);
			System.err.println("dataLength " + dataLength);
		}
		
		if(dataLength == 0) {
			return null;
		}
		
		if(packet == null) {
			packet = new Packet();
		}
		
		packet.start = start;
		packet.end = start;
		packet.buffer = buffer;
		packet.mask = mask;
		
		if(mask == 0) {
			System.out.println("Returning null as mask is null");
			System.err.println("Returning null as mask is null");
			return null;
		}
		
		if(mask + 1 != buffer.length) {
			System.out.println("Error: buffer length doesn't match bit mask");
			System.err.println("Error: buffer length doesn't match bit mask");
			return null;
		}

		int position = start;
		
		int packetId = buffer[position & mask] & 0xff;
		
		ProtocolUnitArray.Op[] ops     = ProtocolUnitArray.ops[packetId];
		int[]                  params  = ProtocolUnitArray.params[packetId];

		if(ops == null) {
			if(dataLength > 0) {
				System.out.println(packet + " Unknown packet Id " + Integer.toHexString(packetId));
				System.err.println(packet + " Unknown packet Id " + Integer.toHexString(packetId));
				StringBuilder sb = new StringBuilder();
				for(int cnt = -80; cnt<80 && cnt < dataLength;cnt++) {
					String value = Integer.toHexString(buffer[(start + cnt)&mask]&0xFF);
					if(cnt != 0) {
						sb.append(value + " ");
					} else {
						sb.append("*" + value + "* ");
					}
				}
				System.err.println(packet + sb.toString());
				throw new IllegalStateException("Unknown packet id + " + Integer.toHexString(packetId));
			}
			if (debug) {
				System.out.println("zero length packet");
				System.err.println("zero length packet");
			}
			return null;
		}

		int opsLength = ops.length;

		for(int cnt=0; cnt<opsLength; cnt++) {
			if (debug) {
				if (debug) {
					System.out.println("Op code: " + ops[cnt]);
					System.err.println("Op code: " + ops[cnt]);
					System.out.println("Param: " + params[cnt]);
					System.err.println("Param: " + params[cnt]);
				}
			}
			switch(ops[cnt]) {
			case JUMP_FIXED: {
				position = (position + params[cnt]);
				break;
			}
			case BYTE_SIZED: {
				int size = getByte(buffer, position, mask) & 0xFF;
				if (debug) {
					System.out.println("Size: " + size);
					System.err.println("Size: " + size);
				}
				position = (position + 1);
				if(size > maxPacketSize) {
					if(position - start <= dataLength) {
						System.err.println("Size to large in byte sized byte array");
						System.out.println("Size to large in byte sized byte array");
					}
					return null;
				}
				position = (position + size);
				if(size < 0) {
					return null;
				}
				break;
			}
			case SHORT_SIZED: {
				short size = getShort(buffer, position, mask);
				if (debug) {
					System.out.println("Size: " + size);
					System.err.println("Size: " + size);
				}
				position = (position + 2);
				if(size > maxPacketSize) {
					if(position - start <= dataLength) {
						System.err.println("Size to large in short sized byte array");
						System.out.println("Size to large in short sized byte array");
					}
					return null;
				}
				position = (position + size);
				if(size < 0) {
					return null;
				}
				break;
			}
			case SHORT_SIZED_DOUBLED: {
				short size = (short)(getShort(buffer, position, mask)<<1);
				if (debug) {
					System.out.println("Size: " + size);
					System.err.println("Size: " + size);
				}
				position = (position + 2);
				if(size > maxPacketSize) {
					if(position - start <= dataLength) {
						System.err.println("Size to large in short sized double byte array");
						System.out.println("Size to large in short sized double byte array");
					}
					return null;
				}
				position = (position + size);
				if(size < 0) {
					return null;
				}
				break;
			}
			case SHORT_SIZED_QUAD: {
				short size = (short)(getShort(buffer, position, mask)<<2);
				if (debug) {
					System.out.println("Size: " + size);
					System.err.println("Size: " + size);
				}
				position = (position + 2);
				if(size > maxPacketSize) {
					if(position - start <= dataLength) {
						System.err.println("Size to large in short sized quad byte array");
						System.out.println("Size to large in short sized quad byte array");
					}
					return null;
				}
				position = (position + size);
				if(size < 0) {
					return null;
				}
				break;
			}
			case INT_SIZED: {
				int size = getInt(buffer, position, mask);
				if (debug) {
					System.out.println("Size: " + size);
					System.err.println("Size: " + size);
				}
				if(packetId == 0x50) {
					System.out.println("Size: " + size);
					System.err.println("Size: " + size);
				}
				position = (position + 4);
				if(size > maxPacketSize) {
					if(position - start <= dataLength) {
						System.err.println("Size to large in int sized byte array");
						System.out.println("Size to large in int sized byte array");
					}
					return null;
				}
				position = (position + size);
				if(size < 0) {
					return null;
				}
				break;
			}
			case INT_SIZED_DUMMY: {
				int size = getInt(buffer, position, mask);
				if (debug) {
					System.out.println("Size: " + size);
					System.err.println("Size: " + size);
				}
				if(packetId == 0x50) {
					System.out.println("Size: " + size);
					System.err.println("Size: " + size);
				}
				position = (position + 4);
				int dummy = getInt(buffer, position, mask);
				position = (position + 4);
				if(size > maxPacketSize) {
					if(position - start <= dataLength) {
						System.err.println("Size to large in int sized byte array");
						System.out.println("Size to large in int sized byte array");
					}
					return null;
				}
				position = (position + size);
				if(size < 0) {
					return null;
				}
				break;
			}
			case INT_SIZED_TRIPLE: {
				int size = getInt(buffer, position, mask)*3;
				if (debug) {
					System.out.println("Size: " + size);
					System.err.println("Size: " + size);
				}
				position = (position + 4);
				if(size > maxPacketSize) {
					if(position - start <= dataLength) {
						System.err.println("Size to large in triple byte array");
						System.out.println("Size to large in triple byte array");
					}
					return null;
				}
				position = (position + size);
				if(size < 0) {
					return null;
				}
				break;
			}
			case INT_SIZED_QUAD: {
				int size = getInt(buffer, position, mask)<<2;
				if (debug) {
					System.out.println("Size: " + size);
					System.err.println("Size: " + size);
				}
				position = (position + 4);
				if(size > maxPacketSize) {
					if(position - start <= dataLength) {
						System.err.println("Size to large in int sized quad byte array");
						System.out.println("Size to large in int sized quad byte array");
					}
					return null;
				}
				position = (position + size);
				if(size < 0) {
					return null;
				}
				break;
			}
			case INT_SIZED_INT_SIZED_SINGLE: {
				int size1 = getInt(buffer, position, mask)<<2;
				if (debug) {
					System.out.println("Size1: " + size1);
					System.err.println("Size1: " + size1);
				}
				if (size1 < 0) {
					return null;
				}
				if (size1 > 65536) {
					if( position - start <= dataLength) {
						System.err.println("Size1 to large in int sized (int sized byte array) array");
						System.out.println("Size1 to large in int sized (int sized byte array) array");
					}
					return null;
				}
				position = (position + 4);
				int totalSize = 4;
				for (int i = 0; i < size1; i++) {
					if (position - start > dataLength) {
						return null;
					}
					if(totalSize > maxPacketSize) {
						if(position - start <= dataLength) {
							System.err.println("Size to large in int sized (int sized byte array) array");
							System.out.println("Size to large in int sized (int sized byte array) array");
						}
					return null;
					}
					int size2 = getInt(buffer, position, mask);
					if (debug) {
						System.out.println("Size2: " + size2);
						System.err.println("Size2: " + size2);
					}
					if (size2 < 0) {
						return null;
					}
					if (size2 > 65536) {
						if( position - start <= dataLength) {
							System.err.println("Size2 to large in int sized (int sized byte array) array");
							System.out.println("Size2 to large in int sized (int sized byte array) array");
						}
						return null;
					}
					position += 4 + size2;
					totalSize += 4 + size2;
				}
				if (totalSize > maxPacketSize) {
					return null;
				}
				break;
			}
			case META_DATA: {
				byte b;
				do {
					int select;
					select = (((b = getByte(buffer, position, mask)) & 0xFF) >> 5);
					position = (position + 1);
					if(b != 127) {
						switch(select) {
						case 0: {
							position = (position + 1); break;
						}
						case 1: {
							position = (position + 2); break;
						}
						case 2:
						case 3: {
							position = (position + 4); break;
						}
						case 4: { // string read
							short size = (short)(getShort(buffer, position, mask)<<1);
							position = (position + 2);
							if(size > maxPacketSize) {
								if(position - start <= dataLength) {
									System.err.println("String to large in meta data");
									System.out.println("String to large in meta data");
								}
								return null;
							}
							position = (position + size);
							if(size < 0) {
								return null;
							}
							break;
						}
						default: {
							if(position - start <= dataLength) {
								System.err.println("Unknown meta data type: " + select);
								System.out.println("Unknown meta data type: " + select);
							}
							return null;
						}
						}				
					}
				} while (b != 127 && position - start <= dataLength);
				break;
			}
			case OPTIONAL_MOTION: {
				int optional = getInt(buffer, position, mask);
				if (debug) {
					System.out.println("optional: " + optional);
					System.err.println("optional: " + optional);
				}
				position = (position + 4);
				if(optional > 0) {
					position = position + 6;
				}
				break;
			}
			case ITEM: {
				position = getItem(buffer, position, mask);
				break;
			}
			case ITEM_ARRAY: {
				short count = getShort(buffer, position, mask);
				if (debug) {
					System.out.println("count: " + count);
					System.err.println("count: " + count);
				}
				position = (position + 2);
				if(count > (maxPacketSize >> 3)) {
					if(position - start <= dataLength) {
						System.err.println("Item stack array to large");
						System.out.println("Item stack array to large");
					}
					return null;
				}
				for(int c=0; c<count && position - start <= dataLength; c++) {
					position = getItem(buffer, position, mask);
				}
				break;
			}
			default: {
				if(position - start <= dataLength) {
					System.err.println("Unknown enum type " + ops[cnt]);
					System.out.println("Unknown enum type " + ops[cnt]);
				}
				return null;
			}
			}
		}
		
		if (debug) {
			System.out.println("position: " + position);
			System.err.println("position: " + position);
			System.out.println("Start: " + start);
			System.err.println("Start: " + start);
			System.out.println("dataLength: " + dataLength);
			System.err.println("dataLength: " + dataLength);	
			System.out.println("maxPacketSize: " + maxPacketSize);
			System.err.println("maxPacketSize: " + maxPacketSize);
			
		}
		
		if(position - start > maxPacketSize) {
			return null;
		}
		
		if(position - start > dataLength) {
			return null;
		}
		
		packet.start = start;
		packet.end = position;
		packet.buffer = buffer;
		packet.mask = mask;

		return packet;

	}
	
	static int getItem(byte[] buffer, int position, int mask) {
		
		short type = getShort(buffer, position, mask);
		position = (position + 2);
		if(type != -1) {
			position = (position + 3);
			if(ProtocolUnitArray.enchantedItemsIds.contains(type)) {
				short length = getShort(buffer, position, mask);
				position = (position + 2);
				if(length >= 0) {
					position += length;
				}
			}
		}
		
		return position;
	}
	
	static byte getByte(byte[] buffer, int position, int mask) {
		return buffer[position & mask];
	}
	
	static short getShort(byte[] buffer, int position, int mask) {
		
		byte a = buffer[(position + 0) & mask];
		byte b = buffer[(position + 1) & mask];
		
		return (short) (
				((a & 0xFF) << 8) |
				((b & 0xFF) << 0)
				);
		
	}
	
	static int getInt(byte[] buffer, int position, int mask) {
		
		byte a = buffer[(position + 0) & mask];
		byte b = buffer[(position + 1) & mask];
		byte c = buffer[(position + 2) & mask];
		byte d = buffer[(position + 3) & mask];
		
		return  (
				((a & 0xFF) << 24) |
				((b & 0xFF) << 16) |
				((c & 0xFF) << 8)  |
				((d & 0xFF) << 0)
				);
		
	}


}
