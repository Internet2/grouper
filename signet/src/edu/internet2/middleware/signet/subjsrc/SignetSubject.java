/*
 * $Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/subjsrc/SignetSubject.java,v 1.5 2006-12-15 20:45:37 ddonn Exp $
 * 
 * Copyright (c) 2006 Internet2, Stanford University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * 
 * @author ddonn
 */
package edu.internet2.middleware.signet.subjsrc;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.set.UnmodifiableSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.internet2.middleware.signet.Assignment;
import edu.internet2.middleware.signet.AssignmentImpl;
import edu.internet2.middleware.signet.Category;
import edu.internet2.middleware.signet.Decision;
import edu.internet2.middleware.signet.DecisionImpl;
import edu.internet2.middleware.signet.Function;
import edu.internet2.middleware.signet.Grantable;
import edu.internet2.middleware.signet.Limit;
import edu.internet2.middleware.signet.LimitValue;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.PrivilegeImpl;
import edu.internet2.middleware.signet.Proxy;
import edu.internet2.middleware.signet.ProxyImpl;
import edu.internet2.middleware.signet.Reason;
import edu.internet2.middleware.signet.SelectionType;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SignetAuthorityException;
import edu.internet2.middleware.signet.SignetRuntimeException;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.Subsystem;
import edu.internet2.middleware.signet.SubsystemImpl;
import edu.internet2.middleware.signet.choice.Choice;
import edu.internet2.middleware.signet.choice.ChoiceNotFoundException;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.reconcile.Reconciler;
import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeNode;
import edu.internet2.middleware.signet.ui.Common;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;

/**
 * Class to represent a Subject within Signet's context. Typically, there are
 * two ways a SignetSubject can be created: from an Application's request to
 * Signet.getSubject(), or via retrieval from the Persisted store (Hibernate).
 */
public class SignetSubject implements Subject, Comparable
{
	// This is the metadata that describes Signet's pre-defined application subject.
	public static final String		SIGNET_NAME = "Signet";
	public static final String		SIGNET_SUBJECT_ID = "Super_" + SignetSubject.class.getSimpleName();
	public static final String		SIGNET_DESC	= "The " + SIGNET_NAME + " System";

	/** Primary key for persistent store of Subjects.
	 * If non-null and non-zero, subject_PK indicates this Subject exists in
	 * Persisted store.
	 * Hibernate field. */
	protected Long			subject_PK;
	/** The identifier of this Subject as defined in the original SubjectAPI
	 * Subject. Hibernate field. */
	protected String		subjectId;
	/** The identifier of the originating Source of this Subject. Hibernate field. */
	protected String		sourceId;
	/** The type of this Subject as defined in the original SubjectAPI Subject.
	 * Hibernate field. */
	protected String		subjectType;
	/** The name of this Subject as defined in the original SubjectAPI Subject.
	 * Hibernate field. */
	protected String		subjectName;
	/** The Date of the most recent modification to this Subject within Signet.
	 * Hibernate field. */
	protected Date			modifyDatetime;
	/** The Date of the most recent synchronization between the SubjectAPI and
	 * persisted store. Hibernate field. */
	protected Date			synchDatetime;
	/** A Set of SignetSubjectAttribute representing the attributes of interest
	 * for this Subject. Hibernate collection */
	protected Set			signetSubjectAttrs;
	/** Reference to the Source of this Subject. Not a Hibernate field. */
	protected SignetSource	signetSource;
	/** A Subject may act as another Subject for the purpose of managing
	 * Proxies and Assignments. Not a Hibernate field. */
	protected SignetSubject	actingAs;

	/** Logging */
	protected Log			log = LogFactory.getLog(SignetSubject.class);


	/**
	 * default constructor for Hibernate
	 */
	public SignetSubject()
	{
		subject_PK = null;
		subjectId = null;
		sourceId = null;
		subjectType = null;
		subjectName = null;
		modifyDatetime = new Date(0L);
		synchDatetime = new Date(0L);
		signetSubjectAttrs = new HashSet();
		signetSource = null;

		actingAs = null;
	}

	/**
	 * Default constructor for fetch from SubjectAPI - essentially a Copy Constructor
	 * that also includes the Subject's Source.
	 * @param source The SignetSource that "owns" this Subject
	 * @param subject The generic Subject object, as delivered from the SubjectAPI
	 */
	public SignetSubject(SignetSource source, Subject subject)
	{
		signetSubjectAttrs = new HashSet();

		signetSource = source;
		sourceId = source.getId();

		synchSubject(subject);

		subject_PK = null;

		actingAs = null;
	}


	/**
	 * Synchronize the data from a SubjectAPI Subject into this Subject. Requires
	 * this Subject to have a valid Source.
	 * @param apiSubject The SubjectAPI Subject to synchronize with
	 * @return True if anything changed, otherwise false
	 */
	protected boolean synchSubject(Subject apiSubject)
	{
		if ((null == signetSource) || (null == apiSubject))
			return (false);

		if (this.equals(apiSubject))
			return(false);

		boolean retval;
		if (apiSubject instanceof SignetSubject)
			retval = copy((SignetSubject)apiSubject);
		else
			retval = copy(apiSubject);

		return (retval);
	}


	/**
	 * Copy the values from the Subject-API Subject. Note that this is a deep copy
	 * in that it also copies the original's attributes. The SynchDate and
	 * ModifyDate are not copied, instead their values are set to 'now'. Note
	 * too that the following values ARE NOT copied or changed: signetSource,
	 * actingAs, assignmentsGranted, assignmentsReceived, proxiesGranted,
	 * and proxiesReceived since these values are not part of the Subject
	 * interface.
	 * @param apiSubject The Subject to copy the values from
	 * @return True if successful, otherwise false
	 */
	protected boolean copy(Subject apiSubject)
	{
		if ((null == signetSource) || (null == apiSubject))
			return (false);

		setId(apiSubject.getId());
		setType(signetSource.getSubjectType()); // Signet only supports 1 type per Source
		setName(apiSubject.getName());

		Date now = new Date();
		setSynchDatetime(now);
		setModifyDatetime(now);

		synchAttributes(apiSubject);

		return (true);
	}


	/**
	 * Copy the values from another SignetSubject. Note that this is a deep copy
	 * in that it also copies the original's attributes. The SynchDate and
	 * ModifyDate are not copied, instead their values are set to 'now'. Note
	 * too that signetSource IS copied or overwritten.
	 * If the incoming sigSubject is persisted, the the following fields are
	 * also copied: actingAs, assignmentsGranted, assignmentsReceived,
	 * proxiesGranted, and proxiesReceived.
	 * @param otherSubject The SignetSubject to copy the values from
	 * @return True if successful, otherwise false
	 */
	protected boolean copy(SignetSubject otherSubject)
	{
		if ((null == signetSource) || (null == otherSubject))
			return (false);

		setId(otherSubject.getId());
		setType(signetSource.getSubjectType()); // Signet only supports 1 type per Source
		setName(otherSubject.getName());

		Date now = new Date();
		setSynchDatetime(now);
		setModifyDatetime(now);

		synchAttributes(otherSubject);

		// don't try to copy run-time data from a Subject that came from SubjectAPI
		if (otherSubject.isPersisted())
		{
//TODO it is questionable whether I should copy the 'actingAs' field
			try
			{
				SignetSubject proxy = otherSubject.getEffectiveEditor();
				if ( !proxy.equals(otherSubject))
					setActingAs(proxy);
				else
					setActingAs(null);
			}
			catch (SignetAuthorityException e)
			{
				signetSource.getSignet().getLogger().error(e);
			}
		}

		return (true);
	}


