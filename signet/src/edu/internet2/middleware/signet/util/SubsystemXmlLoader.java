/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/SubsystemXmlLoader.java,v 1.19 2007-02-24 02:11:32 ddonn Exp $
Copyright 2007 Internet2, Stanford University

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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import edu.internet2.middleware.signet.Category;
import edu.internet2.middleware.signet.DataType;
import edu.internet2.middleware.signet.Function;
import edu.internet2.middleware.signet.Limit;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Permission;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SignetFactory;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.Subsystem;
import edu.internet2.middleware.signet.SubsystemImpl;
import edu.internet2.middleware.signet.choice.ChoiceSet;
import edu.internet2.middleware.signet.choice.ChoiceSetAdapter;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.tree.Tree;

/**
 * Utility application to import Subsystems into Signet's persistent store.
 * 
 * @version $Revision: 1.19 $
 * @author lmcrae@stanford.edu
 *
 */
public class SubsystemXmlLoader
{
    private Map choiceSetMap;
    private Map limitMap;
    private Map categoryMap;
    private Map permissionMap;

    /**
     * Default constructor.
     *
     */
    public SubsystemXmlLoader() throws Exception
    {
		choiceSetMap = new HashMap();
		limitMap = new HashMap();
		categoryMap = new HashMap();
		permissionMap = new HashMap();
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

        // init Signet, persistent store, etc.
        Signet signet = new Signet();
        HibernateDB hibr = signet.getPersistentDB();

        // -- Check for existence of subsystem
        if (subsystemExists(hibr, subsystemId))
        {
          removeSubsystem(hibr, subsystemId);
        }

        System.out.println("+ " + rootElem.getName());
        System.out.println("- " + subsystemIdElem.getName() + " = " + subsystemId);
        System.out.println("- " + subsystemNameElem.getName() + " = " + subsystemName);

        Element scopeElem = rootElem.getChild("Scope");
        String scopeId = scopeElem.getTextTrim();

        System.out.println("- - Scope = " + scopeId);

        // Check for existence of named scope tree
        Tree tempTree = null;
Session hs = hibr.openSession();

        try { tempTree = hibr.getTree(hs, scopeId); }
        catch (ObjectNotFoundException e)
        {
        	hibr.closeSession(hs);
            throw new ObjectNotFoundException("Subsystem " + subsystemId + " -- Scope \"" + scopeId
                + "\" is not a defined Tree");
        }

Transaction tx = hs.beginTransaction();
        Subsystem subsystem = SignetFactory.newSubsystem(
        		signet, subsystemId, subsystemName, subsystemHelpText, Status.ACTIVE);
        subsystem.setTree(tempTree);

        try {
            processChoiceSet(signet, subsystem, rootElem);
            processLimit(signet, subsystem, rootElem);
            processPermission(signet, subsystem, rootElem);
            processCategory(signet, subsystem, rootElem);
            processFunction(signet, subsystem, rootElem);
hs.saveOrUpdate(subsystem);
tx.commit();

        } catch (ObjectNotFoundException e) {
        	tx.rollback();
            throw new ObjectNotFoundException(e.getMessage());
        } catch (NumberFormatException e) {
        	tx.rollback();
            throw new NumberFormatException(e.getMessage());
        }
        finally
        {
        	hibr.closeSession(hs);
        }
    }

    private boolean subsystemExists(HibernateDB hibr, String subsystemId)
	{
    	Session hs = hibr.openSession();
		Query query = hibr.createQuery(hs,
				"from " + SubsystemImpl.class.getName() +
				" where subsystemID='" + subsystemId + "'");
		List results = query.list();
		int subsystemCount = results.size();
		hibr.closeSession(hs);

		return (0 < subsystemCount);
	}

