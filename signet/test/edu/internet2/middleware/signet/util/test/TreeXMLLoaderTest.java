/*--
$Id: TreeXMLLoaderTest.java,v 1.3 2006-06-30 02:04:41 ddonn Exp $
$Date: 2006-06-30 02:04:41 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.util.test;

import java.sql.SQLException;

import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.Status;

import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeNode;
import edu.internet2.middleware.signet.util.TreeXmlLoader;
import junit.framework.TestCase;

/**
 * @author Andy Cohen
 *
 */
public class TreeXMLLoaderTest extends TestCase
{
  private TreeXmlLoader  treeLoader;
  private Signet          signet;
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(TreeXMLLoaderTest.class);
  }

  /*
   * @see TestCase#setUp()
   */
  protected void setUp() throws Exception
  {
    super.setUp();
    treeLoader = new TreeXmlLoader();
    signet = new Signet();
  }

  /*
   * @see TestCase#tearDown()
   */
  protected void tearDown() throws Exception
  {
    super.tearDown();
    treeLoader.commit();
  }

  /**
   * Constructor for TreeXMLLoaderTest.
   * @param arg0
   */
  public TreeXMLLoaderTest(String arg0)
  {
    super(arg0);
  }
  
  public final void testNewTree()
  throws
    SQLException
  {
    treeLoader.newTree
      ("testNewTree_id",
       "testNewTree_name",
       "edu.internet2.middleware.signet.TreeAdapterImpl");
  }
  
  public final void testNewTreeNode()
  throws
    SQLException,
    ObjectNotFoundException
  {
    Tree tree = signet.getPersistentDB().getTree("testNewTree_id");
    
    TreeNode node0
      = treeLoader.newTreeNode
          (tree,
           "testNewTree_nodeId",
           "testNewTree_nodeType",
           Status.ACTIVE,
           "testNewTree_nodeName",
           null);
    
    treeLoader.newTreeNode
      (tree,
       "testNewTree_nodeId1",
       "testNewTree_nodeType",
       Status.ACTIVE,
       "testNewTree_nodeName1",
       node0);
  }

//  public final void testDeleteAll()
//  throws HibernateException, SQLException
//  {
//    subjectManager.deleteAll();
//  }
}
