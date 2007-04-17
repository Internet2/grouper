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

import java.util.*;

/**
 * Convenience class to allow a ResourceBundle to be acessed as a Map - used in
 * JSTL. If a key starts with '*' the '*' is discarded, however, if no value is
 * found and empty String is returned rather than ???key???
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: MapBundleWrapper.java,v 1.3 2007-04-17 12:21:35 isgwb Exp $
 */

public class MapBundleWrapper extends HashMap {

	private ResourceBundle bundle;

	/**
	 * @param bundle
	 *            to wrap
	 */
	public MapBundleWrapper(ResourceBundle bundle) {
		if (bundle == null)
			throw new NullPointerException();
		this.bundle = bundle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public Object get(Object key) {
		boolean isSilent=false;
		if (key instanceof String) {
		} else
			throw new IllegalArgumentException("Strings only as keys");
		try {
			String keyStr = (String)key;
			if(keyStr.startsWith("*")) {
				isSilent=true;
				keyStr = keyStr.substring(1);
			}
			Object returnObj = bundle.getObject(keyStr);
			return returnObj;
		} catch (MissingResourceException e) {
			if(isSilent) return "";

		}
		return "???" + key + "???";
	}
}