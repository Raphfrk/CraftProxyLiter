package com.raphfrk.protocol;


public class ProtocolUnitArray {

	static public enum Op {
		JUMP_FIXED,
		SHORT_SIZED,
		SHORT_SIZED_DOUBLED,
		SHORT_SIZED_QUAD,
		INT_SIZED,
		INT_SIZED_TRIPLE,
		META_DATA,
		ITEM,
		ITEM_ARRAY
	}

	public static Op[][] ops;
	public static int[][] params;

	public static OpPair string8          = new OpPair(Op.SHORT_SIZED, 0);
	public static OpPair string16         = new OpPair(Op.SHORT_SIZED_DOUBLED, 0);
	public static OpPair shortSizedQuad   = new OpPair(Op.SHORT_SIZED_QUAD, 0);
	public static OpPair metaData         = new OpPair(Op.META_DATA, 0);
	public static OpPair intSized         = new OpPair(Op.INT_SIZED, 0);
	public static OpPair intSizedTriple   = new OpPair(Op.INT_SIZED_TRIPLE, 0);
	
	public static OpPair item             = new OpPair(Op.ITEM, 0);
	public static OpPair itemArray        = new OpPair(Op.ITEM_ARRAY, 0);

	static {

		OpPair[][] opPairs = new OpPair[256][];

		// Packet Id byte must be included
		opPairs[0x00] = new OpPair[] {jump(1)};
		opPairs[0x01] = new OpPair[] {jump(5), string16, jump(9)};
		opPairs[0x02] = new OpPair[] {jump(1), string16};
		opPairs[0x03] = new OpPair[] {jump(1), string16};
		opPairs[0x04] = new OpPair[] {jump(9)};
		opPairs[0x05] = new OpPair[] {jump(11)};
		opPairs[0x06] = new OpPair[] {jump(13)};
		opPairs[0x07] = new OpPair[] {jump(10)};
		opPairs[0x08] = new OpPair[] {jump(3)};
		opPairs[0x09] = new OpPair[] {jump(1)};
		opPairs[0x0A] = new OpPair[] {jump(2)};
		opPairs[0x0B] = new OpPair[] {jump(34)};
		opPairs[0x0C] = new OpPair[] {jump(10)};
		opPairs[0x0D] = new OpPair[] {jump(42)};
		opPairs[0x0E] = new OpPair[] {jump(12)};	
		opPairs[0x0F] = new OpPair[] {jump(11), item};	
		opPairs[0x10] = new OpPair[] {jump(3)};	
		opPairs[0x11] = new OpPair[] {jump(15)};	
		opPairs[0x12] = new OpPair[] {jump(6)};	
		opPairs[0x13] = new OpPair[] {jump(6)};	
		opPairs[0x14] = new OpPair[] {jump(5), string16, jump(16)};
		opPairs[0x15] = new OpPair[] {jump(25)};
		opPairs[0x16] = new OpPair[] {jump(9)};
		opPairs[0x17] = new OpPair[] {jump(18)};
		opPairs[0x18] = new OpPair[] {jump(20), metaData};
		opPairs[0x19] = new OpPair[] {jump(5), string16, jump(16)};
		opPairs[0x1B] = new OpPair[] {jump(19)};
		opPairs[0x1C] = new OpPair[] {jump(11)};
		opPairs[0x1D] = new OpPair[] {jump(5)};
		opPairs[0x1E] = new OpPair[] {jump(5)};
		opPairs[0x1F] = new OpPair[] {jump(8)};
		opPairs[0x20] = new OpPair[] {jump(7)};
		opPairs[0x21] = new OpPair[] {jump(10)};
		opPairs[0x22] = new OpPair[] {jump(19)};
		opPairs[0x26] = new OpPair[] {jump(6)};
		opPairs[0x27] = new OpPair[] {jump(9)};
		opPairs[0x28] = new OpPair[] {jump(5), metaData};
		opPairs[0x32] = new OpPair[] {jump(10)};
		opPairs[0x33] = new OpPair[] {jump(14), intSized};
		opPairs[0x34] = new OpPair[] {jump(9), shortSizedQuad};
		opPairs[0x35] = new OpPair[] {jump(12)};
		opPairs[0x36] = new OpPair[] {jump(13)};
		opPairs[0x3C] = new OpPair[] {jump(29), intSizedTriple};
		opPairs[0x46] = new OpPair[] {jump(2)};
		opPairs[0x47] = new OpPair[] {jump(18)};	
		opPairs[0x64] = new OpPair[] {jump(3), string8, jump(1)};
		opPairs[0x65] = new OpPair[] {jump(2)};	
		opPairs[0x66] = new OpPair[] {jump(8), item};
		opPairs[0x67] = new OpPair[] {jump(4), item};
		opPairs[0x68] = new OpPair[] {jump(2), itemArray};
		opPairs[0x69] = new OpPair[] {jump(6)};	
		opPairs[0x6A] = new OpPair[] {jump(5)};	
		opPairs[0x82] = new OpPair[] {jump(11), string16, string16, string16, string16};	
		opPairs[0xC8] = new OpPair[] {jump(6)};	
		opPairs[0xFF] = new OpPair[] {jump(1), string16};	
		
		ops = new Op[256][];
		params = new int[256][];

		for (int cnt=0; cnt<256; cnt++) {
			OpPair[] opPairSingle = opPairs[cnt];
			if(opPairSingle == null) {
				continue;
			}
			int length = opPairSingle.length;
			Op[] opSingle = ops[cnt] = new Op[length];
			int[] paramSingle = params[cnt] = new int[length];
			for(int cnt2=0; cnt2<length; cnt2++) {
				opSingle[cnt2] = opPairSingle[cnt2].operation;
				paramSingle[cnt2] = opPairSingle[cnt2].parameter;
			}
		}

	}
	
	static OpPair jump(int jump) {
		return new OpPair(Op.JUMP_FIXED, jump);
	}

	private static class OpPair {

		OpPair(Op operation, int parameter) {
			this.operation = operation;
			this.parameter = parameter;
		}

		Op operation;
		int parameter;
	}

}
