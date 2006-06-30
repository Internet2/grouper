/*
$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/sources/SignetSubjectSource.java,v 1.1 2006-06-30 02:04:41 ddonn Exp $

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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;


public class SignetSubjectSource
{
	public static final String		statusActive = "active";
	public static final String		statusInactive = "inactive";

	protected String		id;
	protected String		name;
	protected boolean		status;
	protected boolean		failover;
	protected Hashtable		types;
	protected Vector		usage;
	protected Vector		outputXml;
	protected Hashtable		mappedAttributes;

	/**
	 * 
	 */
	public SignetSubjectSource()
	{
		super();
		status = true;
		failover = true;
		types = new Hashtable();
		usage = new Vector();
		outputXml = new Vector();
		mappedAttributes = new Hashtable();
	}


	/**
	 * @return Returns the id.
	 */
	public synchronized String getId()
	{
		return id;
	}

	/**
	 * @param id The id to set.
	 */
	public synchronized void setId(String id)
	{
		this.id = id;
	}


	/**
	 * @return Returns the name.
	 */
	public synchronized String getName()
	{
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public synchronized void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return Returns the status.
	 */
	public synchronized String getStatus()
	{
		return (status ? statusActive : statusInactive);
	}

	/**
	 * @param status The status to set.
	 */
	public synchronized void setStatus(String state)
	{
		if ((null != state) && (0 < state.length()))
		{
			if (state.equalsIgnoreCase(statusActive))
				status = true;
			else if (state.equalsIgnoreCase(statusInactive))
				status = false;
			else
				status = false;
		}
		else
			status = false;
	}


	/**
	 * @return Returns the failover.
	 */
	public synchronized String getFailover()
	{
		return (Boolean.toString(failover));
	}


	/**
	 * @param failover The failover to set.
	 */
	public synchronized void setFailover(String failover)
	{
		this.failover = Boolean.parseBoolean(failover);
	}


	/**
	 * @return Returns the types.
	 */
	public synchronized Hashtable getTypes()
	{
		return types;
	}


	/**
	 * @return Returns the Source types associated with the Signet type key.
	 */
	public synchronized Vector getSourceTypes(String signetTypeKey)
	{
		Vector retval = null;

		if ((null != signetTypeKey) && (0 < signetTypeKey.length()))
			retval = (Vector)types.get(signetTypeKey);

		return (retval);
	}


	/**
	 * @return Returns the usage.
	 */
	public synchronized Vector getUsage()
	{
		return usage;
	}


	/**
	 * @return Returns the outputXml.
	 */
	public synchronized Vector getOutputXml()
	{
		return outputXml;
	}


	public synchronized void addMappedAttribute(String app, String source)
	{
		String oldValue = (String)mappedAttributes.put(app, source);
		if (null != oldValue)
//TODO: replace with Logger message
System.out.println("SignetSubjectSource.addMappedAttribute: Warning - previous value for mappedAttribute \"" +
	app + "\" has been replaced (was \"" + oldValue + "\", now \"" + source + "\").");

//		Vector sources = (Vector)mappedAttributes.get(app);
//		if (null == sources)
//			sources = new Vector();
//		if ( !sources.contains(source))
//			sources.add(source);
//		mappedAttributes.put(app, sources);
	}


	public synchronized void addSubjectType(String signetType, String sourceType)
	{
		sourceType = sourceType.trim();
		Vector sources = (Vector)types.get(signetType);
		if (null == sources)
			sources = new Vector();
		if ( !sources.contains(sourceType))
			sources.add(sourceType);
		types.put(signetType, sources);
	}


	public synchronized void addUsage(String usageStr)
	{
		if ((null != usageStr) && (0 < usageStr.length()))
		{
			usageStr = usageStr.trim();
			String[] values = usageStr.split(",");
			for (int i = 0; i < values.length; i++)
			{
				String tmpValue = values[i].trim();
				if ((0 < tmpValue.length()) && ( !usage.contains(tmpValue)))
					usage.add(tmpValue);
			}
		}
	}


	public synchronized void addOutputXml(String outputXmlStr)
	{
		if ((null != outputXmlStr) && (0 < outputXmlStr.length()))
		{
			outputXmlStr = outputXmlStr.trim();
			String[] values = outputXmlStr.split(",");
			for (int i = 0; i < values.length; i++)
			{
				String tmpValue = values[i].trim();
				if ((0 < tmpValue.length()) && ( !outputXml.contains(tmpValue)))
					outputXml.add(tmpValue);
			}
		}
	}


	/**
	 * @param signetAttribute Signet's attribute, used as a key to find the Source attributes
	 * @return The Source attribute name associated with the given Signet attribute
	 * or null if not found.
	 */
	public synchronized String getSourceAttribute(String signetAttribute)
	{
		return ((String)mappedAttributes.get(signetAttribute));
	}

	/**
	 * Finds the Signet attribute that is mapped to the Source attribute. Note that
	 * if multiple Signet attributes are mapped to a single Source attribute
	 * (shouldn't happen, but there's nothing in SignetSubjectSource to prevent it)
	 * then the first-found match will be returned.
	 * @param sourceAttribute The Source attribute, used to find the associated Signet attribute
	 * @return A String, or null, for the given Source attribute
	 */
	public synchronized String getSignetAttribute(String sourceAttribute)
	{
		String retval = null;

		for (Enumeration e = mappedAttributes.keys(); e.hasMoreElements() && (null == retval);)
		{
			String key = (String)e.nextElement();
			String value = (String)mappedAttributes.get(e);
			if (value.equals(sourceAttribute))
				retval = key;
//			Vector srcAttrs = (Vector)mappedAttributes.get(e);
//			if (srcAttrs.contains(sourceAttribute))
//				retval = key;
		}

		return (retval);
	}


	////////////////////////////////////
	// overrides Object
	////////////////////////////////////

	public String toString()
	{
		return ("SignetSubjectSource: Id=\"" + getId() + "\" " +
				"Name=\"" + getName() + "\" " +
				"Status=\"" + getStatus() + "\" " +
				"Failover=\"" + getFailover() + "\" " +
				"\n" +
				mappedAttributesToString() + "\n" +
				subjectTypesToString() + "\n" +
				usageToString() + "\n" +
				outputXmlToString());
	}

	protected String mappedAttributesToString()
	{
		StringBuffer attrs = new StringBuffer("Mapped Attributes (app=src): ");
		if ((null != mappedAttributes) && (0 < mappedAttributes.size()))
		{
			for (Enumeration e = mappedAttributes.keys(); e.hasMoreElements();)
			{
				String attrKey = (String)e.nextElement();
				String attrValue = (String)mappedAttributes.get(attrKey);
//				StringBuffer values = new StringBuffer();
//				for (Iterator i = attrValue.iterator(); i.hasNext();)
//					values.append(i.next() + ",");
//				if (e.hasMoreElements())
//					values.replace(values.length() - 1, values.length(), " | ");
//				else
//					values.deleteCharAt(values.length() - 1);
//				attrs.append(attrKey + "=" + values);
				attrs.append(attrKey + "=" + attrValue);
				if (e.hasMoreElements())
					attrs.append(" | ");
			}
		}
		else
			attrs.append("(none defined)");

		return (attrs.toString());
	}

	protected String subjectTypesToString()
	{
		StringBuffer typeList = new StringBuffer("Types: ");
		if ((null != types) && (0 < types.size()))
		{
			for (Enumeration e = types.keys(); e.hasMoreElements();)
			{
				String typeKey = (String)e.nextElement();
				Vector typeValue = getSourceTypes(typeKey);
				StringBuffer values = new StringBuffer();
				for (Iterator i = typeValue.iterator(); i.hasNext();)
					values.append(i.next() + ",");
				if (e.hasMoreElements())
					values.replace(values.length() - 1, values.length(), " | ");
				else
					values.deleteCharAt(values.length() - 1);
				typeList.append(typeKey + "=" + values);
			}
		}
		else
			typeList.append("(none defined)");

		return (typeList.toString());
	}

	protected String usageToString()
	{
		StringBuffer usageList = new StringBuffer("Usage: ");
		if ((null != usage) && (0 < usage.size()))
		{
			for (Iterator i = usage.iterator(); i.hasNext();)
				usageList.append(i.next() + ",");
			usageList.deleteCharAt(usageList.length() - 1);
		}
		else
			usageList.append("(none defined)");

		return (usageList.toString());
	}

	protected String outputXmlToString()
	{
		StringBuffer outputXmlList = new StringBuffer("OutputXml: ");
		if ((null != outputXml) && (0 < outputXml.size()))
		{
			for (Iterator i = outputXml.iterator(); i.hasNext();)
				outputXmlList.append(i.next() + ",");
			outputXmlList.deleteCharAt(outputXmlList.length() - 1);
		}
		else
			outputXmlList.append("(none defined)");

		return (outputXmlList.toString());
	}

}
