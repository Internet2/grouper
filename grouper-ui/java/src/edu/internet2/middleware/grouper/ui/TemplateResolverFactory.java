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

/**
 * Creates and caches for re-use a TemplateResolver. In practice, a single
 * resolver is likely to be used, however, different ones could be used based on
 * Locale.
 * <p />
 * To use your own implementation set the key
 * <i>edu.internet2.middleware.grouper.ui.TemplateResolver </i> in
 * resources/media.resources to the name of a class which implements this
 * interface. If none is specified <i>DefaultTemplateREsolverImpl </i> is used.
 * 
 * 
 * @author Gary Brown.
 * @version $Id: TemplateResolverFactory.java,v 1.4 2007-04-11 08:19:24 isgwb Exp $
 */
public class TemplateResolverFactory {

	private static Map cache = new HashMap();

	/**
	 * @param bundle containing key <i>edu.internet2.middleware.grouper.ui.TemplateResolver </i>
	 * @return TemplateResolver implementation
	 */
	public static TemplateResolver getTemplateResolver(ResourceBundle bundle) {
		String resolverName = null;
		try {
			resolverName = bundle
					.getString("edu.internet2.middleware.grouper.ui.TemplateResolver");
		} catch (Exception e) {
			resolverName = "edu.internet2.middleware.grouper.ui.DefaultTemplateResolverImpl";
		}
		if (cache.containsKey(resolverName)) {
			return (TemplateResolver) cache.get(resolverName);
		}
		Class resolverClass = null;
		TemplateResolver resolver = null;
		try {
			resolverClass = Class.forName(resolverName);
			resolver = (TemplateResolver) resolverClass.newInstance();
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(
					"No class found for TemplateResolver - " + resolverName);

		} catch (InstantiationException e) {
			throw new IllegalArgumentException(
					"Cannot instantiate TemplateResolver - " + resolverName);
		} catch (ClassCastException e) {
			throw new IllegalArgumentException(resolverName
					+ " does not implement TemplateResolver");
		} catch (Exception e) {
			throw new IllegalArgumentException("Problem loading "
					+ resolverName + ":" + e.getMessage());
		}
		cache.put(resolverName, resolver);
		return resolver;
	}

}
