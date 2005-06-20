package edu.internet2.middleware.subject.provider;

import java.util.Collection;
import java.util.Iterator;

import edu.internet2.middleware.subject.provider.BaseSourceAdapter;
import edu.internet2.middleware.subject.provider.SourceManager;

import junit.framework.TestCase;

/**
 * Unit tests for SourceManager.
 */
public class SourceManagerTests extends TestCase {

	private SourceManager mgr;
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(SourceManagerTests.class);
	}

	/**
	 * Default constructor.
	 */
	public SourceManagerTests() {
		super();
	}
	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		this.mgr = SourceManager.getInstance();
	}

	/**
	 * Test case for loading sources from sources.xml
	 */
	public void testInitSource() {
		try {
			for (Iterator iter = this.mgr.getSources().iterator(); iter.hasNext();) {
				BaseSourceAdapter source = (BaseSourceAdapter)iter.next();
				source.init();
			}
		} catch (Exception ex) {
			fail("Failed test to init sources: " + ex.getMessage());
		}
	}

	/**
	 * Test case for getting sources by SubjectType
	 */
	public void testGetSourcesByType() {
		Collection sources = mgr.getSources(SubjectTypeEnum.valueOf("person"));
		System.out.println("Person sources = " + sources);
		
		sources = mgr.getSources(SubjectTypeEnum.valueOf("group"));
		System.out.println("Group sources = " + sources);
		
		sources = mgr.getSources(SubjectTypeEnum.valueOf("application"));
		System.out.println("Application sources = " + sources);
	}

}
