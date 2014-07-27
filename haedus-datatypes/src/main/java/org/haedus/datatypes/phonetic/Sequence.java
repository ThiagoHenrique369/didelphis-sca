package org.haedus.datatypes.phonetic;

import org.apache.commons.lang3.ArrayUtils;
import org.haedus.datatypes.Segmenter;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Sequence implements Iterable<Segment> {

	private final List<Segment> sequence;
	private final FeatureModel  features;

	public Sequence() {
		super();
		sequence = new LinkedList<Segment>();
		features = new FeatureModel();
	}

	public Sequence(Segment g) {
		this();
		sequence.add(g);
	}

	public Sequence(Sequence q) {
		sequence = new ArrayList<Segment>(q.getSegments());
		features = q.getFeatures();
	}

	public Sequence(CharSequence word) {
		this(word, new FeatureModel());
	}

	public Sequence(CharSequence word, FeatureModel featureTable) {
		sequence = new LinkedList<Segment>();
		features = featureTable;
		// Split and traverse
		for (String s : Segmenter.segment(word)) {
			sequence.add(new Segment(s));
		}
	}

	public Sequence(CharSequence word, FeatureModel featureTable, VariableStore variables) {
		sequence = new LinkedList<Segment>();
		features = featureTable;
		// Split and traverse
		for (String s : Segmenter.segment(word, variables.getKeys())) {
			sequence.add(new Segment(s));
		}
	}

	public Sequence(CharSequence word, FeatureModel featureTable, VariableStore variables, Normalizer.Form form) {
		sequence = new LinkedList<Segment>();
		features = featureTable;
		// Split and traverse
		for (String s : Segmenter.segment(word, variables.getKeys(), form)) {
			sequence.add(new Segment(s));
		}
	}

	private Sequence(Collection<Segment> segments) {
		this();
		sequence.addAll(segments);
	}

	public void add(Segment s) {
		sequence.add(s);
	}

	public void add(Sequence q) {
		for (Segment s : q) {
			sequence.add(s);
		}
	}

	public void insert(Sequence q, int index) {
		sequence.addAll(index, q.getSegments());
	}

	public void add(Segment[] segments) {
		Collections.addAll(sequence, segments);
	}

	@Override
	public String toString() {
		String s = "";

		for (Segment a_sequence : sequence) {
			s = s.concat(a_sequence.getSymbol() + " ");
		}
		return s.trim();
	}

	public String toStringClean() {
		String s = "";

		for (Segment a_sequence : sequence) {
			s = s.concat(a_sequence.getSymbol());
		}
		return s.trim();
	}

	public Segment get(int i) {
		return sequence.get(i);
	}

	public Segment getFirst() {
		return get(0);
	}

	public Segment getLast() {
		return get(sequence.size() - 1);
	}

	public void set(int i, Segment s) {
		sequence.set(i, s);
	}

	public int size() {
		return sequence.size();
	}

	public Sequence copy() {
		return new Sequence(sequence);
	}

	public Sequence getSubsequence(int i) {
		int endIndex = sequence.size();
		return getSubsequence(i, endIndex);
	}

	/**
	 * Returns a new sub-sequence spanning the provided indices
	 *
	 * @param i the starting index, inclusive - must be greater than zero
	 * @param k the ending index, exclusive - must be less than the sequence length
	 * @return
	 */
	public Sequence getSubsequence(int i, int k) {

		int index = (k <= size()) ? k : size();

		return new Sequence(sequence.subList(i, index));
	}

	public int indexOf(Segment s) {
		return sequence.indexOf(s);
	}

	public Segment remove(int index) {
		return sequence.remove(index);
	}

	public Sequence remove(int start, int end) {
		Sequence q = new Sequence();
		for (int i = 0; i < end - start; i++) {
			q.add(remove(start));
		}
		return q;
	}

	/**
	 * @param subsequence
	 * @return
	 */
	public int indexOf(Sequence subsequence) {
		int size  = subsequence.size();
		int index = -1;

		if (size <= size() && !subsequence.isEmpty()) {
			index = indexOf(subsequence.getFirst());
			if ((index >= 0) && (index + size <= size())) {
				Sequence u = getSubsequence(index, index + size);
				if (!subsequence.equals(u)) {
					index = -1;
				}
			}
		}

		return index;
	}

	public int indexOf(Sequence sequence, int start) {

		int index = -1;
		if (start < size()) {
			Sequence subsequence = getSubsequence(start);
			index = subsequence.indexOf(sequence);

			if (index > -1)
				index += start;
		}
		return index;
	}

	public int[] indicesOf(Sequence q) {
		int[] indices = new int[0];

		int index = indexOf(q);

		while (index >= 0) {
			indices = ArrayUtils.add(indices, index);
			index = indexOf(q, index + 1);
		}
		return indices;
	}

	public Sequence replaceFirst(Sequence source, Sequence target) {
		Sequence result = copy();
		int index = result.indexOf(source);
		result.remove(index, index + source.size());
		result.insert(target, index);

		return result;
	}

	public Sequence replaceAll(Sequence source, Sequence target) {
		Sequence result = copy();

		int index = result.indexOf(source);

		while (index >= 0) {
			if ( index + source.size() <= result.size()) {
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

	@Override
	public int hashCode() {
		int hash = 7741;
		for (Segment segment : sequence) {
			hash = hash * 31 + segment.hashCode() + 3;
		}
		return hash;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null)                  return false;
		if (obj.getClass() != getClass()) return false;

		Sequence object = (Sequence) obj;

		return sequence.equals(object.getSegments()) &&
			   features.equals(object.getFeatures());
	}

	public boolean isEmpty() {
		return sequence.isEmpty();
	}

	public Sequence getReverseSequence() {
		Sequence reversed = new Sequence();
		for (Segment g : sequence) {
			reversed.addFirst(g);
		}
		return reversed;
	}

	public List<Segment> getSegments() {
		return new ArrayList<Segment>(sequence);
	}

	public void addFirst(Segment g) {
		sequence.add(0, g);
	}

	public boolean contains(Sequence sequence) {
		return (indexOf(sequence) >= 0);
	}

	public boolean startsWith(Segment segment) {

		return !isEmpty() && sequence.get(0).equals(segment);
	}

	public boolean startsWith(Sequence sequence) {
		return (indexOf(sequence) == 0);
	}

	public FeatureModel getFeatures() {
		return features;
	}
}