	/**
	 * Synchronize the SignetSubjectAttrs with the SubjectAPI's attr values. Use
	 * this Subject's attributes of interest to get attributes, and attr values,
	 * from the SubjectAPI's Subject and replace this Subject's attrs and values.
	 * @param apiSubject The SubjectAPI Subject to synch with
	 */
	protected void synchAttributes(Subject apiSubject)
	{
		if (null == apiSubject)
			return;

		if (apiSubject instanceof SignetSubject)
		{
log.warn(
 "SignetSubject.synchAttributes(Subject) is redirecting to method " +
 "synchAttributes(SignetSubject) where SignetSubject = \n" +
 ((SignetSubject)apiSubject).toString());
			synchAttributes((SignetSubject)apiSubject);
			return;
		}

		if (null == signetSource) // can't continue without a Source's mapped attributes
			return;

		// Get the "attributes of interest" for our Source and the attribute's value
		Map apiAttrs = apiSubject.getAttributes(); // attributes from the original Subject (a Map of Name/Set pairs)
		Hashtable sourceAttrMap = signetSource.getMappedAttributes(); // mapped attribute names from SubjectSources.xml

		// the mappedAttribute is the key; the sourceAttribute is the value
		for (Enumeration sigAttrNames = sourceAttrMap.keys(); sigAttrNames.hasMoreElements();)
		{
			// get the signet attribute name
			String sigAttrName = (String)sigAttrNames.nextElement();

			// get the mapped attribute name
			String apiAttrName = (String)sourceAttrMap.get(sigAttrName);

			// get the values of the subjectApi attribute (a Set of String objects)
			Set apiAttrValues = (Set)apiAttrs.get(apiAttrName);

			// if the SignetSubjectAttr exists update it, otherwise create it
			SignetSubjectAttr sigAttr = getAttribute(sigAttrName);
			if (null == sigAttr)
			{
				sigAttr = new SignetSubjectAttr(sigAttrName);
				addAttribute(sigAttr);
			}

			sigAttr.setSourceValues(apiAttrValues); // replace the existing attr values
		}
	}


	/**
	 * Synchronize this SignetSubjectAttrs with another SignetSubject's attr values.
	 * No mapping of attribute names is required because they're both SignetSubjects.
	 * @param sigSubject The SignetSubject to synch with
	 */
	protected void synchAttributes(SignetSubject sigSubject)
	{
		if (null == sigSubject)
			return;

		if (null != signetSubjectAttrs)
			signetSubjectAttrs.clear();
		else
			signetSubjectAttrs = new HashSet();

		Map attrs = sigSubject.getAttributes();
		if (null != attrs)
			signetSubjectAttrs.addAll(attrs.values());
	}


	/** Determine if this Subject has been persisted by checking its primary key.
	 * Does a deep compare of all child PK's too.
	 * @return true only if this Subject and all of it's children (Attributes and
	 * AttributeValues) are persisted. Otherwise, false.
	 */
	public boolean isPersisted()
	{
		boolean retval;

		// check my status first
		retval = (null != subject_PK) && (0L < subject_PK.longValue());

		if (retval && (null != signetSubjectAttrs)) // check children's status if necessary
		{
			for (Iterator attrs = signetSubjectAttrs.iterator(); attrs.hasNext() && retval; )
				retval = ((SignetSubjectAttr)attrs.next()).isPersisted();
		}

		return (retval);
	}


	/////////////////////////////////
	// Hibernate support methods
	/////////////////////////////////

	/**
	 * @return Returns the subjectKey.
	 * Support for Hibernate
	 */
	public Long getSubject_PK()
	{
		return subject_PK;
	}

	/**
	 * @param subjectKey The subjectKey to set.
	 * Support for Hibernate
	 */
	public void setSubject_PK(Long subjectKey)
	{
		this.subject_PK = subjectKey;
	}


	/**
	 * @return Returns the modifyDatetime.
	 * Support for Hibernate
	 */
	public Date getModifyDatetime()
	{
		return modifyDatetime;
	}

	/**
	 * @param modifyDatetime The modifyDatetime to set.
	 * Support for Hibernate
	 */
	public void setModifyDatetime(Date modifyDatetime)
	{
		this.modifyDatetime = modifyDatetime;
	}


	/**
	 * @return Returns the synchDatetime.
	 * Support for Hibernate
	 */
	public Date getSynchDatetime()
	{
		return synchDatetime;
	}

	/**
	 * @param synchDatetime The synchDatetime to set.
	 * Support for Hibernate
	 */
	public void setSynchDatetime(Date synchDatetime)
	{
		this.synchDatetime = synchDatetime;
	}


	/**
	 * @return Returns the sourceId.
	 * Support for Hibernate
	 */
	public String getSourceId()
	{
		return (sourceId);
	}


	/**
	 * Set the SourceId for this Subject - Support for Hibernate.  Note: see comment in method body.
	 * @param sourceId The sourceId to set.
	 */
	public void setSourceId(String sourceId)
	{
		this.sourceId = sourceId;
		// Caller is probably Hibernate, so we need to get the whole Source from
		// SignetSources.
		// But if it is Hibernate calling, we'll have to rely on
		// PersistedSignetSource.getSubject() method will call our setSource().
		//
		// The problem is that we've got 2 variables representing the same thing: 
		//   - sourceId - which is persisted
		//   - signetSource - which may or may not exist from one
		//     Signet run to the next.
		// The sourceId is valid (it's how we got this Subject into persisted 
		// store in the frist place), but the original Source may not be
		// available during this execution (hence the invention of the
		// 'failover' flag in SubjectSources.xml.
	}


	/**
	 * @return Returns the signetSubjectAttrs.
	 * Support for Hibernate
	 */
	public Set getSubjectAttrs()
	{
		return (signetSubjectAttrs);
	}


	/**
	 * Support for Hibernate
	 * @param signetSubjectAttrs The signetSubjectAttrs to set.
	 */
	public void setSubjectAttrs(Set signetSubjectAttrs)
	{
		this.signetSubjectAttrs = signetSubjectAttrs;
	}


	/**
	 * Support for Hibernate
	 * @return Returns the assignmentsGranted.
	 * @throws ObjectNotFoundException
	 */
	public Set getAssignmentsGranted()
	{
		Set assignsGranted;
		SignetSources srcs;
		PersistedSignetSource persistSrc;

		if ( !isPersisted()) // it's not persisted, ergo no assignments
			assignsGranted = new HashSet();

		else if ((null != signetSource) &&
			(null != (srcs = signetSource.getParent())) &&
			(null != (persistSrc = srcs.getPersistedSource())))
		{
			assignsGranted = persistSrc.getAssignmentsGranted(subject_PK.longValue(), Status.ACTIVE.toString());
			Signet signet = signetSource.getSignet();
			for (Iterator assigns = assignsGranted.iterator(); assigns.hasNext(); )
				((AssignmentImpl)assigns.next()).setSignet(signet);
		}
		else
		{
			assignsGranted = new HashSet();
			log.warn("No PersistedSource found for SignetSubject with primary key = " + subject_PK);
		}

		return (assignsGranted);
	}
	

	/**
	 * Support for Hibernate
	 * @return Returns the assignmentsReceived.
	 * @throws ObjectNotFoundException
	 */
	public Set getAssignmentsReceived()
	{
		Set assignsReceived;
		SignetSources srcs;
		PersistedSignetSource persistSrc;

		if ( !isPersisted()) // it's not persisted, ergo no assignments
			assignsReceived = new HashSet();

		else if ((null != signetSource) &&
			(null != (srcs = signetSource.getParent())) &&
			(null != (persistSrc = srcs.getPersistedSource())))
		{
			assignsReceived = persistSrc.getAssignmentsReceived(subject_PK.longValue(), Status.ACTIVE.toString());
			Signet signet = signetSource.getSignet();
			for (Iterator assigns = assignsReceived.iterator(); assigns.hasNext(); )
				((AssignmentImpl)assigns.next()).setSignet(signet);
		}
		else
		{
			assignsReceived = new HashSet();
			log.warn("No PersistedSource found for SignetSubject with primary key = " + subject_PK);
		}

		return (assignsReceived);
	}


