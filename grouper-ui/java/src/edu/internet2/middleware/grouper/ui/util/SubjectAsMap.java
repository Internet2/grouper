/*
Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2005 The University Of Bristol

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

import java.util.Set;

import org.apache.commons.beanutils.WrapDynaBean;

import edu.internet2.middleware.subject.Subject;

/**
 * Wraps a Subject - allows non persistent values to be stored for the UI and
 * works well with JSTL
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: SubjectAsMap.java,v 1.5 2006-04-03 12:49:02 isgwb Exp $
 */
public class SubjectAsMap extends ObjectAsMap {

	protected String objType = "I2miSubject";

	private Subject subject = null;

	/**
	 * @param subject
	 *            to wrap
	 */
	public SubjectAsMap(Subject subject) {
		super();
		super.objType = objType;
		dynaBean = new WrapDynaBean(subject);
		if (subject == null)
			throw new NullPointerException(
					"Cannot create SubjectAsMap with a null Subject");
		this.subject = subject;
		wrappedObject = subject;
		put("subjectType", subject.getType().getName());
		put("subjectId", subject.getId());
		if (subject.getType().getName().equals("person")) {

			//put("subjectTypeAdapter",subject.getType().getAdapter().getClass().getName());
			
			put("isMember", Boolean.TRUE);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public Object get(Object key) {
		Class stemClass = subject.getClass();
		Object obj = getByIntrospection(key);
		if(obj!=null) return obj;
		obj = super.get(key);
		//Map overrides wrapped Subject
		
		if(obj!=null && !"".equals(obj)) return obj;
			//if (values != null && values.size() != 0)
			//	obj = values.iterator().next();
		
				if ("id".equals(key))
					obj = subject.getId();
				else if ("description".equals(key) || "desc".equals(key)) {
					obj = subject.getDescription();
					if((obj==null || "".equals(obj)) && subject.getType().getName().equals("group")) {
						obj = subject.getAttributeValue("displayExtension");
					}
				} else if ("subjectType".equals(key))
					obj = subject.getType().getName();
				else if ("source".equals(key))
					obj = subject.getSource().getId();
				if (obj == null) {
					//No value so check wrapped Subject for value
					obj = subject.getAttributeValue((String) key);
				}
		
		return obj;
	}
}