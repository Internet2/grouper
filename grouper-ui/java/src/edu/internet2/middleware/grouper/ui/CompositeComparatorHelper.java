package edu.internet2.middleware.grouper.ui;

import java.util.ResourceBundle;

/**
 * Implementation of ComparatorHelper used to sort Composites and CompositeAsMaps. 
 *
 * 
 * @author Gary Brown.
 * @version $Id: GroupComparatorHelper.java,v 1.4 2009-11-07 14:46:34 isgwb Exp $
 */
public class CompositeComparatorHelper implements GrouperComparatorHelper {

	public String getComparisonString(Object obj, ResourceBundle config,
			String context) {
		//Should appear at top of list
		return "";
	}

}
