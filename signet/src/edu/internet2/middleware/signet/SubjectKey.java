/*--
$Id: SubjectKey.java,v 1.5 2006-02-09 10:25:28 lmcrae Exp $
$Date: 2006-02-09 10:25:28 $

Copyright 2006 Internet2, Stanford University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
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