	/**
	 * Get the set of Proxies granted by this Subject.
	 * @return A Set of ProxyImpl objects that have been granted by grantor.
	 * May be an empty set but never null.
	 */
	public Set getProxiesGranted()
	{
		Set proxiesGranted;
		SignetSources srcs;
		PersistedSignetSource persistSrc;

		if ( !isPersisted()) // it's not persisted, ergo no proxies
			proxiesGranted = new HashSet();

		else if ((null != signetSource) &&
			(null != (srcs = signetSource.getParent())) &&
			(null != (persistSrc = srcs.getPersistedSource())))
		{
			proxiesGranted = persistSrc.getProxiesGranted(subject_PK.longValue(), Status.ACTIVE.toString());
			Signet signet = signetSource.getSignet();
			for (Iterator proxies = proxiesGranted.iterator(); proxies.hasNext(); )
				((ProxyImpl)proxies.next()).setSignet(signet);
		}
		else
		{
			proxiesGranted = new HashSet();
			log.warn("No PersistedSource found for SignetSubject with primary key = " + subject_PK);
		}

		return (proxiesGranted);
	}


	/**
	 * Support for Hibernate
	 * @return A Set of Proxies received by this Subject
	 */
	public Set getProxiesReceived()
	{
		Set proxiesReceived;
		SignetSources srcs;
		PersistedSignetSource persistSrc;

		if ( !isPersisted()) // it's not persisted, ergo no proxies
			proxiesReceived = new HashSet();

		else if ((null != signetSource) &&
			(null != (srcs = signetSource.getParent())) &&
			(null != (persistSrc = srcs.getPersistedSource())))
		{
			proxiesReceived = persistSrc.getProxiesReceived(subject_PK.longValue(), Status.ACTIVE.toString());
			Signet signet = signetSource.getSignet();
			for (Iterator proxies = proxiesReceived.iterator(); proxies.hasNext(); )
				((ProxyImpl)proxies.next()).setSignet(signet);
		}
		else
		{
			proxiesReceived = new HashSet();
			log.warn("No PersistedSource found for SignetSubject with primary key = " + subject_PK);
		}

		return (proxiesReceived);
	}


	/**
	 * Save (persist) this Subject in Signet's persistence layer
	 */
	public void save()
	{
		HibernateDB hibr = signetSource.getSignet().getPersistentDB();
		hibr.beginTransaction();
		hibr.save(this);
try
{
		hibr.commit();
}
catch (SignetRuntimeException e)
{
System.out.println("SignetSubject.save: exception during commit. SignetSubject =\n" + toString());
}
	}


	////////////////////////////////////
	// implements Subject
	////////////////////////////////////

	/**
	 * Return the ID (as originally obtained from the SubjectAPI) of this Subject.
	 * @see edu.internet2.middleware.subject.Subject#getId()
	 * Also provides support for Hibernate
	 */
	public String getId()
	{
		return (subjectId);
	}

	/**
	 * Set the Id
	 * Also provides support for Hibernate
	 */
	public void setId(String subjectId)
	{
		this.subjectId = subjectId;
	}


	/**
	 * @see edu.internet2.middleware.subject.Subject#getType()
	 * Hibernate can't handle SubjectType, @see SignetSubject#getSubjectType(String)
	 */
	public SubjectType getType()
	{
		return (SubjectTypeEnum.valueOf(subjectType));
	}

	/**
	 * Set the Subject Type
	 * Also provides support for Hibernate
	 */
	public void setType(String subjectType)
	{
		this.subjectType = subjectType;
	}


	/**
	 * @return Returns the Subject Type Id
	 * @see edu.internet2.middleware.subject.Subject#getType()
	 * provides support for Hibernate
	 */
	public String getSubjectType()
	{
		return (subjectType);
	}

	/**
	 * Set the Subject Type
	 * provides support for Hibernate
	 */
	public void setSubjectType(String subjectType)
	{
		this.subjectType = subjectType;
	}


	/**
	 * @see edu.internet2.middleware.subject.Subject#getName()
	 * Also provides support for Hibernate
	 */
	public String getName()
	{
		return (subjectName);
	}

	/**
	 * Set the Subject Name
	 * Also provides support for Hibernate
	 */
	public void setName(String subjectName)
	{
		this.subjectName = subjectName;
	}


	/**
	 * Description is stored as an attribute, but is returned as a String for convenience
	 * @see edu.internet2.middleware.subject.Subject#getDescription()
	 */
	public String getDescription()
	{
		String descAttrName = signetSource.getParent().getPersistedSource().getSignetDescription();
		String retval = getAttributeValue(descAttrName);
		return (retval);
	}

	/**
	 * Set the Subject Description. Stored as an attribute. Supercedes previous
	 * value since we only want one description for a Subject (attribute supports
	 * multiple values).
	 * @param subjectDescription The new description for this Subject
	 */
	public void setDescription(String subjectDescription)
	{
		String descAttrName = signetSource.getParent().getPersistedSource().getSignetDescription();
		SignetSubjectAttr attr = getAttribute(descAttrName);
		if (null == attr)
		{
			attr = new SignetSubjectAttr(descAttrName);
			addAttribute(attr);
		}
		attr.getSourceValues().clear(); // we only want one description
		attr.addSourceValue(subjectDescription);
	}


	/**
	 * @see edu.internet2.middleware.subject.Subject#getSource()
	 */
	public Source getSource()
	{
		return (signetSource);
	}

	/**
	 * Set the SubjectAPI Source associated with this SignetSubject
	 * @param signetSource The signetSource to set.
	 */
	public void setSource(SignetSource signetSource)
	{
		this.signetSource = signetSource;
		if (null != signetSource)
			sourceId = signetSource.getId();
	}


	/**
	 * Return a SignetSubjectAttr that has a matching mappedName, or null.
	 * Pseudo-implementation of Subject interface (related, but not part of the
	 * interface).
	 * @param mappedName The mappedName to search for
	 * @return a SignetSubjectAttr that has a matching mappedName, or null
	 */
	public SignetSubjectAttr getAttribute(String mappedName)
	{
		SignetSubjectAttr retval = null;

		if (null != signetSubjectAttrs)
		{
			for (Iterator attrs = signetSubjectAttrs.iterator();
				attrs.hasNext() && (null == retval); )
			{
				SignetSubjectAttr attr = (SignetSubjectAttr)attrs.next();
				if (attr.getMappedName().equals(mappedName))
					retval = attr;
			}
		}

		return (retval);
	}


	/**
	 * Searchs for a SignetSubjectAttr that has a matching mappedName and returns
	 * the Source value, or null if the attribute was not found. If the attribute
	 * has multiple values, the value of the first (i.e. sequence==0) is returned.
	 * @param mappedName The Signet (mapped) attribute name.
	 * @return The SubjectAPI Subject's attribute value.
	 * @see edu.internet2.middleware.subject.Subject#getAttributeValue(java.lang.String)
	 */
	public String getAttributeValue(String mappedName)
	{
		String retval = null;

		Set values = getAttributeValues(mappedName);
		for (Iterator viter = values.iterator(); viter.hasNext() && (null == retval); )
		{
			SignetSubjectAttrValue value = (SignetSubjectAttrValue)viter.next();
			if (0 == value.getSequence())
				retval = value.getValue();
		}

		return (retval);
	}


