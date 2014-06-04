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

import java.util.ResourceBundle;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.ui.util.ObjectAsMap;
import edu.internet2.middleware.grouper.ui.util.SubjectPrivilegeAsMap;

/**
 * Implementation of ComparatorHelper used to sort GroupOrStems. Will also
 * sort SubjectPrivilegeAsMaps as these can be shown as groups or stems.
 * <p>Implementation determines whether underlying Object is a Group or Stem,
 * and calls appropriate Helper implementation</p>
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: GroupOrStemComparatorHelper.java,v 1.1 2007-03-15 15:30:16 isgwb Exp $
 */

public class GroupOrStemComparatorHelper implements GrouperComparatorHelper{
	private GroupComparatorHelper groupHelper = new GroupComparatorHelper();
	private StemComparatorHelper stemHelper = new StemComparatorHelper();
	/**
	 * 
	 */
	public GroupOrStemComparatorHelper() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	/* (non-Javadoc)
	 * @see edu.internet2.middleware.grouper.ui.GrouperComparatorHelper#getComparisonString(java.lang.Object, java.util.ResourceBundle, java.lang.String)
	 */
	public String getComparisonString(Object obj, ResourceBundle config,
			String context) {
		if(obj instanceof GroupOrStem) {
			if(((GroupOrStem)obj).isGroup()) {
				return groupHelper.getComparisonString(((GroupOrStem)obj).getGroup(),config,context);
			}else{
				return stemHelper.getComparisonString(((GroupOrStem)obj).getStem(),config,context);
			}
		}
	
		if(! (obj instanceof SubjectPrivilegeAsMap)) {
			throw new IllegalArgumentException(obj + "is not a group or stem: " + (obj == null ? null : obj.getClass()));
		}
		SubjectPrivilegeAsMap spam = (SubjectPrivilegeAsMap)obj;
		ObjectAsMap groupOrStem = (ObjectAsMap)spam.get("groupOrStem");
		Object wrapped = groupOrStem.getWrappedObject();
		if(wrapped instanceof Group) return groupHelper.getComparisonString(wrapped,config,context);
		if(wrapped instanceof Stem) return stemHelper.getComparisonString(wrapped,config,context);
		throw new IllegalStateException(wrapped + " is not a group or stem");
		
	}
}
