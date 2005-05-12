/*
SubsystemFileLoader.java
Created on Feb 22, 2005

Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/

package edu.internet2.middleware.signet.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.io.*;

import org.apache.commons.collections.set.UnmodifiableSet;



import javax.xml.stream.*;

import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeAdapter;
import edu.internet2.middleware.signet.tree.TreeNode;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;

public class TreeXMLLoader
{
  private static SessionFactory sessionFactory;
  private        Session        session;
  private        Connection     conn;

  private String[] deletionStatements
    = new String[]
        {
          "delete from TreeNodeRelationship",
          "delete from TreeNode",
          "delete from Tree"
        };
  
  private String insertTreeSQL
    = "insert into Tree"
      + "(treeID,"
      + " name,"
      + " adapterClass,"
      + " modifyDatetime)"
      + "values (?, ?, ?, ?)";
  
  private String insertTreeNodeSQL
    = "insert into TreeNode"
      + "(treeID,"
      + " nodeID,"
      + " nodeType,"
      + " status,"
      + " name,"
      + " modifyDatetime)"
      + "values (?, ?, ?, ?, ?, ?)";


  private String insertTreeNodeRelationshipSQL
    = "insert into TreeNodeRelationship"
      + "(treeID,"
      + " nodeID,"
      + " parentNodeID)"
      + "values (?, ?, ?)";
  
  private String ELEMENTNAME_SIGNETTREE   = "SignetTree";
  private String ELEMENTNAME_ID           = "Id";
  private String ELEMENTNAME_NAME         = "Name";
  private String ELEMENTNAME_ORGANIZATION = "Organization";
  private String ELEMENTNAME_TYPE         = "Type";
  
  private String ADAPTERCLASSNAME
    = "edu.internet2.middleware.signet.TreeAdapterImpl";

  
  static
  /* runs at class load time */
  {
    Configuration cfg = new Configuration();

    try
    {
      // Read the "hibernate.cfg.xml" file.
      cfg.configure();
      sessionFactory = cfg.buildSessionFactory();
    }
    catch (HibernateException he)
    {
      throw new RuntimeException(he);
    }
  }
    
  /**
   * Opens a connection to the database for subsequent use in loading
   * and deleting Trees.
   *
   */
  public TreeXMLLoader()
  {
    try
    {
      this.session = sessionFactory.openSession();
      this.conn = session.connection();
    }
    catch (HibernateException he)
    {
      throw new RuntimeException(he);
    }
  }
  
  
  /**
   * Creates a new TreeNode, and stores that value in the database, along with
   * any node-relationship information.
   * This method updates the database, but does not commit any transaction.
   * 
   * @param tree
   * @param nodeID
   * @param nodeType
   * @param status
   * @param name
   * @param parent
   * @throws SQLException
   */
  public TreeNode newTreeNode
    (Tree     tree,
     String   nodeID,
     String   nodeType,
     Status   status,
     String   name,
     TreeNode parent)
  throws
    SQLException
  {
    PreparedStatement pStmt
      = this.conn.prepareStatement(insertTreeNodeSQL);
  
    pStmt.setString(1, tree.getId());
    pStmt.setString(2, nodeID);
    pStmt.setString(3, nodeType);
    pStmt.setString(4, status.toString());
    pStmt.setString(5, name);
    pStmt.setDate(6, new Date(new java.util.Date().getTime()));
    pStmt.executeUpdate();
    
    TreeNode newNode = new NodeImpl(tree, nodeID, nodeType, status, name);
    
    if (parent == null)
    {
      tree.addRoot(newNode);
    }
    else
    {
      newTreeNodeRelationship(newNode, parent);
    }
    
    return newNode;
  }
  
  
  /**
   * Creates a new TreeNodeRelationship record, and stores that value in the
   * database.
   * This method updates the database, but does not commit any transaction.
   * 
   * @param tree
   * @param nodeID
   * @param nodeType
   * @param status
   * @param name
   * @param parent
   * @throws SQLException
   */
  private void newTreeNodeRelationship
    (TreeNode child,
     TreeNode parent)
  throws
    SQLException
  {
    PreparedStatement pStmt
      = this.conn.prepareStatement(insertTreeNodeRelationshipSQL);
  
    pStmt.setString(1, child.getTree().getId());
    pStmt.setString(2, child.getId());
    pStmt.setString(3, parent.getId());
    pStmt.executeUpdate();

    parent.addChild(child);
  }

  
  /**
   * Deletes all Tree data and associated TreeNode and TreeNodeRelationship
   * data.
   * This method updates the database, but does not commit any transaction.
   * 
   * @throws SQLException
   */
  private void deleteAll()
  throws SQLException
  {
    try
    {
      //conn.setAutoCommit(true);
      for (int i = 0; i < this.deletionStatements.length; i++)
      {
        execute(conn, this.deletionStatements[i], "deleted");
      }
    }
    catch (SQLException ex)
    {
      conn.rollback();
      System.out.println("SQL error occurred: " + ex.getMessage());
    }
  }
  
  /**
   * Commits the current database transaction in use by the TreeXMLLoader.
   * 
   * @throws SQLException
   */
  public void commit() throws SQLException
  {
    this.conn.commit();
  }
  
  
  private void execute(Connection conn, String sql, String verb)
  throws SQLException
  {
    PreparedStatement ps = conn.prepareStatement(sql);
    int rows = ps.executeUpdate();
    System.out.println("Number of rows " + verb + ": " + rows);
  }

  /**
   * Creates a new Tree.
   * This method updates the database, but does not commit any transaction.
   * 
   * @param id
   * @param name
   * @param adapterClassName
   * @return
   * @throws SQLException
   */
  public Tree newTree
    (String id,
     String name,
     String adapterClassName)
  throws
    SQLException
  {
    PreparedStatement pStmt
      = this.conn.prepareStatement(insertTreeSQL);
  
    pStmt.setString(1, id);
    pStmt.setString(2, name);
    pStmt.setString(3, adapterClassName);
    pStmt.setDate(4, new Date(new java.util.Date().getTime()));
    pStmt.executeUpdate();
    
    Tree tree = new TreeImpl(id, name, adapterClassName);

    return tree;
  }
  
  private class TreeImpl implements Tree
  {
    private String id;
    private String name;
    private String adapterClassName;
    private Set    roots;
    
    private TreeImpl
      (String id,
       String name,
       String adapterClassName)
    {
      this.id = id;
      this.name = name;
      this.adapterClassName = adapterClassName;
      
      this.roots = new HashSet();
    }
    
    public void addRoot(TreeNode rootNode)
    {
      this.roots.add(rootNode);
    }
    
    public TreeAdapter getAdapter()
    {
      throw new UnsupportedOperationException
        ("This implementation of the Tree interface has '"
         + this.adapterClassName
         + "' as its adapterClassName, but does not yet support"
         + " instantiation of that adapter.");
    }
    
    public String getId()
    {
      return this.id;
    }
    
    public String getName()
    {
      return this.name;
    }
    
    public TreeNode getNode(String nodeId)
    {
      throw new UnsupportedOperationException();
    }
    
    public Set getRoots()
    {
      return UnmodifiableSet.decorate(this.roots);
    }
    
    public Set getTreeNodes()
    {
      throw new UnsupportedOperationException();
    }
}
  
  private class NodeImpl implements TreeNode
  {
    private Tree    tree;
    private String  id;
    private String  type;
    private Status  status;
    private String  name;
    
    private Set     parents;
    private Set     children;
    
    NodeImpl
      (Tree   tree,
       String id,
       String type,
       Status status,
       String name)
    {
      this.tree = tree;
      this.id = id;
      this.type = type;
      this.status = status;
      this.name = name;
      
      this.children = new HashSet();
      this.parents = new HashSet();
    }
    
    public Tree getTree()
    {
      return this.tree;
    }
    
    public String getId()
    {
      return this.id;
    }
    
    public String getType()
    {
      return this.type;
    }
    
    public Status getStatus()
    {
      return this.status;
    }
    
    public String getName()
    {
      return this.name;
    }

    public Set getParents()
    {
      return UnmodifiableSet.decorate(this.parents);
    }

    public Set getChildren()
    {
      return UnmodifiableSet.decorate(this.children);
    }

    public void addChild(TreeNode treeNode)
    {
      this.children.add(treeNode);
    }
    
    public boolean isAncestorOf(TreeNode treeNode)
    {
      throw new UnsupportedOperationException();
    }

    public boolean isAncestorOfAll(Set treeNodes)
    {
      throw new UnsupportedOperationException();
    }

    public boolean isDescendantOf(TreeNode treeNode)
    {
      throw new UnsupportedOperationException();
    }

    public int compareTo(Object o)
    {
      TreeNode otherNode = (TreeNode)o;
      return this.id.compareTo(otherNode.getId());
    }
    
    public int hashCode()
    {
      return this.id.hashCode();
    }
    
    public boolean equals(Object obj)
    {
      TreeNode otherNode = (TreeNode)obj;
      return this.id.equals(otherNode.getId());
    }
  }

  public static void main(String[] args)
  {
    TreeXMLLoader loader = new TreeXMLLoader();

    try
    {
      if (args.length < 1)
      {
        System.err.println("Usage: TreeXMLLoader <inputfile>");
        return;
      }
         
      String inputFileName = args[0];
      BufferedReader in = new BufferedReader(new FileReader(inputFileName));

      loader.processFile(loader, in);

      in.close();
      loader.commit();
    }
    catch (Exception e)
    {
       e.printStackTrace();
    }
  }
  
  private void processFile
    (TreeXMLLoader loader,
     BufferedReader in)
  throws
    XMLStreamException,
    SQLException
  {
    removeTrees();
    
    System.setProperty
      ("javax.xml.stream.XMLInputFactory",
       "com.ctc.wstx.stax.WstxInputFactory");
    XMLInputFactory factory = XMLInputFactory.newInstance();
    ((com.ctc.wstx.stax.WstxInputFactory)factory).configureForMaxConvenience();
    XMLStreamReader parser = factory.createXMLStreamReader(in);
      
    while (true)
    {
      int event = parser.next();
      if (event == XMLStreamConstants.END_DOCUMENT)
      {
         parser.close();
         break;
      }
        
      if (event == XMLStreamConstants.START_ELEMENT)
      {
        if (parser.getLocalName().equals(ELEMENTNAME_SIGNETTREE))
        {
          processSignetTree(parser, loader);
        }
        else
        {
          Set expectedElementSet = new HashSet();
          expectedElementSet.add(ELEMENTNAME_SIGNETTREE);
          reportUnexpectedElement(parser, expectedElementSet);
        }
      }
    }
  }
  
  private Tree processSignetTree
    (XMLStreamReader  parser,
     TreeXMLLoader   loader)
  throws
    XMLStreamException,
    SQLException
  {
    String treeId   = null;
    String treeName = null;
    Tree   tree     = null;
    
    while (true)
    {
      int event = parser.next();
      
      switch (event)
      {
        case XMLStreamConstants.CHARACTERS:
          // We don't care about this.
          break;
        
        case XMLStreamConstants.END_ELEMENT:
          if (parser.getLocalName().equals(ELEMENTNAME_SIGNETTREE))
          {
            // We've finished processing the "SignetTree" element.
            // If we failed to create a Tree, then that's an error.
            if (tree == null)
            {
              reportIncompleteTree(treeId, treeName);
            }
            else
            {
              return tree;
            }
          }
          else
          {
            reportUnexpectedEndElement
              (parser, parser.getLocalName(), ELEMENTNAME_SIGNETTREE);
          }
            
          break;
          
        case XMLStreamConstants.START_ELEMENT:
          String localName = parser.getLocalName();
          if (localName.equals(ELEMENTNAME_ID))
          {
            if (treeId != null)
            {
              reportRepeatedElement(parser);
            }
            else
            {
              treeId = processId(parser);
            }
          }
          else if (localName.equals(ELEMENTNAME_NAME))
          {
            if (treeName != null)
            {
              reportRepeatedElement(parser);
            }
            else
            {
              treeName = processName(parser);
            }
          }
          else if (localName.equals(ELEMENTNAME_ORGANIZATION))
          {
            processOrganization(parser, loader, tree, null);
          }
          
          break;
          
        default:
            System.out.println("FOUND NEW EVENT: " + event);
      }
      
      if (tree == null)
      {
        tree = buildTreeIfComplete(loader, treeId, treeName);
      }
    }
  }
  
  private TreeNode processOrganization
    (XMLStreamReader  parser,
     TreeXMLLoader   loader,
     Tree             tree,
     TreeNode         parent)
  throws
    XMLStreamException,
    SQLException
  {
    String id   = null;
    String type = null;
    String name = null;
    TreeNode treeNode = null;
    
    if (tree == null)
    {
      System.out.println
        ("A '"
         + ELEMENTNAME_ORGANIZATION
         + "' element was encountered before its enclosing '"
         + ELEMENTNAME_SIGNETTREE
         + "' element was successfully created. This is an error.");
    }
    else
    {
      while (true)
      {
        int event = parser.next();
        
        switch (event)
        {
          case XMLStreamConstants.CHARACTERS:
            // We don't care about this.
            break;
          
          case XMLStreamConstants.END_ELEMENT:
            if (parser.getLocalName().equals(ELEMENTNAME_ORGANIZATION))
            {
              // We've finished processing the "Organization" element.
              // If we failed to create a TreeNode, then that's an error.
              if (treeNode == null)
              {
                reportIncompleteTreeNode(id, type, name);
              }
              else
              {
                return treeNode;
              }
            }
            else
            {
              reportUnexpectedEndElement
                (parser, parser.getLocalName(), ELEMENTNAME_ORGANIZATION);
            }
              
            break;
            
          case XMLStreamConstants.START_ELEMENT:
            String localName = parser.getLocalName();
            if (localName.equals(ELEMENTNAME_ID))
            {
              if (id != null)
              {
                reportRepeatedElement(parser);
              }
              else
              {
                id = processId(parser);
              }
            }
            if (localName.equals(ELEMENTNAME_TYPE))
            {
              if (type != null)
              {
                reportRepeatedElement(parser);
              }
              else
              {
                type = processType(parser);
              }
            }
            else if (localName.equals(ELEMENTNAME_NAME))
            {
              if (name != null)
              {
                reportRepeatedElement(parser);
              }
              else
              {
                name = processName(parser);
              }
            }
            else if (localName.equals(ELEMENTNAME_ORGANIZATION))
            {
              processOrganization(parser, loader, tree, treeNode);
            }
            
            break;
            
          default:
              System.out.println("FOUND NEW EVENT: " + event);
        }
        
        if (treeNode == null)
        {
          treeNode
            = buildTreeNodeIfComplete(loader, tree, parent, id, type, name);
        }
      }
      
    }
    
    return treeNode;
  }
  
  private String processId(XMLStreamReader parser)
  throws XMLStreamException
  {
    return parser.getElementText();
  }
  
  private String processName(XMLStreamReader parser)
  throws XMLStreamException
  {
    return parser.getElementText();
  }
  
  private String processType(XMLStreamReader parser)
  throws XMLStreamException
  {
    return parser.getElementText();
  }
  
  private Tree buildTreeIfComplete
    (TreeXMLLoader loader,
     String         id,
     String         name)
  throws SQLException
  {
    Tree tree = null;
    
    if ((id != null) && (name != null))
    {
      tree = loader.newTree(id, name, ADAPTERCLASSNAME);
    }
    
    return tree;
  }
  
  private TreeNode buildTreeNodeIfComplete
    (TreeXMLLoader loader,
     Tree           tree,
     TreeNode       parent,
     String         id,
     String         type,
     String         name)
  throws SQLException
  {
    TreeNode treeNode = null;
    
    if ((id != null) && (type != null) && (name != null))
    {
      treeNode
        = loader.newTreeNode(tree, id, type, Status.ACTIVE, name, parent);
    }
    
    return treeNode;
  }
  
  private void reportIncompleteTree
    (String treeId,
     String treeName)
  {
    if (treeId == null)
    {
      System.out.println
        ("The XML input file contained an incomplete '"
         + ELEMENTNAME_SIGNETTREE
         + "' definition. The required element '"
         + ELEMENTNAME_ID
         + "' was missing. This is an error.");
    }
    
    if (treeName == null)
    {
      System.out.println
        ("The XML input file contained an incomplete '"
         + ELEMENTNAME_SIGNETTREE
         + "' definition. The required element '"
         + ELEMENTNAME_NAME
         + "' was missing. This is an error.");
    }
    
    if ((treeId != null) && (treeName != null))
    {
      throw new RuntimeException
        ("An incomplete '"
         + ELEMENTNAME_SIGNETTREE
         + "' definition was reported, but its '"
         + ELEMENTNAME_ID
         + "' and '"
         + ELEMENTNAME_NAME
         + "' elements were both defined. This is an unexpected program "
         + "condition.");
    }
  }
  
  private void reportIncompleteTreeNode
    (String id,
     String type,
     String name)
  {
    if (id == null)
    {
      System.out.println
        ("The XML input file contained an incomplete '"
         + ELEMENTNAME_ORGANIZATION
         + "' definition. The required element '"
         + ELEMENTNAME_ID
         + "' was missing. This is an error.");
    }
    
    if (type == null)
    {
      System.out.println
        ("The XML input file contained an incomplete '"
         + ELEMENTNAME_ORGANIZATION
         + "' definition. The required element '"
         + ELEMENTNAME_TYPE
         + "' was missing. This is an error.");
    }
    
    if (name == null)
    {
      System.out.println
        ("The XML input file contained an incomplete '"
         + ELEMENTNAME_ORGANIZATION
         + "' definition. The required element '"
         + ELEMENTNAME_NAME
         + "' was missing. This is an error.");
    }
    
    if ((id != null) && (type != null) && (name != null))
    {
      throw new RuntimeException
        ("An incomplete '"
         + ELEMENTNAME_ORGANIZATION
         + "' definition was reported, but its '"
         + ELEMENTNAME_ID
         + "' and '"
         + ELEMENTNAME_TYPE
         + "' and '"
         + ELEMENTNAME_NAME
         + "' elements were all defined. This is an unexpected program "
         + "condition.");
    }
  }
  
  private void reportRepeatedElement
    (XMLStreamReader parser)
  {
    System.out.println
      ("XML parser encountered unexpected element '"
       + parser.getLocalName()
       + "' at line "
       + parser.getLocation().getLineNumber()
       + ", column "
       + parser.getLocation().getColumnNumber()
       + ". This element is illegally repeated: It is allowed to appear only "
       + "once within its enclosing element, and it has already appeared "
       + "within the current enclosing element.");
  }
  
  private void reportUnexpectedEndElement
    (XMLStreamReader parser,
     String unexpectedName,
     String expectedName)
  {
    System.out.println
      ("XML parser encountered unexpected end-element '"
       + unexpectedName
       + "' at line "
       + parser.getLocation().getLineNumber()
       + ", column "
       + parser.getLocation().getColumnNumber()
       + ". Only an end-element '"
       + expectedName
       + "' is allowed at this point in the file.");
  }
  
  private void reportUnexpectedElement
    (XMLStreamReader parser,
     Set             expectedElementNames)
  {
    System.out.println
      ("XML parser encountered unexpected element '"
       + parser.getLocalName()
       + "' at line "
       + parser.getLocation().getLineNumber()
       + ", column "
       + parser.getLocation().getColumnNumber()
       + ". These are the element-names which are expected at this point: "
       + commaSeparatedList(expectedElementNames));
  }
  
  private String commaSeparatedList(Set strings)
  {
    StringBuffer output = new StringBuffer();
    Iterator iterator = strings.iterator();
    while (iterator.hasNext())
    {
      if (output.length() > 0)
      {
        output.append(", ");
      }
      
      output.append((String)(iterator.next()));
    }
    
    return output.toString();
  }
  
  

  //private void processFile
  //  (Signet signet, TreeXMLLoader loader, BufferedReader in)
  //  throws IOException, ObjectNotFoundException, SQLException {
  //    try {
  //
  //    String lineData = "";
  //    String lineData2 = "";
  //    String lineData3 = "";
  //    String keyword = "";
  //    String value = "";
  //    String subjectSourceID = "";
  //    String subjectTypeID = "";
  //    String subjectID = "";
  //    String subjectName = "";
  //    int    lineNumber  = 0;
  //
  //    while ((lineData = in.readLine()) != null) {
  //      lineNumber++;
  //      // System.out.println(lineNumber + ": " + lineData);
  //
  //      if (lineData.startsWith("/"))
  //      {
  //         // skip
  //      }         
  //      else if (lineData.equals(""))
  //      {
  //         //skip
  //      } 
  //       
  //      else {
  //         System.out.println(lineNumber + ": " + lineData);
  //         StringTokenizer st = new StringTokenizer(lineData);
  //   
  //         if (st.hasMoreTokens()) {
  //            keyword = st.nextToken();
  //            if (!keyword.equals("source"))
  //            {
  //               throw new IOException
  //               ("Error in line " + lineNumber + ": Initial keyword must be 'source'");
  //            }
  //         }
  //   
  //         if (st.hasMoreTokens()) {
  //            subjectSourceID = st.nextToken();
  //            if (!subjectSourceID.equals("person"))
  //            {
  //               throw new IOException
  //               ("Error in line " + lineNumber + ": Only source of type 'person' currently allowed");
  //            }
  //         }
  //   
  //         if (st.hasMoreTokens()) {
  //            value = st.nextToken();
  //            throw new IOException
  //            ("Error in line " + lineNumber + ": Extraneous data: " + value);
  //         }
  //         
  //         if (!subjectSourceID.equals("")) {
  //            break;
  //         }
  //      }
  //    }
  //    removeSubjects();
  //
  //    // Temporary -- add back the required SubjectType row...
  //    signet.beginTransaction();
  //    SubjectType subjectType = signet.newSubjectType("person", "Person");
  //    signet.commit();
  //
  //    Subject subject = null;
  //    String  currAttributeName = "";
  //    String  prevAttributeName = "";
  //    String  attributeName = "";
  //    int     attributeInstance = 0;
  //    
  //    while ((lineData = in.readLine()) != null) {
  //      lineNumber++;
  //      System.out.println(lineNumber + ": " + lineData);
  //
  //      if (lineData.startsWith("/"))
  //      {
  //         // skip
  //      }         
  //      else if (lineData.equals(""))
  //      {
  //         //skip
  //      } 
  //       
  //      else {
  //
  //         if (lineData.startsWith("+")) {
  //            
  //            // Get the subject header line
  //            lineData = lineData.substring(1);
  //            
  //            // Get the description (required, must be next)
  //            lineNumber++;
  //            lineData2 = in.readLine();
  //            if (lineData2 == "") {
  //               throw new IOException ("No Description row found");
  //            }
  //            System.out.println(lineNumber + ": " + lineData2);
  //            
  //            // Get the LoginID (required for now, must be next)
  //            lineNumber++;
  //            lineData3 = in.readLine();
  //            if (lineData3 == "") {
  //               throw new IOException ("No LoginID row found");
  //            }
  //            System.out.println(lineNumber + ": " + lineData3);
  //           
  //            subject = loader.processAddSubject(loader, subjectType, lineData, lineData2, lineData3);
  //   
  //            currAttributeName = "";
  //            prevAttributeName = "";
  //            attributeInstance = 1;
  //   
  //         } else {
  //   
  //            currAttributeName = loader.processSubjectAttribute(loader, subject, lineData, prevAttributeName, attributeInstance);
  //            if (currAttributeName.equals(prevAttributeName) ) {
  //               attributeInstance++;
  //            } else {
  //               prevAttributeName = currAttributeName;
  //               attributeInstance = 2;
  //            }
  //         }
  //       }
  //    }
  //    } catch (Exception e) {
  //        System.err.println("Exception caught: " + e.getMessage());
  //    }
  ////    } catch (ObjectNotFoundException e) {
  ////         throw new ObjectNotFoundException(e.getMessage());
  ////    }
  //  }

