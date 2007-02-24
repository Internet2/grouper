/*
 * $Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/subjsrc/SignetSubjectAttr.java,v 1.4 2007-02-24 02:11:31 ddonn Exp $
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

import java.text.DateFormat;
import java.util.Date;

/**
 * An "attribute of interest" that is persisted with it's corresponding Subject.
 * It may have one or more values as SignetSubjectAttrValue objects.
 */
public class SignetSubjectAttr
{
	public static final String ATTR_TYPE_STRING = "string";
	public static final String ATTR_TYPE_INT = "integer";
	public static final String ATTR_TYPE_FLOAT = "float";
	public static final String ATTR_TYPE_DATE = "date";

	/** DB primary key */
	protected Long		subjectAttr_PK;

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

	/** The attribute's value */
	protected String	attr_value;

	/** The attribute's type (e.g. string, integer, float, etc.) */
	protected String	attr_type;

	/** date/time stamp of the most recent update of the persisted value */
	protected Date		modifyDate;

	/** the sequence number for multi-valued attributes */
	protected long sequence;

	/** default constructor */
	public SignetSubjectAttr()
	{
		mappedName = null;
		attr_value = null;
		attr_type = ATTR_TYPE_STRING;
		refreshModifyDate();
		subjectAttr_PK = null;
		parent = null;
		modifyDate = new Date(System.currentTimeMillis());
	}

	/**
	 * Copy constructor does a deep copy of origAttr
	 * @param origAttr
	 */
	public SignetSubjectAttr(SignetSubjectAttr origAttr)
	{
		this();
		if (null != origAttr)
		{
			setAttr_value(origAttr.getAttr_value());
			setAttr_type(origAttr.getAttr_type());
			setMappedName(origAttr.getMappedName());
			setSequence(origAttr.getSequence());
			setParent(origAttr.getParent());
		}
	}

	/**
	 * Create an attribute with the Signet-mapped name and no values
	 * @param mappedName
	 */
	public SignetSubjectAttr(String mappedName)
	{
		this();
		setMappedName(mappedName);
		this.mappedName = mappedName;
	}

	public SignetSubjectAttr(String mappedName, String value)
	{
		this(mappedName);
		setAttr_value(value);
		setAttr_type(ATTR_TYPE_STRING);
	}

	public SignetSubjectAttr(String mappedName, int value)
	{
		this(mappedName);
		setAttr_value(Integer.toString(value));
		setAttr_type(ATTR_TYPE_INT);
	}

	public SignetSubjectAttr(String mappedName, float value)
	{
		this(mappedName);
		setAttr_value(Float.toString(value));
		setAttr_type(ATTR_TYPE_FLOAT);
	}

