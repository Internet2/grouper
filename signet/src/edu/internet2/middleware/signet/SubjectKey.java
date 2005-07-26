/*--
$Id: SubjectKey.java,v 1.4 2005-07-26 18:00:48 acohen Exp $
$Date: 2005-07-26 18:00:48 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectType;


class SubjectKey
implements Serializable
{
private String 			subjectId;
private SubjectType		subjectType;

/**
 * 
 */
SubjectKey()
{
  super();
  // TODO Auto-generated constructor stub
}

SubjectKey(String subjectId, SubjectType subjectType)
{
  this.subjectId = subjectId;
  this.subjectType = subjectType;
}

SubjectKey(Subject subject)
{
  if (subject == null)
  {
    this.subjectId = null;
    this.subjectType = null;
  }
  else
  {
    this.subjectId = subject.getId();
    this.subjectType = subject.getType();
  }
}

/**
 * @return Returns the id.
 */
String getSubjectId()
{
  return this.subjectId;
}
/**
 * @param id The id to set.
 */
void setSubjectId(String subjectId)
{
  this.subjectId = subjectId;
}

/**
 * @return Returns the subjectType.
 */
SubjectType getSubjectType(Signet signet)
{
  return this.subjectType;
}

/**
 * @param subjectType The subjectType to set.
 */
void setSubjectType(SubjectType subjectType)
{
  this.subjectType = subjectType;
}

String getSubjectTypeId()
{
  return this.subjectType.getName();
}


/* (non-Javadoc)
 * @see java.lang.Object#equals(java.lang.Object)
 */
public boolean equals(Object obj)
{
  if ( !(obj instanceof SubjectKey) )
  {
    return false;
  }
  
  SubjectKey rhs = (SubjectKey) obj;
  return new EqualsBuilder()
                  .append(this.getSubjectId(), rhs.getSubjectId())
                  .append(this.getSubjectTypeId(), rhs.getSubjectTypeId())
                  .isEquals();
}

/* (non-Javadoc)
 * @see java.lang.Object#hashCode()
 */
public int hashCode()
{
  // you pick a hard-coded, randomly chosen, non-zero, odd number
  // ideally different for each class
  return new HashCodeBuilder(17, 37).   
     append(this.getSubjectId())
     .append(this.getSubjectTypeId())
     .toHashCode();
}

/* (non-Javadoc)
 * @see java.lang.Object#toString()
 */
public String toString()
{
  String outStr
  	= "id='"
  	  + this.subjectId 
  	  + "', subjectType='" 
  	  + this.subjectType
  	  + "'";
  
  return outStr;
}
}