	/**
	 * For multi-valued (e.g. comma-separated) attributes, returns a Set containing
	 * all values for a given mappedName. If no attribute is found that matches
	 * 'mappedName', then an empty Set is returned (never null!).
	 * @param mappedName The Signet-specific attribute name
	 * @return A Set of attribute values for the given key
	 * @see edu.internet2.middleware.subject.Subject#getAttributeValues(java.lang.String)
	 */
	public Set getAttributeValues(String mappedName)
	{
		Set retval = new HashSet();

		if ((null == mappedName) || (0 >= mappedName.length()))
			return (retval);

		if (null != signetSubjectAttrs)
		{
			for (Iterator attrs = signetSubjectAttrs.iterator();
					attrs.hasNext() && (0 >= retval.size()); )
			{
				SignetSubjectAttr attr = (SignetSubjectAttr)attrs.next();
				if (attr.getMappedName().equals(mappedName))
					retval.addAll(attr.getSourceValues());
			}
		}

		return (retval);
	}

	/**
	 * Returns a HashMap of mappedAttributeName (key) and SourceApi values (as a Set).
	 * @return A Map of name / Attribute Set pairs
	 * @see edu.internet2.middleware.subject.Subject#getAttributes()
	 */
	public Map getAttributes()
	{
		HashMap retval = new HashMap();

		if (null != signetSubjectAttrs)
		{
			for (Iterator attrs = signetSubjectAttrs.iterator(); attrs.hasNext();)
			{
//				Object attr_obj = attrs.next();
//System.out.println("SignetSubject.getAttributes: attr_obj is a " + attr_obj.getClass().getName());
//SignetSubjectAttr attr = (SignetSubjectAttr)attr_obj;
				SignetSubjectAttr attr = (SignetSubjectAttr)attrs.next();

				Set valueSet = new HashSet();
				for (Iterator values = attr.getSourceValues().iterator(); values.hasNext(); )
					valueSet.add(((SignetSubjectAttrValue)values.next()).getValue());

				retval.put(attr.getMappedName(), valueSet);
			}
		}

		return (retval);
	}

	/**
	 * Take care of the bidirectional assoc.
	 * Required by Hibernate
	 */
	public void addAttribute(SignetSubjectAttr attr)
	{
		if (null != attr)
		{
			signetSubjectAttrs.add(attr);
			attr.setParent(this);
		}
	}


	///////////////////////////////////////
	// Carryover from PrivilegedSubjectImpl
	///////////////////////////////////////

	public void setActingAs(SignetSubject actingAs) throws SignetAuthorityException
	{
		if (equals(actingAs))
		{
			// Acting as yourself is expressed as acting as nobody else.
			actingAs = null;
		}
    
		if (canActAs(actingAs, null))
		{
			this.actingAs = actingAs;
		}
		else
		{
			throw new SignetAuthorityException(new DecisionImpl(false, Reason.NO_PROXY, null));
		}
	}

	public SignetSubject getEffectiveEditor()
	{
		return (null == actingAs ? this : actingAs);
	}
  

	public boolean canActAs(SignetSubject actingAs, Subsystem subsystem)
	{
		// Everyone can act as nobody else, i.e. self
		if (null == actingAs)
			return true;

		Set proxies;
		proxies = Common.filterProxies(getProxiesReceived(), Status.ACTIVE);
		proxies = Common.filterProxies(proxies, subsystem);
		proxies = Common.filterProxiesByGrantor(proxies, actingAs);

		return (proxies.size() > 0);
	}


//TODO: This method must also check to see if all Limit-values in the current Assignment are grantable by by this
// PrivilegedSubject. If ANY of those Limit-values are beyond the capability of this PrivilegedSubject's granting abilities, then
// this Assignment is not editable by this PrivilegedSubject.
//
	/**
	 * This method returns true if this SignetSubject has authority to edit
	 * the argument Assignment.  Generally, if the Assignment's SignetSubject 
	 * matches this SignetSubject, then canEdit would return false, since in
	 * most situations it does not make sense to attempt to extend or modify
	 * your own authority.
	 * @throws SubjectNotFoundException
	 */
	public Decision canEdit(Grantable grantableInstance)
	{
		// Cannot edit INACTIVE records
		if (grantableInstance.getStatus().equals(Status.INACTIVE))
			return (new DecisionImpl(false, Reason.STATUS, null));

		SignetSubject effEditor = getEffectiveEditor();

		// Check to see if this editor and the grantee are the same.
		// No one, not even the Signet application subject, is allowed to grant
		// privileges to herself.
		if (effEditor.equals(grantableInstance.getGrantee()))
			return (new DecisionImpl(false, Reason.SELF, null));

		Decision retval = null;

		if (grantableInstance instanceof Assignment)
			retval = canEdit((Assignment)grantableInstance, effEditor);

		else if (grantableInstance instanceof Proxy)
			retval = canEdit((Proxy)grantableInstance, effEditor);

		if (null == retval)
			retval = new DecisionImpl(true, null, null);

		return (retval);
	}


	public Decision canEdit(Assignment assignment, SignetSubject effEditor)
	{
		// The Signet application can only do one thing: Grant (or edit) a Proxy to
		// a System Administrator. The Signet Application can never directly
		// grant (or edit) any Assignment to anyone.
		if (this.equals(signetSource.getSignet().getSignetSubject()))
			return (new DecisionImpl(false, Reason.CANNOT_USE, null));

		// If you're going to edit an Assignment while "acting as" someone
		// else, you must hold a "useable" Proxy from that other person.
		if ( !this.equals(effEditor) &&
				!canUseProxy(effEditor, assignment.getFunction().getSubsystem()))
			return (new DecisionImpl(false, Reason.CANNOT_USE, null));

		Decision retval = null;

		// Next, let's see whether or not this Assignment is in a Scope that we
		// can grant this particular Function in, and if so, whether or not this
		// Assignment has any Limit-values that exceed the ones we're allowed to
		// work with in this particular combination of Scope and Function.

		Set grantableScopes = getGrantableScopes(assignment.getFunction());
		boolean sufficientScopeFound = false;
		for (Iterator grantableScopesIterator = grantableScopes.iterator();
				grantableScopesIterator.hasNext() &&
				!sufficientScopeFound &&
				(null == retval);)
		{
			TreeNode myGrantableScope = (TreeNode)(grantableScopesIterator.next());
			if (myGrantableScope.equals(assignment.getScope()) ||
					myGrantableScope.isAncestorOf(assignment.getScope()))
			{
				sufficientScopeFound = true;
				// This scope is indeed one that we can grant this Function in.
				// Now, let's see whether or not we're allowed to work with all
				// of the Limit-values in this particular Assignment.
				Set limitValues = assignment.getLimitValues();
				for (Iterator limitValuesIterator = limitValues.iterator();
						limitValuesIterator.hasNext() && (null == retval);)
				{
					LimitValue limitValue = (LimitValue)(limitValuesIterator.next());
					Limit limit = limitValue.getLimit();
					Choice choice = null;
					try
					{
						choice = limit.getChoiceSet().getChoiceByValue(limitValue.getValue());
					}
					catch (Exception e)
					{
						throw new SignetRuntimeException(e);
					}

					Set grantableChoices = effEditor.getGrantableChoices(
							assignment.getFunction(), 
							assignment.getScope(),
							limit);
					if ( !grantableChoices.contains(choice))
						retval = new DecisionImpl(false, Reason.LIMIT, limit);
				}
			}
		}

		if (null == retval)
		{
			// Were none of our grantable Scopes high and mighty enough to edit this Assignment.
			if ( !sufficientScopeFound)
				retval = new DecisionImpl(false, Reason.SCOPE, null);
			else
				retval = new DecisionImpl(true, null, null);
		}

		return (retval);
	}


	public Decision canEdit(Proxy proxy, SignetSubject effEditor)
	{
		Decision retval = null;
		boolean eqEffEditor = this.equals(effEditor); // only do this once here

		// If you're going to edit a Proxy while "acting as" someone
		// else, you must hold an "extensible" Proxy from that other person.
		if ( !eqEffEditor &&
				!this.canExtendProxy(effEditor, proxy.getSubsystem()))
			retval = new DecisionImpl(false, Reason.CANNOT_EXTEND, null);

		// If you're "acting as" no one but yourself, then you can't edit
		// any Proxy that you didn't grant.
		else if (eqEffEditor && !this.equals(proxy.getGrantor()))
			retval = new DecisionImpl(false, Reason.SCOPE, null);

		else
			retval = new DecisionImpl(true, null, null);

		return (retval);
	}


