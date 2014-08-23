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

package org.haedus.datatypes;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SegmenterTest
{
	@Test
	public void testEmpty() {
		List<String> expected = new ArrayList<String>();
		List<String> received = Segmenter.segment("");

		Collections.addAll(expected, "");

		assertEquals(expected, received);
	}

	@Test
	public void testBasic() {
		List<String> expected = new ArrayList<String>();
		List<String> received = Segmenter.segment("abcdef");

		Collections.addAll(expected, "a", "b", "c", "d", "e", "f");

		assertEquals(expected, received);
	}

	@Test
	public void testModifierLetters() {
		List<String> expected = new ArrayList<String>();
		List<String> received = Segmenter.segment("pˠtˡtˢtˣpˤtˀpˁkʰgʱdʲtʳtʴtʵgʶkʷnʸ");

		Collections.addAll(expected, "pˠ","tˡ","tˢ","tˣ","pˤ","tˀ","pˁ","kʰ","gʱ","dʲ","tʳ","tʴ","tʵ","gʶ","kʷ","nʸ");

		assertEquals(expected, received);
	}

	@Test
	public void testSubscripts() {
		List<String> expected = new ArrayList<String>();
		List<String> received = Segmenter.segment("h₁óh₁es-");

		Collections.addAll(expected, "h₁", "ó", "h₁", "e", "s", "-");
		assertEquals(expected, received);
	}

	@Test
	public void testDoubleWidth() {
		List<String> expected = new ArrayList<String>();
		List<String> received = Segmenter.segment("k͡pg͡bt͜sd͜zp͡fɔ͝aa͟ʕɛ͠ʌ");
		Collections.addAll(expected,"k͡p", "g͡b", "t͜s", "d͜z", "p͡f", "ɔ͝a", "a͟ʕ", "ɛ͠ʌ");

		assertEquals(expected, received);
	}

	@Test
	public void testDoubleWidthPlusModifiers() {
		List<String> expected = new ArrayList<String>();
		List<String> received = Segmenter.segment("k͡pʰg͡b̤ˁt͜sˠd͜zʷ");

		Collections.addAll(expected,"k͡pʰ","g͡b̤ˁ","t͜sˠ","d͜zʷ");

		assertEquals(expected, received);
	}

	@Test
	public void testBasicVariables01() {
		// TODO: refactor to use correct segmentation call
		List<String> expected  = new ArrayList<String>();
		List<String> variables = new ArrayList<String>();
		Collections.addAll(variables, "CH", "C", "V" );
		List<String> received = Segmenter.segment("aCatVaCHo", variables);

		Collections.addAll(expected, "a", "C", "a", "t", "V", "a", "CH", "o");

		assertEquals(expected, received);
	}

	@Test
	public void testBasicVariables02() {
		// TODO: refactor to use correct segmentation call
		List<String> expected  = new ArrayList<String>();
		List<String> variables = new ArrayList<String>();
		Collections.addAll(variables, "CH", "C", "V", "H" );

		List<String> received = Segmenter.segment("CHatVaCHoC", variables);

		Collections.addAll(expected, "CH", "a", "t", "V", "a", "CH", "o", "C");

		assertEquals(expected, received);
	}

	@Test
	public void testBasicVariables03() {
		// TODO: refactor to use correct segmentation call
		List<String> expected  = new ArrayList<String>();
		List<String> variables = new ArrayList<String>();
		Collections.addAll(variables, "CH", "C", "[+Approximant]", "H" );

		List<String> received = Segmenter.segment("CHatVa[+Approximant]oC", variables);

		Collections.addAll(expected, "CH", "a", "t", "V", "a", "[+Approximant]", "o", "C");

		assertEquals(expected, received);
	}

	@Test
	public void testNaive01() {
		List<String> expected = new ArrayList<String>();
		List<String> reserved = new ArrayList<String>();

		Collections.addAll(reserved, "CH", "C", "H", "ts", "th");

		String word = "arstCHoCotssptsuthetHrCCHHstrest";

		List<String> received = Segmenter.segmentNaively(word, reserved);

		Collections.addAll(expected, "a", "r", "s", "t", "CH", "o", "C", "o", "ts", "s", "p", "ts", "u", "th",
			"e", "t", "H", "r", "C", "CH", "H", "s", "t", "r", "e", "s", "t");

		assertEquals(expected, received);
	}
}
