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

public class EntityMap {
	
	public final static int[][] entityIds = new int[256][];
	
	static {
		
		entityIds[0x05] = new int[] {1};
		entityIds[0x07] = new int[] {1, 5};
		entityIds[0x11] = new int[] {1};
		entityIds[0x12] = new int[] {1};
		entityIds[0x13] = new int[] {1};
		entityIds[0x14] = new int[] {1};
		entityIds[0x15] = new int[] {1};
		entityIds[0x16] = new int[] {1, 5};
		entityIds[0x17] = new int[] {1};
		entityIds[0x18] = new int[] {1};
		entityIds[0x19] = new int[] {1};
		entityIds[0x1C] = new int[] {1};
		entityIds[0x1D] = new int[] {1};
		entityIds[0x1E] = new int[] {1};
		entityIds[0x1F] = new int[] {1};
		entityIds[0x20] = new int[] {1};
		entityIds[0x21] = new int[] {1};
		entityIds[0x22] = new int[] {1};
		entityIds[0x26] = new int[] {1};
		entityIds[0x27] = new int[] {1, 5};
		entityIds[0x28] = new int[] {1};
		entityIds[0x47] = new int[] {1};
	}
	
	

}