//  private static Subject processAddSubject(TreeXMLLoader loader, SubjectType subjectType, String lineData, String lineData2, String lineData3)
//    throws IOException, SQLException
//    {
//
//    String subjectID = "";
//    String subjectName = "";
//    String subjectNormalizedName = "";
//    String subjectDescription = "";
//    String subjectLoginID = "";
//    String attributeName = "";
//
//    StringTokenizer st = new StringTokenizer(lineData);
//
//     if (st.hasMoreTokens()) {
//       subjectID = st.nextToken();
//     } else {
//        throw new IOException ("No Subject ID found");
//     }
//
//     if (!st.hasMoreTokens()) {
//        throw new IOException ("No Subject Name found");
//     }
//     
//     subjectName = lineData.substring(subjectID.length());
//     subjectName = subjectName.trim();
//     subjectNormalizedName = loader.normalizeString(subjectName);
//
//     // System.out.println("--- SubjectID: " + subjectID + ", SubjectName: " + subjectName);
//
//     // --------------  Line 2 must be the description
//     StringTokenizer st2 = new StringTokenizer(lineData2);
//     
//     if (st2.hasMoreTokens()) {
//       attributeName = st2.nextToken();
//     } else {
//        throw new IOException ("No Description attribute found");
//     }
//
//     if (!attributeName.equals("description")) {
//        throw new IOException ("The second line of each subject entry must be 'description'");
//     }
//
//     if (!st2.hasMoreTokens()) {
//        throw new IOException ("No Description Value found");
//     }
//     
//     subjectDescription = lineData2.substring(attributeName.length());
//     subjectDescription = subjectDescription.trim();
//
//     // System.out.println("--- Description: " + subjectDescription);
// 
//     // --------------  Line 3 must be the LoginID
//     StringTokenizer st3 = new StringTokenizer(lineData3);
//     
//     if (st3.hasMoreTokens()) {
//       attributeName = st3.nextToken();
//     } else {
//        throw new IOException ("No loginid attribute found");
//     }
//
//     if (!attributeName.equals("loginid")) {
//        throw new IOException ("The second line of each subject entry must be 'LoginID'");
//     }
//
//     if (!st3.hasMoreTokens()) {
//        throw new IOException ("No loginid Value found");
//     }
//     
//     subjectLoginID = lineData3.substring(attributeName.length());
//     subjectLoginID = subjectLoginID.trim();
//
//     // System.out.println("--- Login id: " + subjectLoginID);
//    
//     Subject subject = loader.newSubject
//        (subjectType, subjectID, subjectName, subjectDescription, subjectLoginID);
//
//     loader.newAttribute
//       (subject, "name", 1, subjectName, subjectNormalizedName);
//
//     return subject;
//  }
//
//  private static String processSubjectAttribute(TreeXMLLoader loader, Subject subject, String lineData, String prevAttributeName, int attributeInstance)
//     throws IOException, SQLException {
//
//     String attributeName;
//     String attributeValue;
//     String attributeSearchValue;
//
//     StringTokenizer st = new StringTokenizer(lineData);
//
//     if (st.hasMoreTokens()) {
//       attributeName = st.nextToken();
//     } else {
//        throw new IOException ("No Attribute ID found");
//     }
//
//     if (!st.hasMoreTokens()) {
//        throw new IOException ("No Attribute Value found");
//     }
//     
//     attributeValue = lineData.substring(attributeName.length());
//     attributeValue = attributeValue.trim();
//     attributeSearchValue = attributeValue.toLowerCase();
//
//     if (!attributeName.equals(prevAttributeName)) {
//        attributeInstance = 1;
//     }
//
//     // System.out.println("--- Attribute: " + attributeName + ", instance: " + attributeInstance + ", Value: " + attributeValue);
//
//     loader.newAttribute
//       (subject, attributeName, attributeInstance, attributeValue, attributeSearchValue);
//       
//     return attributeName;
//  }
   
  private static boolean readYesOrNo(String prompt) {
      while (true) {
          String response = promptedReadLine(prompt);
          if (response.length() > 0) {
              switch (Character.toLowerCase(response.charAt(0))) {
              case 'y':
                  return true;
              case 'n':
                  return false;
              default:
                  System.out.println("Please enter Y or N. ");
              }
          }
      }
  }
  
  private static String promptedReadLine(String prompt) {
      try {
          System.out.print(prompt);
          return reader.readLine();
      } catch (java.io.IOException e) {
          return null;
      }
  }
    
  private static BufferedReader reader;
  
  static {
      reader = new BufferedReader(new InputStreamReader(System.in));
  }


  private void removeTrees() {
      if (! readYesOrNo(
          "\nYou are about to delete and replace all trees."
          + "\nDo you wish"
          + " to continue (Y/N)? ")) {
      System.exit(0);
      }

      try {
          deleteAll();
      }
      catch (SQLException sqle) {
         System.out.println("-Error: unable to delete trees");
         System.out.println(sqle.getMessage());
         System.exit(1);
      }
   }
}
