package com.raphfrk.protocol;


public class PacketScan {
	
	public static final int maxPacketSize = 90*1024;

	static final Packet packetScan(byte[] buffer, int start, int dataLength, int mask, Packet packet) {

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
			return null;
		}

		int opsLength = ops.length;

		for(int cnt=0; cnt<opsLength; cnt++) {
			switch(ops[cnt]) {
			case JUMP_FIXED: {
				position = (position + params[cnt]);
				break;
			}
			case BYTE_SIZED: {
				int size = getByte(buffer, position, mask) & 0xFF;
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
			case INT_SIZED_TRIPLE: {
				int size = getInt(buffer, position, mask)*3;
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
				position = (position + 4);
				if(optional > 0) {
					position = position + 6;
				}
				break;
			}
			case ITEM: {
				short type = getShort(buffer, position, mask);
				position = (position + 2);
				if(type != -1) {
					position = (position + 3);
				}
				break;
			}
			case ITEM_ARRAY: {
				short count = getShort(buffer, position, mask);
				position = (position + 2);
				if(count > (maxPacketSize >> 3)) {
					if(position - start <= dataLength) {
						System.err.println("Item stack array to large");
						System.out.println("Item stack array to large");
					}
					return null;
				}
				for(int c=0; c<count && position - start <= dataLength; c++) {
					short type = getShort(buffer, position, mask);
					position = (position + 2);
					if(type != -1) {
						position = (position + 3);
					}
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
