/*
 * $Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/subjsrc/SignetSubjectAttr.java,v 1.3 2006-11-30 04:21:49 ddonn Exp $
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
 * 
 */
package edu.internet2.middleware.signet.subjsrc;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * An "attribute of interest" that is persisted with it's corresponding Subject.
 * It may have one or more values as SignetSubjectAttrValue objects.
 */
public class SignetSubjectAttr
{
	/** DB primary key */
	protected Long		subjectAttr_PK;

	/** DB foreign key to a SignetSubject record */
	protected Long		subject_FK;

	/** Reference to my parent (support for Hibernate bidirectional assoc.) 
	 * which essentially duplicates subject_FK
	 */
	protected SignetSubject	parent;

	/**
	 * Mapped attribute name as defined in SubjectSources.xml. Note that the
	 * SubjectAPI's attribute name (the name that is mappped _to_) is only
	 * maintained in the SignetSource that owns the SignetSubject that owns
	 * this SignetSubjectAttr. mappedName is the Signet-internal attribute name
	 * that has been homogenized across all Sources. 
	 */
	protected String	mappedName;

	/** Source attribute values from the original Source stored as SignetSubjectAttrValue objects*/
	protected List		sourceValues;

	/** date/time stamp of the most recent update of the persisted value */
	protected Date		modifyDate;


	/** default constructor */
	public SignetSubjectAttr()
	{
		mappedName = null;
		sourceValues = new ArrayList();
		refreshModifyDate();
		subjectAttr_PK = null;
		subject_FK = null;
		parent = null;
	}

	/**
	 * Create an attribute with the Signet-mapped name and no values
	 * @param mappedName
	 */
	public SignetSubjectAttr(String mappedName)
	{
		this();
		this.mappedName = mappedName;
	}

	/**
	 * Create an attribute with the Signet-mapped name and one source value
	 * @param mappedName
	 * @param sourceValue
	 */
	public SignetSubjectAttr(String mappedName, SignetSubjectAttrValue sourceValue)
	{
		this(mappedName);
		addSourceValue(sourceValue);
	}

	/**
	 * Create an attribute with the Signet-mapped name and Set of source values
	 * @param mappedName
	 * @param sourceValues A Set of SignetSubjectAttrValue objects
	 */
	public SignetSubjectAttr(String mappedName, List sourceValues)
	{
		this(mappedName);
		setSourceValues(sourceValues);
	}


	/**
	 * @return the subjectAttr_PK
	 */
	protected Long getSubjectAttr_PK()
	{
		return subjectAttr_PK;
	}

	/**
	 * @param subjectAttr_PK the subjectAttr_PK to set
	 */
	protected void setSubjectAttr_PK(Long subjectAttrKey)
	{
		this.subjectAttr_PK = subjectAttrKey;
	}


	/**
	 * @return Returns the signetSubject DB key.
	 */
	protected Long getSubject_FK()
	{
		return (subject_FK);
	}

	/**
	 * @param signetSubject Set the signetSubject DB key.
	 */
	protected void setSubject_FK(Long signetSubjectKey)
	{
		this.subject_FK = signetSubjectKey;
	}

	/**
	 * Take care of Hibernate bidirectional assoc.
	 */
	public void setParent(SignetSubject subject)
	{
		parent = subject;
		setSubject_FK((null != subject) ? subject.getSubject_PK() : null);
	}

	/**
	 * Take care of Hibernate bidirectional assoc.
	 */
	public SignetSubject getParent()
	{
		return (parent);
	}


	/**
	 * @return The mapped attribute name
	 */
	public String getMappedName()
	{
		return (mappedName);
	}

	/**
	 * @param mappedName The name to set.
	 */
	public void setMappedName(String mappedName)
	{
		this.mappedName = mappedName;
	}


	/**
	 * @return A List of SignetSubjectAttrValue objects, may be empty but never null
	 */
	public List getSourceValues()
	{
		return(sourceValues);
	}

	/**
	 * Replace this Attribute's set of Values and sets each Value's parent.
	 * Support for Hibernate.
	 * @param sourceValues A List of SignetSubjectAttrValue objects
	 */
	public void setSourceValues(List sourceValues)
	{
		if (null != (this.sourceValues = sourceValues)) // yes, I do mean "="
		{
			for (Iterator iter = this.sourceValues.iterator(); iter.hasNext(); )
				((SignetSubjectAttrValue)iter.next()).setParent(this);
		}
	}

	/**
	 * Replace the Attribute's set of Values and sets each Value's parent and sequence.
	 * @param strValues A Set of Strings
	 */
	public void setSourceValues(Set strValues)
	{
		this.sourceValues.clear();

		if (null != strValues)
		{
			for (Iterator strs = strValues.iterator(); strs.hasNext(); )
				addSourceValue((String)strs.next());
		}
	}


