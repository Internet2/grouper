/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/subjsrc/PersistedSignetSource.java,v 1.10 2007-07-27 07:52:31 ddonn Exp $

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
package edu.internet2.middleware.signet.subjsrc;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import org.hibernate.Session;
import org.hibernate.Transaction;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.SignetRuntimeException;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.resource.ResLoaderApp;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;


/**
 * This class represents a SignetSource for the persisted (e.g. Hibernate) Subjects.
 * It is designed to be created when the SubjectSources.xml (Signet's subject-
 * source configuration file) is parsed and the <persistedSubjectSource> tag is found.
 * This allows Signet to treat the persisted store as just another
 * SubjectAPI-like Source.
 */
public class PersistedSignetSource extends SignetSource
{
	public static final String	TYPE_PERSISTED_SRC = "persistedSrcType";

	// conversion factor for minutes to/from milliseconds
	protected static final long minutesToMillis = (60 * 1000);

	/** The Persisted Store Manager */
	protected HibernateDB		persistMgr;

	/**
	 * Elapsed time to wait before refreshing Persistent store from the SourceAPI.
	 * Value is specified in SubjectSources.xml using the 'latency' attribute
	 * of the <persistedSubjectSource> tag.
	 * Default: 60 minutes, stored as milliseconds.
	 */
	protected long				latency;
/** TODO Digester can't find 'long latency' and ignores the xml element unless
 a 'String latencyMinutes' is defined (even though it's never used!) */
public String latencyMinutes;

	////////////////////////////////////
	// The following are "attributes of interest" for Signet (when Signet acts
	// like an application, in it's GUI for example). Their values
	// correspond to mappedAttribute names in each Source brought in from the
	// SubjectAPI via the SubjectSources.xml.
	////////////////////////////////////
	protected String			signetName;
	protected String			signetSortName;
	protected String			signetDescription;
	protected String			signetDisplayId;
	protected String			contactEmail;
	protected Vector			outputXml;
	protected Vector			uniqueId;


	/**
	 * default constructor
	 * Support for Digester
	 */
	public PersistedSignetSource()
	{
		// initialize super's attributes
		super();
		type = TYPE_PERSISTED_SRC;
		status = STATUS_ACTIVE;
		failover = true;
		usage.add(USAGE_DEFAULT);

		// initialize class-specific attributes
		latency = 60 * minutesToMillis;
		signetName = null;
		signetSortName = null;
		signetDescription = null;
		signetDisplayId = null;
		contactEmail = null;
		outputXml = new Vector();
		uniqueId = new Vector();

		persistMgr = null;
	}


	public void setPersistedStoreMgr(HibernateDB persistMgr)
	{
		this.persistMgr = persistMgr;
	}

	public HibernateDB getPersistedStoreMgr()
	{
		return (persistMgr);
	}


	/**
	 * Gets the amount of elapsed time of refreshs between SourceAPI and Persistent store
	 * Supports Digetster and SubjectSources.xml parsing
	 * @return Returns the latency in minutes.
	 */
	public long getLatency()
	{
		return (latency / minutesToMillis);
	}

	/**
	 * Gets the amount of elapsed time of refreshs between SourceAPI and Persistent store
	 * Supports Digetster and SubjectSources.xml parsing
	 * @return Returns the latency in milliseconds.
	 */
	public long getLatencyMillis()
	{
		return (latency);
	}

	/**
	 * Sets the amount of elapsed time of refreshs between SourceAPI and Persistent store.
	 * Supports Digetster and SubjectSources.xml parsing
	 * @param latencyMinutes The latency to set, converted to millis internally
	 */
	public void setLatencyMinutes(String latencyMinutes)
	{
		this.latency = Long.parseLong(latencyMinutes) * minutesToMillis;
	}

	/**
	 * Determines if this Subject may need re-synchronization with it's Source
	 * @param signetSubject The Subject in question
	 * @return true if Subject is persisted and older than 'latency', otherwise false
	 */
	public boolean isStale(SignetSubject signetSubject)
	{
		if ((null == signetSubject) || !signetSubject.isPersisted())
			return (false);

		long now = Calendar.getInstance().getTimeInMillis();
		long syncTime = signetSubject.getSynchDatetime().getTime();
		boolean retval = latency < (now - syncTime);

		return (retval);
	}


