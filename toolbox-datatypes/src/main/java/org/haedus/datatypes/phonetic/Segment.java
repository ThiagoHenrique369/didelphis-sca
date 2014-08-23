/*******************************************************************************
 * Copyright (c) 2014 Haedus - Fabrica Codicis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Samantha Fiona Morrigan McCabe
 */
public class Segment {

	private final String      symbol;
	private final List<Float> features;

	/**
	 * Initialize an empty Segment
	 */
	public Segment() {
		symbol   = "";
		features = new ArrayList<Float>();
	}

	public Segment(String s) {
		symbol   = s;
		features = new ArrayList<Float>();
    }

    public Segment(String s, List<Float> featureArray) {
		symbol = s;
		features = new ArrayList<Float>(featureArray);
	}

    public Segment appendDiacritic(String diacriticSymbol, List<Float> diacriticFeatures) {
		List<Float> featureList = features;
        String s = symbol.concat(diacriticSymbol);

        for (int i = 1; i < diacriticFeatures.size(); i++) {
            float feature = diacriticFeatures.get(i);
            if (feature != -9) {
				featureList.set(i,feature);
            }
        }
        return new Segment(s, features);
    }

	public boolean equals(Object obj) {

		if (obj == null)                  return false;
		if (getClass() != obj.getClass()) return false;

		Segment object = (Segment) obj;

		return symbol.equals(object.symbol) &&
			   features.equals(object.features);
	}

    public int hashCode() {
        int hashCode = 19;
	    for (int i = 0; i < features.size(); i++) {
		    double number = (i + 1) * features.get(i);
		    hashCode *= number;
	    }
        return hashCode * symbol.hashCode() * 23 - 1;
    }

	public String getSymbol() {
		return symbol;
	}

	public List<Float> getFeatures() {
		return Collections.unmodifiableList(features);
	}

	public float getFeatureValue(int i) {
		return features.get(i);
	}

	public boolean isDiacritic() {
		return (features.get(0) == -1.0);
	}

	public int dimension() {
		return features.size();
	}

    @Override
    public String toString() {
        String featureString = " ";
		for (float _feature : features) {
			featureString += _feature + " ";
		}
        return  symbol + featureString.trim();
    }

	public boolean isEmpty() {
		return (symbol != null && symbol.isEmpty() && features.isEmpty());
	}
}