	/**
	 * Add another value to this attribute. Assumes the value's 0-based sequence
	 * has already been set. Sets the value's parent reference.
	 * @param value The SignetSubjectAttrValue to add
	 */
	public void addSourceValue(SignetSubjectAttrValue value)
	{
		if (null != value)
		{
			int seq = (int)value.getSequence();
			sourceValues.add(seq, value);
			value.setParent(this);
			refreshModifyDate();
		}
	}

	/**
	 * Add another value to this attribute. Sets the value's sequence to the next
	 * highest (1-based) number.
	 * @param strValue A String to convert to a SignetSubjectAttrValue, then add
	 */
	public void addSourceValue(String strValue)
	{
		if (null != strValue)
		{
			SignetSubjectAttrValue value = new SignetSubjectAttrValue(strValue);
			value.setSequence(sourceValues.size());
			addSourceValue(value);
		}
	}

	/**
	 * Remove the specified value from this attribute
	 * @param value The value to remove from this attribute
	 * @return true if value was removed, otherwise false
	 */
	public boolean removeSourceValue(SignetSubjectAttrValue value)
	{
//TODO need to resequence the remaining value records!
		boolean retval = sourceValues.remove(value);
		if (retval)
			refreshModifyDate();
		return (retval);
	}


	/**
	 * @return A set of String objects representing the value(s) of this attribute,
	 * may be an empty Set, but never null.
	 */
	public Set getValuesAsStringSet()
	{
		Set retval = new HashSet();

		if (null == sourceValues)
			return (retval);

		// Convert the Set of SignetSubjectAttrValue objects into a Set of Strings
		for (Iterator values = sourceValues.iterator(); values.hasNext(); )
		{
			String tmpValue = ((SignetSubjectAttrValue)values.next()).getValue();
			retval.add(tmpValue);
		}

		return (retval);
	}


	/**
	 * @return The modifyDate
	 */
	public Date getModifyDate()
	{
		return (modifyDate);
	}

	/**
	 * @param modifyDate The modifyDate to set.
	 */
	public void setModifyDate(Date modifyDate)
	{
		this.modifyDate = modifyDate;
	}

	/**
	 * Sets the ModifyDate to the current system time.
	 */
	public void refreshModifyDate()
	{
		setModifyDate(new Date(System.currentTimeMillis()));
	}


	public boolean isPersisted()
	{
		boolean retval;

		// check my status first
		retval = (null != subjectAttr_PK) && (0L < subjectAttr_PK.longValue());

		if (retval && (null != sourceValues)) // check children's status if necessary
		{
			for (ListIterator vals = sourceValues.listIterator(); (vals.hasNext()) && retval; )
			{
				SignetSubjectAttrValue val = (SignetSubjectAttrValue)vals.next();
				retval = val.isPersisted();
			}
		}

		return (retval);
	}

	
	////////////////////////////////////////
	// overrides Object
	////////////////////////////////////////

	/** Returns a String with the following format: mappedName=sourceValue,....
	 * Does not return ModifyDate value for now but could be added later if needed.
	 * @return A String with the following format: mappedName=sourceValue,...
	 */
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(mappedName);
		buf.append("(key=" + ((null != subjectAttr_PK) ? subjectAttr_PK.toString() : "(null)"));
//		buf.append(",subject_PK=" + ((null != subject_FK) ? subject_FK.toString() : "(null)") + ")" + "=");

		for (Iterator values = sourceValues.iterator(); values.hasNext(); )
			buf.append("\"" + values.next() + "\"" + (values.hasNext() ? "," : ""));

		return (buf.toString());
	}

	public boolean equals(Object o)
	{
		if (o instanceof SignetSubjectAttr)
			return (equals((SignetSubjectAttr)o));
		else
			return (false);
	}

	/** Deep compare of <mappedName> and <sourceValues> only  */
	public boolean equals(SignetSubjectAttr attr)
	{
		boolean retval = false; // assume failure

		if (null != attr)
		{
			if (retval = mappedName.equals(attr.getMappedName())) // yes, I do mean "="
			{
				List otherValues = attr.getSourceValues();
				if (retval = sourceValues.size() == otherValues.size()) // yes, I do mean "="
				for (int i = 0; (i < sourceValues.size()) && retval; i++)
				{
					SignetSubjectAttrValue myValue = (SignetSubjectAttrValue)sourceValues.get(i);
					SignetSubjectAttrValue otherValue = (SignetSubjectAttrValue)otherValues.get(i);
					retval = myValue.equals(otherValue);
				}
			}
		}

		return (retval);
	}

	public int hashCode()
	{
		return (toString().hashCode());
	}

}
