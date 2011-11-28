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

import java.util.Iterator;

import org.apache.commons.beanutils.WrapDynaBean;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;

/**
 * Wraps a GrouperStem- allows non persistent values to be stored for the UI and
 * works well with JSTL
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: StemAsMap.java,v 1.7 2008-07-21 04:43:47 mchyzer Exp $
 */
public class StemAsMap extends ObjectAsMap {
	
	
	protected Stem stem = null;

	protected final static String objType = "GrouperStem";

	private GrouperSession grouperSession = null;
	
	protected StemAsMap() {}

	/**
	 * @param stem Stem to wrap
	 * @param s GrouperSession for authenticated user
	 */
	public StemAsMap(Stem stem, GrouperSession s) {
		super();
		init(stem);
	}
	
	protected void init(Stem s) {
		dynaBean = new WrapDynaBean(s);
		super.objType = objType;
		if (s == null)
			throw new NullPointerException(
					"Cannot create StemAsMap with a null stem");
		this.stem = s;
		wrappedObject = s;
		put("subjectType", "stem");
		//put("isGroup",Boolean.FALSE);
		put("isStem", Boolean.TRUE);
		put("id", s.getUuid());
		put("stemId", s.getUuid());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public Object get(Object key) {
 		if (key.equals("alternateName")) {
 			Iterator<String> alternateNamesIterator = stem.getAlternateNames().iterator();
 			if (alternateNamesIterator.hasNext()) {
 				return alternateNamesIterator.next();
 			} else {
 				return null;
 			}
 		}

		//Map would override GrouperGroup values
		Object obj = super.get(key);
		if (obj == null) {
			if("stem".equals(key)) {
				try{
					put("stem",stem.getParentStem().getName());
					}catch(StemNotFoundException e){
						int a=0;
					}
					obj = super.get(key);
			}else{
			//No value, so check the wrapped stem
				obj = getByIntrospection(key);
			}
			
		}
		if (obj == null)
			obj = "";
		return obj;
	}
	

}
