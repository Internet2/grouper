/*--
  $Id: MakeAssignment.java,v 1.1 2004-12-09 20:49:07 mnguyen Exp $
  $Date: 2004-12-09 20:49:07 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.demo;

import edu.internet2.middleware.signet.Assignment;
import edu.internet2.middleware.signet.Function;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SignetAuthorityException;
import edu.internet2.middleware.signet.Subsystem;
import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeNotFoundException;
import edu.internet2.middleware.signet.tree.TreeTypeAdapter;
import edu.internet2.middleware.signet.tree.TreeNode;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

public class MakeAssignment
{

  /**
   * 
   */
  public MakeAssignment()
  {
    super();
    // TODO Auto-generated constructor stub
  }

  public static void main(String[] args)
  throws Exception, ObjectNotFoundException
  {
    Signet signet = new Signet();
    
    PrivilegedSubject superPSubject = signet.getSuperPrivilegedSubject();
    PrivilegedSubject pSubject0 = signet.getPrivilegedSubject("my_id0");
    PrivilegedSubject pSubject1 = signet.getPrivilegedSubject("my_id1");

    // This assignment should be illegal.
    try
    {
      makeAssignment(signet, superPSubject, pSubject0, pSubject0);
      throw new Exception("ERROR - AN ILLEGAL ASSIGNMENT WAS PERMITTED.");
    }
    catch (SignetAuthorityException sae)
    {
      System.out.println("An illegal assignment was properly prevented.");
    }
    
    // This assignment should be illegal.
    try
    {
      makeAssignment(signet, superPSubject, superPSubject, pSubject1);
      throw new Exception("ERROR - AN ILLEGAL ASSIGNMENT WAS PERMITTED.");
    }
    catch (SignetAuthorityException sae)
    {
      System.out.println("An illegal assignment was properly prevented.");
    }
    
    // This assignment should be legal.
    makeAssignment(signet, superPSubject, pSubject0, pSubject1);
  }

  private static void makeAssignment
  	(Signet 						signet,
  	 PrivilegedSubject 	originalGrantor,
  	 PrivilegedSubject 	intermediateGrantor,
  	 PrivilegedSubject 	finalGrantee)
  throws ObjectNotFoundException, SignetAuthorityException
  {
    Subsystem subsystem0
    	= signet.getSubsystem("my_id0");
   
    Function function000 = subsystem0.getFunction("my_id000");
//    Tree tree00 = subsystem0.getScope("my_id00");
//    TreeNode treeNode000 = tree00.getRoot();
//    TreeNode[] rootChildren = treeNode000.getChildrenArray();

    Assignment assignment0 = null;
    Assignment assignment1 = null;
    
    try
    {
      TreeNode treeNode = signet.getTreeNode("my_id00", "my_id0001");
      assignment0
      	= originalGrantor.grant
      			(intermediateGrantor,
      			 treeNode,
      			 function000,
      			 true,		//canGrant
      			 false);	// grantOnly
      	
      
      assignment1
      	= intermediateGrantor.grant
    				(finalGrantee,
    				 treeNode,
    			   function000,
    			   true,		// canGrant
    			   false);	// grantOnly
      
      signet.beginTransaction();
      signet.save(assignment0);
      signet.save(assignment1);
      signet.commit();
    }
    catch (ObjectNotFoundException onfe)
    {
      System.out.println("<<ERROR>> " + onfe);
      onfe.printStackTrace();
    }
    
    signet.close();
    
//    if ((treeNode000.isAncestorOf(treeNode00000))
//        && !(treeNode00000.isAncestorOf(treeNode000)))
//    {
//      System.out.println("TreeNode.isAncestorOf works properly.");
//    }
//    else
//    {
//      System.out.println("<<ERROR>> - TreeNode.isAncestorOf FAILED");
//    }
//    
//    if ((treeNode00000.isDescendantOf(treeNode000))
//        && !(treeNode000.isDescendantOf(treeNode000)))
//    {
//      System.out.println("TreeNode.isDescendantOf works properly.");
//    }
//    else
//    {
//      System.out.println("<<ERROR>> - TreeNode.isDescendantOf FAILED");
//    }
  }
}
