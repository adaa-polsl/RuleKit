/*******************************************************************************
 * Copyright (C) 2019 RuleKit Development Team
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package adaa.analytics.rules.logic.representation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Upper-bounded set of integers represented internally as a bit vector.
 *
 * NOTE: While class implements Serializable interface, serialization/deserialization methods do nothing!
 *
 * @author Adam Gudys
 *
 */
public class IntegerBitSet implements Set<Integer>, Serializable {

	private class BitIterator implements Iterator<Integer> {

		/** Reference to the bit set being iterated. */
		private IntegerBitSet bitset;
		
		/** Current element identifier. */
		private int id = 0;

		private int wordId = 0;

		private long word = 0;
		
		/** 
		 * Creates iterator object.
		 * @param bitset Bit set being iterated.
		 */
		public BitIterator(IntegerBitSet bitset) {
			this.bitset = bitset;
			this.word = bitset.words[0];

			// seek to the first 1 or go outside collection (hasNext() == true) if no more elements
			while (((word & 1L) == 0) && increment()) { }
		}
		
		/** 
		 * Checks if next element exists.
		 * @return Test result.
		 */
		@Override
		public boolean hasNext() {
			return (id < bitset.maxElement);
		}

		/**
		 * Extracts integer at the current location and moves to the next location.
		 * @return Element at the current location.
		 */
		@Override
		public Integer next() {
			int v = id;

			// this will go outside collection (hasNext() == true) if no more elements
			while (increment() && ((word & 1L) == 0)) { }

			return v;
		}
		
		/**
		 * Moves to the next bit or the first bit of the next word if no more bits in the current word.
		 */
		private boolean increment() {
			word = word >>> 1;

			if (word == 0) {
				// no more bits in this word - can safely move to the next word
				++wordId;
				id = wordId * Long.SIZE;
				// if there are more words
				if (wordId < bitset.words.length) {
					word = bitset.words[wordId];
				}
			} else {
				// just increase the id
				++id;
			}

			return id < bitset.maxElement;
		}
	}

	public static final int OFFSET_MASK = 63;

	public static final int ID_SHIFT = 6;
	
	/** Array of words for storing bits. */
	private long[] words;
	
	/** Max element that can be stored in the set. */
	private int maxElement;
	
	/** Gets {@link #maxElement}. */
	public int getMaxElement() { return maxElement; }

	public long[] getRawTable() { return words; }
	
	/**
	 * Allocates words array for storing bits.
	 * @param maxElement Max element that can be stored in the set.
	 */
	public IntegerBitSet(int maxElement) {
		this.maxElement = maxElement;
		int wordsCount = (maxElement + Long.SIZE - 1) / Long.SIZE;
		words = new long[wordsCount];
		
	}

	/**
	 * Allocates words array for storing bits.
	 * @param maxElement Max element that can be stored in the set.
	 */
	public IntegerBitSet(int maxElement, boolean fill) {
		this.maxElement = maxElement;
		int wordsCount = (maxElement + Long.SIZE - 1) / Long.SIZE;
		words = new long[wordsCount];

		if (fill) {
			this.setAll();
		}
	}
	
	/**
	 * Adds new integer to the set (sets an appropriate bit).
	 * @param v Integer to be added.
	 * @return Always true.
	 */
	@Override
	public boolean add(Integer v) {
		int wordId = v >>> ID_SHIFT;
		int wordOffset = v & OFFSET_MASK;
		
		words[wordId] |= 1L << wordOffset;
		return true;
	}

	/**
	 * Adds all elements from a collection of integers to the set (sets appropriate bits). 
	 * If given collection is another bit set, an optimized path is executed (logical operations on bit vectors).
	 * @param arg0 Collection of integers to be added.
	 * @return Always true.
	 */
	@Override
	public boolean addAll(Collection<? extends Integer> arg0) {
		if ((arg0 instanceof IntegerBitSet)) {
			// bitset implementation (fast)
			IntegerBitSet other = (IntegerBitSet)arg0;
			for (int i = 0; i < words.length; ++i) {
				words[i] |= other.words[i];		
			}
		} else {
			// global implementation (slow)
			Iterator<?> it = arg0.iterator();
			while (it.hasNext()) {
				this.add((Integer)it.next());
			}
		}
		
		return true;
	}

