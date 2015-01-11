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
package org.haedus.datatypes.phonetic;

/**
 *
 * @author Samantha Fiona Morrigan McCabe
 */
public class Alignment {

    private final FeatureModel model;
	private final Sequence left;
	private final Sequence right;
    private double score = Double.NaN;

	public Alignment(Sequence l, Sequence r) {
        if (l.getFeatureModel().equals(r.getFeatureModel())) {
		    left  = l;
		    right = r;
            model = l.getFeatureModel();
        } else {
            throw new RuntimeException(
                    "Attempting to create an Alignment with Sequences backed by two different FeatureModels: " +
                    "\t" + l.getFeatureModel().toString() +
                    "\t" + r.getFeatureModel().toString()
            );
        }
	}

    public Alignment(Segment l, Segment r) {
        this(new Sequence(l), new Sequence(r));
    }

    public Alignment(FeatureModel modelParam) {
        left  = new Sequence(modelParam);
        right = new Sequence(modelParam);
        model = modelParam;
    }

	public Alignment(Alignment alignment) {
		left  = new Sequence(alignment.getLeft());
		right = new Sequence(alignment.getRight());
        model = alignment.getModel();
	}

    public void add(Segment l, Segment r) {
        if (left.getFeatureModel().equals(model) &&
            right.getFeatureModel().equals(model)) {
            left.add(l);
            right.add(r);
        } else {
            throw new RuntimeException(
                    "Attempting to create an Alignment with Sequences backed by different FeatureModels than are used in this alignment: " +
                    "\tLeft  " + left.getFeatureModel().toString()  + " vs " + l.getFeatureModel().toString() +
                    "\tRight " + right.getFeatureModel().toString() + " vs " + r.getFeatureModel().toString()
            );
        }
    }

    public void add(Alignment a) {
        left.add(a.getLeft());
        right.add(a.getRight());
    }

    public int size() {
        return left.size();
    }

    @Override
    public String toString() {
        return left.toString() + "|" + right.toString();
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
        if (obj == null)                 return false;
		if (!(obj instanceof Alignment)) return false;

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

    public FeatureModel getModel() {
        return model;
    }
}
