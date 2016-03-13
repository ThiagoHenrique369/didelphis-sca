/******************************************************************************
 * Copyright (c) 2016. Samantha Fiona McCabe                                  *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *     http://www.apache.org/licenses/LICENSE-2.0                             *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 ******************************************************************************/

package org.haedus.phonetic.model;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.haedus.enums.FormatterMode;
import org.haedus.exceptions.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by samantha on 4/27/15.
 */
public class FeatureModelLoader {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(FeatureModelLoader.class);

	private static final String FEATURES    = "FEATURES";
	private static final String SYMBOLS     = "SYMBOLS";
	private static final String MODIFIERS   = "MODIFIERS";
	private static final String CONSTRAINTS = "CONSTRAINTS";
	private static final String ALIASES     = "ALIASES";
	
	private static final String ZONE_STRING = 
		FEATURES    + '|' +
		SYMBOLS     + '|' +
		MODIFIERS   + '|' +
		CONSTRAINTS + '|' +
		ALIASES;
	
	private static final String FEATURES_STRING = "(\\w+)\\s+(\\w*)\\s+(ternary|binary|numeric(\\(-?\\d,\\d\\))?)";

	private static final Pattern COMMENT_PATTERN  = Pattern.compile("\\s*%.*");
	private static final Pattern ZONE_PATTERN     = Pattern.compile(ZONE_STRING);
	private static final Pattern FEATURES_PATTERN = Pattern.compile(FEATURES_STRING, Pattern.CASE_INSENSITIVE);

	private static final Pattern SYMBOL_PATTERN = Pattern.compile("(\\S+)\\t(.*)", Pattern.UNICODE_CHARACTER_CLASS);
	private static final Pattern TAB_PATTERN    = Pattern.compile("\\t");

	private final List<Type>       featureTypes;
	private final List<Constraint> constraints;
	
	private final Map<String, Integer> featureNames;
	private final Map<String, Integer> featureAliases;

	private final Map<String, List<Double>> featureMap;
	private final Map<String, List<Double>> diacritics;
	
	private final Map<String, Map<String,Integer>> aliases;
	
	private final String sourcePath;

	private final FormatterMode formatterMode;
	
	private int numberOfFeatures;
	
	private FeatureModelLoader(String path, FormatterMode modeParam) {
		sourcePath = path;
		formatterMode = modeParam;

		featureTypes   = new ArrayList<Type>();
		constraints    = new ArrayList<Constraint>();
		featureNames   = new LinkedHashMap<String, Integer>();
		featureAliases = new LinkedHashMap<String, Integer>();
		featureMap     = new LinkedHashMap<String, List<Double>>();
		diacritics     = new LinkedHashMap<String, List<Double>>();
		
		aliases        = new LinkedHashMap<String, Map<String,Integer>>();
	}
	
	public FeatureModelLoader(File file, FormatterMode modeParam) {
		this("file:" + file.getAbsolutePath(), modeParam);
		try {
			readModelFromFileNewFormat(FileUtils.readLines(file, "UTF-8"));
		} catch (IOException e) {
			LOGGER.error("Failed to read from file {}", file, e);
		}
	}

	public FeatureModelLoader(String path, Iterable<String> file, FormatterMode modeParam) {
		this(path, modeParam);
		readModelFromFileNewFormat(file);
	}

	public FeatureModelLoader(InputStream stream, FormatterMode modeParam) {
		this(stream.toString(), modeParam);

		try {
			readModelFromFileNewFormat(IOUtils.readLines(stream, "UTF-8"));
		} catch (IOException e) {
			LOGGER.error("Problem while reading from stream {}", stream, e);
		}
	}

	@Override
	public String toString() {
		return "FeatureModelLoader{" + sourcePath + '}';
	}

	public int getNumberOfFeatures() {
		return numberOfFeatures;
	}
	
	@SuppressWarnings("ReturnOfCollectionOrArrayField")
	public Map<String, Integer> getFeatureNames() {
		return featureNames;
	}

	@SuppressWarnings("ReturnOfCollectionOrArrayField")
	public Map<String, Integer> getFeatureAliases() {
		return featureAliases;
	}

	@SuppressWarnings("ReturnOfCollectionOrArrayField")
	public Map<String, List<Double>> getFeatureMap() {
		return featureMap;
	}

	@SuppressWarnings("ReturnOfCollectionOrArrayField")
	public Map<String, Map<String,Integer>> getAliases() {
		return aliases;
	}

	@SuppressWarnings("ReturnOfCollectionOrArrayField")
	public Map<String, List<Double>> getDiacritics() {
		return diacritics;
	}

	@SuppressWarnings("ReturnOfCollectionOrArrayField")
	public List<Constraint> getConstraints() {
		return constraints;
	}

	private void readModelFromFileNewFormat(Iterable<String> file) {
		String currentZone = "";
		
		Collection<String> featureZone    = new ArrayList<String>();
		Collection<String> constraintZone = new ArrayList<String>();
		Collection<String> aliasZone      = new ArrayList<String>();
		Collection<String> symbolZone     = new ArrayList<String>();
		Collection<String> modifierZone   = new ArrayList<String>();

		/* Probably what we need to do here is use the zones to capture every line up to the next zone
		 * or EOF. Put these in lists, one for each zone. Then parse each zone separately. This will
		 * reduce cyclomatic complexity and should avoid redundant checks.
		 */
		for (String string : file) {
			// Remove comments
			String line = COMMENT_PATTERN.matcher(string).replaceAll("");
			Matcher matcher = ZONE_PATTERN.matcher(line);
			if (matcher.find()) {
				currentZone = matcher.group(0);
			} else if (!line.isEmpty() && !line.trim().isEmpty()) {

				if (currentZone.equals(FEATURES)) {
					featureZone.add(line.toLowerCase());
				} else if (currentZone.equals(CONSTRAINTS)) {
					constraintZone.add(line);
				} else if (currentZone.equals(SYMBOLS)) {
					symbolZone.add(line);
				} else if (currentZone.equals(MODIFIERS)) {
					modifierZone.add(line);
				} else if (currentZone.equals(ALIASES)) {
					aliasZone.add(line);
				}
			}
		}
		// Now parse each of the lists
		populateFeatures(featureZone);
		populateAliases(aliasZone);
		populateSymbols(symbolZone);
		populateModifiers(modifierZone);
	}

