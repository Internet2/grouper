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

package edu.internet2.middleware.grouper.ui.util;


import org.apache.commons.beanutils.WrapDynaBean;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemNotFoundException;

/**
 * Wraps a GrouperStem- allows non persistent values to be stored for the UI and
 * works well with JSTL
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: StemAsMap.java,v 1.3 2006-07-14 11:04:11 isgwb Exp $
 */
public class StemAsMap extends ObjectAsMap {
	
	
	protected Stem stem = null;

	protected final static String objType = "GrouperStem";

	private GrouperSession grouperSession = null;

	/**
	 * @param stem Stem to wrap
	 * @param s GrouperSession for authenticated user
	 */
	public StemAsMap(Stem stem, GrouperSession s) {
		super();
		dynaBean = new WrapDynaBean(stem);
		super.objType = objType;
		if (stem == null)
			throw new NullPointerException(
					"Cannot create StemAsMap with a null stem");
		this.stem = stem;
		wrappedObject = stem;
		put("subjectType", "stem");
		//put("isGroup",Boolean.FALSE);
		put("isStem", Boolean.TRUE);
		put("id", stem.getUuid());
		put("stemId", stem.getUuid());
		try{
		put("stem",stem.getParentStem().getName());
		}catch(StemNotFoundException e){}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public Object get(Object key) {
		//Map would override GrouperGroup values
		Object obj = super.get(key);
		if (obj == null) {
			//No value, so check the wrapped stem
			Class stemClass = stem.getClass();
			obj = getByIntrospection(key);
			
		}
		if (obj == null)
			obj = "";
		return obj;
	}
	

}