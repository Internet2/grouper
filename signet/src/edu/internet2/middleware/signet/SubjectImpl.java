/*--
  $Id: SubjectImpl.java,v 1.1 2004-12-09 20:49:07 mnguyen Exp $
  $Date: 2004-12-09 20:49:07 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectType;

/**
 *
 * These are the columns in the Subject table:
 * 
 * subjectTypeID
 * subjectID
 * name
 * description
 * displayID
 * createDatetime
 * createDbAccount
 * createUserID
 * createContext
 * modifyDatetime
 * modifyUserID
 * modifyContext
 * comment
 */
class SubjectImpl
	extends 		EntityImpl
	implements 	Subject
{
  private Signet			signet;
  private SubjectType	type;
  private String			description;
  private String			displayId;
  private Map					attributeLists; // A Map of Lists.
  																		// One List of attrValues per attrName.

  public SubjectImpl()
  {
    super();
    this.attributeLists = new HashMap();
  }

  SubjectImpl
  	(Signet				signet,
  	 SubjectType 	type,
  	 String 			id,
  	 String 			name,
  	 String 			description,
  	 String 			displayId)
  {
    this.signet = signet;
    this.type = type;
    this.setId(id);
    this.setName(name);
    this.setDescription(description);
    this.setDisplayId(displayId);
    this.attributeLists = new HashMap();
  }
  
  void setSignet(Signet signet)
  {
    this.signet = signet;
    
    if (this.type != null)
    {
      ((SubjectTypeImpl)(this.type)).setSignet(signet);
    }
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.subject.Subject#getType()
   */
  public SubjectType getSubjectType()
  {
    return this.type;
  }
  
  public void setSubjectType(SubjectType type)
  {
    this.type = type;
    ((SubjectTypeImpl)(this.type)).setSignet(signet);
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.subject.Subject#getDisplayId()
   */
  public String getDisplayId()
  {
    return this.displayId;
  }
  
  void setDisplayId(String displayId)
  {
    this.displayId = displayId;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.subject.Subject#getDescription()
   */
  public String getDescription()
  {
    return this.description;
  }
  
  public void addAttribute(String name, String value)
  {
    if (name == null)
    {
      throw new IllegalArgumentException
      	("It is illegal to add a Subject-attribute with a NULL name.");
    }
    
    if (value == null)
    {
      // Let's just treat this as a no-op.
      return;
    }
    
    List list = (List)(this.attributeLists.get(name));
    
    if (list == null)
    {
      list = new Vector(1);
      this.attributeLists.put(name, list);
    }
    
    String normalized = this.signet.normalizeSubjectAttributeValue(value);
    
    SubjectAttrKey subjectAttrKey 
    	= new SubjectAttrKey
    			(this.getSubjectType(),
    			 this.getId(),
    			 name,
    			 list.size() + 1);
    
    SubjectAttributeValue attrVal
    	= new SubjectAttributeValue(subjectAttrKey, value, normalized);
    
    list.add(value);
  }

  /**
   * @return an array of values for a specific attribute name.
   */
  public String[] getAttributeArray(String name)
  {
    List list = (List)(this.attributeLists.get(name));
    String[] valuesArray;
    
    if (list == null)
    {
      valuesArray = new String[0];
    }
    else
    {
      valuesArray = new String[list.size()];
      list.toArray(valuesArray);
    }
    
    return valuesArray;
  }
  /**
   * @param description The description to set.
   */
  void setDescription(String description)
  {
    this.description = description;
  }
  
  public boolean equals(Object obj)
  {
    if ( !(obj instanceof SubjectImpl) )
    {
      return false;
    }
    
    SubjectImpl rhs = (SubjectImpl) obj;
    return new EqualsBuilder()
                    .append(this.getId(), rhs.getId())
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
    return new HashCodeBuilder(17, 37).   
       append(this.getId())
       .append(this.getSubjectType())
       .toHashCode();
  }
}
