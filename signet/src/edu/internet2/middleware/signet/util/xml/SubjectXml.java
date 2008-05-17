/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/SubjectXml.java,v 1.1 2008-05-17 20:54:09 ddonn Exp $

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
import java.util.Set;
import org.apache.commons.logging.LogFactory;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;
import edu.internet2.middleware.signet.util.xml.adapter.SignetSubjectXa;
import edu.internet2.middleware.signet.util.xml.adapter.SignetXa;
import edu.internet2.middleware.signet.util.xml.binder.SignetSubjectXb;
import edu.internet2.middleware.signet.util.xml.binder.SignetXb;

/**
 * SubjectXml 
 * 
 */
public class SubjectXml extends XmlUtil
{
	/** private default constructor */
	private SubjectXml()
	{
	}

	/**
	 * Constructor - Initialize Log and Signet instance variables
	 * @param signet A Signet instance
	 * @see Signet
	 */
	public SubjectXml(Signet signet)
	{
		this();
		log = LogFactory.getLog(SubjectXml.class);
		this.signet = signet;
	}

	/**
	 * Constructor - Initialize Log and Signet instance variables, then
	 * export Subject(s) based on parameters in Command
	 * @param signet A Signet instance
	 * @param cmd A Command object containing export parameters
	 * @see Command
	 * @see Signet
	 */
	public SubjectXml(Signet signet, Command cmd)
	{
		this(signet);
		exportSubject(cmd);
	}

	/**
	 * Perform the XML export of this Subject(s)
	 * @param cmd A Command object containing export parameters
	 * @see Command
	 */
	public void exportSubject(Command cmd)
	{
		String[]					subjectIds = null;
		String[]					sourceIds = null;
		Hashtable<String, String>	attrs = new Hashtable();

		Hashtable<String, String> params = cmd.getParams();
		int argCount = 0;
		for (String key : params.keySet())
		{
			if (key.equalsIgnoreCase(Command.PARAM_SUBJID))
			{
				subjectIds = parseList(params.get(key));
				argCount++;
			}
			else if (key.equalsIgnoreCase(Command.PARAM_SOURCEID))
			{
				sourceIds = parseList(params.get(key));
				argCount++;
			}
			else // assume it's an attrName/attrValue pair
			{
				attrs.put(key, params.get(key));
				argCount++;
			}
		}

		SignetXa adapter = new SignetXa(signet);
		SignetXb xml = adapter.getXmlSignet();
		List<SignetSubjectXb> xmlSubjectList = xml.getSubject();
		HibernateDB hibr = signet.getPersistentDB();

		if (null != subjectIds)
		{
			for (String id : subjectIds)
			{
				try
				{
					String[] srcSubj = parsePair(id);
					SignetSubject subj = hibr.getSubject(srcSubj[0], srcSubj[1]);
					xmlSubjectList.add(new SignetSubjectXa(subj, signet).getXmlSubject());
				}
				catch (ObjectNotFoundException e)
				{
					e.printStackTrace();
				}
			}
		}
		else if (null != sourceIds)
		{
			for (String id : sourceIds)
			{
				Set<SignetSubject> subjs = hibr.getSubjectsBySourceId(id);
//				List<SignetSubject> subjs = signet.getSubjectsBySource(id);
				for (SignetSubject subj : subjs)
					xmlSubjectList.add(new SignetSubjectXa(subj, signet).getXmlSubject());
			}
		}
		else if (0 < attrs.size())
		{
			for (String attrKey : attrs.keySet())
			{
//				signet.getPersistedSource().getSubjectByIdentifier(attrs.get(attrKey))
				Set subjs = signet.getPersistentDB().getSubjectsByAttributeValue(attrKey, attrs.get(attrKey));
				for (SignetSubject subj : (Set<SignetSubject>)subjs)
					xmlSubjectList.add(new SignetSubjectXa(subj, signet).getXmlSubject());
			}
		}
		else // get all subjects from signet_subject DB table
		{
			List<SignetSubject> subjs = signet.getSubjectsBySource(signet.getPersistedSource().getId());
			for (SignetSubject subj : subjs)
				xmlSubjectList.add(new SignetSubjectXa(subj, signet).getXmlSubject());
		}

		marshalXml(xml, cmd.getOutFile());

	}


}
