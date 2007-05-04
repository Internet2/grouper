/*
$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/subjsrc/SignetSource.java,v 1.7 2007-05-04 20:43:03 ddonn Exp $

Copyright (c) 2006 Internet2, Stanford University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

	@author ddonn
*/
package edu.internet2.middleware.signet.subjsrc;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * Signet implementation of the SubjectAPI's Source interface. SignetSource acts
 * as a wrapper for the SubjectAPI's Source. The mapping between these objects is
 * defined in the SubjectSources.xml configuration file.
 */
public class SignetSource implements Source
{
	/** Status of this Source */
	public static final String		STATUS_ACTIVE			= "active";
	/** Status of this Source */
	public static final String		STATUS_INACTIVE			= "inactive";
//	public static final String		TYPE_DEFAULT			= "any";
	/** Default Usage value */
	public static final String		USAGE_DEFAULT			= SignetSources.SIGNET_USAGE_ALL;

	// logging
	protected Log			log;

	/** Application and SubjectAPI Source 'id' */
	protected String		id;
	/** SubjectAPI source name, if sourceName is specified in SubjectSources.xml it overrides name from Sources.xml */
	protected String		name;
	/** the Signet type of this Source */
	protected String		type;
	/** active or inactive */
	protected String		status;
	/** indicates whether to run in degraded mode if SubjectAPI is unavailable */
	protected boolean		failover;
	/** list of usage categories */
	protected Vector		usage;
	/** Signet's attributes-of-interest. Key=mappedAttribute name, Value=sourceAttriubute name.
	    Note that the mappedAttribute and it's corresponding sourceValue are
	    stored in each SignetSubject. */
	protected Hashtable		mappedAttributes;

	/** Reference to the real SubjectAPI Source that this is wrapping */
	protected Source		apiSource;

	/** Reference to SignetSources (i.e. back up the chain of command) */
	protected SignetSources	signetSources;


	/** default constructor */
	public SignetSource()
	{
		log = LogFactory.getLog(this.getClass());
		id = null;
		name = null;
		type = null;
		status = STATUS_INACTIVE;
		failover = false;
		usage = new Vector();
		mappedAttributes = new Hashtable();
		apiSource = null;
	}


	/**
	 * @return The SignetSources that owns this SignetSource
	 */
	public SignetSources getSources()
	{
		return (signetSources);
	}

	/** Set the parent (SignetSources) of this Source */
	public void setSources(SignetSources parent)
	{
		signetSources = parent;
	}


	/**
	 * @return the instance of Signet
	 */
	public Signet getSignet()
	{
		return ((null != signetSources) ? signetSources.getSignet() : null);
	}


	/**
	 * Tell this SignetSource about the SubjectAPI SourceManager, then lookup
	 * the corresponding SubjectAPI Source based on the previously-set sourceId.
	 * @param sourceManager The SubjectAPI SourceManager instance
	 */
	public void setSourceManager(SourceManager sourceManager)
	{
		try { apiSource = sourceManager.getSource(id); }
		catch (SourceUnavailableException e)
		{
			log.error("SignetSource.setSourceManager: Source not found for SourceId=" + id);
		}
	}


	/////////////////////////////////
	// Wrapper methods to support Digister
	// (Note: the method Digetster.addSetProperties(String, String[], String[])
	// was unreliable: worked sometimes, didn't work other times.
	/////////////////////////////////

	/**
	 * Add a usage to this source. Called by Digester when parsing SubjectSources.xml.
	 * Supports Digester and SubjectSources.xml parsing
	 */
	public void addUsage(String usageStr)
	{
		addValueListToVector(usageStr, usage);
	}

	/**
	 * Tests to see if this SignetSource supports the given usage (as defined in SubjectSources.xml)
	 * @param usage The usage to test for
	 * @return True is usage is a match, false otherwise.
	 */
	public boolean hasUsage(String usage)
	{
		return (this.usage.contains(usage));
	}

	/**
	 * @return Returns the Vector of all usage values
	 */
	public Vector getUsage()
	{
		return (usage);
	}


	/**
	 * Supports Digester and SubjectSources.xml parsing
	 * @param status
	 */
	public void setStatus(String status)
	{
		this.status = status;
	}

	/**
	 * Supports Digester and SubjectSources.xml parsing
	 * @return Return the status (e.g. active | inactive)
	 */
	public String getStatus()
	{
		return (status);
	}


	/**
	 * Supports Digester and SubjectSources.xml parsing
	 * @return Returns the failover.
	 */
	public String getFailover()
	{
		return (Boolean.toString(failover));
	}

	/**
	 * Supports Digester and SubjectSources.xml parsing
	 * @param failover The failover to set.
	 */
	public void setFailover(String failover)
	{
		this.failover = Boolean.parseBoolean(failover);
	}


