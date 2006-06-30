/*
$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/sources/SignetSubjectSources.java,v 1.1 2006-06-30 02:04:41 ddonn Exp $

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
package edu.internet2.middleware.signet.sources;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.collections.set.UnmodifiableSet;
import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.PrivilegedSubjectImpl;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SignetRuntimeException;
import edu.internet2.middleware.signet.resource.ResLoaderApp;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.provider.SourceManager;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;

/**
 * This class provides support to load Signet-to-SubjectAPI attribute mappings.
 * Apache Commons Digester is used to parse the XML configuration file. Digester
 * invokes various methods in SignetSubjectSources and SignetSubjectSource to capture
 * settings specifed in the XML.
 */
public class SignetSubjectSources extends Hashtable
{
	protected Log			log = LogFactory.getLog(SignetSubjectSources.class);

	protected Signet		signet;

	protected SourceManager sourceManager;


	/**
	 * Constructor
	 * @param signet The instance of Signet
	 * @param configFile An XML file containing the Signet-to-Source attribute mappings
	 */
	public SignetSubjectSources(Signet signet, String configFile)
	{
		super();

		this.signet = signet;

		if ((null != configFile) && (0 < configFile.length()))
		{
			if (parseConfigFile(configFile))
				log.debug("SignetSubjectSources.SignetSubjectSources:\n  " + this);
			else
				log.error("SignetSubjectSources.SignetSubjectSources: problems parsing Subject-Source file \"" +
						configFile + "\"");
		}
	}


