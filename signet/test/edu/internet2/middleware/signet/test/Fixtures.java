/*
 * Created on Jan 18, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.internet2.middleware.signet.test;

import javax.naming.OperationNotSupportedException;

import edu.internet2.middleware.signet.Category;
import edu.internet2.middleware.signet.Function;
import edu.internet2.middleware.signet.Limit;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Permission;
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
  Signet		signet;
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
    this.signet = signet;
    this.subsystem = getOrCreateSubsystem();
    
    for (int i = 0; i < Constants.MAX_CATEGORIES; i++)
    {
      Category category = getOrCreateCategory(i); 
    }
    
    for (int i = 0; i < Constants.MAX_CHOICE_SETS; i++)
    {
      ChoiceSet emptyChoiceSet = getOrCreateChoiceSet(i); 
    }
    
    for (int i = 0; i < Constants.MAX_LIMITS; i++)
    {
      Limit limit = getOrCreateLimit(i);
    }
    
    for (int i = 0; i < Constants.MAX_FUNCTIONS; i++)
    {
      Function function = getOrCreateFunction(i);
    }
    
    for (int i = 0; i < Constants.MAX_PERMISSIONS; i++)
    {
      Permission permission = getOrCreatePermission(i);
      
      // Permission 0 is associated with Limit 0 and Function 0,
      // Permission 1 is associated with Limit 1 and Function 1, and so on.
      permission.addLimit(getOrCreateLimit(i));
      permission.addFunction(getOrCreateFunction(i));
    }
  }
  
  private Subsystem getOrCreateSubsystem()
  throws ObjectNotFoundException
  {
    try
    {
      this.subsystem = this.signet.getSubsystem(Constants.SUBSYSTEM_ID);
    }
    catch (ObjectNotFoundException onfe)
    {
      this.subsystem
      	= signet.newSubsystem
      			(Constants.SUBSYSTEM_ID,
      			 Constants.SUBSYSTEM_NAME,
      			 Constants.SUBSYSTEM_HELPTEXT);
      
      signet.save(subsystem);
      
      // Let's fetch that newly-created Subsystem from the database.
      // This step is not necessary, but it exercises a little more code.
      
      this.subsystem = this.signet.getSubsystem(Constants.SUBSYSTEM_ID);
    }
    
    return this.subsystem;
  }
  
  private ChoiceSet getOrCreateChoiceSet(int choiceSetNumber)
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
      	= this.signet.newChoiceSet
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
      
      this.signet.save(choiceSet);
  
      // Fetch that newly-stored ChoiceSet object from the database.
      // This step is not really necessary, but exercises a little
      // more code.
      choiceSet
      	= this.subsystem.getChoiceSet
      			(makeChoiceSetId(choiceSetNumber));
    }
    
    return choiceSet;
  }
  
  private Limit getOrCreateLimit(int limitNumber)
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
      // Limit 0 contains ChoiceSet 0, Limit 1 contains ChoiceSet 1,
      // and so forth.
      limit
      	= this.signet.newLimit
      			(subsystem,
      			 makeLimitId(limitNumber),
      			 makeLimitValueType(limitNumber),
      			 subsystem.getChoiceSet(makeChoiceSetId(limitNumber)),
      			 makeLimitName(limitNumber),
      		   makeLimitHelpText(limitNumber),
      		   Status.ACTIVE,
      		   "dummyLimitRenderer.jsp");
      
      signet.save(limit);
    
      // Fetch that newly-stored Limit object from the database.
      // This step is not really necessary, but exercises a little
      // more code.
      limit
       	= this.subsystem.getLimit
       			(makeLimitId(limitNumber));
    }
    
    return limit;
  }
  
  private Category getOrCreateCategory(int categoryNumber)
  throws
  	ObjectNotFoundException
  {
    Category category;
    
    try
    {
      category
      	= this.subsystem.getCategory
      			(makeCategoryId(categoryNumber));
    }
    catch (ObjectNotFoundException onfe)
    {      
      category
      	= this.signet.newCategory
      			(subsystem,
      			 makeCategoryId(categoryNumber),
      			 makeCategoryName(categoryNumber),
      		   Status.ACTIVE);
      
      signet.save(category);
    
      // Fetch that newly-stored Function object from the database.
      // This step is not really necessary, but exercises a little
      // more code.
      category
       	= this.subsystem.getCategory
       			(makeCategoryId(categoryNumber));
    }
    
    return category;
  }
  
  /**
   * @param categoryNumber
   * @return
   */
  private String makeCategoryName(int categoryNumber)
  {
    return "CATEGORY_" + categoryNumber + "_NAME";
  }

  /**
   * @param categoryNumber
   * @return
   */
  private String makeCategoryId(int categoryNumber)
  {
    return "CATEGORY_" + categoryNumber + "_ID";
  }

  private Function getOrCreateFunction(int functionNumber)
  throws
  	ObjectNotFoundException
  {
    Function function;
    
    try
    {
      function
      	= this.subsystem.getFunction
      			(makeFunctionId(functionNumber));
    }
    catch (ObjectNotFoundException onfe)
    {      
      // Category 0 contains Function 0, Category 1 contains Function 1,
      // and so forth.
      function
      	= this.signet.newFunction
      			(subsystem.getCategory(makeCategoryId(functionNumber)),
      			 makeFunctionId(functionNumber),
      			 makeFunctionName(functionNumber),
      		   Status.ACTIVE,
      		   makeFunctionHelpText(functionNumber));
      
      signet.save(function);
    
      // Fetch that newly-stored Function object from the database.
      // This step is not really necessary, but exercises a little
      // more code.
      function
       	= this.subsystem.getFunction
       			(makeFunctionId(functionNumber));
    }
    
    return function;
  }
  
  /**
   * @param functionNumber
   * @return
   */
  private String makeFunctionName(int functionNumber)
  {
    return "FUNCTION_" + functionNumber + "_NAME";
  }

  /**
   * @param functionNumber
   * @return
   */
  private String makeFunctionHelpText(int functionNumber)
  {
    return "FUNCTION_" + functionNumber + "_HELPTEXT";
  }

  private Permission getOrCreatePermission(int permissionNumber)
  throws
  	ObjectNotFoundException
  {
    Permission permission;
    
    try
    {
      permission
      	= this.subsystem.getPermission
      			(makePermissionId(permissionNumber));
    }
    catch (ObjectNotFoundException onfe)
    {      
      // Permission 0 contains Limit 0, Permission 1 contains Limit 1,
      // and so forth.
      permission
      	= this.signet.newPermission
      			(subsystem,
      			 makePermissionId(permissionNumber),
      		   Status.ACTIVE);
      
      signet.save(permission);
    
      // Fetch that newly-stored Permission object from the database.
      // This step is not really necessary, but exercises a little
      // more code.
      permission
       	= this.subsystem.getPermission
       			(makePermissionId(permissionNumber));
    }
    
    return permission;
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
  
  public String makePermissionId(int permissionNumber)
  {
    return "PERMISSION_" + permissionNumber + "_ID";
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
  
  public String makeFunctionId(int functionNumber)
  {
    return "FUNCTION_" + functionNumber + "_ID";
  }
}
