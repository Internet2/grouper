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
import edu.internet2.middleware.signet.DataType;
import edu.internet2.middleware.signet.choice.Choice;
import edu.internet2.middleware.signet.choice.ChoiceSet;
import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeNode;
import edu.internet2.middleware.signet.tree.TreeNotFoundException;
import edu.internet2.middleware.subject.Subject;

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
   * @throws TreeNotFoundException
   * 
   */
  public Fixtures(Signet signet)
  throws
  	OperationNotSupportedException,
  	ObjectNotFoundException,
  	TreeNotFoundException
  {
    super();
    this.signet = signet;
    this.subsystem = getOrCreateSubsystem();
    
    Tree tree = getOrCreateTree();
    subsystem.setTree(tree);
    
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
      getOrCreateFunction(i).addPermission(permission);
      permission.addLimit(getOrCreateLimit(i));
    }
    
    for (int i = 0; i < Constants.MAX_SUBJECTS; i++)
    {
      Subject subject = getOrCreateSubject(i);
    }
    
    signet.save(this.subsystem);
  }
  
  /**
   * @param i
   * @return
   * @throws ObjectNotFoundException
   */
  private Subject getOrCreateSubject(int subjectNumber)
  throws ObjectNotFoundException
  {
    Subject subject;
    
    try
    {
      subject = this.signet.getSubject(makeSubjectId(subjectNumber));
    }
    catch (ObjectNotFoundException onfe)
    {
      subject
    	  = this.signet.newSubject
    		  	(makeSubjectId(subjectNumber), makeSubjectName(subjectNumber));
    }
    
    return subject;
  }

  /**
   * @param subjectNumber
   * @return
   */
  private String makeSubjectId(int subjectNumber)
  {
    return "SUBJECT_" + subjectNumber + "_ID";
  }

  private String makeSubjectName(int subjectNumber)
  {
    return "SUBJECT_" + subjectNumber + "_NAME";
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
    }
    
    return this.subsystem;
  }
  
  private Tree getOrCreateTree()
  throws TreeNotFoundException
  {
    Tree tree;
    
    try
    {
      tree = this.signet.getTree(Constants.TREE_ID);
    }
    catch (ObjectNotFoundException onfe)
    {
      tree = signet.newTree(Constants.TREE_ID, Constants.TREE_NAME);
      TreeNode root
      	= signet.newTreeNode
      			(tree, makeTreeNodeId(0, 0), makeTreeNodeName(0, 0));
      tree.addRoot(root);
      createDescendantTreeNodes(root, 1);
    }
    
    return tree;
  }
  
  private void createDescendantTreeNodes(TreeNode parent, int treeLevel)
  throws TreeNotFoundException
  {
    if (treeLevel >= Constants.MAX_TREE_DEPTH)
    {
      return;
    }
    
    for
    	(int siblingNumber = 0;
    	 siblingNumber < Constants.MAX_TREE_WIDTH;
    	 siblingNumber++)
    {
      TreeNode node
      	= signet.newTreeNode
      			(parent.getTree(),
      			 makeTreeNodeId(treeLevel, siblingNumber),
      			 makeTreeNodeName(treeLevel, siblingNumber));
      
      createDescendantTreeNodes(node, treeLevel + 1);
    }
  }

  private String makeTreeNodeId(int level, int siblingNumber)
  {
    return
    	"TREENODE_LEVEL_" + level + "_SIBLINGNUMBER_" + siblingNumber + "_ID";
  }

  private String makeTreeNodeName(int level, int siblingNumber)
  {
    return
    	"TREENODE_LEVEL_" + level + "_SIBLINGNUMBER_" + siblingNumber + "_NAME";
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
  
      // Fetch that newly-stored ChoiceSet object from the subsystem.
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
      			 makeLimitDataType(limitNumber),
      			 subsystem.getChoiceSet(makeChoiceSetId(limitNumber)),
      			 makeLimitName(limitNumber),
      		   makeLimitHelpText(limitNumber),
      		   Status.ACTIVE,
      		   "singleChoicePullDown.jsp");
    
      // Fetch that newly-stored Limit object from the subsystem.
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
    
      // Fetch that newly-stored Function object from the subsystem.
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
    
      // Fetch that newly-stored Function object from the subsystem.
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
    
      // Fetch that newly-stored Permission object from the subsystem.
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
  
  public DataType makeLimitDataType(int limitNumber)
  {
    final int DATA_TYPE_COUNT = 3;
    
    switch (limitNumber % DATA_TYPE_COUNT)
    {
      case 0:
        return DataType.DATE;
      case 1:
        return DataType.NUMERIC;
      case 2:
      default:
        return DataType.TEXT;
    }
  }
  
  public String makeFunctionId(int functionNumber)
  {
    return "FUNCTION_" + functionNumber + "_ID";
  }
}
