/*--
  $Id: SubjectKey.java,v 1.1 2004-12-09 20:49:07 mnguyen Exp $
  $Date: 2004-12-09 20:49:07 $
  
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
  private SubjectType subjectType;
  private String			subjectTypeId;

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
    this.subjectTypeId = subjectType.getId();
  }
  
  SubjectKey(Subject subject)
  {
    this.subjectId = subject.getId();
    this.subjectType = subject.getSubjectType();
    this.subjectTypeId = this.subjectType.getId();
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
    if ((this.subjectTypeId != null) && (this.subjectType == null))
    {
      try
      {
        this.subjectType = signet.getSubjectType(this.subjectTypeId);
      }
      catch (ObjectNotFoundException onfe)
      {
        throw new SignetRuntimeException(onfe);
      }
    }
    
    if (this.subjectType instanceof SubjectTypeImpl)
    {
      ((SubjectTypeImpl)(this.subjectType)).setSignet(signet);
    }
    return this.subjectType;
  }
  
  /**
   * @param subjectType The subjectType to set.
   */
  void setSubjectType(SubjectType subjectType)
  {
    this.subjectType = subjectType;
    this.subjectTypeId = subjectType.getId();
  }
  
  String getSubjectTypeId()
  {
    return this.subjectTypeId;
  }
  
  void setSubjectTypeId(String subjectTypeId)
  throws ObjectNotFoundException
  {
    this.subjectTypeId = subjectTypeId;
  }
  
  boolean isComplete(Signet signet)
  throws ObjectNotFoundException
  {
    if ((signet != null)
        && (this.subjectType == null)
        && (this.subjectTypeId != null))
    {
      this.subjectType = signet.getSubjectType(this.subjectTypeId);
    }
    
    if ((this.subjectId != null) && (this.subjectType != null))
    {
      return true;
    }
    else
    {
      return false;
    }
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
