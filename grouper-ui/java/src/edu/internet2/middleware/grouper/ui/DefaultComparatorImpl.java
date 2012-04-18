/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
Copyright 2004-2007 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2007 The University Of Bristol

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.ui.actions.PopulateGroupSummaryAction;
import edu.internet2.middleware.grouper.ui.util.GroupAsMap;
import edu.internet2.middleware.grouper.ui.util.StemAsMap;
import edu.internet2.middleware.subject.Subject;

/**
 * Implementation of Comparator used for sorting potentially disparate objects. 
 * A context and a config (ResourceBundle) must be set before the Comparator 
 * is used. Stems always sort before Groups. 
 * <p>Each Object passed to the compare method is used to lookup a helper class,
 * configured through media.properties, which returns an appropriate sort String
 * for the Object.</p>
 * <p>The Grouper UI comes pre-configured with the following implementations: 
 * <pre>comparator.helper.edu.internet2.middleware.grouper\
.Group                =<b>edu.internet2.middleware.grouper.ui.{@link GroupComparatorHelper}</b>
.GroupAsMap           =<b>edu.internet2.middleware.grouper.ui.{@link GroupComparatorHelper}</b>
.Stem                 =<b>edu.internet2.middleware.grouper.ui.{@link StemComparatorHelper}</b>
.StemAsMap            =<b>edu.internet2.middleware.grouper.ui.{@link StemComparatorHelper}</b>
.Subject              =<b>edu.internet2.middleware.grouper.ui.{@link StemComparatorHelper}</b>
.SubjectAsMap         =<b>edu.internet2.middleware.grouper.ui.{@link SubjectComparatorHelper}</b>
.Member               =<b>edu.internet2.middleware.grouper.ui.{@link SubjectComparatorHelper}</b>
.Membership           =<b>edu.internet2.middleware.grouper.ui.{@link SubjectComparatorHelper}</b>
.MembershipAsMap      =<b>edu.internet2.middleware.grouper.ui.{@link SubjectComparatorHelper}</b>
.SubjectPrivilegeAsMap=<b>edu.internet2.middleware.grouper.ui.{@link GroupOrStemComparatorHelper}</b></pre>
 * </p>
 * <p>Sites can provide their own implementation for a helper class e.g. for a local implementation of the Subject interface which has complex sorting logic. 
 * The object instance class name is looked up first. If a helper is not found and the object is an instance of Subject, then the Subject helper
 * implementation will be used.</p>
 * <p>This implementation has not been profiled. The sorting used is that provided by the JDK, however, determining the
 * String to sort an object by, may be expensive, the first time it is determined, if lazy instantiation of the object 
 * is used.  
 * <p>Setting a UIThreadLocal GrouperComparatorHelperOverrideClass overrides the normal getHelper. Added so that caller
 * can influence how Memberships are sorted i.e. use Group.class or Stem.class to override default Subject </p>
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: DefaultComparatorImpl.java,v 1.4 2009-08-12 04:52:14 mchyzer Exp $
 */
public class DefaultComparatorImpl implements GrouperComparator {
	protected static final Log LOG = LogFactory.getLog(DefaultComparatorImpl.class);
	
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
		if(context==null || GrouperUiFilter.retrieveSessionMediaResourceBundle()==null) throw new IllegalStateException("A context and config must be set");
		
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
		try {
			String comp = helper.getComparisonString(obj,GrouperUiFilter.retrieveSessionMediaResourceBundle(),context);
			return comp;
		}catch(Exception e) {
			LOG.error(e);
			return "?";
		}
	}
	
	private GrouperComparatorHelper getHelper(Object obj) {
		String claz = null;
		Class overrideClaz = (Class)UIThreadLocal.get("GrouperComparatorHelperOverrideClass");
		if(overrideClaz!=null) {
			claz=overrideClaz.getName();
		}else{
			claz=obj.getClass().getName();
		}
		GrouperComparatorHelper helper = (GrouperComparatorHelper)helpers.get(claz);
		if(helper==null) {
			String helperClass = null;
			String keyLookup="comparator.helper." + claz;
			try {
				helperClass=GrouperUiFilter.retrieveSessionMediaResourceBundle().getString(keyLookup);
			}catch (Exception e) {}
			
			if(helperClass==null && obj instanceof Subject) {
				keyLookup="comparator.helper." + Subject.class.getName();
				try {
					helperClass=GrouperUiFilter.retrieveSessionMediaResourceBundle().getString(keyLookup);
				}catch (Exception e) {}
			}
			
			if(helperClass==null) {
				throw new IllegalStateException(keyLookup + " is not defined");
			}
			try {
				helper=(GrouperComparatorHelper)Class.forName(helperClass).newInstance();
				helpers.put(claz,helper);
			}catch (Exception e) {
				throw new IllegalStateException("Could not instantiate " + helperClass);
			}
		}
		return helper;
	}
}
