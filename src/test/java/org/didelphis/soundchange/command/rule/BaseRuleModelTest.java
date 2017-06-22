/******************************************************************************
 * Copyright (c) 2016 Samantha Fiona McCabe                                   *
 *                                                                            *
 * This software is not licensed for any purpose                              *
 ******************************************************************************/

package org.didelphis.soundchange.command.rule;

import org.didelphis.io.ClassPathFileHandler;
import org.didelphis.language.enums.FormatterMode;
import org.didelphis.language.exceptions.ParseException;
import org.didelphis.language.phonetic.features.IntegerFeature;
import org.didelphis.language.phonetic.model.FeatureMapping;
import org.didelphis.language.phonetic.SequenceFactory;
import org.didelphis.language.phonetic.VariableStore;
import org.didelphis.language.phonetic.model.FeatureModelLoader;
import org.didelphis.language.phonetic.sequences.Sequence;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created with IntelliJ IDEA. User: Samantha Fiona Morrigan McCabe Date:
 * 6/22/13 Time: 3:37 PM To change this template use File | Settings | File
 * Templates.
 */
public class BaseRuleModelTest {

	private static final transient Logger LOGGER = LoggerFactory
			.getLogger(BaseRuleModelTest.class);

	private static final Set<String> EMPTY_SET = new HashSet<>();
	private static final FeatureMapping<Integer> MODEL = loadModel();
	private static final SequenceFactory<Integer> FACTORY = new SequenceFactory<>(
			MODEL, FormatterMode.INTELLIGENT);

	@Test
	void testFeatureTransformOutOfRange() {
		assertThrows(ParseException.class,
				() -> new BaseRule<>("a > g[+hgh]", FACTORY));
	}

	@Test
	void testFeatureTransform01() {
		BaseRule<Integer> rule = new BaseRule<>(
				"[-con, +son, -hgh, +frn, -atr] > [+hgh, +atr]", FACTORY);
		testRule(rule, "a", "i");

		testRule(rule, "ɐ", "ɐ");
		testRule(rule, "æ", "æ");
		testRule(rule, "e", "e");
	}

	@Test
	void testFeatureTransform02() {
		BaseRule<Integer> rule = new BaseRule<>(
				"[+con, -son, -cnt, -rel, -voice] > [+rel]", FACTORY);
		testRule(rule, "t", "ts");
		testRule(rule, "p", "pɸ");
		testRule(rule, "tʰ", "tsʰ");

		testRule(rule, "s", "s");
		testRule(rule, "d", "d");
	}

	@Test
	void testFeatureTransform03() {
		BaseRule<Integer> rule = new BaseRule<>(
				"[+con, -son, +voice] > [-voice] / _[+con, -son, -voice]",
				FACTORY);
		testRule(rule, "dt", "tt");
		testRule(rule, "bt", "pt");

		testRule(rule, "dd", "dd");
		testRule(rule, "ad", "ad");
		testRule(rule, "at", "at");
	}

	@Test
	void testFeaturesIndexing01() {
		BaseRule<Integer> rule = new BaseRule<>("c[-con, +son, +voice] > $1k",
				FACTORY);
		testRule(rule, "ca", "ak");
	}

	@Test
	void testFeaturesIndexing02() {
		assertThrows(ParseException.class,
				() -> new BaseRule<>("c[-con, +son, +voice] > $[+hgh]1k",
						FACTORY));
	}

	@Test
	void testMetathesis01() {
		VariableStore store = new VariableStore(FormatterMode.INTELLIGENT);
		store.add("C = p t k");
		store.add("N = m n");

		SequenceFactory<Integer> factory = new SequenceFactory<>(MODEL, store, EMPTY_SET,
				FormatterMode.INTELLIGENT);

		BaseRule<Integer> rule = new BaseRule<Integer>("CN > $2$1", factory);

		testRule(rule, "pn", "np");
		testRule(rule, "tn", "nt");
		testRule(rule, "kn", "nk");

		testRule(rule, "pm", "mp");
		testRule(rule, "tm", "mt");
		testRule(rule, "km", "mk");

		testRule(rule, "pt", "pt");
		testRule(rule, "tp", "tp");
		testRule(rule, "kp", "kp");
	}

