/*
Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2006 The University Of Bristol

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package edu.internet2.middleware.grouper.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.ui.util.GroupAsMap;
import edu.internet2.middleware.grouper.ui.util.StemAsMap;
import edu.internet2.middleware.subject.Subject;

/**
 * Implementation of Comparator used for sorting potentially disparate objects. 
 * A context and a config (ResourceBundle) must be set before the Comaparator 
 * is used. Stems always sort before Groups. 
 * <p>Each Object passed to the compare method is used to lookup a helper class,
 * configured through media.properties, which returns an appropriate sort String
 * for the Object.</p>
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: DefaultComparatorImpl.java,v 1.1 2007-03-15 15:30:16 isgwb Exp $
 */
public class DefaultComparatorImpl implements GrouperComparator {
	private ResourceBundle config;
	private String context;
	private Map helpers = new HashMap();
	/**
	 * 
	 */
	public DefaultComparatorImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.GrouperComparator#setConfigBundle(java.util.ResourceBundle)
	 */
	public void setConfigBundle(ResourceBundle bundle) {
		this.config=bundle;

	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.GrouperComparator#setContext(java.lang.String)
	 */
	public void setContext(String context) {
		// TODO Auto-generated method stub
		this.context=context;

	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object arg0, Object arg1) {
		// TODO Auto-generated method stub
		if(context==null || config==null) throw new IllegalStateException("A context and config must be set");
		
		if((arg0 instanceof Stem || arg0 instanceof StemAsMap) 
				&& (arg1 instanceof Group || arg1 instanceof GroupAsMap)) return -1;
		if((arg0 instanceof Group || arg0 instanceof GroupAsMap) 
				&& (arg1 instanceof Stem || arg1 instanceof StemAsMap)) return 1;
		
		
		String arg0Comp = getComparisonString(arg0);
		String arg1Comp = getComparisonString(arg1);
		
		return arg0Comp.compareTo(arg1Comp);
	}
	
	private String getComparisonString(Object obj) {
		GrouperComparatorHelper helper = getHelper(obj);
		return helper.getComparisonString(obj,config,context);
	}
	
	private GrouperComparatorHelper getHelper(Object obj) {
		GrouperComparatorHelper helper = (GrouperComparatorHelper)helpers.get(obj.getClass().getName());
		if(helper==null) {
			String helperClass = null;
			String keyLookup=null;
			
			if(obj instanceof Subject) {
				keyLookup="comparator.helper." + Subject.class.getName();
				
			}else{
				keyLookup="comparator.helper." + obj.getClass().getName();
			}
			try {
				helperClass=config.getString(keyLookup);
			}catch (Exception e) {}
			
			if(helperClass==null) {
				throw new IllegalStateException(keyLookup + " is not defined");
			}
			try {
				helper=(GrouperComparatorHelper)Class.forName(helperClass).newInstance();
				helpers.put(obj.getClass().getName(),helper);
			}catch (Exception e) {
				throw new IllegalStateException("Could not instantiate " + helperClass);
			}
		}
		return helper;
	}
}
