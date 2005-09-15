
package edu.internet2.middleware.subject.provider;

import edu.internet2.middleware.subject.provider.JDBCSourceAdapter;

import junit.framework.TestCase;

/**
 * Unit tests for name normalization last-first format.
 */
public class NormalizeNameLFTests extends TestCase{

	public static void main(String[] args) {
		junit.textui.TestRunner.run(NormalizeNameTests.class);
	}
	
	/**
	 * Default constructor.
	 */
	public NormalizeNameLFTests() {
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
			"test1 normalizeNameLF() returned unexpected result",
			"nguyen %",
			JDBCSourceAdapter.normalizeNameLF("nguyen"));
	}
	
	/**
	 * Test case: One word, leading and trailing spaces.
	 */
	public void test2() {
		assertEquals(
			"test2 normalizeNameLF() returned unexpected result",
			"nguyen %",
			JDBCSourceAdapter.normalizeNameLF(" nguyen "));
	}
	
	/**
	 * Test case: Two words.
	 */
	public void test3() {
		assertEquals(
			"test3 normalizeNameLF() returned unexpected result",
			"nguyen minh%",
			JDBCSourceAdapter.normalizeNameLF("minh nguyen"));
	}

	/**
	 * Test case: Two words, comma.
	 */
	public void test4() {
		assertEquals(
			"test4 normalizeNameLF() returned unexpected result",
			"nguyen minh%",
			JDBCSourceAdapter.normalizeNameLF("nguyen, minh"));
	}
	
	/**
	 * Test case: Three words.
	 */
	public void test5() {
		assertEquals(
			"test5 normalizeNameLF() returned unexpected result",
			"nguyen minh% ngoc%",
			JDBCSourceAdapter.normalizeNameLF("minh ngoc nguyen"));
	}

	/**
	 * Test case: Three words, comma.
	 */
	public void test6() {
		assertEquals(
			"test6 normalizeNameLF() returned unexpected result",
			"nguyen minh% ngoc%",
			JDBCSourceAdapter.normalizeNameLF("nguyen, minh ngoc"));
	}
	
	/**
	 * Test case: Hyphenated word.
	 */
	public void test7() {
		assertEquals(
			"test7 normalizeNameLF() returned unexpected result",
			"tran nguyen minh% ngoc%",
			JDBCSourceAdapter.normalizeNameLF("minh ngoc tran-nguyen"));
	}

}
