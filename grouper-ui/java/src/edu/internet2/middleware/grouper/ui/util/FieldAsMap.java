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

package edu.internet2.middleware.grouper.ui.util;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.beanutils.WrapDynaBean;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;

/**
 * Wraps a Field - allows non persistent values to be stored for the UI and
 * works well with JSTL
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: FieldAsMap.java,v 1.5 2008-03-03 13:54:52 isgwb Exp $
 */
public class FieldAsMap extends ObjectAsMap {

	protected String objType = "Field";

	private Field field = null;
	
	protected FieldAsMap() {}
	
  /**
   * @param field
   *            to wrap
    * @param bundle
   *            where to lookup display names
   */
  public FieldAsMap(Field field) {
    super();
    init(field);
  }
  
	protected void init(Field field) {
		super.objType = objType;
		dynaBean = new WrapDynaBean(field);
		if (field == null)
			throw new NullPointerException(
					"Cannot create SubjectAsMap with a null Subject");
		this.field = field;
		wrappedObject = field;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public Object get(Object key) {
		Class stemClass = field.getClass();
		Object obj = getByIntrospection(key);
		if(obj!=null) return obj;
		obj = super.get(key);
		//Map overrides wrapped Subject
		
		if(obj!=null && !"".equals(obj)) return obj;
			//if (values != null && values.size() != 0)
			//	obj = values.iterator().next();
		
				if ("displayName".equals(key)) {
					String displayName = null;
					try {
						displayName =  GrouperUiFilter.retrieveSessionNavResourceBundle().getString("field.displayName." + field.getName());
					}catch(Exception e) {
						displayName = field.getName();
					}
					obj=displayName;
				}
		
		
		return obj;
	}
	
	protected Set getExtraKeys() {
		Set keys  = new HashSet();
		keys.add("displayName");
		return keys;
		
	}

}