	/**
	 * This method returns true if this PrivilegedSubject holds any "useable"
	 * proxies from the specified grantor. A "useable" proxy is one that can
	 * be used(!) to grant Assignments. A proxy that is not "useable" can be
	 * extended to another person, but may not be used to grant Assignments.
	 */
	public boolean canUseProxy(SignetSubject proxyGrantor, Subsystem subsystem)
	{
		Set proxies = getFilteredProxySet(proxyGrantor, subsystem);
		boolean retval = false;
		for (Iterator proxiesIterator = proxies.iterator();
					proxiesIterator.hasNext() && !retval;)
			retval = ((Proxy)proxiesIterator.next()).canUse();

		return (retval);
	}
  

	/**
	 * This method returns true if this PrivilegedSubject holds any "extensible"
	 * proxies from the specified grantor. An "extensible" proxy is one that can
	 * be used to grant Proxies. A proxy that is not "extensible" can be used to
	 * grant Assignments, but may not be used to extend Proxies.
	 */
	public boolean canExtendProxy(SignetSubject proxyGrantor, Subsystem subsystem)
	{
		Set proxies = getFilteredProxySet(proxyGrantor, subsystem);
		boolean retval = false;
		for (Iterator proxiesIterator = proxies.iterator();
					proxiesIterator.hasNext() && !retval;)
			retval = ((Proxy)proxiesIterator.next()).canExtend();

		return (retval);
	}


	/**
	 * From AssignmentImpl constructor... We still need to confirm the following points:
     *    3) At least one of those Proxies described in (2) above must have its
     *       "can use" flag set, thereby allowing this grantor to use that
     *       proxy to grant some Assignment.
     *
     *    4) At least one of the Proxies described in (3) above must encompass
     *       the Subsystem of this Assignment.
	 * @param fromGrantor
	 * @param subsystem
	 * @param returnReason
	 * @return true if Subject has usable proxy, otherwise false
	 */
	public boolean hasUsableProxy(
			SignetSubject fromGrantor, Subsystem subsystem, Reason[] returnReason)
	{
		boolean retval = false; // assume failure
		Set proxies = getFilteredProxySet(fromGrantor.getEffectiveEditor(), subsystem);

		if (0 == proxies.size())
			returnReason[0] = Reason.NO_PROXY;

		else
		{
			for (Iterator proxyIter = proxies.iterator(); proxyIter.hasNext() && !retval; )
			{
				Proxy proxy = (Proxy)(proxyIter.next());
				retval = proxy.canUse();
			}
		}

		return (retval);
	}
  
	/**
	 * From ProxyImpl constructor... We still need to confirm the following points:
	 * 
	 * 3) At least one of those Proxies described in (2) above must have its "can extend" flag set, thereby allowing this grantor to
	 * use that Proxy to grant some new Proxy.
	 * 
	 * 4) At least one of the Proxies described in (3) above must encompass the Subsystem of this Assignment.
	 * @param pSubject
	 * @param subsystem
	 * @param returnReason
	 * @return true if Subject has extensible proxy, otherwise false
	 */
	public boolean hasExtensibleProxy(
			SignetSubject pSubject, Subsystem subsystem, Reason[] returnReason)
	{
		boolean retval = false; // assume failure
		Set proxies = getFilteredProxySet(pSubject.getEffectiveEditor(), subsystem);

		if (0 == proxies.size())
			returnReason[0] = Reason.NO_PROXY;

		else
		{
			for (Iterator proxyIter = proxies.iterator(); proxyIter.hasNext() && !retval; )
			{
				Proxy proxy = (Proxy)(proxyIter.next());
				retval = proxy.canExtend();
			}
		}

		return (retval);
	}


    /**
     * Returns a Set of Proxy objects filtered by ACTIVE + Subsystem + Grantor
     * @param subject The EffectiveEditor ("acting as") SignetSubject
     * @param subsystem The Subsystem
     * @return Filtered Set of Proxy objects
     */
    protected Set getFilteredProxySet(SignetSubject subject, Subsystem subsystem)
	{
		Set proxies = Common.filterProxies(getProxiesReceived(), Status.ACTIVE);
		proxies = Common.filterProxies(proxies, subsystem);
		proxies = Common.filterProxiesByGrantor(proxies, subject);
		return (proxies);
	}


  	/**
	 * This method produces a Set of those Choice values which are grantable by this PrivilegedSubject in the context of the
	 * specified Function and scope.
	 * @param function The Function for which grantable Choices should be found.
	 * @param scope The TreeNode for which grantable Choices should be found.
	 * @param limit The Limit for which grantable Choices should be found.
	 * @return a Set of the grantable Choices for this combination of Function, Tree, and Limit.
	 */
	public Set getGrantableChoices(Function function, TreeNode scope, Limit limit)
	{
		// First, check to see if the we are the Signet superSubject.
		// That Subject can grant any Choice in any Limit in any Function in any
		// scope to anyone.
		if (hasSuperSubjectPrivileges(function.getSubsystem()))
			return UnmodifiableSet.decorate(limit.getChoiceSet().getChoices());

		// We're not the SignetSuperSubject, so let's find out what Limit-values
		// we've been assigned in relation to this Function, scope, and Limit.
		Set receivedLimitChoices = new HashSet();
		Iterator assignmentsReceivedIterator;
		Set assignments = this.getAssignmentsReceived();
		assignments = Common.filterAssignments(assignments, Status.ACTIVE);
		assignments = Common.filterAssignments(assignments, function.getSubsystem());
		assignments = Common.filterAssignments(assignments, function);
		assignmentsReceivedIterator = assignments.iterator();
		while (assignmentsReceivedIterator.hasNext())
		{
			Assignment assignmentReceived = (Assignment)(assignmentsReceivedIterator.next());
			if (assignmentReceived.getScope().equals(scope) || assignmentReceived.getScope().isAncestorOf(scope))
			{
				Set limitValuesReceived = assignmentReceived.getLimitValues();
				Iterator limitValuesReceivedIterator = limitValuesReceived.iterator();
				while (limitValuesReceivedIterator.hasNext())
				{
					LimitValue limitValue = (LimitValue)(limitValuesReceivedIterator.next());
					if (limitValue.getLimit().equals(limit))
					{
						try
						{
							receivedLimitChoices.add(limit.getChoiceSet().getChoiceByValue(limitValue.getValue()));
						}
						catch (ChoiceNotFoundException cnfe)
						{
							throw new SignetRuntimeException(cnfe);
						}
					}
				}
			}
		}
		// Now that we've discovered which Limit-values we've been assigned, let's
		// use that information to discover the whole set of Limit-values that we
		// could possibly assign to others. In the case of a multiple-select, we
		// could grant any of the values that were granted to us. In the case of
		// a single-select, we could grant any of the values that were of equal
		// or lesser rank than the greatest value that was granted to us.
		Set grantableChoices = new HashSet();
		Set allChoices = limit.getChoiceSet().getChoices();
		Iterator allChoicesIterator = allChoices.iterator();
		while (allChoicesIterator.hasNext())
		{
			Choice candidate = (Choice)(allChoicesIterator.next());
			if (limit.getSelectionType().equals(SelectionType.SINGLE))
			{
				if (candidate.doesNotExceed(receivedLimitChoices))
				{
					grantableChoices.add(candidate);
				}
			}
			else if (limit.getSelectionType().equals(SelectionType.MULTIPLE))
			{
				if (receivedLimitChoices.contains(candidate))
				{
					grantableChoices.add(candidate);
				}
			}
			else
			{
				throw new SignetRuntimeException("Unexpected selection-type '" + limit.getSelectionType()
						+ "' encountered in PrivilegedSubject.getGrantableChoices().");
			}
		}
		return UnmodifiableSet.decorate(grantableChoices);
	}


