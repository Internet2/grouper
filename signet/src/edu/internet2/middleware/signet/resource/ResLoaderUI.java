/*
 $Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/resource/ResLoaderUI.java,v 1.1 2006-05-09 01:33:33 ddonn Exp $

 Copyright (c) 2006 Internet2, Stanford University

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 @author ddonn
 */
package edu.internet2.middleware.signet.resource;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/** Resource Loader for Signet properties. Resources are loaded based on Java's
 * resource bundle load algorithm:
 *    basename_language_country.properties (e.g. signet_en_US.properties), 
 *    basename_language.properties (e.g. signet_en.properties),
 *    basename.properties (e.g. signet.properties)
 * ResourceBundle searches the Classpath to find files and appends language,
 * country, and .properties to the basename. Language and country are derived
 * from the system default Locale (Locale.getDefault()).
 * Properties loaded first take precedence (e.g. if foo=bar is defined in
 * signet_en.properties and foo=blah is defined in signet.properties, then
 * foo=bar is what the application will get).
 * This class also loads a second property file containing the app version.
 */

public class ResLoaderUI
{
	private static final String			BUNDLE_NAME	=
		"edu.internet2.middleware.signet.SignetResUI"; //$NON-NLS-1$

	private static final ResourceBundle	RESOURCE_BUNDLE	=
		ResourceBundle.getBundle(BUNDLE_NAME);

	private static final String			verBundleName =
		"edu.internet2.middleware.signet.signet_version"; //$NON-NLS-1$

	private static final ResourceBundle	verBundle =
		ResourceBundle.getBundle(verBundleName);

	private ResLoaderUI()
	{
	}

	public static String getString(String key)
	{
		try { return (verBundle.getString(key)); }
		catch (MissingResourceException e)
		{
			try { return (RESOURCE_BUNDLE.getString(key)); }
			catch (MissingResourceException e2)
			{
				return '!' + key + '!';
			}
		}
	}
}
