/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/ScopeTreeXml.java,v 1.2 2008-05-17 20:54:09 ddonn Exp $

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
import org.hibernate.Session;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.TreeImpl;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.util.xml.adapter.ScopeTreeXa;
import edu.internet2.middleware.signet.util.xml.adapter.SignetXa;
import edu.internet2.middleware.signet.util.xml.binder.ScopeTreeXb;
import edu.internet2.middleware.signet.util.xml.binder.SignetXb;

/**
 * ScopeTreeXml - A class to export a Signet Tree to XML based on
 * Command parameters. <br>
 * Typical usage: new ScopeTreeXml(mySignet).exportScopeTree(myCommand);
 * @see Command
 * @see TreeImpl
 * @see ScopeTreeXa
 * @see ScopeTreeXb
 * 
 */
public class ScopeTreeXml extends XmlUtil
{
	/** private default constructor */
	private ScopeTreeXml()
	{
	}

	/**
	 * Constructor - Initialize Log and Signet instance variables
	 * @param signet A Signet instance
	 * @see Signet
	 */
	public ScopeTreeXml(Signet signet)
	{
		this();
		log = LogFactory.getLog(ScopeTreeXml.class);
		this.signet = signet;
	}

	/**
	 * Constructor - Initialize Log and Signet instance variables, then
	 * export ScopeTree based on parameters in Command
	 * @param signet A Signet instance
	 * @param cmd A Command object containing export parameters
	 * @see Command
	 * @see Signet
	 */
	public ScopeTreeXml(Signet signet, Command cmd)
	{
		this(signet);
		exportScopeTree(cmd);
	}

	/**
	 * Perform the XML export of this ScopeTree
	 * @param cmd A Command object containing export parameters
	 * @see Command
	 */
	public void exportScopeTree(Command cmd)
	{
//		Status status = null;
//		String[] subjIds = null;
//		String[] functionIds = null;
		String[] scopeIds = null;
		String[] subsysIds = null;

		Hashtable<String, String> params = cmd.getParams();
		int argCount = 0;
		for (String key : params.keySet())
		{
//			if (key.equalsIgnoreCase(Command.PARAM_STATUS))
//				status = (Status)Status.getInstanceByName(params.get(key));
//			else if (key.equalsIgnoreCase(Command.PARAM_SUBJID))
//			{
//				subjIds = parseList(params.get(key));
//				argCount++;
//			}
//			else if (key.equalsIgnoreCase(Command.PARAM_FUNCID))
//			{
//				functionIds = parseList(params.get(key));
//				argCount++;
//			}
			if (key.equalsIgnoreCase(Command.PARAM_SCOPEID))
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
			log.error("Too many ScopeTree parameters specified. May only be one of " +
//					Command.PARAM_FUNCID + ", " +
					Command.PARAM_SCOPEID + ", " +
//					Command.PARAM_SUBJID + ", or " +
					Command.PARAM_SUBSYSID + ", " +
					"or no parameter for All records.");
			return;
		}

		SignetXa adapter = new SignetXa(signet);
		SignetXb xml = adapter.getXmlSignet();
		List<ScopeTreeXb> xmlScopeTreeList = xml.getScopeTree();
		HibernateDB hibr = signet.getPersistentDB();
		Session hs = hibr.openSession();

		if ((null != scopeIds) && (0 < scopeIds.length))
		{
			for (String id : scopeIds)
			{
				TreeImpl tree = (TreeImpl)hibr.getTreeById(hs, id);
				if (null != tree)
					xmlScopeTreeList.add(new ScopeTreeXa(tree, signet).getXmlScopeTree());
				else
					log.warn("No ScopeTree found for ScopeTreeId=\"" + id + "\"");
			}
		}
		else if ((null != subsysIds) && (0 < subsysIds.length))
		{
			for (String id : subsysIds)
			{
				TreeImpl tree = (TreeImpl)hibr.getTreeBySubsystemId(hs, id);
				if (null != tree)
					xmlScopeTreeList.add(new ScopeTreeXa(tree, signet).getXmlScopeTree());
				else
					log.warn("No ScopeTree found for SubsystemId=\"" + id + "\"");
			}
		}
		else // export ALL ScopeTrees
		{
			Set trees = hibr.getTrees(hs);
			for (TreeImpl tree : (Set<TreeImpl>)trees)
				xmlScopeTreeList.add(new ScopeTreeXa(tree, signet).getXmlScopeTree());
		}

		marshalXml(xml, cmd.getOutFile());

		hibr.closeSession(hs);

	}

}
