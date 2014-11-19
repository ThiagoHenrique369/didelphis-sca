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

package org.haedus.datatypes.phonetic;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Samantha Fiona Morrigan McCabe
 */
public class FeatureModelTest {
	private static final transient Logger LOGGER = LoggerFactory.getLogger(FeatureModelTest.class);

	private static final List<Double> G_FEATURES  = new ArrayList<Double>();
	private static final List<Double> GH_FEATURES = new ArrayList<Double>();
	private static final List<Double> GJ_FEATURES = new ArrayList<Double>();
	private static final double       NAN         = Double.NaN;

	@BeforeClass
	public static void init() {
		//                                0     1    2    3    4    5    6    7    8    9   10    11   12   13   14   15   16   17
		//                              son   con  vot  rel  nas  lat  lab  rnd  lin  lam  hgt   frn  bck  atr  rad  air  glt  len
		Collections.addAll(G_FEATURES,  0.0, -1.0, NAN, 1.0, NAN, NAN, NAN, NAN, NAN, NAN, 1.0, -1.0, 1.0, NAN, NAN, NAN, 0.0, 0.0);
		Collections.addAll(GH_FEATURES, 0.0, -1.0, 1.0, 1.0, NAN, NAN, NAN, NAN, NAN, NAN, 1.0, -1.0, 1.0, NAN, NAN, NAN, 0.0, 0.0);
		Collections.addAll(GJ_FEATURES, 0.0, -1.0, NAN, 1.0, NAN, NAN, NAN, NAN, NAN, NAN, 1.0,  1.0, 1.0, NAN, NAN, NAN, 0.0, 0.0);
	}

	@Test
	public void testConstructor01() throws Exception {
		Resource resource = new ClassPathResource("featuremodel");
		FeatureModel model = new FeatureModel(resource.getFile());

		Segment received = model.get("g");
		Segment expected = new Segment("g", G_FEATURES);

		assertEquals(expected, received);
	}

	@Test
	public void testGetStringFromFeatures01() throws Exception {
		Resource resource = new ClassPathResource("featuremodel");
		FeatureModel model = new FeatureModel(resource.getFile());

		String bestSymbol = model.getBestSymbol(G_FEATURES);
		LOGGER.info(bestSymbol);
		assertEquals("g", bestSymbol);
	}

	@Test
	public void testGetStringFromFeatures02() throws Exception {
		Resource resource = new ClassPathResource("featuremodel");
		FeatureModel model = new FeatureModel(resource.getFile());

		String bestSymbol = model.getBestSymbol(GH_FEATURES);
		assertEquals("gʰ", bestSymbol);
	}

	@Test
	public void testGetStringFromFeatures03() throws Exception {
		Resource resource = new ClassPathResource("featuremodel");
		FeatureModel model = new FeatureModel(resource.getFile());

		String bestSymbol = model.getBestSymbol(GJ_FEATURES);
		assertEquals("gʲ", bestSymbol);
	}
}