	/**
	 * Add a mapped attribute to this Signet Source. Logs a warning and replaces
	 * the existing value, if the appAttr was mapped prior to this method call.
	 * Supports Digester and SubjectSources.xml parsing
	 * @param appAttr The application-specific attribute identifier
	 * @param sourceAttr The source-specific attribute identifier
	 */
	public void addMappedAttribute(String appAttr, String sourceAttr)
	{
		if ((null == appAttr) || (0 >= appAttr.length()) ||
				(null == sourceAttr) || (0 >= sourceAttr.length()))
		{
			log.warn("SignetSource.addMappedAttribute: Warning - " +
					"invalid parameter " + "(" + appAttr + ", " + sourceAttr + ")");
			return;
		}

		String oldValue = (String)mappedAttributes.put(appAttr, sourceAttr);
		if (null != oldValue)
			log.warn("SignetSource.addMappedAttribute: Warning - " +
					"previous value in Signet Source \"" + name + "\"" +
					" for mappedAttribute \"" + appAttr + "\"" +
					" has been replaced" +
					" (was \"" + oldValue +	"\"" +
					", now \"" + sourceAttr + "\").");
	}

	/**
	 * Get a Source's mapped attributes
	 */
	public Hashtable getMappedAttributes()
	{
		return (mappedAttributes);
	}


	/** Utility method to add comma-separated list of values to a Vector */
	protected void addValueListToVector(String valueList, Vector v)
	{
		if ((null != valueList) && (0 < valueList.length()) && (null != v))
		{
			valueList = valueList.trim();
			String[] values = valueList.split(",");
			for (int i = 0; i < values.length; i++)
			{
				String tmpValue = values[i].trim();
				if ((0 < tmpValue.length()) && ( !v.contains(tmpValue)))
					v.add(tmpValue);
			}
		}
	}


	/**
	 * @see edu.internet2.middleware.subject.Source#getId()
	 * Supports Digetster and SubjectSources.xml parsing: maps to xml property sourceId
	 */
	public String getSourceId()
	{
		return (getId());
	}

	/**
	 * @see edu.internet2.middleware.subject.Source#setId(java.lang.String)
	 * Supports Digetster and SubjectSources.xml parsing: maps to xml property sourceId
	 */
	public void setSourceId(String id)
	{
		setId(id);
	}


	/**
	 * @see edu.internet2.middleware.subject.Source#getName()
	 * Supports Digetster and SubjectSources.xml parsing: maps to xml property sourceName
	 */
	public String getSourceName()
	{
		return (getName());
	}

	/**
	 * @see edu.internet2.middleware.subject.Source#setName(String)
	 * Supports Digetster and SubjectSources.xml parsing: maps to xml property sourceName
	 */
	public void setSourceName(String name)
	{
		setName(name);
	}


	///////////////////////////////
	// implements Source
	///////////////////////////////

	/**
	 * @see edu.internet2.middleware.subject.Source#getId()
	 */
	public String getId()
	{
		return (id);
	}

	/**
	 * @see edu.internet2.middleware.subject.Source#setId(java.lang.String)
	 */
	public void setId(String id)
	{
		this.id = id;
	}


	/**
	 * @see edu.internet2.middleware.subject.Source#getName()
	 */
	public String getName()
	{
		return (name);
	}

	/**
	 * @see edu.internet2.middleware.subject.Source#setName(String)
	 */
	public void setName(String name)
	{
		this.name = name;
	}


	/**
	 * @see edu.internet2.middleware.subject.Source#getSubjectTypes()
	 */
	public Set getSubjectTypes()
	{
		Set set = new HashSet(1);
		set.add(type);
		return (set);
	}

	/**
	 * @see edu.internet2.middleware.subject.Source#getSubjectTypes()
	 */
	public String getSubjectType()
	{
		return (type);
	}

	/**
	 * Set the type of this SignetSource. Called by Digester when parsing SubjectSources.xml.
	 * @param type The type to associate with this source.
	 */
	public void setSubjectType(String type)
	{
		this.type = type;
	}

	/**
	 * Tests to see if this SignetSource is of the given type.
	 * @param type The type to test for
	 * @return True if this SignetSource matches the given type, false otherwise.
	 */
	public boolean isSubjectType(String type)
	{
		return (this.type.equals(type));
	}

	/**
	 * Lookup the Subject from the SubjectAPI, based on subjectId.
	 * @param subjectId The Subject ID
	 * @return Returns a SignetSubject matching the ID, or null
	 * @see edu.internet2.middleware.subject.Source#getSubject(java.lang.String)
	 */
	public Subject getSubject(String subjectId)
	{
		if (null == apiSource)
			return (null);

		SignetSubject retval = null;
		try
		{
			Subject apiSubj = apiSource.getSubject(subjectId);
			retval = new SignetSubject(this, apiSubj);
		}
		catch (SubjectNotFoundException snfe)
		{
			log.warn(snfe);
		}
		catch (SubjectNotUniqueException snue)
		{
			log.warn(snue);
		}

		return (retval);
	}

