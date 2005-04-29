package edu.internet2.middleware.subject.provider;

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
		this.mgr = new SourceManager();
	}

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

}
