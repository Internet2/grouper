/*--
$Id: ExternalSubjectId.java,v 1.2 2004-12-24 04:15:46 acohen Exp $
$Date: 2004-12-24 04:15:46 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.subject.SubjectType;

class ExternalSubjectId
{
private String 			id;
private SubjectType type;

ExternalSubjectId()
{
  super();
  // TODO Auto-generated constructor stub
}

ExternalSubjectId(String id, SubjectType type)
{
  this.id = id;
  this.type = type;
}

/**
 * @return Returns the id.
 */
String getId()
{
  return this.id;
}

/**
 * @param id The id to set.
 */
void setId(String id)
{
  this.id = id;
}

/**
 * @return Returns the type.
 */
SubjectType getType()
{
  return this.type;
}

/**
 * @param type The type to set.
 */
void setType(SubjectType type)
{
  this.type = type;
}

/* (non-Javadoc)
 * @see java.lang.Object#equals(java.lang.Object)
 */
public boolean equals(Object obj)
{
  if ( !(obj instanceof ExternalSubjectId) )
  {
    return false;
  }
  
  ExternalSubjectId rhs = (ExternalSubjectId) obj;
  return new EqualsBuilder()
                  .append(this.getId(), rhs.getId())
                  .append(this.getType(), rhs.getType())
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
     append(this.getId())
     .append(this.getType())
     .toHashCode();
}

/* (non-Javadoc)
 * @see java.lang.Object#toString()
 */
public String toString()
{
  return this.id + ":" + this.type;
}
}