	/** 
	 * Clears the set (resets all the bits).
	 */
	@Override
	public void clear() {
		for (int i = 0; i < words.length; ++i) {
			words[i] = 0L;
		}
	}
	
	/**
	 * Adds all elements up to {@link #maxElement} (sets all bits). 
	 */
	public void setAll() {
		for (int i = 0; i < words.length - 1; ++i) {
			words[i] = ~(0L);
		}
		
		int rest = maxElement % Long.SIZE;
		words[words.length - 1] = (~(0L)) >>> (Long.SIZE - rest);
	}

	/**
	 * Clears set and adds all elements from another collections.
	 */
	public void setAll(Collection<? extends Integer> arg0) {
		this.clear();
		this.addAll(arg0);
	}
	
	/**
	 * Generates complement of the set (negates all bits).
	 */
	public void negate() {
		for (int i = 0; i < words.length - 1; ++i) {
			words[i] = ~words[i];
		}

		int rest = maxElement % Long.SIZE;
		words[words.length - 1] = (~words[words.length - 1]) & (~(0L)) >>> (Long.SIZE - rest);
	}

	/**
	 * Generates complement of the set (negates all bits).
	 */
	public void negate(IntegerBitSet output) {
		for (int i = 0; i < words.length - 1; ++i) {
			output.words[i] = ~words[i];
		}

		int rest = maxElement % Long.SIZE;
		output.words[words.length - 1] = (~words[words.length - 1]) & (~(0L)) >>> (Long.SIZE - rest);
	}


	/**
	 * Checks if the set contains a given integer.
	 * @param arg0 Integer to be checked.
	 * @return Test result.
	 */
	@Override
	public boolean contains(Object arg0) {
		int v = (int)arg0;
		int wordId = v >>> ID_SHIFT;
		int wordOffset = v & OFFSET_MASK;
		
		return (words[wordId] & 1L << wordOffset) != 0;
	}

	/**
	 * Check if the set contains all integers from a given collection.
	 * If given collection is another bit set, an optimized path is executed (logical operations on bit vectors).
	 * @param arg0 Collection of integers to be checked.
	 * @return Test result.
	 */
	@Override
	public boolean containsAll(Collection<?> arg0) {
		if ((arg0 instanceof IntegerBitSet)) {
			// bitset implementation (fast)
			IntegerBitSet other = (IntegerBitSet)arg0;
			for (int i = 0; i < words.length; ++i) {
				if ((words[i] & other.words[i]) != other.words[i]) {
					return false;
				}
			}
		} else {
			// global implementation (slow)
			Iterator<?> it = arg0.iterator();
			while (it.hasNext()) {
				if (!this.contains((Integer)it.next())) {
					return false;
				}
			}
		}
		
		return true;
	}