    private void processChoiceSet(Signet signet, Subsystem subsystem, Element rootElem)
        throws javax.naming.OperationNotSupportedException, NumberFormatException
    {
        List choicesets = rootElem.getChildren("ChoiceSet");
        Iterator choicesetIter = choicesets.iterator();
        while (choicesetIter.hasNext()) {
            System.out.println("- - ChoiceSet");

            Element choicesetElem = (Element) choicesetIter.next();

            Element choicesetIdElem = choicesetElem.getChild("Id");
            String choicesetId = choicesetIdElem.getTextTrim();
            System.out.println("- - - Id = " + choicesetId);

            ChoiceSetAdapter csAdapter = SignetFactory.getChoiceSetAdapter(
            		signet, SignetFactory.DEFAULT_CHOICE_SET_ADAPTER_NAME);
            ChoiceSet choiceset = SignetFactory.newChoiceSet(
            		signet, csAdapter, subsystem, choicesetId);
            choiceSetMap.put(choicesetId, choiceset);

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

                choiceset.addChoice(choiceValue, choiceLabel, choiceOrderInt, choiceRankInt);
            }
        }
    }

    private void processLimit(Signet signet, Subsystem subsystem, Element rootElem)
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
        	= (ChoiceSet)choiceSetMap.get(limitChoiceSetId);
        if (limitChoiceSet == null)
        {
          throw new ObjectNotFoundException
          	("Limit "
          	 + limitId
          	 + " -- ChoiceSet \""
             + limitChoiceSetId + "\" is not defined");
        }

		Limit limit = SignetFactory.newLimit(signet, subsystem, limitId, DataType.TEXT,
				limitChoiceSet, limitName, limitNumber, limitHelpText, Status.ACTIVE, rendererId);
        limitMap.put(limitId, limit);
        
        limitNumber++;
      }
    }

    private void processPermission(Signet signet, Subsystem subsystem, Element rootElem)
        	throws ObjectNotFoundException
    {
        List permissions = rootElem.getChildren("Permission");
        Iterator permissionIter = permissions.iterator();
        while (permissionIter.hasNext()) {
            Element permissionElem = (Element) permissionIter.next();

            System.out.println("- - Permission");

            Element permissionIdElem = permissionElem.getChild("Id");
            String permissionId = permissionIdElem.getTextTrim();
            System.out.println("- - - Id = " + permissionId);

            Permission permission = SignetFactory.newPermission(subsystem, permissionId, Status.ACTIVE);

            List permissionLimits = permissionElem.getChildren("PermissionLimit");
            Iterator permissionLimitsIter = permissionLimits.iterator();
            while (permissionLimitsIter.hasNext()) {
                Element permissionLimitElem = (Element) permissionLimitsIter.next();
                String permissionLimitId = permissionLimitElem.getTextTrim();
                System.out.println("- - - PermissionLimit = "
                    + permissionLimitId);

                Limit permissionLimit = (Limit)limitMap.get(permissionLimitId);
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
            permissionMap.put(permissionId, permission);
        }
    }

    private void processCategory(Signet signet, Subsystem subsystem, Element rootElem)
    {
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

            Category category = SignetFactory.newCategory(subsystem, categoryId, categoryName, Status.ACTIVE);
            categoryMap.put(categoryId, category);
        }
    }

    private void processFunction(Signet signet, Subsystem subsystem, Element rootElem)
        throws ObjectNotFoundException
    {
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

            Category functionCategory = (Category)categoryMap.get(functionCategoryId);
            if (functionCategory == null) {
                throw new ObjectNotFoundException("Function " + functionId + " -- Category \""
                    + functionCategoryId + "\" is not defined");
            }

            // factory method adds function to the category and category's subsystem
            Function function = SignetFactory.newFunction(signet, functionCategory,
            		functionId, functionName, Status.ACTIVE, functionHelpText);

            List functionPermissions = functionElem.getChildren("FunctionPermission");
            Iterator functionPermissionIter = functionPermissions.iterator();
            while (functionPermissionIter.hasNext()) {
                Element functionPermissionElem = (Element) functionPermissionIter.next();
                String functionPermissionId = functionPermissionElem.getTextTrim();
                System.out.println("- - - FunctionPermission = " + functionPermissionId);

                Permission functionPermission = (Permission)permissionMap.get(functionPermissionId);
                if (functionPermission == null) {
                    throw new ObjectNotFoundException("Function " + functionId
                        + " -- Permission \"" + functionPermissionId + "\" is not defined");
                }

                function.addPermission(functionPermission);
            }
        }
    }

    private void removeSubsystem(HibernateDB hibr, String subsystemId) {
        if (! readYesOrNo(
        		"\nYou are about to delete data for subsystem "
        		+ subsystemId
        		+ " and the associated assignments."
				+ "\nDo you wish"
				+ " to continue (Y/N)? "))
        {
        	System.out.println("Operation canceled.");
        	System.exit(0);
        }

    	SubsystemDestroyer destroyer = new SubsystemDestroyer();
    	try {
    		destroyer.execute(hibr, subsystemId);
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
    
    private boolean readYesOrNo(String prompt) {
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
    
    private String promptedReadLine(String prompt)
    {
        try
        {
            System.out.print(prompt);
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            return reader.readLine();
        }
        catch (java.io.IOException e)
        {
            return null;
        }
    }
    
    
    public static void main(String[] args)
    {
    	boolean status = false; // assume failure
        try {
            if (args.length < 1) {
                System.err.println("Usage: SubsystemXmlLoader <xml file>");
                System.exit(0);
            }

			SubsystemXmlLoader processor = new SubsystemXmlLoader();

            String fName = args[0];

            // Process the XML file
            // The second param indicates whether XML will
            // be validated.
            processor.processXML(fName, true);
            status = true;
        } catch (JDOMException e) {
            System.out.println("-" + e.getMessage());
        } catch (ObjectNotFoundException e) {
            System.out.println("-Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("-Error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (status)
        	System.out.println("Operation successful.");
        System.out.println("SubsystemXmlLoader done.");
    }

}
