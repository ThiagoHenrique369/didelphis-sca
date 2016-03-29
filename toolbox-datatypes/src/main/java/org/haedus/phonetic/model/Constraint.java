/******************************************************************************
 * Copyright (c) 2016. Samantha Fiona McCabe                                  *
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * http://www.apache.org/licenses/LICENSE-2.0                             *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 ******************************************************************************/

package org.haedus.phonetic.model;

import org.haedus.phonetic.features.FeatureArray;
import org.haedus.phonetic.features.SparseFeatureArray;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 3/1/2016
 */
public class Constraint {

	private final String label;

	private final FeatureArray<Double> source;
	private final FeatureArray<Double> target;

	public Constraint(String label,
	                  FeatureArray<Double> source,
	                  FeatureArray<Double> target) {

		this.label  = label.replaceAll("\\s+"," ");
		this.source = source;
		this.target = target;
	}

	public FeatureArray<Double> getTarget() {
		return target;
	}

	public FeatureArray<Double> getSource() {
		return source;
	}

	@Override
	public String toString() {
		return "Constraint: " + label;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!getClass().equals(obj.getClass())) return false;

		Constraint constraint = (Constraint) obj;

		return source.equals(constraint.source) &&
		       target.equals(constraint.source);
	}

	@Override
	public int hashCode() {
		return 31 * source.hashCode() * target.hashCode();
	}
}
