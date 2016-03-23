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

package org.haedus.phonetic.model;

import org.haedus.enums.FormatterMode;
import org.haedus.exceptions.ParseException;
import org.haedus.phonetic.Segment;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Samantha Fiona Morrigan McCabe
 */
public class FeatureModel {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(FeatureModel.class);

	public static final FeatureModel EMPTY_MODEL     = new FeatureModel();
	public static final Double       UNDEFINED_VALUE = Double.NaN;
	public static final Double       MASKING_VALUE   = Double.NEGATIVE_INFINITY;
	
	private static final String VALUE  = "(-?\\d|[A-Zα-ω]+)";
	private static final String NAME   = "(\\S+)";
	private static final String ASSIGN = "([=:><])";

	private static final int UNICODE = Pattern.UNICODE_CHARACTER_CLASS;
	
	private static final Pattern VALUE_PATTERN = Pattern.compile(NAME + ASSIGN + VALUE, UNICODE);
	private static final Pattern OTHER_PATTERN = Pattern.compile(VALUE + ASSIGN + NAME, UNICODE);
	
	private static final Pattern BINARY_PATTERN  = Pattern.compile("(\\+|\\-)" + NAME, UNICODE);
	private static final Pattern FEATURE_PATTERN = Pattern.compile("[,;]\\s*|\\s+");
	private static final Pattern FANCY_PATTERN   = Pattern.compile("−");

	private final int numberOfFeatures;

	private final Map<String, Map<Integer, Double>> aliases;

	private final Map<String, Integer>      featureIndices;
	private final Map<String, List<Double>> featureMap;
	private final Map<String, List<Double>> diacritics;

	private final List<String>              featureNames;
	private final List<Constraint>          constraints;

	private final List<Double>  blankArray;
	private final FormatterMode formatterMode;

	// Initializes an empty model; access to this should only be through the EMPTY_MODEL field
	private FeatureModel() {
		numberOfFeatures = 0;
		
		featureIndices = new LinkedHashMap<String, Integer>();
		featureMap     = new LinkedHashMap<String, List<Double>>();
		diacritics     = new LinkedHashMap<String, List<Double>>();
		aliases        = new LinkedHashMap<String, Map<Integer, Double>>();
		constraints    = new ArrayList<Constraint>();
		blankArray     = new ArrayList<Double>();
		featureNames   = new ArrayList<String>();
		formatterMode  = FormatterMode.NONE;
	}

	public FeatureModel(InputStream stream, FormatterMode modeParam) {
		this(new FeatureModelLoader(stream, modeParam), modeParam);
	}

	public FeatureModel(File file, FormatterMode modeParam) {
		this(new FeatureModelLoader(file, modeParam), modeParam);
	}

	public FeatureModel(FeatureModelLoader loader, FormatterMode modeParam) {
		numberOfFeatures = loader.getNumberOfFeatures();

		featureIndices = new LinkedHashMap<String, Integer>();
		featureIndices.putAll(loader.getFeatureNames());
		featureIndices.putAll(loader.getFeatureAliases());

		featureMap     = loader.getFeatureMap();
		diacritics     = loader.getDiacritics();
		aliases        = loader.getAliases();
		constraints    = loader.getConstraints();
		featureNames   = new ArrayList<String>(loader.getFeatureNames().keySet());

		formatterMode = modeParam;

		blankArray = new ArrayList<Double>();
		for (int i = 0; i < numberOfFeatures; i++) {
			blankArray.add(UNDEFINED_VALUE);
		}
	}

	@NotNull
	public static Map<Integer, Double> getValueMap(String features,
												   Map<String, Integer> names,
												   Map<String, Map<Integer, Double>> aliases) {
		int size = features.length();
		String substring = FANCY_PATTERN.matcher(features.substring(1, size - 1)).replaceAll(Matcher.quoteReplacement("-"));
		String[] array = FEATURE_PATTERN.split(substring);

		Map<Integer, Double> map = new HashMap<Integer, Double>();
		for (String element : array) {
			Matcher valueMatcher  = VALUE_PATTERN.matcher(element);
			Matcher otherMatcher  = OTHER_PATTERN.matcher(element);
			Matcher binaryMatcher = BINARY_PATTERN.matcher(element);
			
			if (valueMatcher.matches()) {
				String featureName  = valueMatcher.group(1);
				String assignment   = valueMatcher.group(2); 
				String featureValue = valueMatcher.group(3);
				Integer integer = retrieveIndex(featureName, features, names);
				map.put(integer, Double.valueOf(featureValue));
			} else if (otherMatcher.matches()) {
				String featureName  = otherMatcher.group(3);
				String assignment   = otherMatcher.group(2);
				String featureValue = otherMatcher.group(1);
				Integer integer = retrieveIndex(featureName, features, names);
				map.put(integer, Double.valueOf(featureValue));
			} else if (binaryMatcher.matches()) {
				String featureName = binaryMatcher.group(2);
				String featureValue = binaryMatcher.group(1);
				Integer integer = retrieveIndex(featureName, features, names);
				map.put(integer, featureValue.equals("+") ? 1.0 : -1.0);
			} else if (aliases.containsKey(element)) {
				map.putAll(aliases.get(element));
			} else {
				// invalid format?
				throw new ParseException("Unrecognized feature \"" + element + "\" in definition " + features);
			}
		}
		return map;
	}

