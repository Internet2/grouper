/*--
$Id: ChoiceSetImpl.java,v 1.9 2006-06-30 02:04:41 ddonn Exp $
$Date: 2006-06-30 02:04:41 $

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

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;

import edu.internet2.middleware.signet.choice.Choice;
import edu.internet2.middleware.signet.choice.ChoiceNotFoundException;
import edu.internet2.middleware.signet.choice.ChoiceSet;
import edu.internet2.middleware.signet.choice.ChoiceSetAdapter;

/**
 * @author acohen
 *
 */
public class ChoiceSetImpl implements ChoiceSet
{

  // This field is a simple synthetic key for this record in the database.
  private Integer         key;

  private Signet           signet;
  private String           id;
  private Subsystem        subsystem;
  private ChoiceSetAdapter choiceSetAdapter;
  private Set              choices;
  private String           adapterClassName;
  
  /* The date and time this ChoiceSet was last modified. */
  private Date	modifyDatetime;
  
  /**
   * Every Hibernate-persistable class must have a default,
   * parameterless constructor.
   */
  ChoiceSetImpl()
  {
    super();
    this.choices = new HashSet();
  }
  
  /**
   * @param id
   * @param subsystem
   * @param choiceSetAdapter
   * @param choices
   */
  ChoiceSetImpl
  	(Signet           signet,
     Subsystem        subsystem,
     ChoiceSetAdapter choiceSetAdapter,
  	 String           id)
  {
    super();
    this.setSignet(signet);
    this.id = id;
    this.subsystem = subsystem;
    this.setChoiceSetAdapter(choiceSetAdapter);
    this.choices = new HashSet();
    
    ((SubsystemImpl)subsystem).add(this);
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.choice.ChoiceSet#getId()
   */
  public String getId()
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
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.choice.ChoiceSet#getSubsystem()
   */
  public Subsystem getSubsystem()
  {
    return this.subsystem;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.choice.ChoiceSet#getChoices()
   */
  public Set getChoices()
  {
    return this.choices;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.choice.ChoiceSet#addChoice(java.lang.String, java.lang.String, int, int)
   */
  public Choice addChoice
  	(String choiceValue,
  	 String displayValue,
  	 int 		displayOrder,
  	 int 		rank)
  {
    Choice choice
    	= new ChoiceImpl
    			(this,
    			 choiceValue,
    			 displayValue,
    			 displayOrder,
    			 rank);
    
    this.choices.add(choice);
    
    return choice;
  }

  /**
   * @param signet
   */
  public void setSignet(Signet signet)
  {
    this.signet = signet;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.choice.ChoiceSet#getChoiceByValue(java.lang.String)
   */
  public Choice getChoiceByValue(String value)
  throws ChoiceNotFoundException
  {
    Iterator choicesIterator = this.getChoices().iterator();
    while (choicesIterator.hasNext())
    {
      Choice candidate = (Choice)(choicesIterator.next());
      if (candidate.getValue().equals(value))
      {      
        return candidate;
      }
    }
    
    // If we've gotten this far, then the requested value was not
    // found.
    throw new ChoiceNotFoundException
    	("The ChoiceSet with ID '"
    	 + this.getId()
    	 + "' does not contain a Choice with value '"
    	 + value
    	 + "'.");
  }

  /**
   * @return Returns the modifyDatetime.
   */
  Date getModifyDatetime()
  {
    return this.modifyDatetime;
  }
  
  /**
   * @param modifyDatetime The modifyDatetime to set.
   */
  void setModifyDatetime(Date modifyDatetime)
  {
    this.modifyDatetime = modifyDatetime;
  }

  public ChoiceSetAdapter getChoiceSetAdapter()
  {
    if ((this.choiceSetAdapter == null)
        && (this.adapterClassName != null))
    {
      this.choiceSetAdapter
      	= signet.getChoiceSetAdapter(this.adapterClassName);
    }
    return this.choiceSetAdapter;
  }

  void setChoiceSetAdapter(ChoiceSetAdapter adapter)
  {
    this.choiceSetAdapter = adapter;
    this.adapterClassName = adapter.getClass().getName();

    if (this.choiceSetAdapter instanceof ChoiceSetAdapterImpl)
    {
      ((ChoiceSetAdapterImpl)
          (this.choiceSetAdapter)).setSignet(signet);
    }
  }
  
  /**
   * @param subsystem The subsystem to set.
   */
  void setSubsystem(Subsystem subsystem)
  {
    this.subsystem = subsystem;
  }
  
  /**
   * @param choices The choices to set.
   */
  void setChoices(Set choices)
  {
    this.choices = choices;
  }

  void setAdapterClassName(String name)
  {
    this.adapterClassName = name;

    if (null != signet)
    {
      this.choiceSetAdapter = signet.getChoiceSetAdapter(name);
    }
  }

  String getAdapterClassName()
  {
    return this.adapterClassName;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object o)
  {
    if ( !(o instanceof ChoiceSetImpl) )
    {
      return false;
    }
    
    ChoiceSetImpl rhs = (ChoiceSetImpl) o;
    return new EqualsBuilder()
                    .append(this.getId(), rhs.getId())
                    .isEquals();
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.choice.ChoiceSet#save()
   */
  public void save()
  {
    signet.getPersistentDB().save(this);
  }
  
  /* This method is for use only by Hibernate.
   * 
   */
  private Integer getKey()
  {
    return this.key;
  }

  /* This method is for use only by Hibernate.
   * 
   */
  private void setKey(Integer key)
  {
    this.key = key;
  }
}
