/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/subjsrc/SignetSubjectAttrValue.java,v 1.2 2006-11-30 04:21:49 ddonn Exp $

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


/**
 * An AttributeValue object stores a value associated with a SignetSubject's
 * Attribute (referred to a 'parent' here). An Attribute may have one or more
 * values associated with it.
 */
public class SignetSubjectAttrValue
{
	protected Long				subjectAttrValue_PK; // PK
	protected Long				subjectAttrKey; // FK to parent
	protected SignetSubjectAttr	parent; // ref to parent
	protected String			value;
	protected String			type;
	protected long				sequence;


	/** Default constructor */
	public SignetSubjectAttrValue()
	{
		subjectAttrValue_PK = null;
		subjectAttrKey = null;
		value = null;
		type = null;
		sequence = 0L;
	}

	/** Constructor for a String value */
	public SignetSubjectAttrValue(String value)
	{
		this();
		setValue(value);
		setType("string");
	}


	/**
	 * AttributeValues are sequenced in the persistent DB.
	 * @return the record sequence
	 */
	public long getSequence()
	{
		return sequence;
	}

	/**
	 * AttributeValues are sequenced in the persistent DB.
	 * @param sequence the sequence to set
	 */
	public void setSequence(long sequence)
	{
		this.sequence = sequence;
	}

	/**
	 * Foreign key to parent
	 * @return the subjectAttrKey
	 */
	protected Long getSubjectAttrKey()
	{
		return subjectAttrKey;
	}

	/**
	 * Foreign key to parent
	 * @param subjectAttrKey the subjectAttrKey to set
	 */
	protected void setSubjectAttrKey(Long subjectAttrKey)
	{
		this.subjectAttrKey = subjectAttrKey;
	}

	/**
	 * @return the parent of this AttributeValue
	 */
	public SignetSubjectAttr getParent()
	{
		return parent;
	}

	/**
	 * Set the reference to the parent Attribute
	 * @param parent the parent to set
	 */
	public void setParent(SignetSubjectAttr parent)
	{
		this.parent = parent;
		setSubjectAttrKey((null != parent) ? parent.getSubjectAttr_PK() : null);
	}

	/**
	 * Persistent DB primary key.
	 * Support for Hibernate
	 * @return the subjectAttrValue_PK
	 */
	protected Long getSubjectAttrValue_PK()
	{
		return subjectAttrValue_PK;
	}

	/**
	 * Persistent DB primary key.
	 * Support for Hibernate
	 * @param subjectAttrValue_PK the subjectAttrValue_PK to set
	 */
	protected void setSubjectAttrValue_PK(Long subjectAttrValueKey)
	{
		this.subjectAttrValue_PK = subjectAttrValueKey;
	}

	/**
	 * AttributeValue values are stored as Strings in the persistent store. This
	 * field allows applications to cast values, if desired.
	 * @return the data type of this value
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * AttributeValue values are stored as Strings in the persistent store. This
	 * field allows applications to cast values, if desired.
	 * @param type the type to set
	 */
	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 * @return the value
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value)
	{
		this.value = value;
	}


	/** If this object has no primary key, or the primary key is 0, then the
	 * assumption is that it has never been persisted.
	 */
	public boolean isPersisted()
	{
		return ((null != subjectAttrValue_PK) && (0L < subjectAttrValue_PK.longValue()));
	}


	////////////////////////////////////////
	// overrides Object
	////////////////////////////////////////

	public String toString()
	{
		return (sequence + "." + value + "(" + type + ")");
	}

	public boolean equals(Object o)
	{
		if (o instanceof SignetSubjectAttrValue)
			return (equals((SignetSubjectAttrValue)o));
		else
			return (false);
	}

	/** Compares the value, type and sequence only */
	public boolean equals(SignetSubjectAttrValue attrValue)
	{
		boolean retval = false; // assume failure

		if (null != attrValue)
		{
			if (retval = value.equals(attrValue.getValue())) // yes, I do mean "="
				if (retval = type.equals(attrValue.getType())) // yes, I do mean "="
					retval = sequence == attrValue.getSequence(); // yes, I do mean "="
		}

		return (retval);
	}


}