	public static String formatFeatures(List<Double> features) {
		StringBuilder sb = new StringBuilder(5 * features.size());
		for (double feature : features) {
			sb.append((int) feature);
			sb.append('\t');
		}

		return sb.toString();
	}

	public FormatterMode getFormatterMode() {
		return formatterMode;
	}

	public Segment getSegmentFromFeatures(String features) {
		List<Double> featureArray = new ArrayList<Double>();

		for (int i = 0; i < numberOfFeatures; i++) {
			featureArray.add(MASKING_VALUE);
		}

		Map<Integer, Double> map = getValueMap(features, featureIndices, aliases);
		for (Map.Entry<Integer, Double> entry : map.entrySet()) {
			featureArray.set(entry.getKey(), entry.getValue());
		}
		return new Segment(features, featureArray, this);
	}

	public List<Constraint> getConstraints() {
		return Collections.unmodifiableList(constraints);
	}

	public String getBestSymbol(List<Double> featureArray) {

		List<Double> bestFeatures = new ArrayList<Double>();
		String bestSymbol = "";
		double minimum = Double.MAX_VALUE;

		for (Map.Entry<String, List<Double>> entry : featureMap.entrySet()) {
			List<Double> features = entry.getValue();

			double difference = getDifferenceValue(featureArray, features);
			if (difference < minimum) {
				bestSymbol = entry.getKey();
				minimum = difference;
				bestFeatures = features;
			}
		}

		StringBuilder sb = new StringBuilder();
		if (minimum > 0.0) {
			Collection collection = getBestDiacritic(featureArray, bestFeatures);
			for (String diacritic : diacritics.keySet()) {
				if (collection.contains(diacritic)) {
					sb.append(diacritic);
				}
			}

		}
		return formatterMode.normalize(bestSymbol + sb);
	}

	// Return a list of all segments g such that matches.matches(input) is true
	public Collection<Segment> getMatchingSegments(Segment input) {
		Collection<Segment> collection = new ArrayList<Segment>();

		List<Double> features = input.getFeatures();

		for (Map.Entry<String, List<Double>> entry : featureMap.entrySet()) {
			// This implementation will work but wastes a lot of time on object allocation
			if (Segment.matchesFeatures(features, entry.getValue())) {
				collection.add(new Segment(entry.getKey(), entry.getValue(), this));
			}
		}

		return collection;
	}

	public Set<String> getSymbols() {
		return Collections.unmodifiableSet(featureMap.keySet());
	}

	@Override
	public String toString() {
		String string;
		if (this == EMPTY_MODEL) {
			string = "EMPTY MODEL";
		} else {
			string = "FeatureModel(number.features=" + getNumberOfFeatures() + ", number.symbols=" + featureMap.size() + ')';
		}
		return string;
	}

	@Override
	public int hashCode() {
		int code = 91;
		code *= featureMap != null ? featureMap.hashCode() : 1;
		return code;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (getClass() != obj.getClass()) { return false; }

		FeatureModel other = (FeatureModel) obj;

		boolean diacriticsEquals = diacritics.equals(other.diacritics);
		boolean featureEquals    = featureMap.equals(other.getFeatureMap());
		boolean namesEquals      = featureIndices.equals(other.featureIndices);
		boolean aliasesEquals    = aliases.equals(other.aliases);
		return namesEquals && aliasesEquals && featureEquals && diacriticsEquals;
	}

	public boolean containsKey(String key) {
		return featureMap.containsKey(key);
	}

	public int getNumberOfFeatures() {
		return numberOfFeatures;
	}