	@Test
	void testMetathesis02() {
		VariableStore store = new VariableStore(FormatterMode.INTELLIGENT);
		store.add("C = p t k");
		store.add("N = m n");
		store.add("V = a i u");

		SequenceFactory<Integer> factory = new SequenceFactory<>(MODEL, store, EMPTY_SET,
				FormatterMode.INTELLIGENT);


		BaseRule<Integer> rule = new BaseRule<>("CVN > $3V$1", factory);

		testRule(rule, "pan", "nap");
		testRule(rule, "tin", "nit");
		testRule(rule, "kun", "nuk");

		testRule(rule, "pam", "map");
		testRule(rule, "tim", "mit");
		testRule(rule, "kum", "muk");

		testRule(rule, "pat", "pat");
		testRule(rule, "tip", "tip");
		testRule(rule, "kup", "kup");
	}

	@Test
	void testMetathesis03() {
		VariableStore store = new VariableStore(FormatterMode.INTELLIGENT);
		store.add("C = p t k");
		store.add("G = b d g");
		store.add("N = m n");
		SequenceFactory<Integer> factory = new SequenceFactory<>(MODEL, store, EMPTY_SET,
				FormatterMode.INTELLIGENT);

		BaseRule<Integer> rule = new BaseRule<>("CN > $2$G1", factory);

		testRule(rule, "pn", "nb");
		testRule(rule, "tn", "nd");
		testRule(rule, "kn", "ng");

		testRule(rule, "pm", "mb");
		testRule(rule, "tm", "md");
		testRule(rule, "km", "mg");

		testRule(rule, "pt", "pt");
		testRule(rule, "tp", "tp");
		testRule(rule, "kp", "kp");
	}

	@Test
	void testDeletion01() {
		BaseRule<Integer> rule = new BaseRule<>("∅ - > 0", FACTORY);
		testRule(rule, FACTORY, "∅-s-irentu-pʰen", "sirentupʰen");
	}

	@Test
	void testDeletion02() {
		BaseRule<Integer> rule = new BaseRule<>("a > 0", FACTORY);
		testRule(rule, FACTORY, "aaaabbba", "bbb");
	}

	@Test
	void testDeletion03() {
		BaseRule<Integer> rule = new BaseRule<>("a b > 0", FACTORY);
		testRule(rule, FACTORY, "aaaaccbbccbba", "cccc");
	}

	@Test
	void testRule01() {
		BaseRule<Integer> rule = new BaseRule<>("a > b", FACTORY);

		testRule(rule, FACTORY, "aaaaaaccca", "bbbbbbcccb");
	}

	@Test
	void testRule02() {
		BaseRule<Integer> rule = new BaseRule<>("a e > æ ɛ", FACTORY);

		testRule(rule, FACTORY, "ate", "ætɛ");
		testRule(rule, FACTORY, "atereyamane", "ætɛrɛyæmænɛ");
	}

	@Test
	void testRule03() {
		BaseRule<Integer> rule = new BaseRule<>("a b c d e f g > A B C D E F G",
				FACTORY);

		testRule(rule, FACTORY, "abcdefghijk", "ABCDEFGhijk");
	}

	@Test
	void testConditional01() {
		BaseRule<Integer> rule = new BaseRule<>("a > o / g_", FACTORY);

		testRule(rule, FACTORY, "ga", "go");
		testRule(rule, FACTORY, "adamagara", "adamagora");
	}

	@Test
	void testConditional02() {
		BaseRule<Integer> rule = new BaseRule<>("a > e / _c", FACTORY);
		testRule(rule, FACTORY, "abacaba", "abecaba");
		testRule(rule, FACTORY, "ababaca", "ababeca");
		testRule(rule, FACTORY, "acababa", "ecababa");
		testRule(rule, FACTORY, "acabaca", "ecabeca");
	}

	@Test
	void testConditional03() {
		BaseRule<Integer> rule = new BaseRule<>("a > e / _c+#", FACTORY);
		testRule(rule, FACTORY, "abac", "abec");
		testRule(rule, FACTORY, "abacc", "abecc");
		testRule(rule, FACTORY, "abaccc", "abeccc");
		testRule(rule, FACTORY, "abacccc", "abecccc");
		testRule(rule, FACTORY, "abaccccc", "abeccccc");
	}

	@Test
	void testUnconditional04() {
		BaseRule<Integer> rule = new BaseRule<>("eʔe aʔa eʔa aʔe > ē ā ā ē",
				FACTORY);
		testRule(rule, FACTORY, "keʔe", "kē");
		testRule(rule, FACTORY, "kaʔa", "kā");
		testRule(rule, FACTORY, "keʔa", "kā");
		testRule(rule, FACTORY, "kaʔe", "kē");
	}

	@Test
	void testConditional05() {
		BaseRule<Integer> rule = new BaseRule<>("r̄h l̄h > ər əl / _a",
				FACTORY);
		testRule(rule, FACTORY, "kr̄ha", "kəra");
		testRule(rule, FACTORY, "kl̄ha", "kəla");
		testRule(rule, FACTORY, "kl̄he", "kl̄he");
	}