	public Set getGrantableScopes(Function aFunction)
	{
		Set grantableScopes = new HashSet();
		// First, check to see if the we are the Signet superSubject.
		// That Subject can grant any function at any scope to anyone.
		if (hasSuperSubjectPrivileges(aFunction.getSubsystem()))
		{
			Tree tree = aFunction.getSubsystem().getTree();
			if (tree != null)
				grantableScopes.addAll(tree.getRoots());
		}

		else
		{
			Set grantableAssignments = getGrantableAssignments(aFunction);
			Iterator grantableAssignmentsIterator = grantableAssignments.iterator();
			while (grantableAssignmentsIterator.hasNext())
			{
				Assignment grantableAssignment = (Assignment)(grantableAssignmentsIterator.next());
				grantableScopes.add(grantableAssignment.getScope());
			}
		}

		return UnmodifiableSet.decorate(grantableScopes);
	}


	private Set getGrantableAssignments(Function function)
	{
		Set assignments = new HashSet();

		for (Iterator iterator = getEffectiveEditor().getAssignmentsReceived().iterator();
				iterator.hasNext(); )
		{
			Assignment assignment = (Assignment)(iterator.next());
			if (assignment.canGrant() &&
					assignment.getFunction().equals(function))
				assignments.add(assignment);
		}

		return (assignments);
	}


	private Collection getGrantableAssignments(Subsystem subsystem)
	{
		Collection assignments = new HashSet();
		Iterator iterator = filterAssignments(getEffectiveEditor().getAssignmentsReceived(), Status.ACTIVE).iterator();
		while (iterator.hasNext())
		{
			Assignment assignment = (Assignment)(iterator.next());
			if (assignment.canGrant() && ((subsystem == null) || assignment.getFunction().getSubsystem().equals(subsystem)))
			{
				assignments.add(assignment);
			}
		}
		return assignments;
	}


	public Set getGrantableFunctions(Category category)
	{
		// First, check to see if the we are the Signet superSubject.
		// That Subject can grant any function in any category to anyone.
		if (hasSuperSubjectPrivileges(category.getSubsystem()))
			return category.getFunctions();

		Set functions = new HashSet();
		Iterator iterator = filterAssignments(getEffectiveEditor().getAssignmentsReceived(), Status.ACTIVE).iterator();
		while (iterator.hasNext())
		{
			Assignment assignment = (Assignment)(iterator.next());
			Function candidateFunction = assignment.getFunction();
			Category candidateCategory = candidateFunction.getCategory();
			if (assignment.canGrant() && candidateCategory.equals(category))
				functions.add(candidateFunction);
		}

		return UnmodifiableSet.decorate(functions);
	}


	protected Set filterAssignments(Set all, Status status)
	{
		Set statusSet = new HashSet();
		statusSet.add(status);
		return filterAssignments(all, statusSet);
	}

	protected Set filterAssignments(Set all, Set statusSet)
	{
		if ((null == statusSet) || (null == all))
			return all;

		Set subset = new HashSet();
		for (Iterator assigns = all.iterator(); assigns.hasNext(); )
		{
			Assignment candidate = (Assignment)(assigns.next());
			if (statusSet.contains(candidate.getStatus()))
				subset.add(candidate);
		}

		return (subset);
	}

	protected Set filterProxies(Set all, Status status)
	{
		Set statusSet = new HashSet();
		statusSet.add(status);
		return filterProxies(all, statusSet);
	}

	protected Set filterProxies(Set all, Set statusSet)
	{
		if (statusSet == null)
		{
			return all;
		}
		Set subset = new HashSet();
		Iterator iterator = all.iterator();
		while (iterator.hasNext())
		{
			Proxy candidate = (Proxy)(iterator.next());
			if (statusSet.contains(candidate.getStatus()))
			{
				subset.add(candidate);
			}
		}
		return subset;
	}


	private Set filterProxiesByGrantor(Set all, SignetSubject grantor)
	{
		if ((null == grantor) || (null == all))
			return all;

		Set subset = new HashSet();
		for (Iterator iterator = all.iterator(); iterator.hasNext(); )
		{
			Proxy candidate = (Proxy)(iterator.next());
			if (candidate.getGrantor().equals(grantor))
				subset.add(candidate);
		}
    
		return subset;
	}

	// First, determine who the effectiveEditor is. That's the PrivilegedSubject
	// we're "acting for", if anyone, or ourself, if we're not "acting for"
	// another.
	//
	// Then, we have three possibilities to consider:
	//
	// 1) We are "acting for" ourself only.
	//
	// In this case, we just look through our grantable Assignments,
	// plucking the Subsystem from each.
	//
	// 2) We are "acting for" the Signet subject.
	//
	// In this case, we look through our usable Proxies that we've
	// received from the Signet subject, plucking the Subsystem
	// from each, keeping in mind that the NULL Subsystem indicates
	// that we can grant in any Subsystem that's present in the system.
	//
	// 3) We are "acting for" some other, garden-variety subject.
	//
	// In this case, we look through the usable Proxies that we've
	// received from that other subject, plucking the Subsystem
	// from each, keeping in mind that the NULL Subsystem indicates
	// that we can grant in any Subsystem that our Proxy-grantor
	// can grant in. This set of Subsystems is our "Proxied Subsystems".
	//
	// Then, armed with that list of Subsystems, we look through the
	// grantable Assignments held by our Proxy-grantor, examining the
	// Subsystem of each. If a grantable Assignment's Subsystem is also
	// found in our set of Proxyied Subsystems, then we add it to our list
	// of grantable subsystems.
	//
	public Set getGrantableSubsystemsForAssignment()
	{
		Set grantableSubsystems = new HashSet();
		if (getEffectiveEditor().equals(this))
		{
			// We are acting for no one but ourselves.
			Collection grantableAssignments = this.getGrantableAssignments((Subsystem)null);
			Iterator grantableAssignmentsIterator = grantableAssignments.iterator();
			while (grantableAssignmentsIterator.hasNext())
			{
				Assignment grantableAssignment = (Assignment)(grantableAssignmentsIterator.next());
				grantableSubsystems.add(grantableAssignment.getFunction().getSubsystem());
			}
			return grantableSubsystems;
		}
		else if (getEffectiveEditor().equals(signetSource.getSignet().getSignetSubject()))
		{
			// We are acting for the Signet subject.
			Set proxies = this.getProxiesReceived();
			proxies = filterProxies(proxies, Status.ACTIVE);
			proxies = filterProxiesByGrantor(proxies, signetSource.getSignet().getSignetSubject());
			Iterator proxiesIterator = proxies.iterator();
			while (proxiesIterator.hasNext())
			{
				Proxy proxy = (Proxy)(proxiesIterator.next());
				if (proxy.canUse())
				{
					Subsystem proxySubsys = proxy.getSubsystem();
					if (proxySubsys == null)
					{
						Set subsyss = signetSource.getSignet().getSubsystems();
						for (Iterator candidates = subsyss.iterator(); candidates.hasNext(); )
						{
							Subsystem candidate = (Subsystem)(candidates.next());
							if (((SubsystemImpl)candidate).isPopulatedForGranting())
							{
								grantableSubsystems.add(candidate);
							}
						}
						grantableSubsystems.addAll(subsyss);
					}
					else if (((SubsystemImpl)proxySubsys).isPopulatedForGranting())
						grantableSubsystems.add(proxySubsys);
				}
			}
			return grantableSubsystems;
		}
		else
		{
			// We are acting for some other subject who is not the Signet subject.
			Set proxies = this.getProxiesReceived();
			proxies = filterProxies(proxies, Status.ACTIVE);
			proxies = filterProxiesByGrantor(proxies, this.getEffectiveEditor());
			Set proxiedSubsystems = new HashSet();
			Iterator proxiesIterator = proxies.iterator();
			while (proxiesIterator.hasNext())
			{
				Proxy proxy = (Proxy)(proxiesIterator.next());
				if (proxy.canUse())
				{
					if (proxy.getSubsystem() == null)
					{
						// We can grant every subsystem that's grantable by the subject
						// we're "acting for".
						return this.getEffectiveEditor().getGrantableSubsystemsForAssignment();
					}
					proxiedSubsystems.add(proxy.getSubsystem());
				}
			}
			// Now that we have the set of Proxied Subsystems, let's get the set of
			// our Proxy-grantor's grantable subsystems, and return the intersection
			// of those sets.
			proxiedSubsystems.retainAll(this.getEffectiveEditor().getGrantableSubsystemsForAssignment());
			return proxiedSubsystems;
		}
	}

