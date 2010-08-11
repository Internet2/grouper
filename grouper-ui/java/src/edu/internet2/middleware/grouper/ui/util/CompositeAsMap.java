package edu.internet2.middleware.grouper.ui.util;

import org.apache.commons.beanutils.WrapDynaBean;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Composite;

/**
 * Wraps a Composite - allows non persistent values to be stored for the UI and
 * works well with JSTL
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: CompositeAsMap.java,v 1.7 2008-07-21 04:43:47 mchyzer Exp $
 */
public class CompositeAsMap extends ObjectAsMap {
	protected Composite composite = null;

	protected final static String objType = "Composite";

	private GrouperSession grouperSession = null;
	
	protected CompositeAsMap() {}

	/**
	 * @param stem Stem to wrap
	 * @param s GrouperSession for authenticated user
	 */
	public CompositeAsMap(Composite composite) {
		super();
		init(composite);
	}
	
	protected void init(Composite c) {
		dynaBean = new WrapDynaBean(c);
		super.objType = objType;
		if (c == null)
			throw new NullPointerException(
					"Cannot create CompositeAsMap with a null composite");
		this.composite = c;
		wrappedObject = c;
	}
}
