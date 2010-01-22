/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/ScopeTreeXml.java,v 1.5 2008-09-27 01:02:09 ddonn Exp $

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import edu.internet2.middleware.signet.TreeImpl;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.util.xml.adapter.ScopeTreeXa;
import edu.internet2.middleware.signet.util.xml.adapter.SignetXa;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;
import edu.internet2.middleware.signet.util.xml.binder.ScopeTreeSetXb;
import edu.internet2.middleware.signet.util.xml.binder.ScopeTreeXb;

/**
 * ScopeTreeXml - A class to export a Signet Tree to XML based on
 * CommandArg parameters. <br>
 * Typical usage: new ScopeTreeXml(mySignet).exportScopeTree(myCommand);
 * @see CommandArg
 * @see TreeImpl
 * @see ScopeTreeXa
 * @see ScopeTreeXb
 * 
 */
public class ScopeTreeXml extends XmlUtil
{
	/** logging */
	private static Log	log = LogFactory.getLog(ScopeTreeXml.class);


	/** private default constructor */
	private ScopeTreeXml()
	{
	}

	/**
	 * Constructor - Initialize Signet instance variables
	 * @param signetXmlAdapter A SignetXa instance
	 * @see SignetXa
	 */
	public ScopeTreeXml(SignetXa signetXmlAdapter)
	{
		this();
		this.signetXmlAdapter = signetXmlAdapter;
		this.signet = signetXmlAdapter.getSignet();
	}

	/**
	 * Constructor - Initialize Signet instance variables, then
	 * add ScopeTree to SignetXa based on parameters in CommandArg
	 * @param signetXmlAdapter An instance of SignetXa
	 * @param cmd A CommandArg object containing export parameters
	 * @see CommandArg
	 * @see SignetXa
	 */
	public ScopeTreeXml(SignetXa signetXmlAdapter, CommandArg cmd)
	{
		this(signetXmlAdapter);
		buildXml(cmd);
	}

	/**
	 * Perform the XML export of this ScopeTree
	 * @param cmd A CommandArg object containing export parameters
	 * @see CommandArg
	 */
	public void buildXml(CommandArg cmd)
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
//			if (key.equalsIgnoreCase(CommandArg.PARAM_STATUS))
//				status = (Status)Status.getInstanceByName(params.get(key));
//			else if (key.equalsIgnoreCase(CommandArg.PARAM_SUBJID))
//			{
//				subjIds = parseList(params.get(key));
//				argCount++;
//			}
//			else if (key.equalsIgnoreCase(CommandArg.PARAM_FUNCID))
//			{
//				functionIds = parseList(params.get(key));
//				argCount++;
//			}
			if (key.equalsIgnoreCase(CommandArg.PARAM_SCOPEID))
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
			log.error("Too many ScopeTree parameters specified. May only be one of " +
//					CommandArg.PARAM_FUNCID + ", " +
					CommandArg.PARAM_SCOPEID + ", " +
//					CommandArg.PARAM_SUBJID + ", or " +
					CommandArg.PARAM_SUBSYSID + ", " +
					"or no parameter for All records.");
			return;
		}

		
		ScopeTreeSetXb xmlTreeSet;
		if (null == (xmlTreeSet = signetXmlAdapter.getXmlSignet().getScopeTreeSet()))
		{
			xmlTreeSet = new ObjectFactory().createScopeTreeSetXb();
			signetXmlAdapter.getXmlSignet().setScopeTreeSet(xmlTreeSet);
		}

		List<ScopeTreeXb> xmlScopeTreeList = xmlTreeSet.getScopeTree();

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

//		marshalXml(xml, cmd.getOutFile());

		hibr.closeSession(hs);

	}

}
