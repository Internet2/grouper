/*
SubsystemXmlLoader.java
Created on Jan 15, 2005

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

/**
 * @author lmcrae@stanford.edu
 *
 */
import edu.internet2.middleware.signet.*;
import edu.internet2.middleware.signet.choice.*;
import edu.internet2.middleware.signet.tree.*;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;

import org.jdom.*;
import org.jdom.input.SAXBuilder;

import java.io.*;

// import java.sql.*;
import java.lang.Integer.*;
import java.lang.Exception.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
// import java.util.logging.Logger;

public class SubsystemXmlLoader {
    private Map choiceSetMap = new HashMap();
    private Map limitMap = new HashMap();
    private Map categoryMap = new HashMap();
    private Map permissionMap = new HashMap();
//  private Logger logger;

    /**
     * Default constructor.
     *
     */
    public SubsystemXmlLoader() throws Exception {
        // this.logger = Logger.getLogger(this.toString());
    }

    private void removeSubsystem(String subsystemId) {
        if (! readYesOrNo(
        		"\nYou are about to delete data for subsystem "
        		+ subsystemId
        		+ " and the associated assignments."
				+ "\nDo you wish"
				+ " to continue (Y/N)? ")) {
        	System.exit(0);
        }

    	SubsystemDestroyer destroyer = new SubsystemDestroyer(subsystemId);
    	try {
    		destroyer.execute();
    	}
    	catch (HibernateException he) {
    		System.out.println("-Error: unable to remove subsystem " +
    				subsystemId);
			System.out.println(he.getMessage());
			System.exit(1);
    	}
    	catch (SQLException sqle) {
    		System.out.println("-Error: unable to remove subsystem " +
    				subsystemId);
			System.out.println(sqle.getMessage());
			System.exit(1);
    	}
    }
    
    private void processXML(String fName, boolean validate)
        throws IOException, JDOMException, ObjectNotFoundException,
            javax.naming.OperationNotSupportedException {
        File file = new File(fName);
        Document doc = new SAXBuilder(validate).build(file);

        Element rootElem = doc.getRootElement();

        Element subsystemIdElem = rootElem.getChild("Id");
        String subsystemId = subsystemIdElem.getTextTrim();

        Element subsystemNameElem = rootElem.getChild("Name");
        String subsystemName = subsystemNameElem.getTextTrim();

        Element subsystemHelpTextElem = rootElem.getChild("HelpText");
        String subsystemHelpText = subsystemHelpTextElem.getTextTrim();

        // -- Check for existence of subsystem
        if (subsystemExists(subsystemId))
        {
          removeSubsystem(subsystemId);
        }

        Signet signet = new Signet();

        System.out.println("+ " + rootElem.getName());
        System.out.println("- " + subsystemIdElem.getName() + " = " + subsystemId);
        System.out.println("- " + subsystemNameElem.getName() + " = " + subsystemName);

        Element scopeElem = rootElem.getChild("Scope");
        String scopeId = scopeElem.getTextTrim();

        System.out.println("- - Scope = " + scopeId);

        // Check for existence of named scope tree
        Tree tempTree = null;

        try {
            tempTree = signet.getTree(scopeId);
        } catch (ObjectNotFoundException e) {
            throw new ObjectNotFoundException("Subsystem " + subsystemId + " -- Scope \"" + scopeId
                + "\" is not a defined Tree");
        }

        // Start transactiopn and process remainder of document
        signet.beginTransaction();

        Subsystem subsystem = signet.newSubsystem(subsystemId, subsystemName, subsystemHelpText,
                Status.ACTIVE);
        subsystem.setTree(tempTree);
        subsystem.save();

        try {
            processChoiceSet(signet, subsystem, rootElem);
            processLimit(signet, subsystem, rootElem);
            processPermission(signet, subsystem, rootElem);
            processCategory(signet, subsystem, rootElem);
            processFunction(signet, subsystem, rootElem);

            signet.commit();
        } catch (ObjectNotFoundException e) {
            throw new ObjectNotFoundException(e.getMessage());
        } catch (NumberFormatException e) {
            throw new NumberFormatException(e.getMessage());
        }
    }

    private boolean subsystemExists(String subsystemId)
    {
      Configuration  cfg = new Configuration();
      SessionFactory sessionFactory;
      Session        session;
      Connection     conn;
      int            subsystemCount = 0;

      try
      {
        // Read the "hibernate.cfg.xml" file.
        cfg.configure();
        sessionFactory = cfg.buildSessionFactory();
        session = sessionFactory.openSession();
        conn = session.connection();
        
        Statement stmt = conn.createStatement();
        ResultSet results
          = stmt.executeQuery
              ("select count(*) from signet_subsystem where subsystemID='"
               + subsystemId
               + "'");
        while (results.next())
        {
          subsystemCount = results.getInt(1);
        }
      }
      catch (Exception e)
      {
        throw new RuntimeException(e);
      }
      
      return (subsystemCount > 0);
    }

