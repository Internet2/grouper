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

package edu.internet2.middleware.grouper.ui.util;

import java.io.Serializable;
import java.util.*;

import edu.internet2.middleware.grouper.ui.UIThreadLocal;

/**
 * Given a list of bundles, looks for values in each individually until it finds
 * one
 * <p />
 * 
 * @author Gary Brown.
 * @version $Id: ChainedResourceBundle.java,v 1.1.1.1 2005-08-23 13:04:16 isgwb Exp $
 */

public class ChainedResourceBundle extends ResourceBundle implements
		Serializable {
	private ArrayList chain = new ArrayList();

	private String name = null;

	private String mapName = null;

	/**
	 * Constructor - ensures atleast one bundle!
	 * 
	 * @param bundle
	 *            Resource Bundle
	 * @param name
	 *            of bundle which can be referred to elsewhere
	 */
	public ChainedResourceBundle(ResourceBundle bundle, String name) {
		if (bundle == null)
			throw new NullPointerException();
		if (name == null)
			throw new NullPointerException();
		this.name = name;
		this.mapName = name + "Map";
		chain.add(bundle);
	}

	/**
	 * Extend the chain
	 * 
	 * @param bundle
	 *            to add
	 */
	public synchronized void addBundle(ResourceBundle bundle) {
		if (bundle == null)
			throw new NullPointerException();
		if (chain.contains(bundle))
			throw new IllegalArgumentException(
					"This bundle has already been added");
		chain.add(bundle);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ResourceBundle#handleGetObject(java.lang.String)
	 */
	protected Object handleGetObject(String key) {
		UIThreadLocal.put(name, key);
		ResourceBundle bundle = null;
		Object obj = null;
		for (int i = 0; i < chain.size(); i++) {
			bundle = (ResourceBundle) chain.get(i);
			try {
				obj = bundle.getString(key);
				if (obj != null) {
					UIThreadLocal.put(mapName, key, obj);
					break;
				}
			} catch (Exception e) {
			}
		}
		Boolean doShowResourcesInSitu = (Boolean) UIThreadLocal
				.get("doShowResourcesInSitu");
		if (doShowResourcesInSitu != null
				&& doShowResourcesInSitu.booleanValue()
				&& name.startsWith("nav"))
			return "???" + key + "???";
		return obj;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ResourceBundle#getKeys()
	 */
	public Enumeration getKeys() {
		Set keys = new HashSet();
		ResourceBundle bundle = null;
		Enumeration bundleKeys;
		for (int i = 0; i < chain.size(); i++) {
			bundle = (ResourceBundle) chain.get(i);
			bundleKeys = bundle.getKeys();
			while (bundleKeys.hasMoreElements()) {
				keys.add(bundleKeys.nextElement());
			}

		}
		Vector v = new Vector(keys);
		return v.elements();

	}
}