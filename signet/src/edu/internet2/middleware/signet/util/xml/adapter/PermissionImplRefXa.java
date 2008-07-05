/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/PermissionImplRefXa.java,v 1.2 2008-07-05 01:22:17 ddonn Exp $

Copyright (c) 2007 Internet2, Stanford University

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
package edu.internet2.middleware.signet.util.xml.adapter;

import org.apache.commons.logging.Log;
import edu.internet2.middleware.signet.PermissionImpl;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;
import edu.internet2.middleware.signet.util.xml.binder.PermissionImplRefXb;

/**
 * PermissionImplRefXa<p>
 * Adapter class for Signet XML Binding.
 * Maps a PermissionImpl and a PermissionImplRefXb.
 * @see PermissionImpl
 * @see PermissionImplRefXb
 */
public class PermissionImplRefXa
{
	protected Signet				signet;
	protected PermissionImpl		signetPermission;
	protected PermissionImplRefXb	xmlPermission;
	protected Log					log;

	/**
	 * default constructor
	 */
	public PermissionImplRefXa()
	{
		super();
	}

	/**
	 * Constructor - initialize the signet value only
	 * @param signet An instance of Signet
	 */
	public PermissionImplRefXa(Signet signet)
	{
		this.signet = signet;
	}

	/**
	 * Constructor - Initialize this adapter with the given Signet Permission,
	 * and initialize an XML binder for it
	 * @param signetPermission The Signet Permission
	 * @param signet An instance of Signet
	 */
	public PermissionImplRefXa(PermissionImpl signetPermission, Signet signet)
	{
		this(signet);
		this.signetPermission = signetPermission;
		xmlPermission = new ObjectFactory().createPermissionImplRefXb();
		setValues(signetPermission);
	}

	/**
	 * Constructor - Initialize this adapter with the given XML binder,
	 * and initialize a Signet Permission for it
	 * @param xmlPermission The Permission XML binder
	 * @param signet An instance of Signet
	 */
	public PermissionImplRefXa(PermissionImplRefXb xmlPermission, Signet signet)
	{
		this(signet);
		this.xmlPermission = xmlPermission;
		signetPermission = new PermissionImpl();
		setValues(xmlPermission);
	}


	/**
	 * @return The Signet Permission
	 */
	public PermissionImpl getSignetPermission()
	{
		return (signetPermission);
	}

	/**
	 * Initialize the XML binder (previously created) with the values of the
	 * Signet Permission
	 * @param signetPermission The Signet Permission
	 * @param signet An instance of Signet
	 */
	public void setValues(PermissionImpl signetPermission, Signet signet)
	{
		this.signet = signet;
		setValues(signetPermission);
	}

	/**
	 * Initialize the XML binder (previously created) with the values of the
	 * Signet Permission
	 * @param signetPermission The Signet Permission
	 */
	public void setValues(PermissionImpl signetPermission)
	{
//	private Integer     key;
		xmlPermission.setKey(signetPermission.getKey().intValue());

//	private String		name;
		xmlPermission.setId(signetPermission.getId());

//	private Subsystem	subsystem;
		xmlPermission.setSubsystemId(signetPermission.getSubsystem().getId());

	}


	/**
	 * @return The XML binder
	 */
	public PermissionImplRefXb getXmlPermission()
	{
		return (xmlPermission);
	}

	/**
	 * Initialize the Signet Permission (previous created) with the values in
	 * the XML binder
	 * @param xmlPermission The XML binder
	 * @param signet An instance of Signet
	 */
	public void setValues(PermissionImplRefXb xmlPermission, Signet signet)
	{
		this.signet = signet;
		setValues(xmlPermission);
	}

	/**
	 * Since this is a permission reference, do a DB lookup
	 * @param xmlPermission The XML binder
	 */
	public void setValues(PermissionImplRefXb xmlPermission)
	{
		HibernateDB hibr = signet.getPersistentDB();
		signetPermission = hibr.getPermissionById(xmlPermission.getKey());
	}

}
