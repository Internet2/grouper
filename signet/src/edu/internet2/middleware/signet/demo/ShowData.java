/*--
$Id: ShowData.java,v 1.3 2005-01-04 19:06:43 acohen Exp $
$Date: 2005-01-04 19:06:43 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.demo;

import java.text.DateFormat;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.cfg.Configuration;
import net.sf.hibernate.tool.hbm2java.ClassMapping;
import edu.internet2.middleware.signet.Assignment;
import edu.internet2.middleware.signet.Category;
import edu.internet2.middleware.signet.Function;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Permission;
import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.Subsystem;
import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeNode;
import edu.internet2.middleware.signet.tree.TreeNotFoundException;
import edu.internet2.middleware.signet.tree.TreeTypeAdapter;
import edu.internet2.middleware.subject.SubjectNotFoundException;

public class ShowData
{
private static String SUPERSUBJECT_DISPLAY_ID = "SignetSuperSubject";

  public static void main(String[] args)
  throws
    ObjectNotFoundException, Exception
  {
    Signet signet = new Signet();
    showGrantedAssignments
    	("----", signet.getPrivilegedSubject("SignetSuperSubject"));
    Set pSubjects
    	= getPrivilegedSubjectsByDisplayId
    			(signet, SUPERSUBJECT_DISPLAY_ID);
//    getAssignmentsGrantedMap(signet, getSinglePSubject(pSubjects));
    showSubsystems(signet);
    showPrivilegedSubjects(signet);
    showAssignments(signet);
    showSubjectByDisplayId(signet);
    showTreeNodeAncestry(signet);
    showAllTrees(signet);
    showITnodeInContext(signet);
  }
  
  private static PrivilegedSubject getSinglePSubject(Set set)
  throws Exception
  {
    if (set.size() != 1)
    {
      throw new Exception
      	("Received " + set.size() + " PrivilegedSubjects, expected exactly one.");
    }
    
    PrivilegedSubject pSubject = null;
    
    Iterator iterator = set.iterator();
    while (iterator.hasNext())
    {
      pSubject = (PrivilegedSubject)(iterator.next());
    }
    
    return pSubject;
  }
  
//  private static void getAssignmentsGrantedMap
//  	(Signet signet, PrivilegedSubject pSubject)
//  throws ObjectNotFoundException
//  {
//    Map level1Map = pSubject.getAssignmentsGrantedMap();
//    Set level1Keys = level1Map.keySet();
//    System.out.println("level1Keys.size()=" + level1Keys.size());
//    Iterator level1KeysIterator = level1Keys.iterator();
//    int level1KeysCounter = 1;
//    while (level1KeysIterator.hasNext())
//    {
//      Object level1Key = level1KeysIterator.next();
//      Object level1Value = level1Map.get(level1Key);
//      System.out.println
//      	("level1 key #"
//      	 + level1KeysCounter++ 
//      	 + "=" 
//      	 + level1Key
//      	 + ", valueClass="
//      	 + level1Value.getClass().getName());
//      
//      Map level2Map = (Map)level1Value;
//      Set level2Keys = level2Map.keySet();
//      System.out.println("  level2Keys.size()=" + level2Keys.size());
//      Iterator level2KeysIterator = level2Keys.iterator();
//      int level2KeysCounter = 1;
//      while (level2KeysIterator.hasNext())
//      {
//        Object level2Key = level2KeysIterator.next();
//        Object level2Value = level2Map.get(level2Key);
//        System.out.println
//      	("  level2 key #"
//      	 + level2KeysCounter++ 
//      	 + " id=" 
//      	 + ((Subsystem)level2Key).getId()
//      	 + " class=" 
//      	 + level2Key.getClass().getName()
//      	 + ", valueClass="
//      	 + level2Value.getClass().getName());
//        
//        Map level3Map = (Map)level2Value;
//        Set level3Keys = level3Map.keySet();
//        System.out.println("    level3Keys.size()=" + level3Keys.size());
//        Iterator level3KeysIterator = level3Keys.iterator();
//        int level3KeysCounter = 1;
//        while (level3KeysIterator.hasNext())
//        {
//          Object level3Key = level3KeysIterator.next();
//          Object level3Value = level3Map.get(level3Key);
//          System.out.println
//        	("    level3 key #"
//        	 + level3KeysCounter++ 
//        	 + " id=" 
//        	 + ((Category)level3Key).getId()
//        	 + " class=" 
//        	 + level3Key.getClass().getName()
//        	 + ", valueClass="
//        	 + level3Value.getClass().getName());
//          
//          Set level4Set = (Set)level3Value;
//          System.out.println("    level4Set.size()=" + level4Set.size());
//          Iterator level4SetIterator = level4Set.iterator();
//          int level4SetCounter = 1;
//          while (level4SetIterator.hasNext())
//          {
//            Object level4Value = level4SetIterator.next();
//            System.out.println
//          	("    level4 value #"
//          	 + level4SetCounter++ 
////          	 + " id=" 
////          	 + ((Category)level3Key).getId()
//          	 + " class=" 
//          	 + level4Value.getClass().getName()
//          	 + ", valueClass="
//          	 + level4Value.getClass().getName());
//          }
//        }
//      }
//    }
//  }
  
  private static Set getPrivilegedSubjectsByDisplayId
  	(Signet signet,
  	 String displayId)
  {
    return
    	signet.getPrivilegedSubjectsByDisplayId(SUPERSUBJECT_DISPLAY_ID);
  }
  
  private static void showAllTrees(Signet signet)
  throws TreeNotFoundException, ObjectNotFoundException
  {
    Set allSubsystems = signet.getSubsystems();
    Iterator allSubsystemsIterator = allSubsystems.iterator();
    while (allSubsystemsIterator.hasNext())
    {
      Subsystem subsystem = (Subsystem)(allSubsystemsIterator.next());
      Set functions = subsystem.getFunctions();
      Iterator functionsIterator = functions.iterator();
      if (functionsIterator.hasNext())
      {
        Function anyFunction
        	= (Function)(subsystem.getFunctions().iterator().next());
      
        if (anyFunction != null)
        {
          System.out.println
    	  		(signet.printTreeNodesInContext
              ("<option disabled value=\"",  // ancestorPrefix
               "<option value=\"",           // selfPrefix
               "<option value=\"",           // descendantPrefix
               "",                           // prefixIncrement
               "\">\n",                      // infix
               ". ",                         // infixIncrement
               "</option>\n",                // suffix
               signet
               	.getSuperPrivilegedSubject()
               		.getGrantableScopes(anyFunction)));
        }
      }
    }
  }
  
  private static void showTreeNodeAncestry(Signet signet)
  throws
    ObjectNotFoundException,
    TreeNotFoundException
  { 
    TreeTypeAdapter treeTypeAdapter
    	= signet.getTreeTypeAdapter
    			(Signet.DEFAULT_TREE_TYPE_ADAPTER_NAME);
    
    TreeNode descendant
    	= signet.getTreeNode
    			(treeTypeAdapter,
    			 "my_id00", // treeId
    			 "my_id0002"); // treeNodeId
    
    Set descendants = new HashSet();
    descendants.add(descendant);
   
    String ancestryStr
      = signet.displayAncestry
          (descendant,
           "CHILDSEPARATORPREFIX\n",  // childSeparatorPrefix
           "\nLEVELPREFIX\n>",        // levelPrefix
           "\nLEVELPREFIX\n",         // levelSuffix
           "\nCHILDSEPARATORSUFFIX"); // childSeparatorSuffix
    
    System.out.println("<<TREENODE ANCESTRY>>");
    System.out.println(ancestryStr);
    System.out.println("<<END OF TREENODE ANCESTRY");
    
    
    System.out.println
      (signet.printTreeNodesInContext
        ("<option disabled value=\"",  // ancestorPrefix
         "<option value=\"",           // selfPrefix
         "<option value=\"",           // descendantPrefix
         "",                           // prefixIncrement
         "\">",                        // infix
         ". ",                         // infixIncrement
         "</option>\n",                // suffix
         descendants));
  }

  /**
   * @throws SubjectNotFoundException
   * 
   */
  private static void showSubjectByDisplayId(Signet signet)
  throws ObjectNotFoundException
  {
    PrivilegedSubject subject0
  		= signet.getPrivilegedSubjectByDisplayId
  				(Signet.DEFAULT_SUBJECT_TYPE_ID, "subject0");
    System.out.println("<<SUBJECTBYDISPLAYID>>");
    System.out.println(subject0);
    System.out.println("<<END OF SUBJECTBYDISPLAYID>>");
  }

  /**
   * @param signet
   * @throws TreeNotFoundException
   * 
   */
  private static void showSubsystems(Signet signet) throws TreeNotFoundException
  {
    Set subsystems = signet.getSubsystems();
    
    System.out.println("SUBSYSTEMS:");
    Iterator subsystemsIterator = subsystems.iterator();
    while (subsystemsIterator.hasNext())
    {
      Subsystem subsystem = (Subsystem)(subsystemsIterator.next());
      System.out.println(subsystem);
      
      System.out.println("--<<SCOPE:>>");
      System.out.println
        (signet.printTree
          ("----scope=",
           "----scope=",
           "----scope=",
           "--",
           ",name=",
           "",
           "\n",
           subsystem.getTree()));
      System.out.println("--<<END OF SCOPE>>");
      
      
      Set categories = subsystem.getCategories();
      System.out.println("--<<CATEGORIES:>>");
      Iterator categoriesIterator = categories.iterator();
      while (categoriesIterator.hasNext())
      {
        Category category = (Category)(categoriesIterator.next());
        System.out.println("--" + category);

        Set functions = category.getFunctions();
        Iterator functionsIterator = functions.iterator();
        while (functionsIterator.hasNext())
        {
          Function function = (Function)(functionsIterator.next());
          System.out.println("----" + function);

          Permission[] permissions = function.getPermissionsArray();
          for (int permissionIndex = 0;
          		 permissionIndex < permissions.length; 
          		 permissionIndex++)
          {
            System.out.println("------" + permissions[permissionIndex]);
          }
        }
      }
      System.out.println("--<<END OF CATEGORIES>>");
    }
    System.out.println("<<END OF SUBSYSTEMS>>");
  }
  
  private static void showPrivilegedSubjects(Signet signet)
  throws SubjectNotFoundException, ObjectNotFoundException
  {
    Set privilegedSubjects 
    	= signet.getPrivilegedSubjects();
    
    SortedSet sortSet = new TreeSet(privilegedSubjects);
    Iterator sortSetIterator = sortSet.iterator();
    
    System.out.println("<<PRIVILEGEDSUBJECTS:>>");
    while (sortSetIterator.hasNext())
    {
      PrivilegedSubject pSubject
      	= (PrivilegedSubject)(sortSetIterator.next());
      System.out.println(pSubject);
      System.out.println("--<<GRANTED ASSIGNMENTS:>>");
      showGrantedAssignments("----", pSubject);
      System.out.println("--<<END OF GRANTED ASSIGNMENTS:>>");
      
//      System.out.println("--<<GRANTABLE SUBSYSTEMS>>");
//      showGrantableSubsystems
//      	("----", privilegedSubjects[privilegedSubjectsIndex]);
//      System.out.println("--<<END OF GRANTABLE SUBSYSTEMS>>");
    }
    System.out.println("<<END OF PRIVILEGEDSUBJECTS>>");
  }
  
