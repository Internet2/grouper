/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/adapter/SignetSubjectRefXa.java,v 1.4 2008-05-17 20:54:09 ddonn Exp $

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
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;
import edu.internet2.middleware.signet.util.xml.binder.SignetSubjectRefXb;


/**
 * SignetSubjectRefXa<p>
 * Adapter class for Signet XML Binding.
 * Maps a SignetSubject and a SignetSubjectRefXb.
 * @see SignetSubject
 * @see SignetSubjectRefXb
 */
public class SignetSubjectRefXa
{
	protected Signet				signet;
	protected SignetSubject			signetSubject;
	protected SignetSubjectRefXb	xmlSubject;
	protected Log					log;

	/**
	 * default constructor
	 */
	public SignetSubjectRefXa()
	{
		log = LogFactory.getLog(SignetSubjectRefXa.class);
	}

	/**
	 * constructor - only saves a reference to the Signet instance
	 * @param signet - The Signet instance
	 */
	public SignetSubjectRefXa(Signet signet)
	{
		this.signet = signet;
	}

	/**
	 * constructor - Initialize the adapter with the given SignetSubject and
	 * initialize an XML binder for it
	 * @param signetSubject The SignetSubject
	 * @param signet The instance of Signet
	 */
	public SignetSubjectRefXa(SignetSubject signetSubject, Signet signet)
	{
		this(signet);
		this.signetSubject = signetSubject;
		xmlSubject = new ObjectFactory().createSignetSubjectRefXb();
		setValues(signetSubject);
	}

	/**
	 * constructor - Initialize the adapter with the given XML binder and
	 * initialize a SignetSubject for it. Since this is a _reference_ class,
	 * the SignetSubject must be retrieved for the original Source, so a
	 * lookup is performed based on either a) the original primary key, or b)
	 * the original SourceId and SubjectId.
	 * @param xmlSubject The XML binder instance
	 * @param signet The instance of Signet
	 */
	public SignetSubjectRefXa(SignetSubjectRefXb xmlSubject, Signet signet)
	{
		this.signet = signet;
		this.xmlSubject = xmlSubject;
		signetSubject = new SignetSubject();
		setValues(xmlSubject);
	}


	/**
	 * @return The SignetSubject associated with this adapter
	 */
	public SignetSubject getSignetSubject()
	{
		return (signetSubject);
	}

	/**
	 * @return The XML binder associated with this adapter
	 */
	public SignetSubjectRefXb getXmlSubject()
	{
		return (xmlSubject);
	}

	/**
	 * Initialize the XML binder (previously created) from the SignetSubject
	 * @param signetSubject The Signet Subject
	 * @param signet The Signet instance
	 */
	public void setValues(SignetSubject signetSubject, Signet signet)
	{
		this.signet = signet;
		setValues(signetSubject);
	}

	/**
	 * Initialize the XML binder (previously created) from the SignetSubject
	 * @param signetSubject
	 */
	public void setValues(SignetSubject signetSubject)
	{
//	protected Long			subject_PK;
		xmlSubject.setKey(signetSubject.getSubject_PK());

//	protected String		subjectId;
		xmlSubject.setSubjectId(signetSubject.getId());

//	protected String		sourceId;
		xmlSubject.setSourceId(signetSubject.getSourceId());

//	protected String		subjectType;
//		xmlSubject.setSubjectType(signetSubject.getSubjectType());

//	protected String		subjectName;
//		xmlSubject.setSubjectName(signetSubject.getName());

//	protected Date			modifyDatetime;
//		xmlSubject.setModifyDatetime(Util.convertDateToString(signetSubject.getModifyDatetime()));

//	protected Date			synchDatetime;
//		xmlSubject.setSynchDatetime(Util.convertDateToString(signetSubject.getSynchDatetime()));

//	protected Set<SignetSubjectAttr>	signetSubjectAttrs;
//		// (empty) list of newly-created XML attribute binders
//		List<SignetSubjectAttrXb> xmlAttrs = xmlSubject.getSignetSubjectAttrs();
//		// get the set of SignetSubjectAttrs
//		for (SignetSubjectAttr attr : (Set<SignetSubjectAttr>)signetSubject.getSubjectAttrs())
//			xmlAttrs.add(new SignetSubjectAttrXa(attr).getXmlSubjectAttr());

//	protected Set<AssignmentImpl>	assignmentsGranted;
//	protected Set<AssignmentImpl>	assignmentsReceived;
//	protected Set<ProxyImpl>		proxiesGranted;
//	protected Set<ProxyImpl>		proxiesReceived;
		// Don't care about these. This is a _reference_ class.
	}

	/**
	 * Initialize the SignetSubject (previously created) from the XML binder.
	 * This method may do a DB lookup for the SignetSubject, since the 
	 * information in the XML binder is only a _reference_.
	 * @param xmlSubject The XML binder
	 * @param signet The Signet instance
	 */
	public void setValues(SignetSubjectRefXb xmlSubject, Signet signet)
	{
		this.signet = signet;
		setValues(xmlSubject);
	}

	/**
	 * Initialize the SignetSubject from the XML binder (replacing any
	 * SignetSubject previously contained by this class).
	 * This method does a DB lookup for the SignetSubject, since the 
	 * information in the XML binder is only a _reference_.
	 * @param xmlSubject The XML binder class containing the SignetSubject info.
	 */
	public void setValues(SignetSubjectRefXb xmlSubject)
	{
		signetSubject = null;
		if (null == signet)
		{
			log.error("Unable to lookup SignetSubject (PK=" +
					xmlSubject.getKey() +
					") because no Signet instance is available");
			return;
		}

		HibernateDB hibr = signet.getPersistentDB();
		if (null != hibr)
		{
			long pk = xmlSubject.getKey();
			if (0L < pk)
			{
				signetSubject = hibr.getSubject(pk);
				if (null == signetSubject)
					log.warn("No SignetSubject found with primary key = " + pk);
			}
			else
				log.warn("Invalid SignetSubject primary key \"" + pk + "\" specified");
			if (null == signetSubject)
			{
				log.warn("Attempting to find SignetSubject by SourceId and SubjectId...");

				String srcId = xmlSubject.getSourceId();
				String subjId = xmlSubject.getSubjectId();
				try	{ signetSubject = hibr.getSubject(srcId, subjId); }
				catch (ObjectNotFoundException onfe) { /* handled below */ }

				StringBuffer buf = new StringBuffer();
				if (null == signetSubject)
					buf.append("No ");
				buf.append("SignetSubject found for SourceId \"" + srcId + "\" and SubjectId \"" + subjId + "\"");
				log.warn(buf.toString());
			}
		}
		else
			log.error("Unable to lookup SignetSubject: no HibernateDB instance is available");
	}

}
