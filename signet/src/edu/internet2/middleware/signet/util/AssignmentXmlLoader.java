/*
TreeXmlLoader.java
Created on Feb 22, 2005

Copyright 2006 Internet2, Stanford University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package edu.internet2.middleware.signet.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import edu.internet2.middleware.signet.Assignment;
import edu.internet2.middleware.signet.Function;
import edu.internet2.middleware.signet.Limit;
import edu.internet2.middleware.signet.LimitValue;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SignetAuthorityException;
import edu.internet2.middleware.signet.Subsystem;
import edu.internet2.middleware.signet.tree.TreeNode;

public class AssignmentXmlLoader
{
  private final String ELEMENTNAME_ASSIGNMENTS  = "Assignments";
  private final String ELEMENTNAME_ASSIGNMENT   = "Assignment";
  private final String ELEMENTNAME_LIMIT        = "Limit";
  private final String ELEMENTNAME_LIMITVALUE   = "LimitValue";

  private final String ATTRIBUTENAME_SUBSYSTEM  = "Subsystem";
  private final String ATTRIBUTENAME_FUNCTION   = "Function";
  private final String ATTRIBUTENAME_GRANTOR    = "Grantor";
  private final String ATTRIBUTENAME_ACTINGAS   = "ActingAs";
  private final String ATTRIBUTENAME_GRANTEE    = "Grantee";
  private final String ATTRIBUTENAME_SCOPE      = "Scope";
  private final String ATTRIBUTENAME_ID         = "Id";

// not used
//  private Signet signet;
//  private int    assignmentsAdded = 0;
  
    
  /**
   * Opens a connection to the database for subsequent use in loading
   * and deleting Trees.
   *
   */
  public AssignmentXmlLoader()
  {
    super();
  }

  public static void main(String[] args)
  {
    AssignmentXmlLoader loader = new AssignmentXmlLoader();

    try
    {
      if (args.length < 1)
      {
        System.err.println("Usage: AssignmentXmlLoader <inputfile>");
        return;
      }
         
      String inputFileName = args[0];
      BufferedReader in = new BufferedReader(new FileReader(inputFileName));

      loader.processFile(loader, in);

      in.close();
    }
    catch (SignetAuthorityException auth)
    {
       System.out.println("Assignment authority error: " + auth.getDecision().getReason().toString());
    }
    catch (Exception e)
    {
       e.printStackTrace();
    }
  }
  
  private void processFile
    (AssignmentXmlLoader loader,
     BufferedReader      in)
  throws
    XMLStreamException,
    ObjectNotFoundException,
    SignetAuthorityException
  {
    int    newAssignmentCount = 0;
    Signet signet             = new Signet();
    
    System.setProperty
      ("javax.xml.stream.XMLInputFactory",
       "com.ctc.wstx.stax.WstxInputFactory");
    XMLInputFactory factory = XMLInputFactory.newInstance();
    ((com.ctc.wstx.stax.WstxInputFactory)factory).configureForMaxConvenience();
    XMLStreamReader parser = factory.createXMLStreamReader(in);
    
    //  Get current time
    long start = System.currentTimeMillis();
    
    signet.getPersistentDB().beginTransaction();
      
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
        if (parser.getLocalName().equals(ELEMENTNAME_ASSIGNMENTS))
        {
          newAssignmentCount = processAssignments(signet, parser, loader);
        }
        else
        {
          Set expectedElementSet = new HashSet();
          expectedElementSet.add(ELEMENTNAME_ASSIGNMENTS);
          reportUnexpectedElement(parser, expectedElementSet);
        }
      }
    }
    
    signet.getPersistentDB().commit();
    signet.getPersistentDB().close();
    
    //  Get elapsed time in milliseconds
    long elapsedTimeMillis = System.currentTimeMillis()-start;
    
    // Get elapsed time in seconds
    float elapsedTimeSec = elapsedTimeMillis/1000F;
    float assignmentsPerSecond = newAssignmentCount / elapsedTimeSec;
    
    System.out.println
      ("Loaded "
       + newAssignmentCount
       + " Assignments in "
       + elapsedTimeSec
       + " seconds ("
       + assignmentsPerSecond
       + " Assignments per second).");
  }
  
  private int processAssignments
    (Signet              signet,
     XMLStreamReader     parser,
     AssignmentXmlLoader loader)
  throws
    XMLStreamException,
    ObjectNotFoundException,
    SignetAuthorityException
  {
    Subsystem subsystem       = null;
    int       assignmentCount = 0;
    
    int attributeCount = parser.getAttributeCount();
    for (int i = 0; i < attributeCount; i++)
    {
      if (parser.getAttributeLocalName(i).equals(ATTRIBUTENAME_SUBSYSTEM))
      {
        String subsystemId = parser.getAttributeValue(i);
        subsystem = signet.getPersistentDB().getSubsystem(subsystemId);
      }
    }
    
    while (true)
    {
      int event = parser.next();
      
      switch (event)
      {
        case XMLStreamConstants.CHARACTERS:
          // We don't care about this.
          break;
        
        case XMLStreamConstants.END_ELEMENT:
          if (parser.getLocalName().equals(ELEMENTNAME_ASSIGNMENTS))
          {
            return assignmentCount;
          }
          else
          {
            reportUnexpectedEndElement
              (parser, parser.getLocalName(), ELEMENTNAME_ASSIGNMENTS);
          }
            
          break;
          
        case XMLStreamConstants.START_ELEMENT:
          String localName = parser.getLocalName();
          if (localName.equals(ELEMENTNAME_ASSIGNMENT))
          {
            processAssignment(signet, parser, subsystem);
            assignmentCount++;
          }
          
          break;
          
        default:
            System.out.println("FOUND NEW EVENT: " + event);
      }
    }
  }
  
  private Assignment processAssignment
    (Signet          signet,
     XMLStreamReader parser,
     Subsystem       subsystem)
  throws
    XMLStreamException,
    ObjectNotFoundException,
    SignetAuthorityException
  {
    Function          function    = null;
    PrivilegedSubject grantor     = null;
    PrivilegedSubject actingAs    = null;
    PrivilegedSubject grantee     = null;
    TreeNode          scope       = null;
    Set               limitValues = new HashSet();
    Assignment        assignment  = null;
    
    int attributeCount = parser.getAttributeCount();
    for (int i = 0; i < attributeCount; i++)
    {
      String localName = parser.getAttributeLocalName(i);
      if (localName.equals(ATTRIBUTENAME_FUNCTION))
      {
        String functionId = parser.getAttributeValue(i);
        function = getFunction(subsystem, functionId);
      }
      else if (localName.equals(ATTRIBUTENAME_GRANTOR))
      {
        String grantorId = parser.getAttributeValue(i);
        grantor = getSubject(signet, grantorId);
      }
      else if (localName.equals(ATTRIBUTENAME_ACTINGAS))
      {
        String actingAsId = parser.getAttributeValue(i);
        actingAs = getSubject(signet, actingAsId);
      }
      else if (localName.equals(ATTRIBUTENAME_GRANTEE))
      {
        String granteeId = parser.getAttributeValue(i);
        grantee = getSubject(signet, granteeId);
      }
      else if (localName.equals(ATTRIBUTENAME_SCOPE))
      {
        String scopeId = parser.getAttributeValue(i);
        scope = signet.getTreeNode(subsystem.getTree().getId(), scopeId);
        if (scope == null)
        {
          throw new ObjectNotFoundException
            ("Unable to find the node '"
             + scopeId
             + "' in the tree '"
             + subsystem.getTree().getId()
             + "'.");
        }
      }
    }
    
    while (true)
    {
      int event = parser.next();
      
      switch (event)
      {
        case XMLStreamConstants.CHARACTERS:
          // We don't care about this.
          break;
          
        case XMLStreamConstants.END_ELEMENT:
          if (parser.getLocalName().equals(ELEMENTNAME_ASSIGNMENT))
          {
              System.out.println("Assignment: " + grantor.getName() + " granting " + function.getName() + " to " + grantee.getName());
              assignment
                = buildAssignmentIfComplete
                    (function, grantor, actingAs, grantee, scope, limitValues);
              
              return assignment;
          }
              
          break;
            
        case XMLStreamConstants.START_ELEMENT:
          String localName = parser.getLocalName();
          if (localName.equals(ELEMENTNAME_LIMIT))
          {
            limitValues.addAll(processLimit(parser, subsystem));
          }
          
          break;
            
        default:
          System.out.println("FOUND NEW EVENT: " + event);
        }
      }
  }
  
  private PrivilegedSubject getSubject(Signet signet, String subjectId)
  throws ObjectNotFoundException
  {
    PrivilegedSubject pSubject;
    
    if ("signet".equals(subjectId))
    {
      return signet.getSignetSubject();
    }
    
    try
    {
      pSubject = signet.getSubjectSources().getPrivilegedSubjectByDisplayId(
    		  Signet.DEFAULT_SUBJECT_TYPE_ID, subjectId);
    }
    catch (ObjectNotFoundException onfe)
    {
      // Let's give this exception a little more detail.
      throw new ObjectNotFoundException
        ("Unable to find Subject '"
         + subjectId
         + "' of type '"
         + Signet.DEFAULT_SUBJECT_TYPE_ID + "'.",
         onfe);
    }
    
    return pSubject;
  }

  
  // Returns a Set of LimitValue objects.
  private Set processLimit
    (XMLStreamReader parser,
     Subsystem       subsystem)
  throws
    XMLStreamException,
    ObjectNotFoundException
  {
    Limit limit = null;
    Set   limitValues = new HashSet();
    
    int attributeCount = parser.getAttributeCount();
    for (int i = 0; i < attributeCount; i++)
    {
      String localName = parser.getAttributeLocalName(i);
      if (localName.equals(ATTRIBUTENAME_ID))
      {
        String limitId = parser.getAttributeValue(i);
        limit = subsystem.getLimit(limitId);
      }
    }
    
    while (true)
    {
      int event = parser.next();
      
      switch (event)
      {
        case XMLStreamConstants.CHARACTERS:
          // We don't care about this.
          break;
          
        case XMLStreamConstants.END_ELEMENT:
          if (parser.getLocalName().equals(ELEMENTNAME_LIMIT))
            {
              // We've finished processing the "Limit" element.
              // If we failed to create at least one LimitValue, then that's
              // an error.
              if (limitValues.size() == 0)
              {
                reportIncompleteLimit(subsystem, limit);
              }
              else
              {
                return limitValues;
              }
            }
            else
            {
              reportUnexpectedEndElement
                (parser, parser.getLocalName(), ELEMENTNAME_LIMIT);
            }
              
            break;
            
          case XMLStreamConstants.START_ELEMENT:
            String localName = parser.getLocalName();
            if (localName.equals(ELEMENTNAME_LIMITVALUE))
            {
              limitValues.add(processLimitValue(parser, limit));
            }
            
            break;
            
          default:
              System.out.println("FOUND NEW EVENT: " + event);
        }
      }
  }
  

  private Function getFunction(Subsystem subsystem, String functionId)
  throws ObjectNotFoundException
  {
    Iterator functionsIterator = subsystem.getFunctions().iterator();
    while (functionsIterator.hasNext())
    {
      Function candidate = (Function)(functionsIterator.next());
      if (candidate.getId().equals(functionId))
      {
        return candidate;
      }
    }
    
    // If we've gotten this far, we didn't find the function.
    throw new ObjectNotFoundException
      ("Unable to find the function '"
       + functionId
       + "' in subsystem '"
       + subsystem.getId()
       + "'");
  }


  private LimitValue processLimitValue
    (XMLStreamReader parser,
     Limit           limit)
  throws XMLStreamException
  {
    String value = parser.getElementText();
    return new LimitValue(limit, value);
  }
  

  private Assignment buildAssignmentIfComplete
    (Function          function,
     PrivilegedSubject grantor,
     PrivilegedSubject actingAs,
     PrivilegedSubject grantee,
     TreeNode          scope,
     Set               limitValues)
  throws SignetAuthorityException
  {
    Assignment assignment = null;
    
    if ((function != null)
        && (grantor != null)
        && (grantee != null)
        && (scope != null)
        && (limitValues != null))
    {
      grantor.setActingAs(actingAs);
      
      assignment
        = grantor.grant
            (grantee,
             scope,
             function,
             limitValues,
             true,
             true,
             new java.util.Date(),
             null);
      
      assignment.save();
    }
    
    return assignment;
  }
  

