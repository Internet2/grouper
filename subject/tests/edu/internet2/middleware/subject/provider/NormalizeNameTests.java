
package edu.internet2.middleware.subject.provider;

import edu.internet2.middleware.subject.provider.JDBCSourceAdapter;

import junit.framework.TestCase;

/**
 * Unit tests for name normalization.
 */
public class NormalizeNameTests extends TestCase{

	public static void main(String[] args) {
		junit.textui.TestRunner.run(NormalizeNameTests.class);
	}
	
	/**
	 * Default constructor.
	 */
	public NormalizeNameTests() {
		super();
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * Test case: One word
	 */
	public void test1() {
		assertEquals(
			"test1 normalizeName() returned unexpected result",
			"% nguyen",
			JDBCSourceAdapter.normalizeName("nguyen"));
	}
	
	/**
	 * Test case: One word, leading and trailing spaces.
	 */
	public void test2() {
		assertEquals(
			"test2 normalizeName() returned unexpected result",
			"% nguyen",
			JDBCSourceAdapter.normalizeName(" nguyen "));
	}
	
	/**
	 * Test case: Two words.
	 */
	public void test3() {
		assertEquals(
			"test3 normalizeName() returned unexpected result",
			"minh% nguyen",
			JDBCSourceAdapter.normalizeName("minh nguyen"));
	}
	
	/**
	 * Test case: Three words.
	 */
	public void test4() {
		assertEquals(
			"test5 normalizeName() returned unexpected result",
			"minh% ngoc% nguyen",
			JDBCSourceAdapter.normalizeName("minh ngoc nguyen"));
	}

	/**
	 * Test case: Hyphenated word.
	 */
	public void test5() {
		assertEquals(
			"test7 normalizeName() returned unexpected result",
			"minh% ngoc% tran nguyen",
			JDBCSourceAdapter.normalizeName("minh ngoc tran-nguyen"));
	}

}