	private void populateAliases(Collection<String> strings) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.putAll(featureNames);
		map.putAll(featureAliases);
		
		for (String string : strings) {
			String[] split = string.split("\\s*=\\s*", 2);
			
			String alias = split[0].replaceAll("\\[|\\]", "");
			String value = split[1];

			List<Double> array = getUnderspecifiedArray();
			Map<Integer, Double> values = FeatureModel.getValueMap(value, map, aliases);
			for (Map.Entry<Integer, Double> entry : values.entrySet()) {
				array.set(entry.getKey(), entry.getValue());
			}
			aliases.put(alias, array);
		}
	}

	private void populateConstraints(Iterable<String> constraintZone) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.putAll(featureNames);
		map.putAll(featureAliases);
		
		for (String entry : constraintZone) {
			String[] split = entry.split("\\s*>\\s*", 2);

			String source = split[0];
			String target = split[1];

			Map<Integer, Double> sMap = FeatureModel.getValueMap(source, map, null);
			Map<Integer, Double> tMap = FeatureModel.getValueMap(target, map, null);

			constraints.add(new Constraint(sMap, tMap));
		}
	}

	private void populateModifiers(Iterable<String> modifierZone) {
		for (String entry : modifierZone) {
			Matcher matcher = SYMBOL_PATTERN.matcher(entry);

			if (matcher.matches()) {
				String symbol = matcher.group(1);
				String[] values = TAB_PATTERN.split(matcher.group(2), -1);

				List<Double> features = new ArrayList<Double>();
				for (String value : values) {
					features.add(getDouble(value, FeatureModel.MASKING_VALUE));
				}
				diacritics.put(symbol, features);
			} else {
				LOGGER.error("Unrecognized diacritic definition {}", entry);
			}
		}
	}

	private void populateSymbols(Iterable<String> symbolZone) {
		for (String entry : symbolZone) {
			Matcher matcher = SYMBOL_PATTERN.matcher(entry);

			if (matcher.matches()) {
				String symbol = formatterMode.normalize(matcher.group(1));
				String[] values = TAB_PATTERN.split(matcher.group(2), -1);

				List<Double> features = new ArrayList<Double>();
				for (int i = 0; i < featureTypes.size();i++) {
					Type type = featureTypes.get(i);
					String value = values[i];
					if (!type.matches(value)) {
						LOGGER.warn("Value '{}' at position {} is not valid for {} in array: {}",
							value, i, type, Arrays.toString(values));
					} 
					features.add(getDouble(value, FeatureModel.UNDEFINED_VALUE));
				}
				checkFeatureCollisions(symbol, features);
				featureMap.put(symbol, features);
			} else {
				LOGGER.error("Unrecognized symbol definition {}", entry);
			}
		}
	}

	private void checkFeatureCollisions(String symbol, List<Double> features) {
		if (featureMap.containsValue(features)) {
			for (Map.Entry<String, List<Double>> e : featureMap.entrySet()) {
				if (features.equals(e.getValue())) {
					LOGGER.warn("Collision between features {} and {} --- both have value {}",
							symbol, e.getKey(), features);
				}
			}
		}
	}

	private void populateFeatures(Iterable<String> featureZone) {
		int i = 0;
		for (String entry : featureZone) {
			Matcher matcher = FEATURES_PATTERN.matcher(entry);

			if (matcher.matches()) {

				String name  = matcher.group(1);
				String alias = matcher.group(2);
				String type  = matcher.group(3).replaceAll("\\(.*\\)","");
				
				try { // catch and rethrow if type is invalid\
					featureTypes.add(Type.valueOf(type.toUpperCase()));
				} catch (IllegalArgumentException e) {
					throw new ParseException("Illegal feature type " + type + " in definition: " + entry, e);
				}
				
				featureNames.put(name, i);
				featureAliases.put(alias, i);

			} else {
				LOGGER.error("Unrecognized command in FEATURE block: {}", entry);
				throw new ParseException("Unrecognized command in FEATURE block: " + entry);
			}
			i++;
		}
		numberOfFeatures = i;
	}

	private List<Double> getUnderspecifiedArray() {
		List<Double> list = new ArrayList<Double>();
		int size = featureNames.size();
		for (int i = 0; i < size; i++) {
			list.add(FeatureModel.MASKING_VALUE);
		}
		return list;
	}

	private static double getDouble(String cell, double defaultValue) {
		double featureValue;
		if (cell.isEmpty()) {
			featureValue = defaultValue;
		} else if (cell.equals("+")) {
			featureValue = 1.0;
		} else if (cell.equals("-") || cell.equals("−")) {
			featureValue = -1.0;
		} else {
			featureValue = Double.valueOf(cell);
		}
		return featureValue;
	}
	
	private enum Type {
		BINARY("(\\+|-|−)?"),
		TERNARY("(\\+|-|−|0)?"),
		NUMERIC("(-?\\d+)?");

		private Pattern pattern;

		Type(String value) {
			pattern = Pattern.compile(value);
		}
		
		boolean matches(CharSequence value) {
			return pattern.matcher(value).matches();
		}
	}
}
