/*--
$Id: ExternalSubjectId.java,v 1.3 2006-02-09 10:19:51 lmcrae Exp $
$Date: 2006-02-09 10:19:51 $

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
