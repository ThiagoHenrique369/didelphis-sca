/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 * (at your option) any later version.                                        *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 ******************************************************************************/

package org.haedus.phonetic.model;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Samantha Fiona Morrigan McCabe Created: 7/4/2016
 */
public class FeatureSpecificationTest {
	private static final transient Logger LOGGER = LoggerFactory.getLogger(
			FeatureSpecificationTest.class);
	
	private static final FeatureSpecification MODEL = load();
	
	private static FeatureSpecification load() {
		String path = "AT_hybrid.spec";
		try {
			return FeatureSpecification.loadFromClassPath(path);
		} catch (IOException e) {
			LOGGER.error("Failed to load {}", path);
		}
		return null;
	}
	
	@Test
	public void test() {
		List<Constraint> constraints = MODEL.getConstraints();
		
		assertFalse("Constraints should not empty", constraints.isEmpty());
	}
}