	/**
	 * First, determine who the effectiveEditor is. That's the PrivilegedSubject we're "acting for", if anyone, or ourself, if we're
	 * not "acting for" another.
	 * 
	 * Then, we have two possibilities to consider:
	 * 
	 * 1) We are "acting for" ourself only.
	 * 
	 * In this case, we can grant a Proxy for any Subsystem, without regard to any Assignments or Proxies we currently hold.
	 * 
	 * 2) We are "acting for" another.
	 * 
	 * In this case, we look through the extensible Proxies that we've received from that other PrivilegedSubject, plucking the
	 * Subsystem from each, keeping in mind that the NULL Subsystem indicates that we can extend a Proxy for any Subsystem.
	 */
	public Set getGrantableSubsystemsForProxy()
	{
		Set grantableSubsystems = new HashSet();
		if (this.equals(getEffectiveEditor()))
		{
			grantableSubsystems = signetSource.getSignet().getPersistentDB().getSubsystems();
		}
		else
		{
			Set proxiesReceived;
			proxiesReceived = Common.filterProxies(getProxiesReceived(), Status.ACTIVE);
			proxiesReceived = Common.filterProxiesByGrantor(proxiesReceived, getEffectiveEditor());
			// Iterator proxiesReceivedIterator = proxiesReceived.iterator();
			for (Iterator iter = proxiesReceived.iterator(); iter.hasNext();)
			{
				Proxy proxy = (Proxy)(iter.next());
				if (proxy.canExtend())
				{
					if (proxy.getSubsystem() == null)
					{
						grantableSubsystems = signetSource.getSignet().getPersistentDB().getSubsystems();
					}
					else
					{
						grantableSubsystems.add(proxy.getSubsystem());
					}
				}
			}
		}
		return grantableSubsystems;
	}


	public Set getGrantableCategories(Subsystem subsystem)
	{
		if (hasSuperSubjectPrivileges(subsystem))
		{
			return subsystem.getCategories();
		}
		Set grantableCategories = new HashSet();
		Collection grantableAssignments = getGrantableAssignments(subsystem);
		Iterator assignmentsIterator = grantableAssignments.iterator();
		while (assignmentsIterator.hasNext())
		{
			Assignment assignment = (Assignment)(assignmentsIterator.next());
			grantableCategories.add(assignment.getFunction().getCategory());
		}
		return UnmodifiableSet.decorate(grantableCategories);
	}


	private boolean hasSuperSubjectPrivileges(Subsystem subsystem)
	{
		boolean retval = false;

		SignetSubject privSubj = signetSource.getSignet().getSignetSubject();
		// First, check to see if the we are the Signet superSubject.
		// That Subject can grant any privilege in any category to anyone.
		if (getEffectiveEditor().equals(privSubj))
		{
			// We're either the SignetSuperSubject or we're "acting as" that
			// esteemed personage. If we're just "acting as", then we need to make
			// sure that our set of active Proxies actually includes this Subystem.
			Set proxies = getFilteredProxySet(privSubj, subsystem);
			retval = (this.equals(privSubj)) || (0 < proxies.size());
		}

		return (retval);
	}


	public Assignment grant(
			SignetSubject grantee,
			TreeNode scope,
			Function function,
			Set limitValues,
			boolean canUse, boolean canGrant,
			Date effectiveDate, Date expirationDate)
		throws SignetAuthorityException
	{
		if ( !isPersisted())
			save();
		if ( !grantee.isPersisted())
			grantee.save();

		Assignment newAssignment = new AssignmentImpl(
				signetSource.getSignet(), this, grantee, scope, function, limitValues,
				canUse, canGrant, effectiveDate, expirationDate);
		newAssignment.save();

		return (newAssignment);
	}


	public Proxy grantProxy(SignetSubject grantee, Subsystem subsystem,
			boolean canUse, boolean canExtend,
			Date effectiveDate,	Date expirationDate)
		throws SignetAuthorityException
	{
		if (grantee == null)
		{
			throw new IllegalArgumentException("Cannot grant a Proxy to a NULL grantee.");
		}

		Signet mySignet = signetSource.getSignet();
		Proxy newProxy = new ProxyImpl(mySignet, this, grantee, subsystem,
				canUse, canExtend, effectiveDate, expirationDate);
		newProxy.save();

		return (newProxy);
	}


	/*
	 * (non-Javadoc)
	 * @see edu.internet2.middleware.signet.PrivilegedSubject#getPrivileges()
	 */
	public Set getPrivileges()
	{
		Set privileges = new HashSet();

		Set assignments = getAssignmentsReceived();
		assignments = Common.filterAssignments(assignments, Status.ACTIVE);
		for (Iterator assignmentsIterator = assignments.iterator(); assignmentsIterator.hasNext(); )
		{
			Assignment assignment = (Assignment)(assignmentsIterator.next());
			Set assignmentPrivileges = PrivilegeImpl.getPrivileges(assignment);
			privileges.addAll(assignmentPrivileges);
		}

		return privileges;
	}


	public Set reconcile()
	{
		return (reconcile(new Date()));
	}


	/**
	 * For all received Assignments and Proxies, reconcile by the given date
	 * @param date The Date to reconcile against
	 * @return A Set of Grantables whose status changed due to reconciliation,
	 * may be an empty Set, but never null.
	 */
	public Set reconcile(Date date)
	{
		Set changedGrantables = new HashSet();

		if (null == date)
			return (changedGrantables);

		Signet signet = signetSource.getSignet();
		Reconciler recon = new Reconciler(signet, signet.getPersistentDB());

		changedGrantables.addAll(recon.reconcileGrantables(getAssignmentsReceived(), date));
		changedGrantables.addAll(recon.reconcileGrantables(getProxiesReceived(), date));

		return (changedGrantables);
	}


	////////////////////////////////////
	// implements Comparable
	////////////////////////////////////

	/**
	 * String compares this SignetSubject.getName() to the target
	 * @param target A SignetSubject
	 * @return -1 if this.name .lt. target.name, 0 if this.name == target.name, +1 if this.name .gt. target.name
	 */
	public int compareTo(Object target)
	{
		int retval = -1; // assume failure
		String thisName = getName();
		if ((null != thisName) && (null != target) && (target instanceof SignetSubject))
		{
			String otherName = ((SignetSubject)target).getName();
			retval = thisName.compareToIgnoreCase(otherName);
		}
		return (retval);
	}


  	////////////////////////////////////
	// overrides Object
	////////////////////////////////////

	/**
	 * @return A String that represents this SignetSubject
	 */
	public String toString()
	{
		StringBuffer buf = new StringBuffer();

		buf.append("Key=\"" + ((null != subject_PK) ? subject_PK.toString() : "null") + "\" ");
		buf.append("Id=\"" + subjectId + "\" ");
		buf.append("Type=\"" + subjectType + "\" ");
		buf.append("Name=\"" + subjectName + "\" ");
		buf.append("ModifyDate=\"" + dateToString(modifyDatetime) + "\" ");
		buf.append("SynchDate=\"" + dateToString(synchDatetime) + "\" ");
		buf.append("SourceId=\"" + getSourceId() + "\" ");
		buf.append("SubjectAttrs:" + attrsToString());

		return (buf.toString());
	}

