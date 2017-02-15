/******************************************************************************
 * Copyright (c) 2015. Samantha Fiona McCabe                                  *
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

package org.didelphis.soundchange;

import org.didelphis.common.language.enums.FormatterMode;
import org.didelphis.common.language.phonetic.SequenceFactory;
import org.didelphis.common.language.phonetic.model.StandardFeatureModel;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Samantha Fiona Morrigan McCabe
 * Created: 4/19/2015
 */
public class ConditionModelTest {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(ConditionModelTest.class);

	private static final SequenceFactory FACTORY = loadModel();
	
	@Test
	public void testBasicStateMachine01() {
		Condition condition = new Condition("_a[+son, -hgh, +frn, -atr]+", FACTORY);

		fail(condition, "xa");
		test(condition, "xaa");
		test(condition, "xaaa");
		test(condition, "xaa̤");
		test(condition, "xaa̤a");

		test(condition, "xaa̤a");
		test(condition, "xaa̤");

		fail(condition, "xb");
		fail(condition, "xc");
	}

	@Test
	public void testComplex01() {
		Condition condition = new Condition("_[-con, +voice, -creaky][-son, -voice, +vot]us", FACTORY);

		test(condition, "xapʰus");
		test(condition, "xatʰus");
		test(condition, "xakʰus");
		test(condition, "xacʰus");

		test(condition, "xepʰus");
		test(condition, "xetʰus");
		test(condition, "xekʰus");
		test(condition, "xecʰus");

		test(condition, "xopʰus");
		test(condition, "xotʰus");
		test(condition, "xokʰus");
		test(condition, "xocʰus");

		test(condition, "xāpʰus");
		test(condition, "xātʰus");
		test(condition, "xākʰus");
		test(condition, "xācʰus");

		test(condition, "xēpʰus");
		test(condition, "xētʰus");
		test(condition, "xēkʰus");
		test(condition, "xēcʰus");

		test(condition, "xōpʰus");
		test(condition, "xōtʰus");
		test(condition, "xōkʰus");
		test(condition, "xōcʰus");

		test(condition, "xipʰus");
		test(condition, "xitʰus");
		test(condition, "xikʰus");
		test(condition, "xicʰus");

		fail(condition, "xōpus");
		fail(condition, "xōtus");
		fail(condition, "xōkus");
		fail(condition, "xōcus");

		fail(condition, "xa̰pʰus");
		fail(condition, "xa̰tʰus");
		fail(condition, "xa̰kʰus");
		fail(condition, "xa̰cʰus");
	}

	@Test
	public void testComplex02() {
		Condition condition = new Condition("_[-con][-son]us#", FACTORY);

		test(condition, "xapʰus");
		test(condition, "xatʰus");
		test(condition, "xakʰus");
		test(condition, "xacʰus");

		test(condition, "xepʰus");
		test(condition, "xetʰus");
		test(condition, "xekʰus");
		test(condition, "xecʰus");

		test(condition, "xāpʰus");
		test(condition, "xātʰus");
		test(condition, "xākʰus");
		test(condition, "xācʰus");

		test(condition, "xōpʰus");
		test(condition, "xōtʰus");
		test(condition, "xōkʰus");
		test(condition, "xōcʰus");

		test(condition, "xōpus");
		test(condition, "xōtus");
		test(condition, "xōkus");
		test(condition, "xōcus");

		test(condition, "xa̤pʰus");
		test(condition, "xa̤tʰus");
		test(condition, "xa̤kʰus");
		test(condition, "xa̤cʰus");

		fail(condition, "xpʰeus");
		fail(condition, "xtʰeus");
		fail(condition, "xkʰeus");
		fail(condition, "xcʰeus");
	}

	private static void test(Condition condition, String target) {
		assertTrue(condition.isMatch(FACTORY.getSequence(target), 0));
	}

	private static void fail(Condition condition, String target) {
		assertFalse(condition.isMatch(FACTORY.getSequence(target), 0));
	}

	private static SequenceFactory loadModel() {
		InputStream stream = ConditionModelTest.class.getClassLoader().getResourceAsStream("AT_hybrid.model");
		FormatterMode mode = FormatterMode.INTELLIGENT;
		return new SequenceFactory(new StandardFeatureModel(stream, mode),mode);
	}
}