//  /**
//   * @param string
//   * @param subject
//   */
//  private static void showGrantableSubsystems
//    (String 						prefix,
//     PrivilegedSubject 	privilegedSubject)
//  {
//    Map grantableFunctions = privilegedSubject.getGrantableFunctions();
//    Set grantableSubsystems = grantableFunctions.keySet();
//    Iterator grantableSubsystemsIterator = grantableSubsystems.iterator();
//    while (grantableSubsystemsIterator.hasNext())
//    {
//      Subsystem subsystem = (Subsystem)(grantableSubsystemsIterator.next());
//      System.out.println(prefix + subsystem.getId());
//    }
//  }

  private static void showGrantedAssignments
  	(String 						prefix,
  	 PrivilegedSubject 	grantor)
  throws SubjectNotFoundException, ObjectNotFoundException
  {
    Set assignments = new TreeSet(grantor.getAssignmentsGranted(null, null));
    Iterator assignmentsIterator = assignments.iterator();
    while (assignmentsIterator.hasNext())
    {
      Assignment assignment = (Assignment)(assignmentsIterator.next());
      System.out.println
        (prefix
         + "grantee=" + assignment.getGrantee().getDisplayId() + ","
         + "subsystem=" + assignment.getFunction().getSubsystem().getId() + ","
         + "category=" + assignment.getFunction().getCategory().getId() + ","
         + "function=" + assignment.getFunction().getId());
    }
  }
  
  private static void showAssignments(Signet signet)
  {
    Assignment[] assignments 
    	= signet.getAssignments();
    DateFormat dateFormat = DateFormat.getDateInstance();
    
    System.out.println("<<ASSIGNMENTS:>>");
    for (int assignmentsIndex = 0;
    assignmentsIndex < assignments.length; 
    assignmentsIndex++)
    {
      Assignment assignment = assignments[assignmentsIndex];
      System.out.println(assignment);
      System.out.println
      	("--GRANTOR: " + assignment.getGrantor());
      System.out.println
      	("--GRANTEE: " + assignment.getGrantee());
      System.out.println
      	("--FUNCTION: " + assignment.getFunction());
      System.out.println
      	("--SCOPE: " + assignment.getScope());
      System.out.println
      	("--CREATEDATE: "
      	 + dateFormat.format(assignment.getCreateDatetime()));
    }
    System.out.println("<<END OF ASSIGNMENTS>>");
  }

  /**
   * @param config
   */
  private static void showConfig(Configuration config)
  {
    Properties properties = config.getProperties();
    Enumeration propertyNames = properties.propertyNames();
    
    System.out.println("CONFIG VALUES");
    
    while (propertyNames.hasMoreElements())
    {
      String propertyName = (String)propertyNames.nextElement();
      System.out.println
      	("property name: " + propertyName);
      System.out.println
      	("property value: " + properties.getProperty(propertyName));
    }
    
    System.out.println("END OF CONFIG VALUES");
    
    System.out.println("CLASS MAPPINGS");
    Iterator classMappings = config.getClassMappings();
    while (classMappings.hasNext())
    {
      ClassMapping classMapping = (ClassMapping)classMappings.next();
      System.out.println(classMapping.getName());
    }
    System.out.println("END OF CLASS MAPPINGS");
  }
  
  private static void showITnodeInContext(Signet signet)
  throws ObjectNotFoundException, TreeNotFoundException
  {
    Tree tree = signet.getTree("adminorgs");
    TreeNode node = tree.getNode("IT");
    Set nodeSet = new HashSet();
    nodeSet.add(node);
     
    String nodeInContext
    	= signet.printTreeNodesInContext
     ("<option disabled value=\"",  // ancestorPrefix
      "<option value=\"",           // selfPrefix
      "<option value=\"",           // descendantPrefix
      "",                           // prefixIncrement
      "\">\n",                      // infix
      ". ",                         // infixIncrement
      "</option>\n",                // suffix
      nodeSet);
    
    System.out.println("IT NODE IN CONTEXT:");
    System.out.println(nodeInContext);
    System.out.println("END OF IT NODE IN CONTEXT");
  }
}