// not used
// private void reportIncompleteAssignment
//    (Function          function,
//     PrivilegedSubject grantor,
//     PrivilegedSubject grantee,
//     TreeNode          scope)
//  {
//    if (function == null)
//    {
//      System.out.println
//        ("The XML input file contained an incomplete '"
//         + ELEMENTNAME_ASSIGNMENT
//         + "' definition. The required attribute '"
//         + ATTRIBUTENAME_FUNCTION
//         + "' was missing. This is an error.");
//    }
//    
//    if (grantor == null)
//    {
//      System.out.println
//        ("The XML input file contained an incomplete '"
//         + ELEMENTNAME_ASSIGNMENT
//         + "' definition. The required attributet '"
//         + ATTRIBUTENAME_GRANTOR
//         + "' was missing. This is an error.");
//    }
//    
//    if (grantee == null)
//    {
//      System.out.println
//        ("The XML input file contained an incomplete '"
//         + ELEMENTNAME_ASSIGNMENT
//         + "' definition. The required attributet '"
//         + ATTRIBUTENAME_GRANTEE
//         + "' was missing. This is an error.");
//    }
//    
//    if (scope == null)
//    {
//      System.out.println
//        ("The XML input file contained an incomplete '"
//         + ELEMENTNAME_ASSIGNMENT
//         + "' definition. The required attributet '"
//         + ATTRIBUTENAME_SCOPE
//         + "' was missing. This is an error.");
//    }
//  }
  

  private void reportIncompleteLimit
    (Subsystem subsystem,
     Limit     limit)
  {
    System.out.println
        ("The XML input file contained an incomplete '"
         + ELEMENTNAME_LIMIT
         + "' definition. The required element '"
         + ELEMENTNAME_LIMITVALUE
         + "' was missing for the limit '"
         + limit.getId()
         + "' in the subsystem '"
         + subsystem.getId()
         + "'. This is an error.");
  }
  

// not used
//  private void reportRepeatedElement
//    (XMLStreamReader parser)
//  {
//    System.out.println
//      ("XML parser encountered unexpected element '"
//       + parser.getLocalName()
//       + "' at line "
//       + parser.getLocation().getLineNumber()
//       + ", column "
//       + parser.getLocation().getColumnNumber()
//       + ". This element is illegally repeated: It is allowed to appear only "
//       + "once within its enclosing element, and it has already appeared "
//       + "within the current enclosing element.");
//  }
  
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
}
