/*
 * Created on Jan 18, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.internet2.middleware.signet.choice.test;

import javax.naming.OperationNotSupportedException;

import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.Subsystem;
import edu.internet2.middleware.signet.choice.Choice;
import edu.internet2.middleware.signet.choice.ChoiceSet;

/**
 * @author acohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class Fixtures
{
  Subsystem	subsystem;
  
  /**
   * @throws OperationNotSupportedException
   * @throws ObjectNotFoundException
   * 
   */
  Fixtures(Signet signet)
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
  }
  
  Subsystem getSubsystem()
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
  
  String makeChoiceValue(int choiceNumber)
  {
    return "CHOICE_" + choiceNumber + "_VALUE";
  }
  
  String makeChoiceDisplayValue(int choiceNumber)
  {
    return "CHOICE_" + choiceNumber + "_DISPLAY_VALUE";
  }
  
  int makeChoiceDisplayOrder(int choiceNumber)
  {
    return choiceNumber;
  }
  
  int makeChoiceRank(int choiceNumber, int choiceCount)
  {
    return choiceCount - choiceNumber;
  }
  
  String makeChoiceSetId(int choiceNumber)
  {
    return "CHOICE_SET_" + choiceNumber + "_ID";
  }
}