	protected String dateToString(Date date)
	{
		String dateStr;

		if (null != date)
			dateStr = DateFormat.getInstance().format(date);
		else
			dateStr = "(null)";

		return (dateStr);
	}

	/** @return a formatted String of the SignetSubjectAttrs */
	protected String attrsToString()
	{
		StringBuffer buf = new StringBuffer();

		if (null != signetSubjectAttrs)
		{
			for (Iterator attrs = signetSubjectAttrs.iterator(); attrs.hasNext();)
			{
//Object o = attrs.next();
//if ( !(o instanceof SignetSubjectAttr))
//{
//	buf.append("ATTR IS A " + o.getClass().getName() + " ");
//	Thread.currentThread().dumpStack();
//}
//else
//{
				SignetSubjectAttr attr = (SignetSubjectAttr)attrs.next();
//SignetSubjectAttr attr = (SignetSubjectAttr)o;
				buf.append(attr.toString() + " ");
//}
			}
		}
		else
			buf.append("(none)");

		return (buf.toString());
	}

	public int hashCode()
	{
		return (toString().hashCode());
	}

	public boolean equals(Object o)
	{
		if (o instanceof SignetSubject)
			return (equals((SignetSubject)o));
		else if (o instanceof Subject)
			return (equals((Subject)o));
		else
			return (false);
	}

	/**
	 * Field-by-field comparison to another SignetSubject
	 * @return true if all fields are equal, otherwise false
	 */
	protected boolean equals(SignetSubject sigSubject)
	{
//System.out.println("SignetSubject.equals(SignetSubject): top");
//		if (null == sigSubject)
//			return (false);
//System.out.println("SignetSubject.equals(SignetSubject): comparing this to that...");
//System.out.println("  this=" + toString());
//System.out.println("  that=" + sigSubject.toString());
//boolean eq = toString().equals(sigSubject.toString());
//System.out.println("  this " + (eq ? "equals" : "not equal to") + " that ");
//		// quick (to implement) and dirty
//		return (eq);
		boolean retval = false; // assume failure
		if (null != sigSubject)
		{
			if (retval = valuesEqual(subjectId, sigSubject.getId())) // yes, I do mean "="
				if (retval = valuesEqual(subjectType, sigSubject.getType().getName())) // yes, I do mean "="
					if (retval = valuesEqual(subjectName, sigSubject.getName())) // yes, I do mean "="
						if (retval = valuesEqual(sourceId, sigSubject.getSourceId())) // yes, I do mean "="
							retval = compareAttributes(sigSubject);
		}
		return (retval);
	}

	/**
	 * Field-by-field comparison to a Subject
	 * @return true if equivilent fields are equal, otherwise false
	 */
	protected boolean equals(Subject apiSubject)
	{
		if (null == apiSubject)
			return (false);

		boolean retval;

		if (apiSubject instanceof SignetSubject)
			retval = equals((SignetSubject)apiSubject);
		else
		{
			if (retval = valuesEqual(subjectId, apiSubject.getId())) // yes, I do mean "="
				if (retval = valuesEqual(subjectType, apiSubject.getType().getName())) // yes, I do mean "="
					if (retval = valuesEqual(subjectName, apiSubject.getName())) // yes, I do mean "="
						if (retval = valuesEqual(sourceId, apiSubject.getSource().getId())) // yes, I do mean "="
							retval = compareSubjApiAttrs(signetSubjectAttrs, apiSubject.getAttributes());
		}

		return (retval);
	}


	/** Compare two Strings for equality. Does a few more checks than String.equals() */
	protected boolean valuesEqual(String value1, String value2)
	{
		boolean retval = false; // assume failure

		// if they're both null, they're equal
		if ((null == value1) && (null == value2))
			retval = true;

		// if they're both not null, and they compare, they're equal
		else if ((null != value1) && (null != value2))
			retval = value1.equals(value2);

		// else one is null and the other is not... they're not equal

		return (retval);
	}

	/**
	 * Compare the Attributes between this and another SignetSubject.
	 * This method DOES NOT dereference the SignetSubject's attribute name mapping
	 * because the attributes names should already be identical.
	 * @param The SignetSubject that contains the attributes to compare (passing the
	 * whole SignetSubject because getAttributes returns an unwieldy Map)
	 * @return true if all attribute's values are equal, otherwise false
	 */
	protected boolean compareAttributes(SignetSubject subject)
	{
		if (null == subject)
			return (false);

		Map otherAttrs = subject.getAttributes();
		if (null == otherAttrs)
			return (false);

		// if list sizes are not equal, they're not equal
		boolean retval = (signetSubjectAttrs.size() == otherAttrs.size());

		if (retval)
		{
			// for each of my attributes, get the corresponding attribute for
			// the other SignetSubject, and compare values. Break on
			// first non-equal value.
			for (Iterator myAttrs = signetSubjectAttrs.iterator();
					myAttrs.hasNext() && retval; )
			{
				SignetSubjectAttr myAttr = (SignetSubjectAttr)myAttrs.next();
				SignetSubjectAttr otherAttr = subject.getAttribute(myAttr.getMappedName());
				retval = myAttr.equals(otherAttr);
			}
		}

		return (retval);
	}


	/**
	 * Compare the Attributes for a SignetSubject and a Subject (from SubjectAPI).
	 * This method must dereference the SignetSubject's attribute name mapping
	 * to match the SubjectAPI's attribute names before the value comparison
	 * can be performed.
	 * @param sigAttrs A Set of SignetSubjectAttr objects
	 * @param apiAttrs A Map of Attr Name (key) / Attr Values (a Set!) pairs
	 * @return true if all attribute's values are equal, otherwise false
	 */
	protected boolean compareSubjApiAttrs(Set sigAttrs, Map apiAttrs)
	{
		// if both are null, they're equal
		if ((null == sigAttrs) && (null == apiAttrs))
			return (true);

		// if one is null and the other is not, they're not equal
		if ((null == sigAttrs) || (null == apiAttrs))
			return (false);

		// if list sizes are not equal, they're not equal
		boolean retval = (sigAttrs.size() == apiAttrs.size());

		if (retval)
		{
			// for each signet attribute, deref the mapped name, get the
			// corresponding SubjectAPI attr, and compare values. Break on
			// first non-equal value.
			Hashtable mappedNames = signetSource.getMappedAttributes();
			for (Iterator attrs = sigAttrs.iterator();
					attrs.hasNext() && retval;)
			{
				SignetSubjectAttr attr = (SignetSubjectAttr)attrs.next();
				String apiAttrName = (String)mappedNames.get(attr.getMappedName());
				Set apiValues = (Set)apiAttrs.get(apiAttrName);
				retval = compareAttrValues(attr.getValuesAsStringSet(), apiValues);
			}
		}

		return (retval);
	}


	/**
	 * Compares the values associated with a SignetSubjectAttr and a Subject API attribute value Set
	 * @param sigSubjAttrValues A Set of Strings
	 * @param apiAttrValues A Set of Strings
	 * @return true if a values are equal, otherwise false
	 */
	protected boolean compareAttrValues(Set sigSubjAttrValues, Set apiAttrValues)
	{
		// if both are null, they're equal
		if ((null == sigSubjAttrValues) && (null == apiAttrValues))
			return (true);

		// if one is null and the other is not, they're not equal
		if ((null == sigSubjAttrValues) || (null == apiAttrValues))
			return (false);

		// if list sizes are not equal, they're not equal
		if (sigSubjAttrValues.size() != apiAttrValues.size())
			return (false);

		boolean retval = sigSubjAttrValues.containsAll(apiAttrValues);
		return (retval);
	}

}