	/**
	 * Set the signetName
	 * Supports Digetster and SubjectSources.xml parsing
	 * @param name The Signet name to set
	 */
	public void setSignetName(String name)
	{
		signetName = name;
	}

	/**
	 * Get the Signet name
	 * Supports Digetster and SubjectSources.xml parsing
	 * @return The Signet name
	 */
	public String getSignetName()
	{
		return (signetName);
	}

	/**
	 * Sets the Signet Sort Name
	 * Supports Digetster and SubjectSources.xml parsing
	 * @param sortName
	 */
	public void setSignetSortName(String sortName)
	{
		signetSortName = sortName;
	}

	/**
	 * Supports Digetster and SubjectSources.xml parsing
	 * @return The Signet Sort Name
	 */
	public String getSignetSortName()
	{
		return (signetSortName);
	}


	/**
	 * Set the Signet Description
	 * Supports Digetster and SubjectSources.xml parsing
	 * @param signetDesc
	 */
	public void setSignetDescription(String signetDesc)
	{
		signetDescription = signetDesc;
	}

	/**
	 * Supports Digetster and SubjectSources.xml parsing
	 * @return The Signet Description
	 */
	public String getSignetDescription()
	{
		return (signetDescription);
	}


	/**
	 * Sets the SignetDisplayId
	 * Supports Digetster and SubjectSources.xml parsing
	 * @param displayId
	 */
	public void setSignetDisplayId(String displayId)
	{
		signetDisplayId = displayId;
	}

	/**
	 * Supports Digetster and SubjectSources.xml parsing
	 * @return The Signet Display Id
	 */
	public String getSignetDisplayId()
	{
		return (signetDisplayId);
	}

	
	/**
	 * Define the field used by Signet as the email contact
	 * Supports Digetster and SubjectSources.xml parsing
	 * @param email The email contact
	 */
	public void setContactEmail(String email)
	{
		contactEmail = email;
	}

	/**
	 * @return The email contact value
	 */
	public String getContactEmail()
	{
		return (contactEmail);
	}


	/**
	 * Adds OutputXml field names to this Source. OutputXml tags may contain
	 * either single entries or multiple, comma-separated entries.
	 * Supports Digetster and SubjectSources.xml parsing
	 * @param outputXmlStr
	 */
	public void addOutputXml(String outputXmlStr)
	{
		addValueListToVector(outputXmlStr, outputXml);
	}

	/**
	 * @return The entire list (Vector) of outputXml
	 */
	public Vector getOutputXml()
	{
		return (outputXml);
	}

	/**
	 * Tests to see if outputXmlStr is in the list (Vector) of outputXml values
	 * @param outputXmlStr
	 * @return True if found, false otherwise
	 */
	public boolean hasOutputXmlValue(String outputXmlStr)
	{
		return (outputXml.contains(outputXmlStr));
	}


	/**
	 * Adds UniqueId field names to this Source. UniqueId tags may contain
	 * either single entries or multiple, comma-separated entries. Collectively,
	 * UniqueId values define the fields used as a primary key
	 * Supports Digetster and SubjectSources.xml parsing
	 * @param uniqueIdStr
	 */
	public void addUniqueId(String uniqueIdStr)
	{
		addValueListToVector(uniqueIdStr, uniqueId);
	}

	/**
	 * @return The entire list (Vector) of uniqueId
	 */
	public Vector getUniqueId()
	{
		return (uniqueId);
	}

	/**
	 * Tests to see if uniqueIdStr is in the list (Vector) of uniqueId values
	 * @param uniqueIdStr
	 * @return True if found, false otherwise
	 */
	public boolean hasUniqueIdValue(String uniqueIdStr)
	{
		return (uniqueId.contains(uniqueIdStr));
	}


	///////////////////////////////////
	// overrides SignetSource
	///////////////////////////////////

	/**
	 * Override SignetSource.setSourceManager() because there is no corresponding
	 * SubjectAPI Source lookup to perform for a PersistedSignetSource. Each
	 * Subject retrieved from Persisted store has it's own, original SourceId reference!
	 * @see edu.internet2.middleware.signet.subjsrc.SignetSource#setSourceManager(edu.internet2.middleware.subject.provider.SourceManager)
	 */
	public void setSourceManager(SourceManager sourceManager)
	{
		; // do nothing
	}


