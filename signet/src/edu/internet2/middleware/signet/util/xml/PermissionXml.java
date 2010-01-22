/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/PermissionXml.java,v 1.4 2008-09-27 01:02:09 ddonn Exp $

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

import java.io.OutputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
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
 * CommandArg parameters. <br>
 * Typical usage: new PermissionXml(mySignet).exportPermission(myCommand);
 * @see CommandArg
 * @see PermissionImpl
 * @see PermissionImplXa
 * @see PermissionImplXb
 */
public class PermissionXml extends XmlUtil
{
	/** logging */
	private static Log	log = LogFactory.getLog(PermissionXml.class);


	/** private default constructor */
	private PermissionXml()
	{
	}

	/**
	 * Constructor - Initialize Signet instance variables
	 * @param signetXmlAdapter A SignetXa instance
	 * @see SignetXa
	 */
	public PermissionXml(SignetXa signetXmlAdapter)
	{
		this();
		this.signetXmlAdapter = signetXmlAdapter;
		this.signet = signetXmlAdapter.getSignet();
	}

	/**
	 * Constructor - Initialize Signet instance variables, then
	 * export Permission based on parameters in CommandArg
	 * @param signetXmlAdapter A SignetXa instance
	 * @param cmd A CommandArg object containing export parameters
	 * @see CommandArg
	 * @see Signet
	 */
	public PermissionXml(SignetXa signetXmlAdapter, CommandArg cmd)
	{
		this(signetXmlAdapter);
		buildXml(cmd);
	}

	/**
	 * Build the XML of this Permission
	 * @param cmd A CommandArg object containing export parameters
	 * @see CommandArg
	 */
	public void buildXml(CommandArg cmd)
	{
		Status status = null;
		String[] subjIds = null;

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
			else
			{
				log.error("Invalid Parameter (" + key + ") in command - " + cmd.toString());
				return;
			}
		}

		if (2 <= argCount)
		{
			log.error("Too many Permission parameters specified. May only be one of " +
					CommandArg.PARAM_SUBJID);
			return;
		}

		List<PermissionsDocXb> xmlPermDocs = signetXmlAdapter.getXmlSignet().getPermissions();

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
//			marshalXml(xml, cmd.getOutFile());
		}
		else
			log.error("At least one subjId is required");
	}

	/**
	 * Perform the XML export of this Subject's Permissions. Note that this
	 * method is for support of the PermissionsXMLServlet in Signet UI.
	 * @param subj Export the Subject's permissions
	 * @param status Filter the output for one of ACTIVE, INACTIVE, PENDING.
	 * If null, no filtering occurs.
	 * @param outFile The destination of the output. If null, uses stdout
	 */
	public void exportPermission(SignetSubject subj, Status status, OutputStream outFile)
	{
		if (null == subj)
			return;
		if (null == outFile)
			outFile = System.out;

		SignetXa adapter = new SignetXa(signet);
		SignetXb xml = adapter.getXmlSignet();
		List<PermissionsDocXb> xmlPermDocs = xml.getPermissions(); // get the empty list

		String statusStr = (null == status) ? null : status.getName();

		PermissionsDocXb permDoc = new PermissionsDocXb();

		// export the SubjectRef
		permDoc.setSubject(new SignetSubjectRefXa(subj, signet).getXmlSubject());

		List<PrivilegeXb> privList = permDoc.getPermission(); // get the empty list
		Set<AssignmentImpl> privs = subj.getAssignmentsReceived(statusStr);

		// export the assigned privileges
		for (AssignmentImpl priv : privs)
		{
			PrivilegeXb xmlPriv = new PrivilegeXa(priv, signet).getXmlPrivilege();
			privList.add(xmlPriv);
		}

		// export the Proxies
		List<ProxyImplRefXb> proxyList = permDoc.getProxy(); // get the empty list
		Set<ProxyImpl> proxies = subj.getProxiesReceived(statusStr);
		for (ProxyImpl proxy : proxies)
		{
			ProxyImplRefXb xmlProxy = new ProxyImplRefXa(proxy, signet).getXmlProxyRef();
			proxyList.add(xmlProxy);
		}

		xmlPermDocs.add(permDoc);

		marshalXml(xml, outFile);
	}

}
