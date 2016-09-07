/*******************************************************************************
 * Copyright (c) 2015. Samantha Fiona McCabe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.haedus.phonetic;

import org.haedus.phonetic.model.FeatureSpecification;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Samantha Fiona Morrigan McCabe
 */
public class Sequence implements List<Segment>, SpecificationBearer, Comparable<Sequence> {

	public static final Sequence EMPTY_SEQUENCE = new Sequence(Segment.EMPTY_SEGMENT);

	private static final transient Logger LOGGER = LoggerFactory.getLogger(Sequence.class);
	private static final Object[] OBJECTS = new Object[0];
	
	private final List<Segment> sequence;
	private final FeatureSpecification specification;

	public Sequence(Sequence q) {
		sequence = new ArrayList<Segment>(q.getSegments());
		specification = q.getSpecification();
	}

	public Sequence(Segment g) {
		this(g.getSpecification());
		sequence.add(g);
	}

	// Used to produce empty copies with the same model
	public Sequence(FeatureSpecification modelParam) {
		sequence = new LinkedList<Segment>();
		specification = modelParam;
	}

	private Sequence() {
		this(FeatureSpecification.EMPTY);
	}

	private Sequence(Collection<Segment> segments, FeatureSpecification featureTable) {
		sequence = new LinkedList<Segment>(segments);
		specification = featureTable;
	}

	@Override
	public boolean add(Segment s) {
		validateModelOrFail(s);
		return sequence.add(s);
	}

	@Override
	public boolean remove(Object o) {
		return sequence.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return sequence.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends Segment> c) {
		return sequence.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Segment> c) {
		return sequence.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return sequence.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return sequence.retainAll(c);
	}

	@Override
	public void clear() {
		sequence.clear();
	}

	public void add(Sequence otherSequence) {
		validateModelOrFail(otherSequence);
		for (Segment s : otherSequence) {
			sequence.add(s);
		}
	}

	public void insert(Sequence q, int index) {
		validateModelOrFail(q);
		sequence.addAll(index, q.getSegments());
	}

	@Override
	public Segment get(int index) {
		return sequence.get(index);
	}

	public Segment getFirst() {
		return get(0);
	}

	public Segment getLast() {
		return get(sequence.size() - 1);
	}

	@Override
	public Segment set(int i, Segment s) {
		return sequence.set(i, s);
	}

	@Override
	public void add(int index, Segment element) {
		sequence.add(index, element);
	}

	@Override
	public int size() {
		return sequence.size();
	}

	public Sequence getSubsequence(int i) {
		return getSubsequence(i, sequence.size());
	}

	/**
	 * Returns a new sub-sequence spanning the provided indices
	 * @param i the starting index, inclusive - must be greater than zero
	 * @param k the ending index, exclusive - must be less than the sequence length
	 * @return
	 */
	public Sequence getSubsequence(int i, int k) {
		return new Sequence(sequence.subList(i, k), specification);
	}

	public int indexOf(Segment target) {
		validateModelOrWarn(target);
		int index = -1;

		for (int i = 0; i < sequence.size() && index == -1; i++) {
			Segment segment = sequence.get(i);
			index = segment.matches(target) ? i : -1;
		}
		return index;
	}

	@Override
	public Segment remove(int index) {
		return sequence.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return sequence.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return sequence.lastIndexOf(o);
	}

	@NotNull
	@Override
	public ListIterator<Segment> listIterator() {
		return sequence.listIterator();
	}

	@NotNull
	@Override
	public ListIterator<Segment> listIterator(int index) {
		return sequence.listIterator(index);
	}

	@NotNull
	@Override
	public List<Segment> subList(int fromIndex, int toIndex) {
		return sequence.subList(fromIndex, toIndex);
	}

	public Sequence remove(int start, int end) {
		Sequence q = new Sequence(specification);
		for (int i = 0; i < end - start; i++) {
			q.add(remove(start));
		}
		return q;
	}

	/**
	 * Determines if a sequence is consistent with this sequence.
	 * Sequences must be of the same length
	 * 
	 * Two sequences are consistent if each other if all corresponding segments
	 * are consistent; i.e. if, for ever segment in each sequence, all
	 * corresponding features are equal OR if one is NaN
	 * 
	 * @param target a sequence to check against this one
	 * @return true if, for each segment in both sequences, all specified
	 * (non NaN) features in either segment are equal
	 */
	public boolean matches(Sequence target) {
		validateModelOrFail(target);
		boolean matches = false;
		if (specification == FeatureSpecification.EMPTY) {
			matches = equals(target);
		} else {
			int size = size();
			if (size == target.size()) {
				matches = true;
				for (int i = 0; i < size && matches; i++) {
					Segment a = get(i);
					Segment b = target.get(i);
					matches = a.matches(b);
				}
			}
		}
		return matches;
	}