	@Test
	void testConditional06() {
		BaseRule<Integer> rule = new BaseRule<>(
				"pʰ tʰ kʰ cʰ > b d g ɟ / _{r l}?{a e o ā ē ō}{i u}?{n m l r}?{pʰ tʰ kʰ cʰ}",
				FACTORY);

		testRule(rule, FACTORY, "pʰācʰus", "bācʰus");
		testRule(rule, FACTORY, "pʰentʰros", "bentʰros");
		testRule(rule, FACTORY, "pʰlacʰmēn", "blacʰmēn");
		testRule(rule, FACTORY, "pʰoutʰéyet", "boutʰéyet");
		testRule(rule, FACTORY, "pʰɛcʰus", "pʰɛcʰus");
	}

	@Test
	void testConditional07() {
		BaseRule<Integer> rule = new BaseRule<>(
				"pʰ tʰ kʰ kʲʰ > b d g ɟ / _{a e o}{pʰ tʰ kʰ kʲʰ}", FACTORY);

		testRule(rule, FACTORY, "pʰakʲʰus", "bakʲʰus");
		testRule(rule, FACTORY, "pʰaːkʲʰus", "pʰaːkʲʰus");
	}

	@Test
	void testConditional08() {
		BaseRule<Integer> rule = new BaseRule<>("d > t / _#", FACTORY);

		testRule(rule, FACTORY, "abad", "abat");
		testRule(rule, FACTORY, "abada", "abada");
	}

	@Test
	void testInsertion01() {
		BaseRule<Integer> rule = new BaseRule<>("q > qn", FACTORY);

		testRule(rule, FACTORY, "aqa", "aqna");
	}

	@Test
	void testUnconditional() {
		SequenceFactory<Integer> factory = new SequenceFactory<>(MODEL,
				new VariableStore(FormatterMode.INTELLIGENT), EMPTY_SET,
				FormatterMode.INTELLIGENT);
		Sequence<Integer> word = factory.getSequence("h₁óh₁es-");
		Sequence<Integer> expected = factory.getSequence("ʔóʔes-");

		Rule<Integer> rule = new BaseRule<Integer>("h₁ h₂ h₃ h₄ > ʔ x ɣ ʕ", factory);

		assertEquals(expected, rule.apply(word));
	}

	@Test
	void testUnconditional02() {
		Sequence<Integer> expected = FACTORY.getSequence("telə");
		Rule<Integer> rule = new BaseRule<>("eʔé > ê", FACTORY);

		assertEquals(expected, rule.apply(expected));
	}

	@Test
	void testDebug01() {

		VariableStore store = new VariableStore(FormatterMode.INTELLIGENT);
		store.add("V = a e i o u");

		SequenceFactory<Integer> factory = new SequenceFactory<>(MODEL, store, EMPTY_SET,
				FormatterMode.INTELLIGENT);

		Sequence<Integer> original = factory.getSequence("mlan");
		Sequence<Integer> expected = factory.getSequence("blan");

		Rule<Integer> rule = new BaseRule<Integer>("ml > bl / #_V", factory);

		assertEquals(expected, rule.apply(original));
	}

	@Test
	void testUnconditional03() {
		BaseRule<Integer> rule = new BaseRule<>("ox > l", FACTORY);

		testRule(rule, FACTORY, "oxoxoxox", "llll");
		testRule(rule, FACTORY, "moxmoxmoxmoxmox", "mlmlmlmlml");
		testRule(rule, FACTORY, "mmoxmmoxmmoxmmoxmmox", "mmlmmlmmlmmlmml");
	}

