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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.haedus.phonetic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author Samantha Fiona Morrigan McCabe
 */
public class Alignment implements ModelBearer, Iterable<Alignment> {

	private final FeatureModel featureModel;
	private final Sequence     left;
	private final Sequence     right;
	private double score = Double.NaN;

	public Alignment(FeatureModel modelParam) {
		featureModel = modelParam;
		left  = new Sequence(modelParam);
		right = new Sequence(modelParam);
	}

	public Alignment(Sequence l, Sequence r) {
		modelConsistencyCheck(l, r);
		left = l;
		right = r;
		featureModel = l.getFeatureModel();
	}

	public Alignment(Segment l, Segment r) {
		this(new Sequence(l), new Sequence(r));
	}

	public Alignment(Alignment alignment) {
		left = new Sequence(alignment.getLeft());
		right = new Sequence(alignment.getRight());
		featureModel = alignment.getFeatureModel();
	}

	public void add(Segment l, Segment r) {
		modelConsistencyCheck(l, r);
		left.add(l);
		right.add(r);
	}

	public void add(Alignment a) {
		validateModelOrFail(a);
		left.add(a.getLeft());
		right.add(a.getRight());
	}

	public int size() {
		return left.size();
	}

	@Override
	public String toString() {
		return left + "|" + right;
    }

    public Alignment get(int i) {
        Sequence l = left.getSubsequence(i, i + 1);
        Sequence r = right.getSubsequence(i, i + 1);
        return new Alignment(l,r);
    }

    public Alignment getLast() {
        return get(size()-1);
    }

    public void setScore(double scoreParam) {
        score = scoreParam;
    }

	@Override
    public boolean equals(Object obj) {
        if (obj == null)                 { return false; }
		if (!(obj instanceof Alignment)) { return false; }

		Alignment alignment = (Alignment) obj;

		return alignment.getLeft().equals(left) &&
		       alignment.getRight().equals(right);
	}

    public Sequence getLeft() {
        return left;
    }

    public Sequence getRight() {
        return right;
    }

    @Override
	public FeatureModel getFeatureModel() {
		return featureModel;
	}

	@Override
	public Iterator<Alignment> iterator() {
		Collection<Alignment> pairs = new ArrayList<Alignment>();
		for (int i = 0; i < left.size(); i++) {
			pairs.add(new Alignment(
				left.get(i),
				right.get(i))
			);
		}
		return pairs.iterator();
	}

	private void validateModelOrFail(ModelBearer that) {
		FeatureModel thatFeatureModel = that.getFeatureModel();
		if (!featureModel.equals(thatFeatureModel)) {
			throw new RuntimeException(
				"Attempting to add " + that.getClass() + " with an incompatible model!\n" +
					'\t' + this + '\t' + featureModel.getFeatureNames() + '\n' +
					'\t' + that + '\t' + thatFeatureModel.getFeatureNames()
			);
		}
	}

	private static void modelConsistencyCheck(ModelBearer l, ModelBearer r) {
		FeatureModel mL = l.getFeatureModel();
		FeatureModel mR = r.getFeatureModel();
		if (!mL.equals(mR)) {
			throw new RuntimeException(
				"Attempting to create Alignment using incompatible models!\n" +
					'\t' + l + '\t' + mL.toString() + '\n' +
					'\t' + r + '\t' + mR.toString() + '\n'
			);
		}
	}
}
