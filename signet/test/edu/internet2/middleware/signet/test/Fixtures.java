/*
 * Created on Jan 18, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.internet2.middleware.signet.test;

import javax.naming.OperationNotSupportedException;

import edu.internet2.middleware.signet.HTMLLimitRenderer;
import edu.internet2.middleware.signet.Limit;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.Subsystem;
import edu.internet2.middleware.signet.ValueType;
import edu.internet2.middleware.signet.choice.Choice;
import edu.internet2.middleware.signet.choice.ChoiceSet;

/**
 * @author acohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Fixtures
{
  Subsystem	subsystem;
  
  /**
   * @throws OperationNotSupportedException
   * @throws ObjectNotFoundException
   * 
   */
  public Fixtures(Signet signet)
  throws
  	OperationNotSupportedException,
  	ObjectNotFoundException
  {
    super();
    subsystem
    	= getOrCreateSubsystem(signet, Constants.SUBSYSTEM_ID);
    
    for (int i = 0; i < Constants.MAX_CHOICE_SETS; i++)
    {
      ChoiceSet emptyChoiceSet = getOrCreateChoiceSet(signet, i); 
    }
    
    for (int i = 0; i < Constants.MAX_LIMITS; i++)
    {
      Limit limit = getOrCreateLimit(signet, i);
    }
  }
  
  public Subsystem getSubsystem()
  {
    return this.subsystem;
  }
  
  private Subsystem getOrCreateSubsystem
  	(Signet signet,
  	 String id)
  throws ObjectNotFoundException
  {
    Subsystem subsystem = null;
    
    try
    {
      signet.getSubsystem(id);
    }
    catch (ObjectNotFoundException onfe)
    {
      subsystem
      	= signet.newSubsystem
      			(id,
      			 Constants.SUBSYSTEM_NAME,
      			 Constants.SUBSYSTEM_HELPTEXT);
      
      signet.save(subsystem);
    }
    
    // Let's fetch that newly-created Subsystem from the database.
    // This step is not necessary, but it exercises a little more code.
    
    return signet.getSubsystem(id);
  }
  
  ChoiceSet getOrCreateChoiceSet
  	(Signet		signet,
  	 int			choiceSetNumber)
  throws
  	OperationNotSupportedException,
  	ObjectNotFoundException
  {
    ChoiceSet choiceSet;
    
    try
    {
      choiceSet
      	= this.subsystem.getChoiceSet
      			(makeChoiceSetId(choiceSetNumber));
    }
    catch (ObjectNotFoundException onfe)
    {
      choiceSet
      	= signet.newChoiceSet
      			(subsystem, makeChoiceSetId(choiceSetNumber));
      
      for (int i = 0; i < choiceSetNumber; i++)
      {
        // Create and persist the Choice object to be tested.
        // ChoiceSet 0 has 0 choices, choiceSet 1 has 1, and so on.
        Choice choice
        	= choiceSet.addChoice
        			(makeChoiceValue(i),
        			 makeChoiceDisplayValue(i),
  	  			   makeChoiceDisplayOrder(i),
  	  			   makeChoiceRank(i, choiceSetNumber));
      }
      
      signet.save(choiceSet);
  
      // Fetch that newly-stored ChoiceSet object from the database.
      // This step is not really necessary, but exercises a little
      // more code.
      choiceSet
      	= this.subsystem.getChoiceSet
      			(makeChoiceSetId(choiceSetNumber));
    }
    
    return choiceSet;
  }
  
  Limit getOrCreateLimit
  	(Signet		signet,
  	 int			limitNumber)
  throws
  	ObjectNotFoundException
  {
    Limit limit;
    
    try
    {
      limit
      	= this.subsystem.getLimit
      			(makeLimitId(limitNumber));
    }
    catch (ObjectNotFoundException onfe)
    {
      HTMLLimitRenderer htmlRenderer = new HTMLRendererImpl();
      
      // Limit 0 contains ChoiceSet 0, Limit 1 contains ChoiceSet 1,
      // and so forth.
      limit
      	= signet.newLimit
      			(subsystem,
      			 makeLimitId(limitNumber),
      			 makeLimitValueType(limitNumber),
      			 subsystem.getChoiceSet(makeChoiceSetId(limitNumber)),
      			 htmlRenderer,
      			 makeLimitName(limitNumber),
      		   makeLimitHelpText(limitNumber),
      		   Status.ACTIVE);
    }
      
    signet.save(limit);
  
    // Fetch that newly-stored Limit object from the database.
    // This step is not really necessary, but exercises a little
    // more code.
    limit
     	= this.subsystem.getLimit
     			(makeLimitId(limitNumber));
    
    return limit;
  }
  
  public String makeChoiceValue(int choiceNumber)
  {
    return "CHOICE_" + choiceNumber + "_VALUE";
  }
  
  public String makeChoiceDisplayValue(int choiceNumber)
  {
    return "CHOICE_" + choiceNumber + "_DISPLAY_VALUE";
  }
  
  public int makeChoiceDisplayOrder(int choiceNumber)
  {
    return choiceNumber;
  }
  
  public int makeChoiceRank(int choiceNumber, int choiceCount)
  {
    return choiceCount - choiceNumber;
  }
  
  public String makeChoiceSetId(int choiceNumber)
  {
    return "CHOICE_SET_" + choiceNumber + "_ID";
  }
  
  public String makeLimitId(int limitNumber)
  {
    return "LIMIT_" + limitNumber + "_ID";
  }
  
  public String makeLimitName(int limitNumber)
  {
    return "LIMIT_" + limitNumber + "_NAME";
  }
  
  public String makeLimitHelpText(int limitNumber)
  {
    return "LIMIT_" + limitNumber + "_HELPTEXT";
  }
  
  public ValueType makeLimitValueType(int limitNumber)
  {
    final int VALUE_TYPE_COUNT = 3;
    
    switch (limitNumber % VALUE_TYPE_COUNT)
    {
      case 0:
        return ValueType.DATE;
      case 1:
        return ValueType.NUMERIC;
      case 2:
      default:
        return ValueType.STRING;
    }
  }
}
