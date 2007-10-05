/*
 * $Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/subjsrc/SignetSubjectAttr.java,v 1.9 2007-10-05 08:27:42 ddonn Exp $
 * 
 * Copyright (c) 2007 Internet2, Stanford University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package edu.internet2.middleware.signet.subjsrc;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An "attribute of interest" that is persisted with it's corresponding Subject.
 * It may have one or more values as SignetSubjectAttrValue objects.
 */
public class SignetSubjectAttr
{
//TODO - Use enum instead of Strings
//	public enum ATTR_TYPE
//	{
//		STRING("string"), INTEGER("integer"), FLOAT("float"), DATE("date");
//		ATTR_TYPE(String value) { this.value = value; }
//		protected final String value;
//		public String value() { return (value); }
//	};
	public static final String ATTR_TYPE_STRING = "string";
	public static final String ATTR_TYPE_INT = "integer";
	public static final String ATTR_TYPE_FLOAT = "float";
	public static final String ATTR_TYPE_DATE = "date";
	public static final String ATTR_TYPE_DEFAULT = ATTR_TYPE_STRING;

	protected Log log;

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
	protected String	attrValue;

	/** The attribute's type (e.g. string, integer, float, etc.) */
	protected String	attrType;

	/** date/time stamp of the most recent update of the persisted value */
	protected Date		modifyDate;

	/** the sequence number for multi-valued attributes */
	protected int sequence;

	/** default constructor */
	public SignetSubjectAttr()
	{
		log = LogFactory.getLog(SignetSubjectAttr.class);
		mappedName = null;
		attrValue = null;
		attrType = ATTR_TYPE_DEFAULT;
		refreshModifyDate();
		subjectAttr_PK = null;
		parent = null;
		modifyDate = Calendar.getInstance().getTime();
	}

	/**
	 * Copy constructor does a deep copy of origAttr
	 * @param origAttr
	 */
	public SignetSubjectAttr(SignetSubjectAttr origAttr)
	{
		this();
		copy(origAttr);
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
		setAttrValue(value);
		setAttrType(ATTR_TYPE_STRING);
	}

	public SignetSubjectAttr(String mappedName, int value)
	{
		this(mappedName);
		setAttrValue(Integer.toString(value));
		setAttrType(ATTR_TYPE_INT);
	}

	public SignetSubjectAttr(String mappedName, float value)
	{
		this(mappedName);
		setAttrValue(Float.toString(value));
		setAttrType(ATTR_TYPE_FLOAT);
	}

	public SignetSubjectAttr(String mappedName, Date value)
	{
		this(mappedName);
		setAttrValue(DateFormat.getInstance().format(value));
		setAttrType(ATTR_TYPE_DATE);
	}


	/**
	 * @return the subjectAttr_PK
	 */
	protected Long getSubjectAttr_PK()
	{
		return subjectAttr_PK;
	}

	/**
	 * @param subjectAttrKey the subjectAttr_PK to set
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
	protected String getAttrValue()
	{
		return (getValue());
	}

	/** Support for Hibernate, wrapper for setValue(String) */
	protected void setAttrValue(String newValue)
	{
		setValue(newValue);
	}


	public String getValue()
	{
		return (attrValue);
	}

	/**
	 * Set the value and type of this attribute
	 * @param newValue the value to set
	 * @param attrType the attribute type
	 */
	public void setValue(String newValue, String attrType)
	{
		attrValue = newValue;
		setAttrType(attrType);
	}

	public void setValue(String newValue)
	{
		setValue(newValue, ATTR_TYPE_STRING);
	}

	public void setValue(int newValue)
	{
		setValue(new Integer(newValue));
	}

	public void setValue(Integer newValue)
	{
		setValue(newValue.toString(), ATTR_TYPE_INT);
	}

	public void setValue(float newValue)
	{
		setValue(new Float(newValue));
	}

	public void setValue(Float newValue)
	{
		setValue(newValue.toString(), ATTR_TYPE_FLOAT);
	}

	public void setValue(Date newValue)
	{
		setValue(DateFormat.getInstance().format(newValue), ATTR_TYPE_DATE);
	}


