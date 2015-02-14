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
import java.util.Collections;
import java.util.List;

/**
 * Samantha Fiona Morrigan McCabe
 */
public class Segment implements ModelBearer {

	public static final Segment EMPTY_SEGMENT = new Segment("∅");

	private final FeatureModel featureModel;
	private final String       symbol;
	private final List<Double> features;

	// Copy-constructor
	public Segment(Segment segment) {
		symbol = segment.symbol;
		featureModel = segment.featureModel;
		features = segment.features;
	}

	@Deprecated
	public Segment(String s, FeatureModel modelParam) {
		symbol = s;
		featureModel = modelParam;
		features = featureModel.getValue(s);
	}

	public Segment(String s, List<Double> featureArray, FeatureModel modelParam) {
		symbol = s;
		featureModel = modelParam;
		features = new ArrayList<Double>(featureArray);
	}

	// Never used
	private Segment() {
		this("");
	}

	// Used to create the empty segment
	private Segment(String string) {
		symbol = string;
		featureModel = FeatureModel.EMPTY_MODEL;
		features = new ArrayList<Double>();
	}

	public boolean matches(Segment other) {
		validateModelOrFail(other);
		int size = features.size();
		List<Double> otherFeatures = other.getFeatures();
		for (int i = 0; i < size; i++) {
			Double a = features.get(i);
			Double b = otherFeatures.get(i);
			// One-way comparison
			if (!a.equals(b) && !b.equals(Double.NaN)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public FeatureModel getFeatureModel() {
		return featureModel;
	}

	@Override
	public int hashCode() {
		int hash = 19;
		hash *= 31 + symbol.hashCode();
		hash *= 31 + features.hashCode();
		hash *= 31 + featureModel.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null) { return false; }
		if (getClass() != obj.getClass()) { return false; }

		Segment other = (Segment) obj;
		return symbol.equals(other.getSymbol()) &&
			features.equals(other.features)
			&& featureModel.equals(other.getFeatureModel());
	}

	@Override
	public String toString() {
		return symbol;
	}

	public String getSymbol() {
		return symbol;
	}

	public List<Double> getFeatures() {
		return Collections.unmodifiableList(features);
	}

	public double getFeatureValue(int i) {
		return features.get(i);
	}

	public String toStringLong() {
		StringBuilder sb = new StringBuilder(symbol + '\t');
		for (Double feature : features) {
			if (feature.equals(Double.NaN)) {
				sb.append(" ***");
			} else {
				if (feature < 0.0) {
					sb.append(feature);
				} else {
					sb.append(' ');
					sb.append(feature);
				}
			}
			sb.append(' ');
		}
		return sb.toString();
	}

	private void validateModelOrFail(ModelBearer that) {
		FeatureModel otherModel = that.getFeatureModel();
		if (!featureModel.equals(otherModel)) {
			throw new RuntimeException(
				"Attempting to add " + that.getClass() + " with an incompatible featureModel!\n" +
					'\t' + this + '\t' + featureModel.getFeatureNames() + '\n' +
					'\t' + that + '\t' + otherModel.getFeatureNames()
			);
		}
	}
}
