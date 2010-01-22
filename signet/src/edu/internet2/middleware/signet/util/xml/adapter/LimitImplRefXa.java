/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/LimitImplRefXa.java,v 1.5 2008-09-29 00:48:45 ddonn Exp $

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
import org.apache.commons.logging.LogFactory;
import edu.internet2.middleware.signet.LimitImpl;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.util.xml.binder.LimitImplRefXb;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;

/**
 * LimitImplRefXa<p>
 * Adapter class for Signet XML Binding.
 * Maps a LimitImpl and a LimitImplRefXb
 * @see LimitImpl
 * @see LimitImplRefXb
 */
public class LimitImplRefXa
{
	protected Signet			signet;
	protected LimitImpl			signetLimitImpl;
	protected LimitImplRefXb	xmlLimitImplRef;
	/** logging */
	private static Log			log = LogFactory.getLog(LimitImplRefXa.class);


	/**
	 * Constructor
	 */
	public LimitImplRefXa()
	{
	}

	/**
	 * Constructor
	 * @param signet
	 */
	public LimitImplRefXa(Signet signet)
	{
		this();
		this.signet = signet;
	}

	/**
	 * Constructor
	 * @param signetLimitImpl
	 * @param signet
	 */
	public LimitImplRefXa(LimitImpl signetLimitImpl, Signet signet)
	{
		this(signet);
		this.signetLimitImpl = signetLimitImpl;
		xmlLimitImplRef = new ObjectFactory().createLimitImplRefXb();
		setValues(signetLimitImpl);
	}

	/**
	 * Constructor
	 * @param xmlLimitImpl
	 * @param signet
	 */
	public LimitImplRefXa(LimitImplRefXb xmlLimitImpl, Signet signet)
	{
		this(signet);
		this.xmlLimitImplRef = xmlLimitImpl;
		signetLimitImpl = new LimitImpl();
		setValues(xmlLimitImpl);
	}

	/**
	 * Get the limit from this adapter
	 * @return the limit
	 */
	public LimitImpl getSignetLimitImpl()
	{
		return (signetLimitImpl);
	}

	/**
	 * Set the limit
	 * @param signetLimitImpl
	 */
	public void setValues(LimitImpl signetLimitImpl)
	{
//	protected Integer			key;
		xmlLimitImplRef.setKey(signetLimitImpl.getKey().intValue());

//	protected String			subsystemId;
		xmlLimitImplRef.setSubsystemId(signetLimitImpl.getSubsystem().getId());

//	protected String			id;
		xmlLimitImplRef.setId(signetLimitImpl.getId());

	}

	/**
	 * Get the XML limit from this adapter
	 * @return the XML limit impl ref
	 */
	public LimitImplRefXb getXmlLimitImplRef()
	{
		return (xmlLimitImplRef);
	}

	/**
	 * Set the limit
	 * @param xmlLimitImpl
	 */
	public void setValues(LimitImplRefXb xmlLimitImpl)
	{
		signetLimitImpl = null;

		if (null == signet)
		{
			log.error("Unable to lookup Limit (PK=" +
					xmlLimitImpl.getKey() +
					") because no Signet instance is available");
			return;
		}

		HibernateDB hibr = signet.getPersistentDB();
		if (null != hibr)
		{
			int limit_pk = xmlLimitImpl.getKey();
			if (0 < limit_pk)
			{
				signetLimitImpl = hibr.getLimit(limit_pk);
				if (null == signetLimitImpl)
					log.warn("No Limit found with primary key = " + limit_pk);
			}
			else
				log.warn("Invalid Limit primary key \"" + limit_pk + "\" specified");
			if (null == signetLimitImpl)
			{
				log.warn("Attempting to find Limit by Subsystem and LimitId...");

				String subsysId = xmlLimitImpl.getSubsystemId();
				String limitId = xmlLimitImpl.getId();
				signetLimitImpl = hibr.getLimit(subsysId, limitId);

				StringBuffer buf = new StringBuffer();
				if (null == signetLimitImpl)
					buf.append("No ");
				buf.append("Limit found for SubsystemId \"" + subsysId + "\" and LimitId \"" + limitId + "\"");
				log.warn(buf.toString());
			}
		}
		else
			log.error("Unable to lookup Limit: no HibernateDB instance is available");
	}

}
