/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Bristol
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Bristol nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Bristol, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Bristol, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
 * <p/>
 * 
 * @author Gary Brown.
 * @version $Id: TemplateResolverFactory.java,v 1.1.1.1 2005-08-23 13:04:14 isgwb Exp $
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