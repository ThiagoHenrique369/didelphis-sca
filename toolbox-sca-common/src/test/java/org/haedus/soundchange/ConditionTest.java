package org.haedus.soundchange;

import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.soundchange.exceptions.RuleFormatException;
import org.haedus.datatypes.phonetic.VariableStore;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: goats
 * Date: 7/7/13
 * Time: 11:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConditionTest {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(ConditionTest.class);

	// We just need to see that this parses correctly
    @Test
    public void testEmptyCondition() throws RuleFormatException {
    	new Condition("_");
    }

	// We just need to see that this parses correctly
	@Test(expected = RuleFormatException.class)
	public void testBadCondition() throws RuleFormatException {
		new Condition("a_b_c");
	}

	@Test
	public void testPostconditionMatching01() throws RuleFormatException {
		Condition condition = new Condition("a_x");
		Sequence  sequence  = new Sequence("balx");

		assertTrue("", condition.isMatch(sequence, 2));
	}

	@Test
	public void testPostconditionMatching02() throws RuleFormatException {
		Condition condition = new Condition("b_#");
		Sequence  sequence  = new Sequence("aba");

		assertFalse("0", condition.isMatch(sequence, 0));
		assertFalse("1", condition.isMatch(sequence, 1));
		assertTrue("2", condition.isMatch(sequence, 2));
	}

	@Test
	public void testPostconditionMatching03() throws RuleFormatException {
		Condition condition = new Condition("b_lx");
		Sequence  sequence  = new Sequence("balx");

		assertTrue("1", condition.isMatch(sequence, 1));
		assertFalse("2", condition.isMatch(sequence, 2));
		assertFalse("3", condition.isMatch(sequence, 3));
	}

	@Test
	public void testPostconditionMatching04() throws RuleFormatException {
		Condition condition = new Condition("_lxpld");
		Sequence  sequence  = new Sequence("beralxpld");

		assertTrue("T", condition.isMatch(sequence, 3));
		assertFalse("F", condition.isMatch(sequence, 2));
	}

	@Test
	public void testOptional01() throws RuleFormatException {

		Condition condition = new Condition("_a?(b?c?)d?b");

		String[] positive = {
				"xb",     "xbb",
				"xcb",    "xbcb",
				"xab",    "xbab",
				"xdb",    "xbdb",
				"xacb",   "xbacb",
				"xadb",   "xbadb",
				"xcdb",   "xbcdb",
				"xabcb",  "xbabcb"
		};

		testPositive(condition, positive);
	}

	@Test
	public void testOptional02() throws RuleFormatException {

		Condition condition = new Condition("_d?ab");

		testTrue(condition,"xab",0);
		testTrue(condition,"xdab",0);

		assertFalse("xadb", condition.isMatch(new Sequence("xadb"), 0));
		assertFalse("xacb", condition.isMatch(new Sequence("xacb"), 0));
		assertFalse("xdb",  condition.isMatch(new Sequence("xdb"),  0));
	}

	@Test
	public void testOptional03() throws RuleFormatException {

		Condition condition = new Condition("_a(l(hamb)?ra)?#");

		testTrue(condition,  "xalhambra", 0);
		testTrue(condition,  "xalra",     0);
		testTrue(condition,  "xa",        0);
		testFalse(condition, "xalh",      0);
	}

	@Test
	public void testOptional04() throws RuleFormatException {

		Condition condition = new Condition("_a(ba)?b");

		testTrue(condition,  "xab",   0);
		testTrue(condition,  "xabab", 0);
		testFalse(condition, "xalh",  0);
	}

	@Test
	public void testStar01() throws RuleFormatException {

		Condition condition = new Condition("_a*b");

		assertTrue("xb", condition.isMatch(new Sequence("xb"), 0));
		assertTrue("xab	", condition.isMatch(new Sequence("xab"), 0));
		assertTrue("xaab", condition.isMatch(new Sequence("xaab"), 0));
		assertTrue("xaaab", condition.isMatch(new Sequence("xaaab"), 0));
		assertTrue("xaaaab", condition.isMatch(new Sequence("xaaaab"), 0));
		assertTrue("xaaaaab", condition.isMatch(new Sequence("xaaaaab"), 0));
		assertFalse("xcaaaab", condition.isMatch(new Sequence("xcaaaab"), 0));
	}

	@Test
	public void testStar02() throws RuleFormatException {

		Condition condition = new Condition("_aa*b");

		assertFalse("xb", condition.isMatch(new Sequence("xb"), 0));
		assertTrue("xab	", condition.isMatch(new Sequence("xab"), 0));
		assertTrue("xaab", condition.isMatch(new Sequence("xaab"), 0));
		assertTrue("xaaab", condition.isMatch(new Sequence("xaaab"), 0));
		assertTrue("xaaaab", condition.isMatch(new Sequence("xaaaab"), 0));
		assertTrue("xaaaaab", condition.isMatch(new Sequence("xaaaaab"), 0));
		assertFalse("xcaaaab", condition.isMatch(new Sequence("xcaaaab"), 0));
	}

	@Test
	public void testStar03() throws RuleFormatException {

		Condition condition = new Condition("_da*b");

		assertTrue("xdb", condition.isMatch(new Sequence("xdb"), 0));
		assertTrue("xdab", condition.isMatch(new Sequence("xdab"), 0));
		assertTrue("xdaab", condition.isMatch(new Sequence("xdaab"), 0));
		assertTrue("xdaaab", condition.isMatch(new Sequence("xdaaab"), 0));
		assertTrue("xdaaaab", condition.isMatch(new Sequence("xdaaaab"), 0));
		assertTrue("xdaaaaab", condition.isMatch(new Sequence("xdaaaaab"), 0));
		assertFalse("xdcaaaab", condition.isMatch(new Sequence("xdcaaaab"), 0));
	}

	@Test
	public void testStar04() throws RuleFormatException {

		Condition condition = new Condition("_d(eo)*b");

		assertTrue("xdb", condition.isMatch(new Sequence("xdb"), 0));
		assertTrue("xdeob", condition.isMatch(new Sequence("xdeob"), 0));
		assertTrue("xdeoeob", condition.isMatch(new Sequence("xdeoeob"), 0));
		assertTrue("xdeoeoeob", condition.isMatch(new Sequence("xdeoeoeob"), 0));
		assertFalse("xdcaaaab", condition.isMatch(new Sequence("xdcaaaab"), 0));
	}

	@Test
	public void testStar05() throws RuleFormatException {

		Condition condition = new Condition("_d(eo*)*b");

		String[] positive = {
				"xdb",         "xdeob",
				"xdeb",        "xdeooeob",
				"xdeeb",       "xdeoooeob",
				"xdeeeb",      "xdeoeoob",
				"xdeeeeb",     "xdeoeooob",
				"xdeoeob",     "xdeoob",
				"xdeoeoeob",   "xdeooob",
				"xdeoeoeoeob", "xdeoooob",
		};
		testPositive(condition, positive);
	}

	@Test
	public void testStar06() throws RuleFormatException {

		Condition condition = new Condition("_(ab)*#");
		String[] positive = {
				"x",     "xababab",
				"xab",   "xabababab",
				"xabab", "xababababab"
		};

		testPositive(condition, positive);

		String[] negative = {
				"xa",
				"xabababa",
				"xaba",   "xababababa",
				"xababa", "xabababababa"
		};

		testNegative(condition, negative);
	}

	@Test
	public void testPlus01() throws RuleFormatException {

		Condition condition = new Condition("_a+b");

		String[] positive = {
				"xab",
				"xaab",
				"xaaab",
				"xaaaab",
				"xaaaaab",
		};

		testPositive(condition, positive);

		testFalse(condition, "xb");
		testFalse(condition, "xcb");
		testFalse(condition, "xacb");
		testFalse(condition, "xaacb");
		testFalse(condition, "xaaacb");
		testFalse(condition, "xba");
	}

	@Test
	public void testPlus02() throws RuleFormatException {

		Condition condition = new Condition("_a+l(ham+b)+ra");

		String[] positive = {
				"xalhambra",    "xaalhambra",    "xalhambhambhambra",
				"xalhammbra",   "xaaalhambra",   "xalhammbhammbra",
				"xalhammmbra",  "xaaaalhambra",  "xalhammbhambra",
				"xalhammmmbra", "xalhambhambra", "xalhammmmbhambra"
		};
		testPositive(condition, positive);
	}

	@Test
	public void testPlus03() throws RuleFormatException {

		Condition condition = new Condition("_(a+l(ham+b)*ra)+");
		String [] positive = {
				"xalhambra",             "xaalhambra",
				"xalhammbra",            "xaaalhambra",
				"xalhammmbra",           "xaaaalhambra",
				"xalhammmmbra",          "xalhambhambra",

				"xalhambhambhambra",     "xalhambraalhambra",
				"xalhammbhammbra",       "xalhammbraalhambra",
				"xalhammbhambra",        "xalhammmbraalhambra",
				"xalhammmmbhambra",      "xalhammmmbraalhambra",

				"xalhambraalhammbra",    "xalhambraalhammmbra",
				"xalhammbraalhammbra",   "xalhammbraalhammmbra",
				"xalhammmbraalhammbra",  "xalhammmbraalhammmbra",
				"xalhammmmbraalhammbra", "xalhammmmbraalhammmbra",
		};

		testPositive(condition, positive);
	}

	@Test
	public void testStar07() throws RuleFormatException {

		Condition condition = new Condition("_(a+l(ham+b)+ra)*");

		String[] positive = {
				"xalhambra",         "xalhammmmmbra",
				"xalhammbra",        "xalhammmmmmbra",
				"xalhammmbra",       "xalhammmmmmmbra",
				"xalhammmmbra",      "xalhammmmmmmmbra",
				"xaalhambra",        "xalhammbhambra",
				"xaaalhambra",       "xalhammbhammbra",
				"xaaaalhambra",      "xalhammbhambra",
				"xaaaaalhambra",     "xalhammmmbhambra",
				"xalhambhambra",     "xalhambraalhambra",
				"xalhambhambhambra", "xalhambraalhambraalhambra", "x"
		};

		testPositive(condition, positive);
	}

	@Test
		 public void testGroups01() throws RuleFormatException {
		Condition condition = new Condition("_(ab)(cd)(ef)");

		testTrue(condition,"xabcdef",0);
		testFalse(condition,"xabcd",0);
		testFalse(condition,"xab",0);
		testFalse(condition,"bcdef",0);

	}

	@Test
	public void testGroups02() throws RuleFormatException {
		Condition condition = new Condition("_(ab)*(cd)(ef)");
		testTrue(condition, "xcdef",0);
		testTrue(condition, "xabcdef",0);
		testTrue(condition, "xababcdef",0);
		testTrue(condition, "xabababcdef",0);

		testFalse(condition, "xabbcdef",0);
		testFalse(condition, "xacdef",0);
		testFalse(condition, "xabdef",0);
		testFalse(condition, "xabcef",0);
		testFalse(condition, "xabcdf",0);
		testFalse(condition, "xabcde",0);
	}

	@Test
	public void testGroups03() throws RuleFormatException {
		Condition condition = new Condition("_(ab)(cd)*(ef)");
		testTrue(condition, "xabef",0);
		testTrue(condition, "xabcdef",0);
		testTrue(condition, "xabcdcdef",0);
	}

	@Test
	public void testGroups04() throws RuleFormatException {
		Condition condition = new Condition("_(ab)(cd)(ef)*");
		testTrue(condition, "xabcd",0);
		testTrue(condition, "xabcdef",0);
		testTrue(condition, "xabcdefef",0);
	}

	@Test
	public void testGroups05() throws RuleFormatException {
		Condition condition = new Condition("_(ab)?(cd)(ef)");
		testTrue(condition, "xabcdef",0);
		testTrue(condition, "xcdef",0);
	}

	@Test
	public void testGroups06() throws RuleFormatException {
		Condition condition = new Condition("_(ab)(cd)?(ef)");
		testTrue(condition, "xabcdef",0);
		testTrue(condition, "xabef",0);
	}

	@Test
	public void testGroups07() throws RuleFormatException {
		Condition condition = new Condition("_(ab)(cd)(ef)?");
		testTrue(condition, "xabcdef",0);
		testTrue(condition, "xabcd",0);
	}

	@Test
	public void testGroups08() throws RuleFormatException {
		Condition condition = new Condition("_(ab)?(cd)?(ef)?");

		testTrue(condition, "xabcdef",0);
		testTrue(condition, "x",0);
		testTrue(condition, "xab",0);
		testTrue(condition, "xcd",0);
		testTrue(condition, "xef",0);
		testTrue(condition, "xabef",0);
		testTrue(condition, "xabcd",0);
		testTrue(condition, "xcdef",0);
	}

	@Test
	public void testFullCondition()  throws RuleFormatException {
		Condition condition = new Condition("(ab)?(cd)?(ef)?_(ab)?(cd)?(ef)?");
		testTrue(condition, "xabcdef",0);
		testTrue(condition, "efxabcdef",2);
		testTrue(condition, "cdefxabcdef",4);
		testTrue(condition, "abcdefxabcdef",6);
		testTrue(condition, "abcdefxabcd",6);
		testTrue(condition, "abcdefxab",6);
		testTrue(condition, "abcdefx",6);

		testTrue(condition, "abx",2);
		testTrue(condition, "cdx",2);
		testTrue(condition, "efx",2);

		testTrue(condition, "abefx",4);
		testTrue(condition, "abcdx",4);
		testTrue(condition, "cdefx",4);
	}

	@Test
	public void testSet01()  throws RuleFormatException {
		Condition condition = new Condition("_{a b c}ds");
		testTrue(condition,  "xads",0);
		testTrue(condition,  "xbds",0);
		testTrue(condition,  "xcds",0);
		testFalse(condition, "xds", 0);
	}

	@Test
	public void testSet02()  throws RuleFormatException {
		Condition condition = new Condition("_{ab cd ef}tr");
		testTrue(condition,  "xabtr",0);
		testTrue(condition,  "xcdtr",0);
		testTrue(condition,  "xeftr",0);
		testFalse(condition, "xabcd", 0);
		testFalse(condition, "xtr",0);
	}

	@Test
	public void testSet04()  throws RuleFormatException {
		Condition condition = new Condition("_{ab* cd+ ef}tr");

		testTrue(condition,  "xabtr", 0);
		testTrue(condition,  "xcdtr", 0);
		testTrue(condition,  "xeftr", 0);

		testFalse(condition, "xacd", 0);
		testFalse(condition, "xabbcd", 0);
		testFalse(condition, "xabx", 0);
		testFalse(condition, "xabcd", 0);
		testFalse(condition, "xb", 0);
		testFalse(condition, "x", 0);
		testFalse(condition, "xc", 0);
		testFalse(condition, "xcdef", 0);
		testFalse(condition, "xtr",   0);
	}

	@Test
	public void testSet05()  throws RuleFormatException {
		Condition condition = new Condition("_{ab* (cd?)+ ((ae)*f)+}tr");

		testTrue(condition,  "xabtr",0);

		testTrue(condition,  "xcdtr",0);
		testTrue(condition,  "xcctr",0);
		testTrue(condition,  "xccctr",0);

		testTrue(condition,  "xftr",0);
		testTrue(condition,  "xfftr",0);
		testTrue(condition,  "xaeftr",0);
		testTrue(condition,  "xaeaeftr",0);
		testTrue(condition,  "xaefaeftr",0);
		testTrue(condition,  "xaefffffaeftr",0);

		testFalse(condition, "xabcd",0);
		testFalse(condition, "xtr",0);
	}

	@Test
	public void testSet06()  throws RuleFormatException {
		Condition condition = new Condition("_{ab {cd xy} ef}tr");
		testTrue(condition,  "xabtr",0);
		testTrue(condition,  "xcdtr",0);
		testTrue(condition,  "xeftr",0);
		testTrue(condition,  "xxytr",0);
		testFalse(condition, "xabcd",0);
		testFalse(condition, "xtr",0);
	}

	@Test
	public void testSet07() throws RuleFormatException {
		Condition condition = new Condition("_{ x ɣ }");

		testTrue(condition, "pxi");
		testFalse(condition, "paxi");
	}

	@Test
	public void testComplex01() throws RuleFormatException {
		Condition condition = new Condition("_{r l}?{a e o ā ē ō}{i u}?{n m l r}?{pʰ tʰ kʰ ḱʰ}");

		testTrue(condition, "pʰāḱʰus");
		testTrue(condition, "pʰentʰros");
		testTrue(condition, "pʰlaḱʰmēn");
		testTrue(condition, "pʰoutʰéyet");

		testFalse(condition, "pʰuǵos");
	}

	@Test
	public void testAdditional01() throws RuleFormatException {

		testTrue(new Condition("_c+#"),  "abaccc", 2);
		testTrue(new Condition("_#"),    "abad",   3);
	}

	@Test
	public void testWithVariables01() throws RuleFormatException {

		String[] terminals = {
				"p","t","k","b","d","g","pʰ","tʰ","kʰ"
		};
		VariableStore vs = new VariableStore();

		vs.put("C", toList(terminals), true);
		Condition condition = new Condition("_C+#",vs);

		testTrue(condition,  "abaptk",  2);
		testTrue(condition,  "abapppp", 2);
		testTrue(condition,  "ababdg",  2);
//		testTrue(condition,  "abatʰkʰ", 2); // ???

		testTrue(condition,  "abaptk",  3);
		testTrue(condition,  "abapppp", 3);
		testTrue(condition,  "ababdg",  3);
//		testTrue(condition,  "abapʰtʰkʰ", 3);

		testFalse(condition,  "abatʰkʰ", 1);
		testFalse(condition,  "abatʰkʰ", 0);
	}
	
	@Test
	public void testVariablesDebug01() throws RuleFormatException {
		String[] terminals = {
				"p","t","k","b","d","g","pʰ","tʰ","kʰ"
		};
		VariableStore vs = new VariableStore();

		vs.put("C", toList(terminals), true);
		Condition condition = new Condition("_C+#", vs);
		
		testTrue(condition,  "abatʰkʰ", 2);
	}

	@Ignore
	@Test
	public void testNegative00() throws RuleFormatException {
		Condition condition = new Condition("_!a#");

		testTrue(condition,  "zb", 0);
		testTrue(condition,  "zc", 0);
		testTrue(condition,  "zd", 0);
		testTrue(condition,  "ze", 0);

        testFalse(condition, "za", 0); // Good
	}

	@Ignore
	@Test
	public void testNegative01() throws RuleFormatException {
		Condition condition = new Condition("_!(abc)#");

		testTrue(condition,  "zbab", 0);
		testTrue(condition,  "zcab", 0);
		testTrue(condition,  "zdab", 0);
		testTrue(condition,  "zeab", 0);

		testTrue(condition,  "zaba", 0);
		testFalse(condition, "zabc", 0);
		testTrue(condition,  "zcba", 0);
		testTrue(condition,  "zaaa", 0);
	}

	@Ignore
	@Test
	public void testNegative02() throws RuleFormatException {
		Condition condition = new Condition("_!{a b c}#");

		testTrue(condition, "yz", 0);
		testTrue(condition, "ym", 0);
		testTrue(condition, "yr", 0);
		testTrue(condition, "yd", 0);

		testFalse(condition, "x!a", 0); // Good
		testFalse(condition, "xa",  0);
		testFalse(condition, "xb",  0);
		testFalse(condition, "xc",  0);
	}

	private Iterable<String> toList(String... strings) {
		Collection<String> list = new ArrayList<String>();

		Collections.addAll(list,strings);
		return list;
	}

	private void testTrue(Condition condition, String testString, int index) {
		assertTrue(testString, condition.isMatch(new Sequence(testString), index));
	}

	private void testFalse(Condition condition, String testString, int index) {
		assertFalse(testString, condition.isMatch(new Sequence(testString), index));
	}

	private void testFalse(Condition condition, String testString) {
		testFalse(condition, testString, 0);
	}

	private void testTrue(Condition condition, String testString) {
		testTrue(condition, testString, 0);
	}

	private void testPositive(Condition condition, String[] positive) {
		for (String p : positive) {
			testTrue(condition, p, 0);
		}
	}

	private void testNegative(Condition condition, String[] negative) {
		for (String n : negative) {
			testFalse(condition, n, 0);
		}
	}
}