	/**
	 * PersistedSignetSource is always set to 'all' Usage. Called by Digester when parsing SubjectSources.xml.
	 * Supports Digester and SubjectSources.xml parsing
	 */
	public void addUsage(String usageStr)
	{
		; // do nothing
	}

	/**
	 * Tests to see if this SignetSource supports the given usage (as defined in SubjectSources.xml)
	 * @param usage The usage to test for
	 * @return Always True because PersistedSignetSource is 'all'
	 */
	public boolean hasUsage(String usage)
	{
		return (true);
	}


	/**
	 * Supports Digester and SubjectSources.xml parsing
	 * @param status
	 */
	public void setStatus(String status)
	{
		; // do nothing
	}


	/**
	 */
	public void setSubjectType(String type)
	{
		; // do nothing
	}


	/**
	 * Get the SignetSubject that matches the DB primary key from Persisted Store.
	 * @param subject_pk The primary key
	 * @return The matching SignetSubject or null
	 */
	public SignetSubject getSubject(long subject_pk)
	{
		SignetSubject retval;

		if (null != persistMgr)
		{
			retval = persistMgr.getSubject(subject_pk);
			setSubjectSource(retval);
			if (isStale(retval))
				resynchSubject(retval);
		}
		else
		{
			retval = null;
			log.warn("PersistedSignetSource.getSubject(long): No Persistence Manager found");
		}

		return (retval);
	}


	/**
	 * Find a SignetSubject from the Persisted store that matches the given
	 * sourceId and subjectId.
	 * Pseudo-override of SignetSubject#getSubject(String subjectId)
	 * @param sourceId The sourceId
	 * @param subjectId The subjectId
	 * @return A SignetSubject if found, otherwise null
	 */
	public SignetSubject getSubject(String sourceId, String subjectId)
	{
		SignetSubject retval;

		try
		{
			retval = persistMgr.getSubject(sourceId, subjectId);
			setSubjectSource(retval);
			if (isStale(retval))
				resynchSubject(retval);
		}
		catch (ObjectNotFoundException e)
		{
			log.debug(e);
			retval = null;
		}

		return (retval);
	}


	/**
	 * @see edu.internet2.middleware.subject.Source#getSubjectByIdentifier(java.lang.String)
	 */
	public Subject getSubjectByIdentifier(String identifier)
	{
		SignetSubject retval = null;
		Object[] msgData;
		MessageFormat msgFmt;

		try
		{
			retval = persistMgr.getSubjectByIdentifier(identifier);
			setSubjectSource(retval);
			if (isStale(retval))
				resynchSubject(retval);
		}
		catch (ObjectNotFoundException onfe)
		{
			msgData = new Object[] { getSourceId(), identifier };
			String msgTemplate = ResLoaderApp.getString("HibernateDb.msg.exc.SubjNotFound");  //$NON-NLS-1$
			msgFmt = new MessageFormat(msgTemplate);
			log.info(msgFmt.format(msgData));
		}
		catch (SignetRuntimeException rte)
		{
			msgData = new Object[] { rte.getMessage(), getSourceId(), identifier };
			msgFmt = new MessageFormat(ResLoaderApp.getString("HibernateDb.msg.exc.multiSigSubj_1") + //$NON-NLS-1$
					ResLoaderApp.getString("HibernateDb.msg.exc.multiSigSubj_2") + //$NON-NLS-1$
					ResLoaderApp.getString("HibernateDb.msg.exc.multiSigSubj_3")); //$NON-NLS-1$
			log.error(msgFmt.format(msgData));
		}

		return (retval);
	}


	/**
	 * Returns a Vector of SignetSubject objects from the Persisted store.
	 * @return A Vector of SignetSubject objects, or empty Vector (never null!)
	 */
	public Vector getSubjects()
	{
//TODO Implement getSubjects
System.out.println("PersistedSignetSource.getSubjects: not implemented yet!");
		return new Vector();
	}


