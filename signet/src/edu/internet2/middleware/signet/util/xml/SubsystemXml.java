/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/SubsystemXml.java,v 1.5 2008-09-27 01:02:09 ddonn Exp $

Copyright (c) 2008 Internet2, Stanford University

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
package edu.internet2.middleware.signet.util.xml;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.SubsystemImpl;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.util.xml.adapter.SignetXa;
import edu.internet2.middleware.signet.util.xml.adapter.SubsystemImplXa;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;
import edu.internet2.middleware.signet.util.xml.binder.SignetXb;
import edu.internet2.middleware.signet.util.xml.binder.SubsystemImplXb;
import edu.internet2.middleware.signet.util.xml.binder.SubsystemSetXb;

/**
 * SubsystemXml - A class to export a Signet Subsystem to XML based on
 * CommandArg parameters. <br>
 * Typical usage: new SubsystemXml(mySignet).exportSubsystem(myCommand);
 * @see CommandArg
 * @see SubsystemImpl
 * @see SubsystemImplXa
 * @see SubsystemImplXb
 */
public class SubsystemXml extends XmlUtil
{
	/** logging */
	private static Log	log = LogFactory.getLog(SubsystemXml.class);


	/** private default constructor */
	private SubsystemXml()
	{
	}

	/**
	 * Constructor - Initialize Signet instance variables
	 * @param signetXmlAdapter A SignetXa instance
	 * @see Signet
	 */
	public SubsystemXml(SignetXa signetXmlAdapter)
	{
		this();
		this.signetXmlAdapter = signetXmlAdapter;
		this.signet = signetXmlAdapter.getSignet();
	}

	/**
	 * Constructor - Initialize Signet instance variables, then
	 * export Assignment based on parameters in CommandArg
	 * @param signetXmlAdapter A SignetXa instance
	 * @param cmd A CommandArg object containing export parameters
	 * @see CommandArg
	 * @see SignetXa
	 */
	public SubsystemXml(SignetXa signetXmlAdapter, CommandArg cmd)
	{
		this(signetXmlAdapter);
		buildXml(cmd);
	}

	/**
	 * Build the XML of this Subsystem
	 * @param cmd A CommandArg object containing export parameters
	 * @see CommandArg
	 */
	public void buildXml(CommandArg cmd)
	{
		Status status = null;
		String[] names = null;
		String[] scopeIds = null;
		String[] subsysIds = null;

		Hashtable<String, String> params = cmd.getParams();
		int argCount = 0;
		for (String key : params.keySet())
		{
			if (key.equalsIgnoreCase(CommandArg.PARAM_STATUS))
				status = (Status)Status.getInstanceByName(params.get(key));
			else if (key.equalsIgnoreCase(CommandArg.PARAM_NAME))
			{
				names = parseList(params.get(key));
				argCount++;
			}
			else if (key.equalsIgnoreCase(CommandArg.PARAM_SCOPEID))
			{
				scopeIds = parseList(params.get(key));
				argCount++;
			}
			else if (key.equalsIgnoreCase(CommandArg.PARAM_SUBSYSID))
			{
				subsysIds = parseList(params.get(key));
				argCount++;
			}
			else
			{
				log.error("Invalid Parameter (" + key + ") in command - " + cmd.toString());
				return;
			}
		}

		if (2 <= argCount)
		{
			log.error("Too many Subsystem parameters specified. May only be one of " +
					CommandArg.PARAM_NAME +
					CommandArg.PARAM_SCOPEID + ", " +
					", or " +
					CommandArg.PARAM_SUBSYSID +
					", or no parameter for All records.");
			return;
		}

		SignetXb xml = signetXmlAdapter.getXmlSignet();
		// collect all Signet Subsystems first
		List<SubsystemImpl> signetSubsystemList = new Vector<SubsystemImpl>();

		if ((null != names) && (0 < names.length))
		{
			HibernateDB hibr = signet.getPersistentDB();
			Session hs = hibr.openSession();
			for (String subsysName : names)
				signetSubsystemList.addAll(hibr.getSubsystemsByName(hs, subsysName, status));
			hibr.closeSession(hs);
		}
		else if ((null != scopeIds) && (0 < scopeIds.length))
		{
			HibernateDB hibr = signet.getPersistentDB();
			Session hs = hibr.openSession();
			for (String scopeId : scopeIds)
				signetSubsystemList.addAll(hibr.getSubsystemsByScopeTree(hs, scopeId, status));
			hibr.closeSession(hs);
		}
		else if ((null != subsysIds) && (0 < subsysIds.length))
		{
			HibernateDB hibr = signet.getPersistentDB();
			Session hs = hibr.openSession();
			for (String subsysId : subsysIds)
			{
				SubsystemImpl subsys = (SubsystemImpl)hibr.getSubsystem(subsysId);
				if (null != subsys)
					signetSubsystemList.add(subsys);
			}
			hibr.closeSession(hs);
		}
		else // export ALL Subsystems
		{
			HibernateDB hibr = signet.getPersistentDB();
			Session hs = hibr.openSession();
			signetSubsystemList.addAll(hibr.getSubsystems(status));
			hibr.closeSession(hs);
		}

		// get the SubsystemSetXb
		SubsystemSetXb subsysSet;
		if (null == (subsysSet = xml.getSubsystemSet()))
		{
			subsysSet = new ObjectFactory().createSubsystemSetXb();
			xml.setSubsystemSet(subsysSet);
		}

		// after collecting all Signet Subsystems, create a binder for each
		List<SubsystemImplXb> subsysList = subsysSet.getSubsystem();
		for (SubsystemImpl subsys : signetSubsystemList)
		{
			SubsystemImplXb xmlSubsys = new SubsystemImplXa(subsys, signet).getXmlSubsystem();
			subsysList.add(xmlSubsys);
		}
	}

}