	/**
	 * @see edu.internet2.middleware.subject.Source#getSubjectByIdentifier(java.lang.String)
	 */
	public Subject getSubjectByIdentifier(String id)
	{
		Subject retval = null;

		if ((null == id) || (0 >= id.length()))
			return (retval);

		if (null == apiSource)
			return (retval);

		try
		{
			Subject apiSubject = apiSource.getSubjectByIdentifier(id);
			retval = new SignetSubject(this, apiSubject);
		}
		catch (SubjectNotFoundException snfe)
		{
			log.info(snfe);
		}
		catch (SubjectNotUniqueException snue)
		{
			log.warn(snue);
		}

		return (retval);
	}

	/**
	 * Returns a Set of SignetSubject objects that match the searchValue. If no
	 * Subjects are found that match the searchValue, an empty Set is returned.
	 * @param searchValue The search criteria
	 * @return A Set of SignetSubjects that match searchValue, or an empty set (never null!)
	 * @see edu.internet2.middleware.subject.Source#search(java.lang.String)
	 */
	public Set search(String searchValue)
	{
		Set retval = new HashSet();

		if ((null == searchValue) || (0 >= searchValue.length()))
			return (retval);

		if (null == apiSource)
			return (retval);

		Set results = apiSource.search(searchValue);
		if (null != results)
		{
			for (Iterator subjects = results.iterator(); subjects.hasNext();)
			{
				Subject subj = (Subject)subjects.next();
				SignetSubject sigSubj = new SignetSubject(this, subj);
				retval.add(sigSubj);
			}
		}

		return (retval);
	}

	/**
	 * @see edu.internet2.middleware.subject.Source#init()
	 */
	public void init()
	{
	}


	//////////////////////////////////
	// Signet enhancements to Source
	//////////////////////////////////

	/**
	 * Returns a Vector of SignetSubject objects from the SubjectAPI Source that
	 * this SignetSource represents. It is implied that only Subject objects are
	 * returned that match the Type specified for this SignetSource in SubjectSources.xml
	 * @return A Vector of SignetSubject objects, or empty Vector (never null!)
	 */
	public Vector getSubjects()
	{
		Vector retval = new Vector(search(type));

		return (retval);
	}


	//////////////////////////////////
	// overrides Object
	//////////////////////////////////

	/**
	 * Returns a formatted String representation of SignetSource.
	 */
	public String toString()
	{
		return (new String(
				"SignetSource: Id=\"" + getId() + "\" " +
				"Name=\"" + getName() + "\" " +
				"Status=\"" + getStatus() + "\" " +
				"Failover=\"" + getFailover() + "\" " +
				"Type=\"" + getSubjectType() + "\" " +
				"\n" +
				mappedAttributesToString() + " \n" +
				vectorToString("Usage", usage)));
	}

	/**
	 * Returns a formatted String representation of the mapped attributes.
	 */
	protected String mappedAttributesToString()
	{
		StringBuffer retval = new StringBuffer("Mapped Attributes (mappedName=sourceName): ");
		if ((null != mappedAttributes) && (0 < mappedAttributes.size()))
		{
			for (Enumeration keys = mappedAttributes.keys(); keys.hasMoreElements();)
			{
				String mappedName = (String)keys.nextElement();
				String sourceName = (String)mappedAttributes.get(mappedName);
				retval.append(mappedName + "=" + sourceName);

				if (keys.hasMoreElements())
					retval.append(" | ");
			}
		}
		else
			retval.append("(none defined)");

		return (retval.toString());
	}

//	/**
//	 * Returns a formatted String representing all of this Source's
//	 * _persisted_ Subjects.
//	 */
//	protected String subjectsToString()
//	{
//		StringBuffer retval = new StringBuffer("Subjects:\n");
//
//		if (null != persistedSubjects)
//		{
//			for (Enumeration subj = persistedSubjects.keys(); subj.hasMoreElements();)
//			{
//				PrivilegedSubject psubject = (PrivilegedSubject)subj.nextElement();
//				retval.append(psubject.toString() + "\n");
//			}
//		}
//
//		return (retval.toString());
//	}
//
	/**
	 * @param label A label to place at the beginning of the returned string
	 * @param v A Vector of objects to be concatenated into the returned string
	 * @return a formatted String of comma-separated values preceded by a label.
	 */
	protected String vectorToString(String label, Vector v)
	{
		StringBuffer buf = new StringBuffer(label + ":");
		if ((null != v) && (0 < v.size()))
		{
			for (Iterator i = v.iterator(); i.hasNext(); )
			{
				buf.append(i.next());
				if (i.hasNext())
					buf.append(",");
			}
		}
		else
			buf.append("(none defined)");

		buf.append(" ");
		return (buf.toString());
	}


}