	/**
	 * Checks if set is empty.
	 * @return Test result.
	 */
	@Override
	public boolean isEmpty() {
		for (long word : words) {
			if (word != 0) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Gets a set iterator.
	 * @return Set iterator.
	 */
	@Override
	public Iterator<Integer> iterator() {
		return new BitIterator(this);
	}

	/**
	 * Removes an integer from the set (resets an appropriate bit).
	 * @param arg0 Integer to be removed.
	 * @return Always true.
	 */
	@Override
	public boolean remove(Object arg0) {
		int v = (int)arg0;

		int wordId = v >>> ID_SHIFT;
		int wordOffset = v & OFFSET_MASK;
		
		words[wordId] &= ~(1L << wordOffset);
		return true;
	}

	/**
	 * Removes all elements from a collection of integers to the set (resets appropriate bits). 
	 * If given collection is another bit set, an optimized path is executed (logical operations on bit vectors).
	 * @param arg0 Collection of integers to be removed.
	 * @return Always true.
	 */
	@Override
	public boolean removeAll(Collection<?> arg0) {
		if ( (arg0 instanceof IntegerBitSet)) {
			// bitset implementation (fast)
			IntegerBitSet other = (IntegerBitSet)arg0;
			for (int i = 0; i < words.length; ++i) {
				words[i] &= ~other.words[i];		
			}
		} else {
			// global implementation (slow)
			Iterator<?> it = arg0.iterator();
			while (it.hasNext()) {
				this.remove((Integer)it.next());
			}
		}
		
		return true;
	}

	/**
	 * Retain from the set all of its elements that are contained in the specified collection.
	 * If given collection is another bit set, an optimized path is executed (logical operations on bit vectors).
	 * @param arg0 Collection with elements to be retained.
	 * @return Always true.
	 */
	@Override
	public boolean retainAll(Collection<?> arg0) {
		if ((arg0 instanceof IntegerBitSet)) {
			// bitset implementation (fast)
			IntegerBitSet other = (IntegerBitSet)arg0;
			for (int i = 0; i < words.length; ++i) {
				words[i] &= other.words[i];
			}
		} else {
			// global implementation (slow)

			// iterate over this elements
			for (int i = 0; i < maxElement; ++i) {
				int wordId = i >>> ID_SHIFT;
				int wordOffset = i & OFFSET_MASK;

				long val = words[wordId] & (1L << wordOffset);

				// remove element if not present in another collection
				if(val != 0 && !arg0.contains(i)) {
					words[wordId] &= ~(1L << wordOffset);
				}
			}
		}

		return true;
	}
	
	/**
	 * Calculates set size.
	 * @return Set size.
	 */
	@Override
	public int size() {
		int s = 0;
		for (int i = 0; i < words.length; ++i) {
			s += Long.bitCount(words[i]);
		}
		return s;
	}

	
	/**
	 * Generates a text representation of the set.
	 * @return Text representation.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(size() + "[");
		
		Iterator<Integer> it = this.iterator();
		while (it.hasNext()) {
			int v = it.next();
			sb.append(v);
			sb.append(",");
		}
		sb.append("]");
		
		return sb.toString();
	}
	
	/**
	 * Calculates size of the interesection between this set and another one.
	 * @param other Other set.
	 * @return Intersection size.
	 */
	public int calculateIntersectionSize(IntegerBitSet other) {
		int s = 0;
		for (int i = 0; i < words.length; ++i) {
			long x = words[i] & other.words[i];	
			s += Long.bitCount(x);
		}
		
		return s;
	}
	
	/**
	 * Calculates size of the interesection between this set and two other ones.
	 * @param other1 First other set.
	 * @param other2 Second other set.
	 * @return Intersection size.
	 */
	public int calculateIntersectionSize(IntegerBitSet other1, IntegerBitSet other2) {
		int s = 0;
		for (int i = 0; i < words.length; ++i) {
			long x = words[i] & other1.words[i] & other2.words[i];	
			s += Long.bitCount(x);
		}
		
		return s;
	}
	
	/**
	 * To be implemented.
	 * @return Always null.
	 */
	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * To be implemented.
	 * @param arg0 Unused argument.
	 * @return Always null.
	 */
	@Override
	public <T> T[] toArray(T[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Clones the set.
	 * @return Deep copy of the set.
	 */
	public IntegerBitSet clone() {
		IntegerBitSet out = new IntegerBitSet(this.maxElement);
		out.addAll(this);
		return out;
	}
	
	/**
	 * Compares two other integer sets using this set as a mask.
	 * @param arg0 First set.
	 * @param arg1 Second set.
	 * @return Value indicating if given two sets are equal after masking.
	 */
	public boolean filteredCompare(IntegerBitSet arg0, IntegerBitSet arg1) {
		for (int i = 0; i < words.length; ++i) {
			if ((this.words[i] & arg0.words[i]) != (this.words[i] & arg1.words[i])) {
				return false;
			}
		}
	
		return true;
	}

	/** Empty deserialization method */
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
	}

	/** Empty serialization method */
	private void writeObject(ObjectOutputStream os) throws IOException {
	}
}