    private void processChoiceSet(Signet signet, Subsystem subsystem, Element rootElem)
        throws javax.naming.OperationNotSupportedException, NumberFormatException {
        List choicesets = rootElem.getChildren("ChoiceSet");
        Iterator choicesetIter = choicesets.iterator();
        while (choicesetIter.hasNext()) {
            System.out.println("- - ChoiceSet");

            Element choicesetElem = (Element) choicesetIter.next();

            Element choicesetIdElem = choicesetElem.getChild("Id");
            String choicesetId = choicesetIdElem.getTextTrim();
            System.out.println("- - - Id = " + choicesetId);

            ChoiceSet choiceset = signet.newChoiceSet(subsystem, choicesetId);
            choiceset.save();
            this.choiceSetMap.put(choicesetId, choiceset);

            Element choicesetChoice = choicesetElem.getChild("Choice");

            List choices = choicesetElem.getChildren("Choice");
            Iterator choicesIter = choices.iterator();
            while (choicesIter.hasNext()) {
                System.out.println("- - - " + choicesetChoice.getName());

                Element choiceElem = (Element) choicesIter.next();

                Element choiceValueElem = choiceElem.getChild("Value");
                String choiceValue = choiceValueElem.getTextTrim();
                System.out.println("- - - - Value = " + choiceValue);

                Element choiceLabelElem = choiceElem.getChild("Label");
                String choiceLabel = choiceLabelElem.getTextTrim();
                System.out.println("- - - - Label = " + choiceLabel);

                Element choiceOrderElem = choiceElem.getChild("Order");
                String choiceOrder = choiceOrderElem.getTextTrim();
                System.out.println("- - - - Order = " + choiceOrder);

                int choiceOrderInt = 0;
                int choiceRankInt = 0;

                try {
                    choiceOrderInt = Integer.parseInt(choiceOrder);
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("ChoiceSet " + choicesetId + ", Choice "
                        + choiceValue + " -- Order \"" + choiceOrder
                        + "\" invalid, must be an integer");
                }

                Element choiceRankElem = choiceElem.getChild("Rank");
                String choiceRank = choiceRankElem.getTextTrim();
                System.out.println("- - - - Rank = " + choiceRank);

                try {
                    choiceRankInt = Integer.parseInt(choiceRank);
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("ChoiceSet " + choicesetId + ", Choice "
                        + choiceValue + " -- Rank \"" + choiceRank
                        + "\" invalid, must be an integer");
                }

                Choice choice = choiceset.addChoice(choiceValue, choiceLabel, choiceOrderInt,
                        choiceRankInt);
            }
        }
    }

    private void processLimit
      (Signet signet, Subsystem subsystem, Element rootElem)
    throws ObjectNotFoundException
    {
      List limits = rootElem.getChildren("Limit");
      Iterator limitIter = limits.iterator();
      int limitNumber = 0;
      
      while (limitIter.hasNext())
      {
        System.out.println("- - Limit");

        Element limitElem = (Element) limitIter.next();

        Element limitIdElem = limitElem.getChild("Id");
        String limitId = limitIdElem.getTextTrim();
        System.out.println("- - - Id = " + limitId);

        Element limitNameElem = limitElem.getChild("Name");
        String limitName = limitNameElem.getTextTrim();
        System.out.println("- - - Name = " + limitName);

        Element limitHelpTextElem = limitElem.getChild("HelpText");
        String limitHelpText = limitHelpTextElem.getTextTrim();
        System.out.println("- - - HelpText = " + limitHelpText);

        Element limitChoiceSetElem = limitElem.getChild("LimitChoiceSet");
        String limitChoiceSetId = limitChoiceSetElem.getTextTrim();
        System.out.println("- - - LimitChoiceSet = " + limitChoiceSetId);

        Element rendererElem = limitElem.getChild("Renderer");
        String rendererId = rendererElem.getTextTrim();
        System.out.println("- - - Renderer = " + rendererId);

        ChoiceSet limitChoiceSet
        	= (ChoiceSet) this.choiceSetMap.get(limitChoiceSetId);
        if (limitChoiceSet == null)
        {
          throw new ObjectNotFoundException
          	("Limit "
          	 + limitId
          	 + " -- ChoiceSet \""
             + limitChoiceSetId + "\" is not defined");
        }

        Limit limit
        	= signet.newLimit
        			(subsystem,
        			 limitId,
        			 DataType.TEXT,
        			 limitChoiceSet,
               limitName,
               limitNumber,
               limitHelpText,
               Status.ACTIVE,
               rendererId);
        limit.save();
        this.limitMap.put(limitId, limit);
        
        limitNumber++;
      }
    }

