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
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.beanutils.WrapDynaBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.ui.actions.PopulateGroupSummaryAction;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * Wraps a Subject - allows non persistent values to be stored for the UI and
 * works well with JSTL
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: SubjectAsMap.java,v 1.13 2009-03-04 15:36:09 isgwb Exp $
 */
public class SubjectAsMap extends ObjectAsMap {
	protected static final Log LOG = LogFactory.getLog(SubjectAsMap.class);


	protected String objType = "I2miSubject";

	private Subject subject = null;
	
	protected SubjectAsMap() {
		
	}
	/**
	 * @param subject
	 *            to wrap
	 */
	public SubjectAsMap(Subject subject) {
		super();
		init(subject);
		
		
	}
	
	protected void init(Subject subject) {
		super.objType = objType;
		dynaBean = new WrapDynaBean(subject);
		if (subject == null)
			throw new NullPointerException(
					"Cannot create SubjectAsMap with a null Subject");
		this.subject = subject;
		wrappedObject = subject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public Object get(Object key) {
		try {
			Object obj = getByIntrospection(key);
			if(obj!=null) return obj;
			obj = super.get(key);
			//Map overrides wrapped Subject
			if(obj==null && "useMulti".equals(key)) {
				return null;
			}
			
			if(obj!=null && !"".equals(obj)) return obj;
				//if (values != null && values.size() != 0)
				//	obj = values.iterator().next();
			
					if ("id".equals(key)||"subjectId".equals(key))
						obj = subject.getId();
					else if ("description".equals(key) || "desc".equals(key)) {
						obj = subject.getDescription();
						if((obj==null || "".equals(obj)) && subject.getType().getName().equals("group")) {
							obj = subject.getAttributeValue("displayExtension");
						}
					} else if ("subjectType".equals(key))
						obj = subject.getType().getName();
					else if ("sourceId".equals(key))
						obj = subject.getSource().getId();
					if (obj == null) {
						//No value so check wrapped Subject for value
						String sep = (String)this.get("useMulti");
						if(sep==null) {
							obj = subject.getAttributeValue((String) key);
						}else{
							StringBuffer sb = new StringBuffer();
							Set values = (Set)subject.getAttributeValues((String)key);
							if(values==null) return null;
							Iterator it = values.iterator();
							Object val;
							int count=0;
							while(it.hasNext()) {
								val=it.next();
								if(count>0) {
									sb.append(sep);
									sb.append(" ");
								}
								sb.append(val);
							}
							obj=sb.toString();
						}
					}
			
			return obj;
		}catch(Exception e) {
			LOG.error(e);
				return "Unresolvable";
		}
	}
	
	protected Set getExtraKeys() {
		Set keys  = new HashSet();
		keys.add("sourceId");
		keys.add("subjectType");
		keys.add("subjectId");
		return keys;
	}

}