	/**
	 * Parse the configuration file
	 * @param configFile
	 * @return true on success, false otherwise
	 */
	protected synchronized boolean parseConfigFile(String configFile)
	{
		boolean status = false; // assume failure

		Digester digester = new Digester();
		digester.setValidating(false);
		digester.push(this);

		digester.addObjectCreate("signetSubjectSources/signetSubjectSource", SignetSubjectSource.class);
		digester.addSetProperties("signetSubjectSources/signetSubjectSource");
		digester.addSetNext("signetSubjectSources/signetSubjectSource", "addSubjectSource");

		digester.addCallMethod("signetSubjectSources/signetSubjectSource/subjectType", "addSubjectType", 2);
		digester.addCallParam("signetSubjectSources/signetSubjectSource/subjectType", 0, "signet");
		digester.addCallParam("signetSubjectSources/signetSubjectSource/subjectType", 1, "source");

		digester.addCallMethod("signetSubjectSources/signetSubjectSource/usage", "addUsage", 1);
		digester.addCallParam("signetSubjectSources/signetSubjectSource/usage", 0);

		digester.addCallMethod("signetSubjectSources/signetSubjectSource/outputXml", "addOutputXml", 1);
		digester.addCallParam("signetSubjectSources/signetSubjectSource/outputXml", 0);

		digester.addCallMethod("signetSubjectSources/signetSubjectSource/mappedAttribute", "addMappedAttribute", 2);
		digester.addCallParam("signetSubjectSources/signetSubjectSource/mappedAttribute", 0, "app");
		digester.addCallParam("signetSubjectSources/signetSubjectSource/mappedAttribute", 1, "source");

		try
		{
			InputStream inStream = getClass().getResourceAsStream(configFile);
			digester.parse(inStream);
			status = true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (SAXException e)
		{
			e.printStackTrace();
		}
		finally
		{
			digester = null;
		}

		return (status);
	}


	public synchronized void addSubjectSource(SignetSubjectSource subjSrc) //throws Exception
	{
		if (null != subjSrc)
		{
			String srcId = subjSrc.getId();
			if ((null != srcId) && (0 < srcId.length()))
			{
				if (null == get(srcId))
					put(srcId, subjSrc);
				else
					log.info("A SignetSubjectSource with ID=\"" + srcId + "\" already exists. Ignoring.");
			}
		}
	}


	/**
	 * @param subjectSourceId Used as the key to find a SignetSubjectSource previously
	 * loaded during parse().
	 * @return The SignetSubjectSource with the given subjectSourceId
	 */
	public synchronized SignetSubjectSource getSubjectSource(String subjectSourceId)
	{
		return ((SignetSubjectSource)get(subjectSourceId));
	}


	/**
	 * @param subjectSourceId Used as the key to find a SignetSubjectSource previously
	 * loaded during parse().
	 * @param signetAttribute Signet's attribute, used as a key to find the Source attributes
	 * @return A Vector of Source attribute names associated with the given Signet attribute
	 * or null if not found.
	 */
	public synchronized String getSourceAttribute(String subjectSourceId, String signetAttribute)
	{
		String retval = null;

		SignetSubjectSource subjSrc = getSubjectSource(subjectSourceId);
		if (null != subjSrc)
			retval = subjSrc.getSourceAttribute(signetAttribute);

		return (retval);
	}

	/**
	 * Finds the Signet attribute that is mapped to the Source attribute. Note that
	 * if multiple Signet attributes are mapped to a single Source attribute
	 * (shouldn't happen, but there's nothing in SignetSubjectSource to prevent it)
	 * then the first-found match will be returned.
	 * @param subjectSourceId Used as the key to find a SignetSubjectSource previously
	 * loaded during parse().
	 * @param sourceAttribute The Source attribute, used to find the associated Signet attribute
	 * @return A String, or null, for the given Source attribute
	 */
	public synchronized String getSignetAttribute(String subjectSourceId, String sourceAttribute)
	{
		String retval = null;

		SignetSubjectSource subjSrc = getSubjectSource(subjectSourceId);
		if (null != subjSrc)
			retval = subjSrc.getSignetAttribute(sourceAttribute);

		return (retval);
	}


	///////////////////////////////////
	// Source Manager stuff
	///////////////////////////////////


	protected SourceManager getSourceManager()
	{
		if (null == sourceManager)
		{
			try { sourceManager = SourceManager.getInstance(); }
			catch (Exception e)
			{
				throw new SignetRuntimeException(ResLoaderApp.getString("Signet.msg.exc.srcMgr"), e); //$NON-NLS-1$
			}
		}
   
		return sourceManager;
	}


	/**
	 * Gets a single PrivilegedSubject by its underlying Subject.
	 * 
	 * @param subject
	 * @return the specified PrivilegedSubject
	 * @throws ObjectNotFoundException
	 */
	public PrivilegedSubject getPrivilegedSubject(Subject subject)
	{
		PrivilegedSubject pSubject;
		try
		{
			pSubject = getPrivilegedSubject(subject.getType().getName(), subject.getId());
		}
		catch (ObjectNotFoundException onfe)
		{
			// This should never happen - we already have the Subject in hand,
			// so the method we just called should not have failed to find it.
			throw new SignetRuntimeException(onfe);
		}
		return pSubject;
	}
 
	/**
	 * Gets a single PrivilegedSubject by type and ID.
	 * 
	 * @param subjectTypeId
	 * @param subjectId
	 * @return the specified PrivilegedSubject
	 * @throws ObjectNotFoundException
	 */
	public PrivilegedSubject getPrivilegedSubject(String subjectTypeId, String subjectId) throws ObjectNotFoundException
	{
		PrivilegedSubject pSubject;
		try
		{
			pSubject = signet.getPersistentDB().fetchPrivilegedSubject(subjectTypeId, subjectId);
		}
		catch (ObjectNotFoundException onfe)
		{
			// We've got to fetch the specified Subject from the SubjectAdapter,
			// and build a new PrivilegedSubject around it.
			Subject subject = getSubject(subjectTypeId, subjectId);
			pSubject = new PrivilegedSubjectImpl(signet, subject);
		}
		return pSubject;
	}


	/**
	 * Finds a set of Subjects which matches the argument search value.
	 * 
	 * @param searchValue
	 * @return the set of matching Subjects
	 */
	public Set findPrivilegedSubjects(String searchValue)
	{
		Set pSubjects = new HashSet();

		for (Iterator iter = getSourceManager().getSources().iterator(); iter.hasNext(); )
		{
			Set result = ((Source)iter.next()).search(searchValue);
			for (Iterator iter2 = result.iterator(); iter2.hasNext();)
			{
				PrivilegedSubject pSubject = getPrivilegedSubject((Subject)iter2.next());
				pSubjects.add(pSubject);
			}
		}

		return pSubjects;
	}


	/**
	 * Gets a single PrivilegedSubject by its type and displayID.
	 * 
	 * @param subjectTypeId
	 * @param displayId
	 * @return the specified PrivilegedSubject
	 * @throws ObjectNotFoundException if the PrivilegedSubject is not found.
	 */
	public PrivilegedSubject getPrivilegedSubjectByDisplayId(String subjectTypeId, String displayId) throws ObjectNotFoundException
	{
		Subject subject = getSubjectByDisplayId(subjectTypeId, displayId);
		if (subject == null)
		{
			throw new ObjectNotFoundException(ResLoaderApp.getString("Signet.msg.exc.privSubjByDisplay")); //$NON-NLS-1$
		}
		return getPrivilegedSubject(subject);
	}


	/**
	 * Gets PrivilegedSubjects by type and display ID.
	 * 
	 * @param subjectTypeId
	 * @param displayId
	 * @return Set of PrivilegedSubjects
	 */
	public Set getPrivilegedSubjectsByDisplayId(String subjectTypeId, String displayId)
	{
		Set pSubjects = new HashSet();

		for (Iterator iter = getSources(subjectTypeId).iterator(); iter.hasNext();)
		{
			try
			{
				Subject result = ((Source)iter.next()).getSubjectByIdentifier(displayId);
				PrivilegedSubject pSubject = getPrivilegedSubject(result);
				pSubjects.add(pSubject);
			}
			catch (SubjectNotFoundException snfe)
			{
			}
		}

		return UnmodifiableSet.decorate(pSubjects);
	}


	/**
	 * Gets a single Subject by type and display ID.
	 * 
	 * @param subjectTypeId
	 * @param displayId
	 * @return the specified Subject
	 * @throws ObjectNotFoundException
	 */
	public Subject getSubjectByDisplayId(String subjectTypeId, String displayId) throws ObjectNotFoundException
	{
		Subject subject = null;
		for (Iterator iter = getSources(subjectTypeId).iterator(); iter.hasNext();)
		{
			try
			{
				Source source = (Source)(iter.next());
				subject = source.getSubjectByIdentifier(displayId);
			}
			catch (SubjectNotFoundException snfe)
			{
				// Don't do anything since we may find the subject
				// in other sources.
			}
		}
		if (subject == null)
		{
			throw new ObjectNotFoundException(ResLoaderApp.getString("Signet.msg.exc.subjectDisplayIdNotFound")); //$NON-NLS-1$
		}

		return subject;
	}

	/**
	 * Finds a set of Subjects by type and search value.
	 * 
	 * @param subjectTypeId
	 * @param searchValue
	 * @return the specified Subjects
	 */
	public Set findPrivilegedSubjects(String subjectTypeId, String searchValue)
	{
		Set pSubjects = new HashSet();

		for (Iterator iter = getSources(subjectTypeId).iterator(); iter.hasNext(); )
		{
			Set result = ((Source)iter.next()).search(searchValue);
			for (Iterator iter2 = result.iterator(); iter2.hasNext();)
			{
				PrivilegedSubject pSubject = getPrivilegedSubject((Subject)iter2.next());
				pSubjects.add(pSubject);
			}
		}

		return pSubjects;
	}


	/**
	 * Gets a single Subject by type and ID.
	 * 
	 * @param subjectTypeId
	 * @param subjectId
	 * @return the specified Subject
	 * @throws ObjectNotFoundException
	 */
	public Subject getSubject(String subjectTypeId, String subjectId) throws ObjectNotFoundException
	{
		Subject retval = null;

		// Special case: A null subjectTypeId and subjectId will yield a null Subject.
		if ((null == subjectTypeId) && (null == subjectId))
			return (retval);

		// Special case: Is it the current signet application subject?
		PrivilegedSubject privSubject = signet.getSignetSubject();
		if ((null != signet) &&
				(null != privSubject) &&
				(privSubject.getSubjectTypeId().equals(subjectTypeId) &&
				privSubject.getSubjectId().equals(subjectId)))
		{
			retval = privSubject.getSubject();
		}

		else
		{
			for (Iterator iter = getSources(subjectTypeId).iterator(); iter.hasNext();)
			{
				try { retval = ((Source)iter.next()).getSubject(subjectId); }
				catch (SubjectNotFoundException snfe)
				{
					// Don't do anything since we may find the subject in other sources.
				}
			}
	
			if (null == retval)
			{
				Object[] msgData = new Object[] { subjectId, subjectTypeId };
				MessageFormat msg = new MessageFormat(ResLoaderApp.getString("Signet.msg.exc.subjectNotFound")); //$NON-NLS-1$
				throw new ObjectNotFoundException(msg.format(msgData));
			}
		}

		return (retval);
	}

	/**
	 * Returns Source adapters which supports the argument SubjectType.
	 * @return Collection of Source adapters
	 */
	public Collection getSources(String subjectTypeId)
	{
		SubjectType type = SubjectTypeEnum.valueOf(subjectTypeId);

		Collection sources = getSourceManager().getSources(type);

		// Sometimes, SourceManager.getSources() returns null. We want to avoid doing that.
		if (sources == null)
			sources = new HashSet();

		return sources;
	}
 

	////////////////////////////////////
	// overrides Object
	////////////////////////////////////

	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		for (Enumeration e = keys(); e.hasMoreElements();)
		{
			SignetSubjectSource subjSrc = (SignetSubjectSource)get(e.nextElement());
			buf.append(subjSrc.toString() + "\n");
			if (e.hasMoreElements())
				buf.append("----------\n");
		}

		return (buf.toString());
	}


	////////////////////////////////
	// for testing only
	////////////////////////////////

	public static void main(String[] args)
	{
//System.out.println("main: working dir = " + System.getProperty("user.dir"));
		new SignetSubjectSources(null, "/subjectSources.xml");
	}

}
