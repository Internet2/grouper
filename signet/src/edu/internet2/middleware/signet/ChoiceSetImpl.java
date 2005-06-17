/*--
$Id: ChoiceSetImpl.java,v 1.4 2005-06-17 23:24:28 acohen Exp $
$Date: 2005-06-17 23:24:28 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.naming.OperationNotSupportedException;

import org.apache.commons.lang.builder.EqualsBuilder;

import edu.internet2.middleware.signet.choice.Choice;
import edu.internet2.middleware.signet.choice.ChoiceNotFoundException;
import edu.internet2.middleware.signet.choice.ChoiceSet;
import edu.internet2.middleware.signet.choice.ChoiceSetAdapter;
import edu.internet2.middleware.signet.tree.TreeAdapter;

/**
 * @author acohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class ChoiceSetImpl implements ChoiceSet
{
  private Signet						signet;
  private String						id;
  private Subsystem					subsystem;
  private ChoiceSetAdapter	choiceSetAdapter;
  private Set								choices;
  private String						adapterClassName;
  
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
  	(Signet						signet,
  	 Subsystem				subsystem,
     ChoiceSetAdapter	choiceSetAdapter,
  	 String						id)
  {
    super();
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
    // Let's make sure that each of these Choice objects contains a
    // valid Signet reference before we let them  out into the wider
    // world.
    
    Iterator choicesIterator = this.choices.iterator();
    while (choicesIterator.hasNext())
    {
      Choice choice = (Choice)(choicesIterator.next());
      if (choice instanceof ChoiceImpl)
      {
        ((ChoiceImpl)choice).setSignet(this.signet);
      }
    }
    
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
  throws OperationNotSupportedException
  {
    Choice choice
    	= new ChoiceImpl
    			(this.signet,
    			 this,
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
  void setSignet(Signet signet)
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
        // Let's make sure that each of this Choice object contains a
        // valid Signet reference before we let it out into the wider
        // world.
        
        if (candidate instanceof ChoiceImpl)
        {
          ((ChoiceImpl)candidate).setSignet(this.signet);
        }
      
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
      	= this.signet.getChoiceSetAdapter(this.adapterClassName);
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
          (this.choiceSetAdapter)).setSignet(this.signet);
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
    
    Iterator choiceIterator = choices.iterator();
    while (choiceIterator.hasNext())
    {
      Choice choice = (Choice)(choiceIterator.next());
      if (choice instanceof ChoiceImpl)
      {
        ((ChoiceImpl)choice).setSignet(this.signet);
      }
    }
  }

  void setAdapterClassName(String name)
  {
    this.adapterClassName = name;

    if (this.signet != null)
    {
      this.choiceSetAdapter = this.signet.getChoiceSetAdapter(name);
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
}
