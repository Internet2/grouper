/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/PermissionXml.java,v 1.1 2008-05-17 20:54:09 ddonn Exp $

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
import edu.internet2.middleware.signet.PermissionImpl;
import edu.internet2.middleware.signet.ProxyImpl;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;
import edu.internet2.middleware.signet.util.xml.adapter.PermissionImplXa;
import edu.internet2.middleware.signet.util.xml.adapter.PrivilegeXa;
import edu.internet2.middleware.signet.util.xml.adapter.ProxyImplRefXa;
import edu.internet2.middleware.signet.util.xml.adapter.SignetSubjectRefXa;
import edu.internet2.middleware.signet.util.xml.adapter.SignetXa;
import edu.internet2.middleware.signet.util.xml.binder.PermissionImplXb;
import edu.internet2.middleware.signet.util.xml.binder.PermissionsDocXb;
import edu.internet2.middleware.signet.util.xml.binder.PrivilegeXb;
import edu.internet2.middleware.signet.util.xml.binder.ProxyImplRefXb;
import edu.internet2.middleware.signet.util.xml.binder.SignetXb;

/**
 * PermissionXml - A class to export a Signet Permission to XML based on
 * Command parameters. <br>
 * Typical usage: new PermissionXml(mySignet).exportPermission(myCommand);
 * @see Command
 * @see PermissionImpl
 * @see PermissionImplXa
 * @see PermissionImplXb
 */
public class PermissionXml extends XmlUtil
{
	/** private default constructor */
	private PermissionXml()
	{
	}

	/**
	 * Constructor - Initialize Log and Signet instance variables
	 * @param signet A Signet instance
	 * @see Signet
	 */
	public PermissionXml(Signet signet)
	{
		this();
		log = LogFactory.getLog(PermissionXml.class);
		this.signet = signet;
	}

	/**
	 * Constructor - Initialize Log and Signet instance variables, then
	 * export Permission based on parameters in Command
	 * @param signet A Signet instance
	 * @param cmd A Command object containing export parameters
	 * @see Command
	 * @see Signet
	 */
	public PermissionXml(Signet signet, Command cmd)
	{
		this(signet);
		exportPermission(cmd);
	}

	/**
	 * Perform the XML export of this Permission
	 * @param cmd A Command object containing export parameters
	 * @see Command
	 */
	public void exportPermission(Command cmd)
	{
		Status status = null;
		String[] subjIds = null;

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
			else
			{
				log.error("Invalid Parameter (" + key + ") in command - " + cmd.toString());
				return;
			}
		}

		if (2 <= argCount)
		{
			log.error("Too many Permission parameters specified. May only be one of " +
					Command.PARAM_SUBJID);
			return;
		}

		SignetXa adapter = new SignetXa(signet);
		SignetXb xml = adapter.getXmlSignet();
		List<PermissionsDocXb> xmlPermDocs = xml.getPermissionsDoc(); // get the empty list

		if ((null != subjIds) && (0 < subjIds.length))
		{
			String statusStr = (null == status) ? null : status.getName();

			for (String subjId : subjIds)
			{
				SignetSubject subj = signet.getSubjectByIdentifier(subjId);
				if (null != subj)
				{
					PermissionsDocXb permDoc = new PermissionsDocXb();

					// export the SubjectRef
					permDoc.setSubject(new SignetSubjectRefXa(subj, signet).getXmlSubject());

					List<PrivilegeXb> privList = permDoc.getPermission(); // get the empty list
					Set<AssignmentImpl> privs = subj.getAssignmentsReceived(statusStr);
					for (AssignmentImpl priv : privs)
					{
						PrivilegeXb xmlPriv = new PrivilegeXa(priv, signet).getXmlPrivilege();
						privList.add(xmlPriv);
					}
//					// export the Permissions
//					List<PermissionImplRefXb> permList = permDoc.getPermission(); // get the empty list
//					Set<PrivilegeImpl> privs = (Set<PrivilegeImpl>)subj.getPrivileges(statusStr);
//					for (PrivilegeImpl priv : privs)
//					{
//						PermissionImpl perm = (PermissionImpl)priv.getPermission();
//						PermissionImplRefXb xmlPerm = new PermissionImplRefXa(perm, signet).getXmlPermission();
//						permList.add(xmlPerm);
//					}

					// export the Proxies
					List<ProxyImplRefXb> proxyList = permDoc.getProxy(); // get the empty list
					Set<ProxyImpl> proxies = subj.getProxiesReceived(statusStr);
					for (ProxyImpl proxy : proxies)
					{
						ProxyImplRefXb xmlProxy = new ProxyImplRefXa(proxy, signet).getXmlProxyRef();
						proxyList.add(xmlProxy);
					}

					xmlPermDocs.add(permDoc);
				}
				else
					log.error("No Subject with ID=" + subjId + " found during export");
			}
			marshalXml(xml, cmd.getOutFile());
		}
		else
			log.error("At least one subjId is required");
	}

}