	/** @return the data type of this attribute */
	public String getType()
	{
		return (attrType);
	}

	/** for Hibernate only */
	protected String getAttrType()
	{
		return (attrType);
	}

	/** for Hibernate only */
	public void setAttrType(String attrType)
	{
		boolean ok = false;
		if ((null != attrType) && (0 < attrType.length()))
			if ( ! (ok = attrType.equalsIgnoreCase(ATTR_TYPE_STRING)))
				if ( ! (ok = attrType.equalsIgnoreCase(ATTR_TYPE_INT)))
					if ( ! (ok = attrType.equalsIgnoreCase(ATTR_TYPE_FLOAT)))
						ok = attrType.equalsIgnoreCase(ATTR_TYPE_DATE);
		if (ok)
			this.attrType = attrType.toLowerCase();
		else
		{
			log.error("Invalid Attribute Type specified (" + attrType + "). Converting to default type \"" + ATTR_TYPE_DEFAULT + "\" instead.");
			this.attrType = ATTR_TYPE_DEFAULT;
		}
	}

//	private void setAttrType(ATTR_TYPE _attr_type)
//	{
//		attrType = _attr_type.value();
//	}

	/** @return the sequence number of this attribute (0 for single-valued attribute) */
	public int getSequence()
	{
		return (sequence);
	}

	public void setSequence(int sequence)
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
	public void setModifyDate(Date modifyDate)
	{
		this.modifyDate = modifyDate;
	}

	/**
	 * Sets the ModifyDate to the current system time.
	 */
	private void refreshModifyDate()
	{
		setModifyDate(Calendar.getInstance().getTime());
	}


	public boolean isPersisted()
	{
		boolean retval;

		// check my status first
		retval = (null != subjectAttr_PK) && (0L < subjectAttr_PK.longValue());

		return (retval);
	}

	
	/**
	 * Deep copy of origAttr, excluding subjectAttr_PK and modifyDate
	 * @param origAttr The source of the copy
	 */
	public void copy(SignetSubjectAttr origAttr)
	{
		if (null != origAttr)
		{
			setAttrValue(origAttr.getAttrValue());
			setAttrType(origAttr.getAttrType());
			setMappedName(origAttr.getMappedName());
			setSequence(origAttr.getSequence());
			setParent(origAttr.getParent());
		}
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
		buf.append(", value=" + attrValue);
		buf.append(", type=" + attrType);
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
	 * @return true if mappedName, attrValue, attrType, and sequence are equal, otherwise false
	 */
	public boolean equals(SignetSubjectAttr attr)
	{
		if (null == attr)
			return (false);

		boolean retval = false; // assume failure

		if (retval = mappedName.equals(attr.getMappedName())) // yes, I do mean "="
			if (retval = attrValue.equals(attr.getAttrValue()))  // yes, I do mean "="
				if (retval = attrType.equals(attr.getAttrType())) // yes, I do mean "="
//					if (retval = modifyDate.equals(attr.getModifyDate())) // yes, I do mean "="
						retval = (sequence == attr.getSequence()); // yes, I do mean "="
//						if (retval = (sequence == attr.getSequence())) // yes, I do mean "="
//							if (retval = ((null != parent) & (null != attr.getParent()))) // yes, I do mean "=" and "&"
//								retval = parent.getSubject_PK().equals(attr.getParent().getSubject_PK());
//		if (null != attr)
//		{
//			if (retval = subj_attr_id.equals(attr.subj_attr_id)) // yes, I do mean "="
//				if (retval = attrValuesEqual(attr.getValue()))
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

//	protected boolean attrValuesEqual(String otherValue)
//	{
//		boolean retval;
//
//		if (null != attrValue)
//		{
//			if (null != otherValue)
//				retval = attrValue.equals(otherValue);
//			else
//				retval = false;
//		}
//		else
//		{
//			if (null == otherValue)
//				retval = true;
//			else
//				retval = false;
//		}
//		return (retval);
//		
//	}

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
//		buf.append(attrValue);
//		buf.append(attrType);
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