	/**
	 * Retrieve a copy of the original Subject from the original Source, copy
	 * it's contents into the supplied Subject, and if the copy worked,
	 * persist the supplied Subject.
	 * @param subj The SignetSubject to receive the updated contents
	 */
	public void resynchSubject(SignetSubject subj)
	{
//System.out.println("PersistedSignetSource.resynchSubject: about to resynch Subject=" + subj.toString());
		// get a fresh copy of the Subject
		Subject apiSubject = getSources().getSubjectBySource(subj.getSourceId(), subj.getId());
		// attempt to copy the fresh one into this one
		if (subj.synchSubject(apiSubject))
		{
			// the copy happened, so persist this one
			Session hs = persistMgr.openSession();
			Transaction tx = hs.beginTransaction();
			persistMgr.save(hs, subj);
			tx.commit();
			hs.refresh(subj);
			persistMgr.closeSession(hs);
//System.out.println("PersistedSignetSource.resynchSubject: resynch'd Subject=" + subj.toString());
		}
	}


	/**
	 * Attempt to lookup the original SubjectAPI Source for the Subject and
	 * set its value. If the original Source is unavailable, set the Subject's
	 * Source to the PersistedSignetSource (i.e. this).
	 * @param subject The just-retrieved-from-persisted-store subject
	 */
	protected void setSubjectSource(SignetSubject subject)
	{
		if (null == subject)
			return;

		SignetSource src = signetSources.getSource(subject.getSourceId());
		if (null != src)
			subject.setSource(src);
		else
			subject.setSource(this);
	}


	/**
	 * Get the set of Proxies granted by the grantor.
	 * @param grantorId The primary key of the proxy grantor
	 * @param status The status of the proxy
	 * @return A Set of ProxyImpl objects that have been granted by grantor.
	 * May be an empty set but never null.
	 */
	public Set getProxiesGranted(long grantorId, String status)
	{
		Set proxies;

		if (null != persistMgr)
		{
			proxies = persistMgr.getProxiesGranted(grantorId, status);
		}
		else
		{
			proxies = new HashSet();
			log.warn("No Persistence Manager found");
		}

		return (proxies);
	}

	/**
	 * Get the set of Proxies granted to grantee.
	 * @param granteeId The primary key of the proxy grantee
	 * @param status The status of the proxy
	 * @return A Set of ProxyImpl objects that have been received by grantee
	 * May be an empty set but never null.
	 */
	public Set getProxiesReceived(long granteeId, String status)
	{
		Set proxies;

		if (null != persistMgr)
		{
			proxies = persistMgr.getProxiesReceived(granteeId, status);
		}
		else
		{
			proxies = new HashSet();
			log.warn("No Persistence Manager found");
		}

		return (proxies);
	}

	/**
	 * Get the set of Assignments granted by the grantor
	 * @param grantorId The primary key of the assignment grantor
	 * @return A Set of AssignmentImpl object that have been granted by grantor.
	 * May be an empty set but never null.
	 */
	public Set getAssignmentsGranted(long grantorId, String status)
	{
		Set assigns;

		if (null != persistMgr)
		{
			assigns = persistMgr.getAssignmentsGranted(grantorId, status);
		}
		else
		{
			assigns = new HashSet();
			log.warn("No Persistence Manager found");
		}

		return (assigns);
	}

	/**
	 * Get the set of Assignments granted to grantee
	 * @param granteeId The primary key of the assignment grantee
	 * @return A Set of AssignmentImpl object that have been granted to grantee.
	 * May be an empty set but never null.
	 */
	public Set getAssignmentsReceived(long granteeId, String status)
	{
		Set assigns;

		if (null != persistMgr)
		{
			assigns = persistMgr.getAssignmentsReceived(granteeId, status);
		}
		else
		{
			assigns = new HashSet();
			log.warn("No Persistence Manager found");
		}

		return (assigns);
	}


	////////////////////////////////
	// overrides Object
	////////////////////////////////

	public String toString()
	{
		return (new String(
				"PersistedSignetSource: Id=\"" + getId() + "\" " +
				"Name=\"" + getName() + "\" " +
				"Description=\"" + getSignetDescription() + "\" " +
				"RefreshLatency=" + (latency / minutesToMillis) + " " +
				"SortName=\"" + getSignetSortName() + "\" " +
				"DisplayId=\"" + getSignetDisplayId() + "\" " +
				"ContactEmail=\"" + getContactEmail() + "\" " +
				"\n" +
				vectorToString("UniqueIds", uniqueId) + " \n" +
				vectorToString("OutputXml", outputXml) + " \n" +
				vectorToString("Usage", usage)));
	}

}
