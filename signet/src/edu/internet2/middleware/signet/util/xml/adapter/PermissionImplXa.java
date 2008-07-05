/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/PermissionImplXa.java,v 1.4 2008-07-05 01:22:17 ddonn Exp $

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

import java.util.List;
import java.util.Set;
import org.hibernate.Session;
import edu.internet2.middleware.signet.LimitImpl;
import edu.internet2.middleware.signet.PermissionImpl;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SubsystemImpl;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.util.xml.binder.LimitImplRefXb;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;
import edu.internet2.middleware.signet.util.xml.binder.PermissionImplXb;

/**
 * PermissionImplXa<p>
 * Adapter class for Signet XML Binding.
 * Maps a PermissionImpl and a PermissionImplXb.
 * @see PermissionImpl
 * @see PermissionImplXb
 */
public class PermissionImplXa extends EntityImplXa
{
	/**
	 * default constructor
	 */
	public PermissionImplXa()
	{
		super();
	}

	/**
	 * Constructor - initialize the signet value only
	 * @param signet An instance of Signet
	 */
	public PermissionImplXa(Signet signet)
	{
		super(signet);
	}

	/**
	 * Constructor - Initialize this adapter with the given Signet Permission,
	 * and initialize an XML binder for it
	 * @param signetPermission The Signet Permission
	 * @param signet An instance of Signet
	 */
	public PermissionImplXa(PermissionImpl signetPermission, Signet signet)
	{
		this(signet);
		signetEntity = signetPermission;
		xmlEntity = new ObjectFactory().createPermissionImplXb();
		setValues(signetPermission);
	}

	/**
	 * Constructor - Initialize this adapter with the given XML binder,
	 * and initialize a Signet Permission for it
	 * @param xmlPermission The Permission XML binder
	 * @param signet An instance of Signet
	 */
	public PermissionImplXa(PermissionImplXb xmlPermission, Signet signet)
	{
		this(signet);
		xmlEntity = xmlPermission;
		signetEntity = new PermissionImpl();
		setValues(xmlPermission);
	}


	/**
	 * @return The Signet Permission
	 */
	public PermissionImpl getSignetPermission()
	{
		return ((PermissionImpl)signetEntity);
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
		super.setValues(signetPermission);

		PermissionImplXb xmlPermission = (PermissionImplXb)xmlEntity;

//	private Integer     key;
		xmlPermission.setKey(signetPermission.getKey());

//	private Subsystem	subsystem;
		xmlPermission.setSubsystemId(signetPermission.getSubsystem().getId());

//	private Set			limits;
		// get the (empty) list of XML limits
		List<LimitImplRefXb> xmlLimits = xmlPermission.getLimit();
		for (LimitImpl sigLimit : (Set<LimitImpl>)signetPermission.getLimits())
			xmlLimits.add(new LimitImplRefXa(sigLimit, signet).getXmlLimitImplRef());
	}


	/**
	 * @return The XML binder
	 */
	public PermissionImplXb getXmlPermission()
	{
		return ((PermissionImplXb)xmlEntity);
	}

	/**
	 * Initialize the Signet Permission (previous created) with the values in
	 * the XML binder
	 * @param xmlPermission The XML binder
	 * @param signet An instance of Signet
	 */
	public void setValues(PermissionImplXb xmlPermission, Signet signet)
	{
		this.signet = signet;
		setValues(xmlPermission);
	}

	/**
	 * Initialize the Signet Permission (previous created) with the values in
	 * the XML binder
	 * @param xmlPermission The XML binder
	 */
	public void setValues(PermissionImplXb xmlPermission)
	{
		super.setValues(xmlPermission);

		PermissionImpl sigPerm = (PermissionImpl)signetEntity;

//	private Integer     key;
		sigPerm.setKey(new Integer(xmlPermission.getKey()));

		HibernateDB hibr = signet.getPersistentDB();
		Session hs = hibr.openSession();

//	private Subsystem	subsystem;
		SubsystemImpl subsys = (SubsystemImpl)hibr.load(hs, SubsystemImpl.class, xmlPermission.getSubsystemId());
		sigPerm.setSubsystem(subsys);

//	private Set			limits;
		for (LimitImplRefXb xmlLimit : xmlPermission.getLimit())
		{
			LimitImpl sigLimit = hibr.getLimit(xmlLimit.getKey());
			sigPerm.addLimit(sigLimit);
		}
		hibr.closeSession(hs);
	}

}