	@Test
	void testDebug02() {
		VariableStore store = new VariableStore(FormatterMode.INTELLIGENT);
		store.add("X  = h₁  h₂ h₃ h₄");
		store.add("A  = r   l  m  n");
		store.add("W  = y   w");
		store.add("Q  = kʷʰ kʷ gʷ");
		store.add("K  = kʰ  k  g");
		store.add("KY = cʰ  c  ɟ");
		store.add("T  = pʰ  p  b");
		store.add("P  = tʰ  t  d");

		store.add("[PLOSIVE] = P T K KY Q");
		store.add("[OBSTRUENT] = [PLOSIVE] s");
		store.add("C = [OBSTRUENT] A W");

		SequenceFactory<Integer> factory = new SequenceFactory<>(MODEL, store,
				new HashSet<>(), FormatterMode.INTELLIGENT);

		Sequence<Integer> original = factory.getSequence("trh₂we");
		Sequence<Integer> expected = factory.getSequence("tə̄rwe");

		Rule<Integer> rule1 = new BaseRule<>(
				"rX lX nX mX > r̩X l̩X n̩X m̩X / [OBSTRUENT]_", factory);
		Rule<Integer> rule2 = new BaseRule<>("r l > r̩ l̩ / [OBSTRUENT]_{C #}", factory);
		Rule<Integer> rule3 = new BaseRule<>("r̩ l̩ > r l / C_N{C #}", factory);
		Rule<Integer> rule4 = new BaseRule<>("r̩X l̩X > ə̄r ə̄l   / _{C #}", factory);

		Sequence<Integer> sequence = rule1.apply(original);

		sequence = rule2.apply(sequence);
		sequence = rule3.apply(sequence);
		sequence = rule4.apply(sequence);

		assertEquals(expected, sequence);
	}

	@Test
	void testDebug03() {
		Sequence<Integer> original = FACTORY.getSequence("pʰabopa");
		Sequence<Integer> expected = FACTORY.getSequence("papoba");

		Rule<Integer> rule = new BaseRule<>("pʰ p b > p b p", FACTORY);

		Sequence<Integer> received = rule.apply(original);
		assertEquals(expected, received);
	}

	@Test
	void testCompound01() {
		BaseRule<Integer> rule = new BaseRule<>("a > b / x_ OR _y", FACTORY);

		testRule(rule, FACTORY, "axa", "axb");
		testRule(rule, FACTORY, "aya", "bya");
		testRule(rule, FACTORY, "ayxa", "byxb");
		testRule(rule, FACTORY, "axya", "axya");
	}

	@Test
	void testCompound02() {
		BaseRule<Integer> rule = new BaseRule<>("a > b / x_ NOT _y", FACTORY);

		testRule(rule, FACTORY, "axa", "axb");
		testRule(rule, FACTORY, "axay", "axay");
		testRule(rule, FACTORY, "xayxa", "xayxb");
	}

	@Test
	void testCompound03() {
		VariableStore store = new VariableStore(FormatterMode.INTELLIGENT);
		store.add("C = x y z");

		SequenceFactory<Integer> factory = new SequenceFactory<>(MODEL, store, EMPTY_SET,
				FormatterMode.INTELLIGENT);

		BaseRule<Integer> rule = new BaseRule<>("a > b / C_ NOT x_", factory);

		testRule(rule, factory, "axa", "axa");
		testRule(rule, factory, "aya", "ayb");
		testRule(rule, factory, "aza", "azb");
	}

	@Test
	void testAliases01() {
		BaseRule<Integer> rule = new BaseRule<>(
				"[alveolar, -continuant] > [retroflex] / r_", FACTORY);

		testRule(rule, FACTORY, "arka", "arka");
		testRule(rule, FACTORY, "arpa", "arpa");
		testRule(rule, FACTORY, "arta", "arʈa");
		testRule(rule, FACTORY, "arsa", "arsa");
	}

	@Test
	void testAliases02() {
		BaseRule<Integer> rule = new BaseRule<>("[alveolar]y > [palatal]",
				FACTORY);

		testRule(rule, FACTORY, "akya", "akya");
		testRule(rule, FACTORY, "apya", "apya");
		testRule(rule, FACTORY, "atya", "aca");
		testRule(rule, FACTORY, "asya", "aça");
	}


	@Test
	void testAliases03() {
		BaseRule<Integer> rule = new BaseRule<>("[alveolar] > [palatal]",
				FACTORY);

		//		testRule(rule, FACTORY, "aka", "aka");
		//		testRule(rule, FACTORY, "apa", "apa");
		testRule(rule, FACTORY, "ata", "aca");
		//		testRule(rule, FACTORY, "asa", "aça");
	}

	private static void testRule(BaseRule<Integer> rule, String seq,
			String exp) {
		testRule(rule, FACTORY, seq, exp);
	}

	private static void testRule(Rule<Integer> rule, SequenceFactory<Integer> factory, String seq,
			String exp) {
		Sequence<Integer> sequence = factory.getSequence(seq);
		Sequence<Integer> expected = factory.getSequence(exp);
		Sequence<Integer> received = rule.apply(sequence);

		assertEquals(expected, received);
	}

	private static FeatureMapping<Integer> loadModel() {
		FeatureModelLoader<Integer> loader = new FeatureModelLoader<>(
				IntegerFeature.INSTANCE, ClassPathFileHandler.INSTANCE,
				"AT_hybrid.model");

		return loader.getFeatureMapping();
	}
}
