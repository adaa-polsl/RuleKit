package adaa.analytics.rules.logic.representation;

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class IntegerBitSet implements Set<Integer> {

	private class BitIterator implements Iterator<Integer> {

		private IntegerBitSet bitset;
		private int id;
		
		public BitIterator(IntegerBitSet bitset) {
			this.bitset = bitset;
			this.id = 0;
			moveToNext();
		}
		
		@Override
		public boolean hasNext() {
			return (id < bitset.maxElement);
		}

		@Override
		public Integer next() {
			int v = id;
			++id;
			moveToNext();
			return v;
		}
		
		private void moveToNext() {
			for (; id < bitset.maxElement ;++id) {
				if (bitset.contains(id)) {
					return;
				}
			}
		}
	}
	
	
	private long[] words;
	private int maxElement;
	
	public int getMaxElement() { return maxElement; }
	
	public IntegerBitSet(int maxElement) {
		this.maxElement = maxElement;
		int wordsCount = (maxElement + Long.SIZE - 1) / Long.SIZE;
		words = new long[wordsCount];
		
	}
	
	@Override
	public boolean add(Integer v) {
		int wordId = v / Long.SIZE;
		int wordOffset = v % Long.SIZE;
		
		words[wordId] |= 1L << wordOffset;
		return true;
	}

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
				this.add((int)it.next());
			}
		}
		
		return true;
	}

	@Override
	public void clear() {
		for (int i = 0; i < words.length; ++i) {
			words[i] = 0L;
		}
	}
	
	public void setAll() {
		for (int i = 0; i < words.length - 1; ++i) {
			words[i] = ~(0L);
		}
		
		int rest = maxElement % Long.SIZE;
		words[words.length - 1] = (~(0L)) >>> (Long.SIZE - rest);
	}

	@Override
	public boolean contains(Object arg0) {
		int v = (int)arg0;
		int wordId = v / Long.SIZE;
		int wordOffset = v % Long.SIZE;
		
		return (words[wordId] & 1L << wordOffset) != 0;
	}

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
				if (!this.contains((int)it.next())) {
					return false;
				}
			}
		}
		
		return true;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterator<Integer> iterator() {
		return new BitIterator(this);
	}

	@Override
	public boolean remove(Object arg0) {
		int v = (int)arg0;
		
		int wordId = v / Long.SIZE;
		int wordOffset = v % Long.SIZE;
		
		words[wordId] &= ~(1L << wordOffset);
		return true;
	}

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
				this.remove((int)it.next());
			}
		}
		
		return true;
	}

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
			this.clear();
			Iterator<?> it = arg0.iterator();
			while (it.hasNext()) {
				this.add((int)it.next());
			}
		}
		
		return true;
	}

	@Override
	public int size() {
		int s = 0;
		for (int i = 0; i < words.length; ++i) {
			s += Long.bitCount(words[i]);
		}
		return s;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
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
	
}