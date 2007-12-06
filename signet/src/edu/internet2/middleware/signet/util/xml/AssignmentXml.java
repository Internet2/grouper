/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/AssignmentXml.java,v 1.1 2007-12-06 01:18:32 ddonn Exp $

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
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.LogFactory;
import edu.internet2.middleware.signet.AssignmentImpl;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;
import edu.internet2.middleware.signet.util.xml.adapter.AssignmentImplXa;
import edu.internet2.middleware.signet.util.xml.adapter.AssignmentSetXa;
import edu.internet2.middleware.signet.util.xml.adapter.SignetXa;
import edu.internet2.middleware.signet.util.xml.binder.AssignmentImplXb;
import edu.internet2.middleware.signet.util.xml.binder.SignetXb;

/**
 * AssignmentXml - A class to export a Signet Assignment to XML based on
 * Command parameters. <br>
 * Typical usage: new AssignmentXml(mySignet).exportAssignment(myCommand);
 * @see Command
 * @see AssignmentImpl
 * @see AssignmentImplXa
 * @see AssignmentImplXb
 */
public class AssignmentXml extends XmlUtil
{
	/** private default constructor */
	private AssignmentXml()
	{
	}

	/**
	 * Constructor - Initialize Log and Signet instance variables
	 * @param signet A Signet instance
	 * @see Signet
	 */
	public AssignmentXml(Signet signet)
	{
		this();
		log = LogFactory.getLog(AssignmentXml.class);
		this.signet = signet;
	}

	/**
	 * Constructor - Initialize Log and Signet instance variables, then
	 * export Assignment based on parameters in Command
	 * @param signet A Signet instance
	 * @param cmd A Command object containing export parameters
	 * @see Command
	 * @see Signet
	 */
	public AssignmentXml(Signet signet, Command cmd)
	{
		this(signet);
		exportAssignment(cmd);
	}

	/**
	 * Perform the XML export of this Assignment
	 * @param cmd A Command object containing export parameters
	 * @see Command
	 */
	public void exportAssignment(Command cmd)
	{
		Status status = null;
		String[] subjIds = null;
		String[] functionIds = null;
		String[] scopeIds = null;
		String[] subsysIds = null;

		Hashtable<String, String> params = cmd.getParams();
		int argCount = 0;
		for (String key : params.keySet())
		{
			if (key.equalsIgnoreCase(Command.PARAM_STATUS))
				status = (Status)Status.getInstanceByName(params.get(key));
			else if (key.equalsIgnoreCase(Command.PARAM_SUBJID))
			{
				subjIds = parseList(params.get(key));
				argCount++;
			}
			else if (key.equalsIgnoreCase(Command.PARAM_FUNCID))
			{
				functionIds = parseList(params.get(key));
				argCount++;
			}
			else if (key.equalsIgnoreCase(Command.PARAM_SCOPEID))
			{
				scopeIds = parseList(params.get(key));
				argCount++;
			}
			else if (key.equalsIgnoreCase(Command.PARAM_SUBSYSID))
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
					Command.PARAM_FUNCID + ", " + Command.PARAM_SCOPEID + ", " +
					Command.PARAM_SUBJID + ", or " + Command.PARAM_SUBSYSID +
					", or no parameter for All records.");
			return;
		}

		SignetXa adapter = new SignetXa(signet);
		SignetXb xml = adapter.getXmlSignet();
		List<AssignmentImplXb> xmlAssignList = xml.getAssignment();

		if ((null != subjIds) && (0 < subjIds.length))
		{
			for (String subjId : subjIds)
			{
				SignetSubject subj = signet.getSubjectByIdentifier(subjId);
				if (null != subj)
				{
					Set<AssignmentImpl> assigns;
					if (null != status)
						assigns = subj.getAssignmentsReceived(status.getName());
					else
						assigns = subj.getAssignmentsReceived();
					AssignmentSetXa set = new AssignmentSetXa(assigns, signet);
					xmlAssignList.addAll(set.getXmlAssignments().getAssignments());
				}
				else
					log.error("No Subject found during export with ID=" + subjId);
			}
			marshalXml(xml, cmd.getOutFile());
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
				xmlAssignList.addAll(set.getXmlAssignments().getAssignments());
			}
			marshalXml(xml, cmd.getOutFile());
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
				xmlAssignList.addAll(set.getXmlAssignments().getAssignments());
			}
			marshalXml(xml, cmd.getOutFile());
		}
		else if ((null != subsysIds) && (0 < subsysIds.length))
		{
			HibernateDB hibr = signet.getPersistentDB();
			String statusStr = (null != status) ? status.toString() : null;
			for (String subsysId : subsysIds)
			{
				Set<AssignmentImpl> assigns = hibr.getAssignmentsBySubsystem(subsysId, statusStr);
				AssignmentSetXa set = new AssignmentSetXa(assigns, signet);
				xmlAssignList.addAll(set.getXmlAssignments().getAssignments());
			}
			marshalXml(xml, cmd.getOutFile());
		}
		else // export ALL assignments
		{
			HibernateDB hibr = signet.getPersistentDB();
			String statusStr = (null != status) ? status.toString() : null;
			Set<AssignmentImpl> assigns = hibr.getAssignments(statusStr);
			AssignmentSetXa set = new AssignmentSetXa(assigns, signet);
			xmlAssignList.addAll(set.getXmlAssignments().getAssignments());
			marshalXml(xml, cmd.getOutFile());
		}
	}

}