	public Map<String, List<Double>> getFeatureMap() {
		return Collections.unmodifiableMap(featureMap);
	}

	public List<Double> getValue(String key) {
		if (featureMap.containsKey(key)) {
			return new ArrayList<Double>(featureMap.get(key));
		} else {
			return new ArrayList<Double>(blankArray);
		}
	}

	public List<String> getFeatureNames() {
		return Collections.unmodifiableList(featureNames);
	}

	public List<Double> getBlankArray() {
		return blankArray;
	}

	// This should be here because how the segment is constructed is a function of what kind of model this is
	public Segment getSegment(String head, Iterable<String> modifiers) {
		List<Double> featureArray = getValue(head); // May produce a null value if the head is not found for some reason
		StringBuilder sb = new StringBuilder(head);
		for (String modifier : modifiers) {
			sb.append(modifier);
			if (diacritics.containsKey(modifier)) {
				List<Double> doubles = diacritics.get(modifier);
				for (int i = 0; i < doubles.size(); i++) {
					Double d = doubles.get(i);
					// this will need to change if we support value modification (up or down)
					if (!d.equals(MASKING_VALUE)) {
						featureArray.set(i, d);
					}
				}
			}
		}
		return new Segment(sb.toString(), featureArray, this);
	}

	private static Integer retrieveIndex(String label, String features, Map<String, Integer> names) {
		if (names.containsKey(label)) {
			return names.get(label);
		}
		throw new ParseException("Invalid feature label \"" + label + "\" provided in \"" + features + '"');
	}

	private static List<Double> getDifferenceArray(List<Double> left, List<Double> right) {
		List<Double> list = new ArrayList<Double>();
		if (left.size() == right.size()) {
			for (int i = 0; i < left.size(); i++) {
				Double l = left.get(i);
				Double r = right.get(i);
				list.add(Math.abs(getDifference(l, r)));
			}
		} else {
			LOGGER.warn("Attempt to compare arrays of differing length! {} vs {}", left, right);
		}
		return list;
	}

	private static double getDifferenceValue(List<Double> left, List<Double> right) {
		double sum = 0.0;
		List<Double> differenceArray = getDifferenceArray(left, right);
		if (differenceArray.isEmpty()) {
			sum = Double.NaN;
		} else {
			for (Double value : differenceArray) {
				sum += value;
			}
		}
		return sum;
	}

	private static Double getDifference(Double a, Double b) {
		if (a.equals(b)) {
			return 0.0;
		} else if (a.isNaN()) {
			return b;
		} else if (b.isNaN()) {
			return a;
		} else {
			return Math.abs(a - b);
		}
	}

	private Collection<String> getBestDiacritic(List<Double> featureArray, List<Double> bestFeatures, double lastMinimum) {
		String bestDiacritic = "";
		double minimumDifference = lastMinimum;
		List<Double> bestCompiled = new ArrayList<Double>();

		Collection<String> diacriticList = new ArrayList<String>();

		for (Map.Entry<String, List<Double>> entry : diacritics.entrySet()) {
			List<Double> diacriticFeatures = entry.getValue();
			List<Double> compiledFeatures = new ArrayList<Double>();
			if (diacriticFeatures.size() == bestFeatures.size()) {
				for (int i = 0; i < diacriticFeatures.size(); i++) {
					Double left = diacriticFeatures.get(i);
					Double right = bestFeatures.get(i);

					if (left.equals(MASKING_VALUE)) {
						compiledFeatures.add(right);
					} else {
						compiledFeatures.add(left);
					}
				}
			} else {
				LOGGER.error("Difference in array sizes: {} vs  {}", diacriticFeatures, bestFeatures);
			}

			if (!compiledFeatures.equals(bestFeatures)) {
				double difference = getDifferenceValue(compiledFeatures, featureArray);
				if (difference < minimumDifference) {
					minimumDifference = difference;
					bestDiacritic = entry.getKey();
					bestCompiled = compiledFeatures;
				}
			}
		}

		if (minimumDifference > 0.0 && minimumDifference < lastMinimum) {
			diacriticList.add(bestDiacritic);
			diacriticList.addAll(getBestDiacritic(featureArray, bestCompiled, minimumDifference));
		} else {
			diacriticList.add(bestDiacritic);
		}
		return diacriticList;
	}

	private Collection getBestDiacritic(List<Double> featureArray, List<Double> bestFeatures) {
		return getBestDiacritic(featureArray, bestFeatures, Double.MAX_VALUE);
	}
}
