/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/AssignmentXml.java,v 1.4 2008-09-27 01:02:09 ddonn Exp $

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
package edu.internet2.middleware.signet.util.xml;

import java.util.Hashtable;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.internet2.middleware.signet.AssignmentImpl;
import edu.internet2.middleware.signet.ProxyImpl;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;
import edu.internet2.middleware.signet.util.xml.adapter.AssignmentImplXa;
import edu.internet2.middleware.signet.util.xml.adapter.AssignmentSetXa;
import edu.internet2.middleware.signet.util.xml.adapter.ProxySetXa;
import edu.internet2.middleware.signet.util.xml.adapter.SignetXa;
import edu.internet2.middleware.signet.util.xml.binder.AssignmentImplXb;
import edu.internet2.middleware.signet.util.xml.binder.AssignmentSetXb;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;
import edu.internet2.middleware.signet.util.xml.binder.ProxySetXb;
import edu.internet2.middleware.signet.util.xml.binder.SignetXb;

/**
 * AssignmentXml - A class to export a Signet Assignment to XML based on
 * CommandArg parameters. <br>
 * Typical usage: new AssignmentXml(mySignet).exportAssignment(myCommand);
 * @see CommandArg
 * @see AssignmentImpl
 * @see AssignmentImplXa
 * @see AssignmentImplXb
 */
public class AssignmentXml extends XmlUtil
{
	/** logging */
	private static Log	log = LogFactory.getLog(AssignmentXml.class);


	/** private default constructor */
	private AssignmentXml()
	{
	}

//	/**
//	 * Constructor - Initialize Log and Signet instance variables
//	 * @param signet A Signet instance
//	 * @see Signet
//	 */
//	public AssignmentXml(Signet signet)
//	{
//		this();
//		log = LogFactory.getLog(AssignmentXml.class);
//		this.signet = signet;
//	}
//
	/**
	 * Constructor - Initialize SignetXmlAdapter instance variables
	 * @param signetXmlAdapter A SignetXa instance
	 * @see SignetXa
	 */
	public AssignmentXml(SignetXa signetXmlAdapter)
	{
		this();
		this.signetXmlAdapter = signetXmlAdapter;
		this.signet = signetXmlAdapter.getSignet();
	}