    private void processPermission(Signet signet, Subsystem subsystem, Element rootElem)
        throws ObjectNotFoundException {
        List permissions = rootElem.getChildren("Permission");
        Iterator permissionIter = permissions.iterator();
        while (permissionIter.hasNext()) {
            Element permissionElem = (Element) permissionIter.next();

            System.out.println("- - Permission");

            Element permissionIdElem = permissionElem.getChild("Id");
            String permissionId = permissionIdElem.getTextTrim();
            System.out.println("- - - Id = " + permissionId);

            Permission permission = signet.newPermission(subsystem, permissionId, Status.ACTIVE);

            List permissionLimits = permissionElem.getChildren("PermissionLimit");
            Iterator permissionLimitsIter = permissionLimits.iterator();
            while (permissionLimitsIter.hasNext()) {
                Element permissionLimitElem = (Element) permissionLimitsIter.next();
                String permissionLimitId = permissionLimitElem.getTextTrim();
                System.out.println("- - - PermissionLimit = "
                    + permissionLimitId);

                Limit permissionLimit = (Limit) this.limitMap.get(permissionLimitId);
                if (permissionLimit == null) {
                    throw new ObjectNotFoundException("Permission " + permissionId + " -- Limit \""
                        + permissionLimitId + "\" is not defined");
                }

                permission.addLimit(permissionLimit);
            }

            List permissionPrereqs = permissionElem.getChildren("PermissionPrerequisite");
            Iterator permissionPrereqsIter = permissionPrereqs.iterator();
            while (permissionPrereqsIter.hasNext()) {
                Element permissionPrereqElem = (Element) permissionPrereqsIter.next();
                String permissionPrereqId = permissionPrereqElem.getTextTrim();
                System.out.println("- - - PermissionPrerequisite = " + permissionPrereqId);
            }
            permission.save();
            this.permissionMap.put(permissionId, permission);
        }
    }

    private void processCategory(Signet signet, Subsystem subsystem, Element rootElem) {
        List categories = rootElem.getChildren("Category");
        Iterator categoryIter = categories.iterator();
        while (categoryIter.hasNext()) {
            System.out.println("- - Category");

            Element categoryElem = (Element) categoryIter.next();

            Element categoryIdElem = categoryElem.getChild("Id");
            String categoryId = categoryIdElem.getTextTrim();
            System.out.println("- - - Id = " + categoryId);

            Element categoryNameElem = categoryElem.getChild("Name");
            String categoryName = categoryNameElem.getTextTrim();
            System.out.println("- - - Name = " + categoryName);

            Category category = signet.newCategory(subsystem, categoryId, categoryName,
                    Status.ACTIVE);
            category.save();
            this.categoryMap.put(categoryId, category);
        }
    }

    private void processFunction(Signet signet, Subsystem subsystem, Element rootElem)
        throws ObjectNotFoundException {
        List functions = rootElem.getChildren("Function");
        Iterator functionIter = functions.iterator();
        while (functionIter.hasNext()) {
            System.out.println("- - Function");

            Element functionElem = (Element) functionIter.next();

            Element functionIdElem = functionElem.getChild("Id");
            String functionId = functionIdElem.getTextTrim();
            System.out.println("- - - Id = " + functionId);

            Element functionNameElem = functionElem.getChild("Name");
            String functionName = functionNameElem.getTextTrim();
            System.out.println("- - - Name = " + functionName);

            Element functionHelpTextElem = functionElem.getChild("HelpText");
            String functionHelpText = functionHelpTextElem.getTextTrim();
            System.out.println("- - - HelpText = " + functionHelpText);

            Element functionCategoryElem = functionElem.getChild("CategoryID");
            String functionCategoryId = functionCategoryElem.getTextTrim();
            System.out.println("- - - CategoryID = " + functionCategoryId);

            Category functionCategory = (Category) this.categoryMap.get(functionCategoryId);
            if (functionCategory == null) {
                throw new ObjectNotFoundException("Function " + functionId + " -- Category \""
                    + functionCategoryId + "\" is not defined");
            }

            Function function = signet.newFunction(functionCategory, functionId, functionName,
                    Status.ACTIVE, functionHelpText);
            function.save();

            List functionPermissions = functionElem.getChildren("FunctionPermission");
            Iterator functionPermissionIter = functionPermissions.iterator();
            while (functionPermissionIter.hasNext()) {
                Element functionPermissionElem = (Element) functionPermissionIter.next();
                String functionPermissionId = functionPermissionElem.getTextTrim();
                System.out.println("- - - FunctionPermission = " + functionPermissionId);

                Permission functionPermission = (Permission) this.permissionMap.get(functionPermissionId);
                if (functionPermission == null) {
                    throw new ObjectNotFoundException("Function " + functionId
                        + " -- Permission \"" + functionPermissionId + "\" is not defined");
                }

                function.addPermission(functionPermission);
            }
        }
    }

    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                System.err.println("Usage: SubsystemXmlLoader <xml file>");
                return;
            }
            
            SubsystemXmlLoader processor = new SubsystemXmlLoader();

            String fName = args[0];

            // Process the XML file
            // The second param indicates whether XML will
            // be validated.
            processor.processXML(fName, true);
        } catch (JDOMException e) {
            System.out.println("-" + e.getMessage());
        } catch (ObjectNotFoundException e) {
            System.out.println("-Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("-Error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
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

}