	public int indexOf(Sequence target) {
		validateModelOrWarn(target);

		int size = target.size();
		int index = -1;

		if (size <= size() && !target.isEmpty()) {
			index = indexOf(target.getFirst());
			if (index >= 0 && index + size <= size()) {
				Sequence u = getSubsequence(index, index + size);
				// originally was equals, but use matches instead
				if (!target.matches(u)) {
					index = -1;
				}
			}
		}
		return index;
	}

	public int indexOf(Sequence target, int start) {
		int index = -1;
		if (start < size()) {
			Sequence subsequence = getSubsequence(start);
			index = subsequence.indexOf(target);
			if (index > -1) {
				index += start;
			}
		}
		return index;
	}

	public Sequence replaceAll(Sequence source, Sequence target) {
		validateModelOrFail(source);
		validateModelOrFail(target);
		Sequence result = new Sequence(this);

		int index = result.indexOf(source);
		while (index >= 0) {
			if (index + source.size() <= result.size()) {
				result.remove(index, index + source.size());
				result.insert(target, index);
			}
			index = result.indexOf(source, index + target.size());
		}
		return result;
	}

	@Override
	public Iterator<Segment> iterator() {
		return sequence.iterator();
	}

	@NotNull
	@Override
	public Object[] toArray() {
		int size = sequence.size();
		Object[] objects = new Object[size];
		for (int i = 0; i < size; i++) {
			objects[i] = sequence.get(i);
		}
		return objects;
	}

	@NotNull
	@Override
	public <T> T[] toArray(T[] a) {
		int size = sequence.size();
		Object[] elementData = toArray();
		if (a.length < size) {
			//noinspection unchecked
			return (T[]) Arrays.copyOf(elementData, size, a.getClass());
		}
		System.arraycopy(elementData, 0, a, 0, size);
		if (a.length > size) {
			a[size] = null;
		}
		return a;
	}

	@Override
	public int hashCode() {
		int hash = 23;
		hash *= sequence.hashCode();
		hash *= specification.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof Sequence)) { return false; }
		
		Sequence object = (Sequence) obj;
		return specification.equals(object.specification) && sequence.equals(object.sequence);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Segment a_sequence : sequence) {
			sb.append(a_sequence.getSymbol());
		}
		return sb.toString();
	}

	@Override
	public boolean isEmpty() {
		return sequence.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return false;
	}

	public Sequence getReverseSequence() {
		Sequence reversed = new Sequence(specification);
		for (Segment g : sequence) {
			reversed.addFirst(g);
		}
		return reversed;
	}

	public List<Segment> getSegments() {
		return new ArrayList<Segment>(sequence);
	}

	public void addFirst(Segment g) {
		validateModelOrFail(g);
		sequence.add(0, g);
	}

	public boolean contains(Sequence sequence) {
		return indexOf(sequence) >= 0;
	}

	public boolean startsWith(Segment aSegment) {
		validateModelOrWarn(aSegment);
		return !isEmpty() && sequence.get(0).matches(aSegment);
	}

	public boolean startsWith(Sequence aSequence) {
		return indexOf(aSequence) == 0;
	}

	@Override
	public FeatureSpecification getSpecification() {
		return specification;
	}

	@Override
	public int compareTo(Sequence o) {
		
		for (int i = 0; i < size() && i < o.size(); i++) {
			int value = get(i).compareTo(o.get(i));
			if (value != 0) {
				return value;
			}
		}
		return size() > o.size() ? 1 : -1;
	}

	// Visible for testing
	List<Integer> indicesOf(Sequence q) {
		List<Integer> indices = new ArrayList<Integer>();

		int index = indexOf(q);

		while (index >= 0) {
			indices.add(index);
			index = indexOf(q, index + 1);
		}
		return indices;
	}

	private void validateModelOrWarn(SpecificationBearer that) {
		if (!specification.equals(that.getSpecification())) {
			LOGGER.warn("Attempting to check a {} with an incompatible model!\n\t{}\t{}\n\t{}\t{}",
				that.getClass(), this, that, specification, that.getSpecification());
		}
	}

	private void validateModelOrFail(SpecificationBearer that) {
		if (!specification.equals(that.getSpecification())) {
			throw new RuntimeException(
				"Attempting to add " + that.getClass() + " with an incompatible model!\n" +
					'\t' + this + '\t' + specification + '\n' +
					'\t' + that + '\t' + that.getSpecification()
			);
		}
	}
}