	public SignetSubjectAttr(String mappedName, Date value)
	{
		this(mappedName);
		setAttr_value(DateFormat.getInstance().format(value));
		setAttr_type(ATTR_TYPE_DATE);
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
	 * Take care of Hibernate bidirectional assoc.
	 */
	public void setParent(SignetSubject subject)
	{
		parent = subject;
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


	/** Support for Hibernate, wrapper for getValue() */
	protected String getAttr_value()
	{
		return (getValue());
	}

	/** Support for Hibernate, wrapper for setValue(String) */
	protected void setAttr_value(String newValue)
	{
		setValue(newValue);
	}


	public String getValue()
	{
		return (attr_value);
	}

	public void setValue(String newValue)
	{
		attr_value = newValue;
		setAttr_type(ATTR_TYPE_STRING);
	}

	public void setValue(int newValue)
	{
		setValue(new Integer(newValue));
	}

	public void setValue(Integer newValue)
	{
		attr_value = newValue.toString();
		setAttr_type(ATTR_TYPE_INT);
	}

	public void setValue(float newValue)
	{
		setValue(new Float(newValue));
	}

	public void setValue(Float newValue)
	{
		attr_value = newValue.toString();
		setAttr_type(ATTR_TYPE_FLOAT);
	}

	public void setValue(Date newValue)
	{
		attr_value = DateFormat.getInstance().format(newValue);
		setAttr_type(ATTR_TYPE_DATE);
	}


	/** @return the data type of this attribute */
	public String getType()
	{
		return (attr_type);
	}

	/** for Hibernate only */
	protected String getAttr_type()
	{
		return (attr_type);
	}

	/** for Hibernate only */
	private void setAttr_type(String _attr_type)
	{
		attr_type = _attr_type;
	}


	/** @return the sequence number of this attribute (0 for single-valued attribute) */
	public long getSequence()
	{
		return (sequence);
	}

	protected void setSequence(long sequence)
	{
		this.sequence = sequence;
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
	private void setModifyDate(Date modifyDate)
	{
		this.modifyDate = modifyDate;
	}

	/**
	 * Sets the ModifyDate to the current system time.
	 */
	private void refreshModifyDate()
	{
		setModifyDate(new Date(System.currentTimeMillis()));
	}


	public boolean isPersisted()
	{
		boolean retval;

		// check my status first
		retval = (null != subjectAttr_PK) && (0L < subjectAttr_PK.longValue());

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

		buf.append("name=" + mappedName);
		buf.append(", value=" + attr_value);
		buf.append(", type=" + attr_type);
		buf.append(", seq=" + sequence);
		buf.append(", modifyDate=" + DateFormat.getDateInstance().format(modifyDate));
		buf.append(", DBkey=" + ((null != subjectAttr_PK) ? subjectAttr_PK.toString() : "(null)"));
		buf.append(", parentFK=");
		if ((null != parent) && (null != parent.getSubject_PK()))
			buf.append(parent.getSubject_PK().toString());
		else
			buf.append("(null)");

		return (buf.toString());
	}


	/** Overrides Object#equals(java.lang.Object).
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o)
	{
		if (o instanceof SignetSubjectAttr)
			return (equals((SignetSubjectAttr)o));
		else
			return (false);
	}

	/**
	 * Compare two SignetSubjectAttr objects for equality
	 * @param attr The SignetSubjectAttr to compare
	 * @return true if mappedName, attr_value, attr_type, and sequence are equal, otherwise false
	 */
	public boolean equals(SignetSubjectAttr attr)
	{
		if (null == attr)
			return (false);

		boolean retval = false; // assume failure

		if (retval = mappedName.equals(attr.getMappedName())) // yes, I do mean "="
			if (retval = attr_value.equals(attr.getAttr_value()))  // yes, I do mean "="
				if (retval = attr_type.equals(attr.getAttr_type())) // yes, I do mean "="
//					if (retval = modifyDate.equals(attr.getModifyDate())) // yes, I do mean "="
						retval = (sequence == attr.getSequence()); // yes, I do mean "="
//						if (retval = (sequence == attr.getSequence())) // yes, I do mean "="
//							if (retval = ((null != parent) & (null != attr.getParent()))) // yes, I do mean "=" and "&"
//								retval = parent.getSubject_PK().equals(attr.getParent().getSubject_PK());
//		if (null != attr)
//		{
//			if (retval = subj_attr_id.equals(attr.subj_attr_id)) // yes, I do mean "="
//				if (retval = attr_valuesEqual(attr.getValue()))
//					retval = modifyDatesEqual(attr.getModifyDate());
////			if (retval = mappedName.equals(attr.getMappedName())) // yes, I do mean "="
////			{
////				List otherValues = attr.getSourceValues();
////				if (retval = sourceValues.size() == otherValues.size()) // yes, I do mean "="
////				for (int i = 0; (i < sourceValues.size()) && retval; i++)
////				{
////					SignetSubjectAttrValue myValue = (SignetSubjectAttrValue)sourceValues.get(i);
////					SignetSubjectAttrValue otherValue = (SignetSubjectAttrValue)otherValues.get(i);
////					retval = myValue.equals(otherValue);
////				}
////			}
////		}

		return (retval);
	}

	public boolean attr_valuesEqual(String otherValue)
	{
		boolean retval;

		if (null != attr_value)
		{
			if (null != otherValue)
				retval = attr_value.equals(otherValue);
			else
				retval = false;
		}
		else
		{
			if (null == otherValue)
				retval = true;
			else
				retval = false;
		}
		return (retval);
		
	}

	public boolean modifyDatesEqual(Date otherDate)
	{
		boolean retval = false; // assume failure
		if (null != modifyDate)
		{
			retval = modifyDate.equals(otherDate);
		}
		else
		{
			if (null == otherDate)
				retval = true;
			else
				retval = false;
		}
		return (retval);
		
	}


//	public int hashCode()
//	{
//		StringBuffer buf = new StringBuffer();
//
//		buf.append(mappedName);
//		buf.append(attr_value);
//		buf.append(attr_type);
//		buf.append(sequence);
//System.out.println("SignetSubjectAttr.hashCode: Attr=" + this.mappedName + " ownerId=" + parent.getId());
//		DateFormat df = DateFormat.getInstance();
//		String fmtDate = df.format(modifyDate);
//		buf.append(fmtDate);
//		buf.append((null != subjectAttr_PK) ? subjectAttr_PK.toString() : "(null)");
//		if ((null != parent) && (null != parent.getSubject_PK()))
//			buf.append(parent.getSubject_PK().toString());
//		else
//			buf.append("(null)");
//
//		return (buf.toString().hashCode());
//	}

}
