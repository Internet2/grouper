/*--
$Id: SubjectAttrKey.java,v 1.2 2004-12-24 04:15:46 acohen Exp $
$Date: 2004-12-24 04:15:46 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.subject.SubjectType;

class SubjectAttrKey
implements Serializable
{
private SubjectType 	subjectType;
private String 				subjectId;
private String 				name;
private int 					instance;


/**
 * 
 */
public SubjectAttrKey()
{
  super();
  // TODO Auto-generated constructor stub
}

public SubjectAttrKey
	(SubjectType 	subjectType,
	 String				subjectId,
	 String 			name,
	 int					instance)
{
  this.subjectType = subjectType;
  this.subjectId = subjectId;
  this.name = name;
  this.instance = instance;
}

/**
 * @return Returns the instance.
 */
int getInstance()
{
  return this.instance;
}
/**
 * @param instance The instance to set.
 */
void setInstance(int instance)
{
  this.instance = instance;
}
/**
 * @return Returns the name.
 */
String getName()
{
  return this.name;
}
/**
 * @param name The name to set.
 */
void setName(String name)
{
  this.name = name;
}
/**
 * @return Returns the subjectID.
 */
String getSubjectId()
{
  return this.subjectId;
}
/**
 * @param subjectID The subjectID to set.
 */
void setSubjectId(String subjectId)
{
  this.subjectId = subjectId;
}
/**
 * @return Returns the subjectTypeId.
 */
SubjectType getSubjectType()
{
  return this.subjectType;
}
/**
 * @param subjectTypeId The subjectTypeId to set.
 */
void setSubjectType(SubjectType subjectType)
{
  this.subjectType = subjectType;
}


/* (non-Javadoc)
 * @see java.lang.Object#equals(java.lang.Object)
 */
public boolean equals(Object obj)
{
  if ( !(obj instanceof SubjectAttrKey) )
  {
    return false;
  }
  
  SubjectAttrKey rhs = (SubjectAttrKey) obj;
  return new EqualsBuilder()
                  .append(this.getInstance(), rhs.getInstance())
                  .append(this.getName(), rhs.getName())
                  .append(this.getSubjectId(), rhs.getSubjectId())
                  .append(this.getSubjectType(), rhs.getSubjectType())
                  .isEquals();
}

/* (non-Javadoc)
 * @see java.lang.Object#hashCode()
 */
public int hashCode()
{
  // you pick a hard-coded, randomly chosen, non-zero, odd number
  // ideally different for each class
  return new HashCodeBuilder(17, 37) 
  		.append(this.getInstance())
  		.append(this.getName())
  		.append(this.getSubjectId())
  		.append(this.getSubjectType())
  		.toHashCode();
}
}
