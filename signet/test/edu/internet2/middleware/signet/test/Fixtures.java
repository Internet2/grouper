/*--
$Id: Fixtures.java,v 1.13 2005-06-01 06:13:08 mnguyen Exp $
$Date: 2005-06-01 06:13:08 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.test;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.naming.OperationNotSupportedException;

import edu.internet2.middleware.signet.Assignment;
import edu.internet2.middleware.signet.Category;
import edu.internet2.middleware.signet.Function;
import edu.internet2.middleware.signet.Limit;
import edu.internet2.middleware.signet.LimitValue;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Permission;
import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SignetAuthorityException;
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
  Tree			tree;
  
  /**
   * @throws OperationNotSupportedException
   * @throws TreeNotFoundException
   * @throws ObjectNotFoundException
   * @throws SignetAuthorityException
   * 
   * This method creates a data-set with the following shape:
   * 
   * Subsystem
   *   Category 0
   *     Function 0
   *       Permission 0
   *         Limit 0
   *           ChoiceSet 0
   *             Choice 0
   *   Category 1
   *     Function 1
   *       Permission 1
   *         Limit 0
   *           ChoiceSet 0
   *             Choice 0
   *         Limit 1
   *           ChoiceSet 1
   *             Choice 0
   *             Choice 1
   *   Category 2
   *     Function 2
   *       Permission 2
   *         Limit 0
   *           ChoiceSet 0
   *             Choice 0
   *         Limit 1
   *           ChoiceSet 1
   *             Choice 0
   *             Choice 1
   *         Limit 2
   *           ChoiceSet 2
   *             Choice 0
   *             Choice 1
   *             Choice 2
   * 
   *   Assignment 0
   *     grantor: SuperPrivilegedSubject
   *     grantee: Subject 0
   *     Function 0
   *   Assignment 1
   *     grantor: SuperPrivilegedSubject
   *     grantee: Subject 1
   *     Function 1
   *   Assignment 2
   *     grantor: SuperPrivilegedSubject
   *     grantee: Subject 2
   *     Function 2
   */
  public Fixtures(Signet signet)
  throws
  	OperationNotSupportedException,
  	TreeNotFoundException,
  	SignetAuthorityException,
  	ObjectNotFoundException
  {
    super();
    this.signet = signet;
    
    this.subsystem = getOrCreateSubsystem();
    
    tree = this.signet.getTree(Constants.TREE_ID);
    this.subsystem.setTree(this.tree);
    
    for (int i = 0; i < Constants.MAX_CATEGORIES; i++)
    {
      Category category = getOrCreateCategory(i); 
    }
    
    for (int i = 0; i < Constants.MAX_CHOICE_SETS; i++)
    {
      ChoiceSet choiceSet = getOrCreateChoiceSet(i); 
    }
    
    for (int i = 0; i < Constants.MAX_LIMITS; i++)
    {
      Limit limit = getOrCreateLimit(i);
    }
    
    for (int i = 0; i < Constants.MAX_FUNCTIONS; i++)
    {
      Function function = getOrCreateFunction(i);
    }
    
    for (int permissionNum = 0;
         permissionNum < Constants.MAX_PERMISSIONS;
         permissionNum++)
    {
      Permission permission = getOrCreatePermission(permissionNum);
      
      // Permission 0 is associated with Limit 0 and Function 0,
      // Permission 1 is associated with Limits 0 and 1 and Function 1,
      // and so on.
      getOrCreateFunction(permissionNum).addPermission(permission);
      
      for (int limitNum = 0; limitNum <= permissionNum; limitNum++)
      {
        permission.addLimit(getOrCreateLimit(limitNum));
      }
    }
    
    signet.save(this.subsystem);
    
    for (int i = 0; i < Constants.MAX_SUBJECTS; i++)
    {
      Assignment assignment = getOrCreateAssignment(i);
      this.signet.save(assignment);
    }
    
    // This is intended to create an Assignment for Subject 0 that can
    // be edited by Subject 2, but does not allow Subject 0 to edit the
    // similar (but using different Limit-values) Assignment held by
    // Subject 2.
    int[] limitChoiceNumbers = {0, 0, 0};
    Assignment assignment
    	= getOrCreateAssignment
    			(0,  // subject-number
    			 2,  // function-number
    			 limitChoiceNumbers);
    this.signet.save(assignment);
  }
  
  private Assignment getOrCreateAssignment
    (int   subjectNumber,
     int   functionNumber,
     int[] limitValueNumbers)
  throws
  	SignetAuthorityException,
  	ObjectNotFoundException
  {
    PrivilegedSubject superPrivilegedSubject
  	  = signet.getSuperPrivilegedSubject();
    TreeNode rootNode = getRoot(tree);
    Subject subject = getOrCreateSubject(subjectNumber);
    PrivilegedSubject pSubject = signet.getPrivilegedSubject(subject);
    Function function = getOrCreateFunction(functionNumber);
    Limit[] limitsInDisplayOrder = function.getLimitsArray();
    
    Set limitValues = new HashSet();
    for (int i = 0; i < limitValueNumbers.length; i++)
    {
      Limit limit = limitsInDisplayOrder[i];
      int limitChoiceNumber = limitValueNumbers[i];
      String value = makeChoiceValue(limitChoiceNumber);
      LimitValue limitValue = new LimitValue(limit, value);
      limitValues.add(limitValue);
    }
    
    // Before granting this new Assignment, let's see if it already exists
    // in the database.
    
    Set assignmentsReceived
      = pSubject.getAssignmentsReceived
          (Status.ACTIVE, this.subsystem, function);
    
    Iterator assignmentsReceivedIterator = assignmentsReceived.iterator();
    while (assignmentsReceivedIterator.hasNext())
    {
      Assignment assignmentReceived
      	= (Assignment)(assignmentsReceivedIterator.next());
      
      if (assignmentReceived.getScope().equals(rootNode)
          && (assignmentReceived.getLimitValuesArray().length
              == limitValues.size()))
      {
        return assignmentReceived;
      }
    }
    
    // If we've gotten this far, we must not have this Assignment yet.
    
    Assignment assignment
    	= superPrivilegedSubject.grant
    			(pSubject,
    	     rootNode,
    	     function,
    	     limitValues,
    	     true,    // canGrant
    	     false);  // grantOnly
    
    return assignment;
  }
  
  private Assignment getOrCreateAssignment(int assignmentNumber)
  throws
  	SignetAuthorityException,
  	ObjectNotFoundException
  {
    int[] limitChoiceNumbers = new int[assignmentNumber + 1];
    for (int i = 0; i <= assignmentNumber; i++)
    {
      // We'll choose the first LimitChoice for Limit 0, the second for Limit 1,
      // and so forth.
      limitChoiceNumbers[i] = i;
    }
    
    return getOrCreateAssignment
    	(assignmentNumber, // subjectNumber
       assignmentNumber, // functionNumber,
       limitChoiceNumbers);
  }

  /**
   * Gets the Nth choice-value of the Nth limit for the specified function.
   * The ordering is determined by the display-order.
   * 
   * @param function
   * @param limitAndValueNumber
   * @return
   * @throws ObjectNotFoundException
   */
  private LimitValue getLimitValue
  	(Function function,
  	 int			limitAndValueNumber)
  throws ObjectNotFoundException
  {
    Limit limit = function.getLimitsArray()[limitAndValueNumber];
    Choice[] choices = limit.getChoiceSet().getChoicesInDisplayOrder();
    Choice choice = choices[limitAndValueNumber];
    
    LimitValue limitValue = new LimitValue(limit, choice.getValue());
    return limitValue;
  }

  /**
   * @param i
   * @return
   * @throws ObjectNotFoundException
   */
  private Subject getOrCreateSubject(int subjectNumber)
  throws ObjectNotFoundException
  {
    Subject subject = null;
    
    try
    {
      subject = this.signet.getSubject(
      		Signet.DEFAULT_SUBJECT_TYPE_ID, makeSubjectId(subjectNumber));
    }
    catch (ObjectNotFoundException onfe)
    {
      //subject
    	  //= this.signet.newSubject
    		  	//(makeSubjectId(subjectNumber),
    		  	// makeSubjectName(subjectNumber),
    		  	// makeSubjectDescription(subjectNumber),
    		  	// makeSubjectDisplayId(subjectNumber));
    }
    
    return subject;
  }

  /**
   * @param subjectNumber
   * @return
   */
  String makeSubjectId(int subjectNumber)
  {
    return
      "SUBJECT"
      + Constants.DELIMITER
      + subjectNumber
      + Constants.DELIMITER
      + "ID";
  }

  private String makeSubjectName(int subjectNumber)
  {
    return
      "SUBJECT"
      + Constants.DELIMITER
      + subjectNumber
      + Constants.DELIMITER
      + "NAME";
  }

  private String makeSubjectDescription(int subjectNumber)
  {
    return
      "SUBJECT"
      + Constants.DELIMITER
      + subjectNumber
      + Constants.DELIMITER
      + "DESCRIPTION";
  }

  private String makeSubjectDisplayId(int subjectNumber)
  {
    return
      "SUBJECT"
      + Constants.DELIMITER
      + subjectNumber
      + Constants.DELIMITER
      + "DISPLAYID";
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
  
//  private Tree getOrCreateTree()
//  throws TreeNotFoundException
//  {
//    Tree tree;
//    
//    try
//    {
//      tree = this.signet.getTree(Constants.TREE_ID);
//    }
//    catch (ObjectNotFoundException onfe)
//    {
//      tree = signet.newTree(Constants.TREE_ID, Constants.TREE_NAME);
//      TreeNode root
//      	= signet.newTreeNode
//      			(tree, makeTreeNodeId(0, null, 0), makeTreeNodeName(0, null, 0));
//      tree.addRoot(root);
//      createDescendantTreeNodes(root, 1);
//    }
//    
//    return tree;
//  }

//  private void createDescendantTreeNodes(TreeNode parent, int treeLevel)
//  throws TreeNotFoundException
//  {
//    if (treeLevel >= Constants.MAX_TREE_DEPTH)
//    {
//      return;
//    }
//    
//    for
//    	(int S = 0;
//    	 S < Constants.MAX_TREE_WIDTH;
//    	 S++)
//    {
//      TreeNode node
//      	= signet.newTreeNode
//      			(parent.getTree(),
//      			 makeTreeNodeId(treeLevel, parent, S),
//      			 makeTreeNodeName(treeLevel, parent, S));
//      
//      parent.addChild(node);
//      
//      createDescendantTreeNodes(node, treeLevel + 1);
//    }
//  }

  public String makeTreeNodeId(int level, TreeNode parent, int S)
  {
    return
    	"L" 
      + Constants.DELIMITER
      + level
      + Constants.DELIMITER
      + "PID"
      + Constants.DELIMITER
      + "["
      + (parent == null ? "NOPARENTID" : parent.getId())
      + "]"
      + Constants.DELIMITER
      + "S"
      + Constants.DELIMITER
      + S
      + Constants.DELIMITER
      + "ID";
  }

  private String makeTreeNodeName(int level, TreeNode parent, int S)
  {
    return
    	"L"
      + Constants.DELIMITER
      + level
      + Constants.DELIMITER
      + "PNAME"
      + Constants.DELIMITER
      + "["
      + (parent == null ? "NOPARENTNAME" : parent.getName())
      + "]"
      + Constants.DELIMITER
      + "S"
      + Constants.DELIMITER
      + S
      + Constants.DELIMITER
      + "NAME";
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
      
      for (int i = 0; i <= choiceSetNumber; i++)
      {
        // Create and persist the Choice object to be tested.
        // ChoiceSet 0 has choice 0, choiceSet 1 has choices 0 and 1, and so on.
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
      //
      // Even-numbered Limits are multiple-selects, odd-numbered Limits are
      // single-selects.
      limit
      	= this.signet.newLimit
      			(subsystem,
      			 makeLimitId(limitNumber),
      			 makeLimitDataType(limitNumber),
      			 subsystem.getChoiceSet(makeChoiceSetId(limitNumber)),
      			 makeLimitName(limitNumber),
      			 limitNumber,
      		   makeLimitHelpText(limitNumber),
      		   Status.ACTIVE,
      		   isEven(limitNumber)
      		   	? "multipleChoiceCheckboxes.jsp"
      		   	  : "singleChoicePullDown.jsp");
    
      // Fetch that newly-stored Limit object from the subsystem.
      // This step is not really necessary, but exercises a little
      // more code.
      limit
       	= this.subsystem.getLimit
       			(makeLimitId(limitNumber));
    }
    
    return limit;
  }
  
  private boolean isEven(int number)
  {
    if ((number % 2) == 0)
    {
      return true;
    }
    else
    {
      return false;
    }
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
    return "CATEGORY" + Constants.DELIMITER + categoryNumber + Constants.DELIMITER + "NAME";
  }

  /**
   * @param categoryNumber
   * @return
   */
  private String makeCategoryId(int categoryNumber)
  {
    return "CATEGORY" + Constants.DELIMITER + categoryNumber + Constants.DELIMITER + "ID";
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
    return "FUNCTION" + Constants.DELIMITER + functionNumber + Constants.DELIMITER + "NAME";
  }

  /**
   * @param functionNumber
   * @return
   */
  private String makeFunctionHelpText(int functionNumber)
  {
    return "FUNCTION" + Constants.DELIMITER + functionNumber + Constants.DELIMITER + "HELPTEXT";
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
    return "CHOICE" + Constants.DELIMITER + choiceNumber + Constants.DELIMITER + "VALUE";
  }
  
  public String makeChoiceDisplayValue(int choiceNumber)
  {
    return "CHOICE" + Constants.DELIMITER + choiceNumber + Constants.DELIMITER + "DISPLAY_VALUE";
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
    return "CHOICE_SET" + Constants.DELIMITER + choiceNumber + Constants.DELIMITER + "ID";
  }
  
  public String makeLimitId(int limitNumber)
  {
    return "LIMIT" + Constants.DELIMITER + limitNumber + Constants.DELIMITER + "ID";
  }
  
  public String makePermissionId(int permissionNumber)
  {
    return "PERMISSION" + Constants.DELIMITER + permissionNumber + Constants.DELIMITER + "ID";
  }
  
  public String makeLimitName(int limitNumber)
  {
    return "LIMIT" + Constants.DELIMITER + limitNumber + Constants.DELIMITER + "NAME";
  }
  
  public String makeLimitHelpText(int limitNumber)
  {
    return "LIMIT" + Constants.DELIMITER + limitNumber + Constants.DELIMITER + "HELPTEXT";
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
    return "FUNCTION" + Constants.DELIMITER + functionNumber + Constants.DELIMITER + "ID";
  }

  /**
   * @param limitNumber
   * @return
   */
  public String makeLimitValue(int limitNumber)
  {
    return "LIMIT" + Constants.DELIMITER + limitNumber + Constants.DELIMITER + "VALUE";
  }
  
  public TreeNode getRoot(Tree tree)
  {
    TreeNode root = null;
    
    Set roots = tree.getRoots();
    Iterator rootsIterator = roots.iterator();
    while (rootsIterator.hasNext())
    {
      root = (TreeNode)(rootsIterator.next());
    }
    
    return root;
  }
  
  public int expectedLimitValuesCount(int subjectNumber)
  {
    return subjectNumber + 1;
  }
}