	/**
	 * Constructor - Initialize SignetXmlAdapter instance variables,
	 * then add Assignment export data to signetXmlAdapter, based on parameters
	 * in CommandArg
	 * @param signetXmlAdapter A SignetXa instance
	 * @param cmd A CommandArg object containing export parameters
	 * @see CommandArg
	 * @see SignetXa
	 */
	public AssignmentXml(SignetXa signetXmlAdapter, CommandArg cmd)
	{
		this(signetXmlAdapter);
		buildXml(cmd);
//		exportAssignment(cmd);
	}

//	/**
//	 * Constructor - Initialize Signet instance variables, then
//	 * export Assignment based on parameters in CommandArg
//	 * @param signet A Signet instance
//	 * @param cmd A CommandArg object containing export parameters
//	 * @see CommandArg
//	 * @see Signet
//	 */
//	public AssignmentXml(Signet signet, CommandArg cmd)
//	{
//		this(signet);
//		exportAssignment(cmd);
//	}
//
	/**
	 * Construct the XML binders for the given CommandArg
	 * @param cmd A CommandArg object containing export parameters
	 * @see CommandArg
	 */
	public void buildXml(CommandArg cmd)
	{
		if ((null == signetXmlAdapter) || (null == cmd))
			return;

		Status status = null;
		String[] subjIds = null;
		String[] functionIds = null;
		String[] scopeIds = null;
		String[] subsysIds = null;

		Hashtable<String, String> params = cmd.getParams();
		int argCount = 0;
		for (String key : params.keySet())
		{
			if (key.equalsIgnoreCase(CommandArg.PARAM_STATUS))
				status = (Status)Status.getInstanceByName(params.get(key));
			else if (key.equalsIgnoreCase(CommandArg.PARAM_SUBJID))
			{
				subjIds = parseList(params.get(key));
				argCount++;
			}
			else if (key.equalsIgnoreCase(CommandArg.PARAM_FUNCID))
			{
				functionIds = parseList(params.get(key));
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
			log.error("Too many Assignment parameters specified. May only be one of " +
					CommandArg.PARAM_FUNCID + ", " + CommandArg.PARAM_SCOPEID + ", " +
					CommandArg.PARAM_SUBJID + ", or " + CommandArg.PARAM_SUBSYSID +
					", or no parameter for All records.");
			return;
		}

		SignetXb xml = signetXmlAdapter.getXmlSignet();

		AssignmentSetXb xmlAssignSet;
		if (null == (xmlAssignSet = xml.getAssignmentSet()))
		{
			xmlAssignSet = new ObjectFactory().createAssignmentSetXb();
			xml.setAssignmentSet(xmlAssignSet);
		}

		ProxySetXb xmlProxySet;
		if (null == (xmlProxySet = xml.getProxySet()))
		{
			xmlProxySet = new ObjectFactory().createProxySetXb();
			xml.setProxySet(xmlProxySet);
		}

		if ((null != subjIds) && (0 < subjIds.length))
		{
			for (String subjId : subjIds)
			{
				SignetSubject subj = signet.getSubjectByIdentifier(subjId);
				if (null != subj)
				{
					Set<ProxyImpl> proxies;
					Set<AssignmentImpl> assigns;
					if (null != status)
					{
						proxies = subj.getProxiesReceived(status.getName());
						assigns = subj.getAssignmentsReceived(status.getName());
					}
					else
					{
						proxies = subj.getProxiesReceived();
						assigns = subj.getAssignmentsReceived();
					}
					ProxySetXa pSet = new ProxySetXa(proxies, signet);
					xmlProxySet.getProxy().addAll(pSet.getXmlProxies().getProxy());

					AssignmentSetXa aSet = new AssignmentSetXa(assigns, signet);
					xmlAssignSet.getAssignment().addAll(aSet.getXmlAssignments().getAssignment());
				}
				else
					log.error("No Subject found during export with ID=" + subjId);
			}
		}
		else if ((null != functionIds) && (0 < functionIds.length))
		{
			HibernateDB hibr = signet.getPersistentDB();
			String statusStr = (null != status) ? status.toString() : null;
			for (String functionId : functionIds)
			{
				String[] subfunc = parsePair(functionId);
				Set<AssignmentImpl> assigns =
						hibr.getAssignmentsByFunction(subfunc, statusStr);
				AssignmentSetXa set = new AssignmentSetXa(assigns, signet);
				xmlAssignSet.getAssignment().addAll(set.getXmlAssignments().getAssignment());
			}
		}
		else if ((null != scopeIds) && (0 < scopeIds.length))
		{
			HibernateDB hibr = signet.getPersistentDB();
			String statusStr = (null != status) ? status.toString() : null;
			for (String scopeId : scopeIds)
			{
				String[] scopeAndNode = parsePair(scopeId);
				Set<AssignmentImpl> assigns =
						hibr.getAssignmentsByScope(scopeAndNode, statusStr);
				AssignmentSetXa set = new AssignmentSetXa(assigns, signet);
				xmlAssignSet.getAssignment().addAll(set.getXmlAssignments().getAssignment());
			}
		}
		else if ((null != subsysIds) && (0 < subsysIds.length))
		{
			HibernateDB hibr = signet.getPersistentDB();
			String statusStr = (null != status) ? status.toString() : null;
			for (String subsysId : subsysIds)
			{
				Set<AssignmentImpl> assigns = hibr.getAssignmentsBySubsystem(subsysId, statusStr);
				AssignmentSetXa set = new AssignmentSetXa(assigns, signet);
				xmlAssignSet.getAssignment().addAll(set.getXmlAssignments().getAssignment());
			}
		}
		else // export ALL assignments
		{
			HibernateDB hibr = signet.getPersistentDB();
			String statusStr = (null != status) ? status.toString() : null;

			Set<ProxyImpl> proxies = hibr.getProxies(statusStr);
			ProxySetXa pSet = new ProxySetXa(proxies, signet);
			xmlProxySet.getProxy().addAll(pSet.getXmlProxies().getProxy());

			Set<AssignmentImpl> assigns = hibr.getAssignments(statusStr);
			AssignmentSetXa aSet = new AssignmentSetXa(assigns, signet);
			xmlAssignSet.getAssignment().addAll(aSet.getXmlAssignments().getAssignment());
		}
	}

}
