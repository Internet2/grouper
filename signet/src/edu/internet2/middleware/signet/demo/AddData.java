/*--
$Id: AddData.java,v 1.4 2005-01-12 17:28:05 acohen Exp $
$Date: 2005-01-12 17:28:05 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.demo;

import javax.naming.OperationNotSupportedException;

import edu.internet2.middleware.signet.Category;
import edu.internet2.middleware.signet.Function;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.Subsystem;
import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeNode;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectType;

public class AddData
{
private static final String PERSON_SUBJECT_TYPE_ID
	= "person";
private static final String PERSON_SUBJECT_TYPE_NAME
	= "database-resident person";

/**
 * 
 */
public AddData()
{
  super();
  // TODO Auto-generated constructor stub
}

public static void main(String[] args)
{
  Signet signet = new Signet();
  signet.beginTransaction();
  
  try
  {
  createTestData(signet);
  createSampleData(signet);
  }
  catch (Exception e)
  {
    System.out.println("ERROR: " + e);
    e.printStackTrace();
  }
  
  signet.commit();
  signet.close();
}

private static void createSampleData(Signet signet)
throws
	OperationNotSupportedException,
	ObjectNotFoundException
{
  SubjectType personSubjectType;
  
  personSubjectType = createPersonSubjectType(signet);
  createSampleSubjects(signet, personSubjectType);
  Tree tree = createSampleTree(signet);
  createSampleSubsystems(signet, tree);
}

private static Tree createSampleTree(Signet signet)
throws ObjectNotFoundException
{
  Tree adminOrgsTree
    = signet.newTree
    		("adminorgs", "Administrative Organizations");
  

    TreeNode presNode
  	  = signet.newTreeNode
  			  (adminOrgsTree, "PRES", "Office of the President");
    adminOrgsTree.addRoot(presNode);
    
      TreeNode registrarNode
  	    = signet.newTreeNode
      			(adminOrgsTree, "REGISTRAR", "University Registrar");
      presNode.addChild(registrarNode);
      
        TreeNode admissionNode
        	= signet.newTreeNode
        			(adminOrgsTree, "ADMISSION", "Admissions");
        registrarNode.addChild(admissionNode);
        
        TreeNode finaidNode
        	= signet.newTreeNode
        			(adminOrgsTree, "FINAID", "Financial Aid");
        registrarNode.addChild(finaidNode);
        
        TreeNode bursarNode
        	= signet.newTreeNode
        			(adminOrgsTree, "BURSAR", "Bursar");
        registrarNode.addChild(bursarNode);
        
        TreeNode sturecNode
        	= signet.newTreeNode
        			(adminOrgsTree, "STUREC", "Student Records");
        registrarNode.addChild(sturecNode);
        
  
      TreeNode hrNode
  	    = signet.newTreeNode
      			(adminOrgsTree, "HR", "Human Resources");
      presNode.addChild(hrNode);
      
        TreeNode adminNode
        	= signet.newTreeNode
        			(adminOrgsTree, "ADMIN", "HR Administration");
        hrNode.addChild(adminNode);
        
        TreeNode benefitsNode
        	= signet.newTreeNode
        			(adminOrgsTree, "BENEFITS", "Benefits");
        hrNode.addChild(benefitsNode);
        
        TreeNode hrrecNode
        	= signet.newTreeNode
        			(adminOrgsTree, "HRREC", "HR Records");
        hrNode.addChild(hrrecNode);
        
        TreeNode salaryNode
        	= signet.newTreeNode
        			(adminOrgsTree, "SALARY", "Salaries");
        hrNode.addChild(salaryNode);
        
      
      TreeNode controllerNode
  	    = signet.newTreeNode
      			(adminOrgsTree, "CONTROLLER", "Controller's Office");
      presNode.addChild(controllerNode);
      
      	TreeNode reqsNode
      		= signet.newTreeNode
      				(adminOrgsTree, "REQS", "Requisitions");
      	controllerNode.addChild(reqsNode);
      
      TreeNode libraryNode
  	    = signet.newTreeNode
      			(adminOrgsTree, "LIBRARY", "University Library");
      presNode.addChild(libraryNode);
  
      TreeNode itNode
  	    = signet.newTreeNode
      			(adminOrgsTree, "IT", "Information Technology");
      presNode.addChild(itNode);
      
      	TreeNode networkingNode
      		= signet.newTreeNode
      				(adminOrgsTree, "NETWORKING", "Networking");
      	itNode.addChild(networkingNode);
      	
      	TreeNode commNode
      		= signet.newTreeNode
      				(adminOrgsTree, "COMM", "Commnications");
      	itNode.addChild(commNode);
      	
      	TreeNode acadcompNode
      		= signet.newTreeNode
      				(adminOrgsTree, "ACADCOMP", "Academic Computing");
      	itNode.addChild(acadcompNode);
      	
      	TreeNode admincompNode
      		= signet.newTreeNode
      				(adminOrgsTree, "ADMINCOMP", "Administrative Computing");
      	itNode.addChild(admincompNode);
      	
      	TreeNode developmentNode
      		= signet.newTreeNode
      				(adminOrgsTree, "DEVELOPMENT", "Development");
      	itNode.addChild(developmentNode);
      	

        

    TreeNode provostNode
      = signet.newTreeNode
          (adminOrgsTree, "PROVOST", "Office of the Provost");
    adminOrgsTree.addRoot(provostNode);
    
    	TreeNode archNode
    		= signet.newTreeNode
    				(adminOrgsTree, "ARCH", "School of Architecture");
    	provostNode.addChild(archNode);
    	
    	TreeNode businessNode
    		= signet.newTreeNode
    				(adminOrgsTree, "BUSINESS", "Business School");
    	provostNode.addChild(businessNode);
    	
    	TreeNode engineerNode
    		= signet.newTreeNode
    				(adminOrgsTree, "ENGINEER", "School of Engineering");
    	provostNode.addChild(engineerNode);
    	
    	TreeNode humsciNode
    		= signet.newTreeNode
    				(adminOrgsTree, "HUMSCI", "Humanities & Sciences");
    	provostNode.addChild(humsciNode);
    	
    		TreeNode artNode
    			= signet.newTreeNode
    					(adminOrgsTree, "ART", "Art Department");
    		humsciNode.addChild(artNode);
    		
    		TreeNode athleticsNode
    			= signet.newTreeNode
							(adminOrgsTree, "ATHLETICS", "Department of Athletics and Physical Education");
    		humsciNode.addChild(athleticsNode);
    		
    		TreeNode biologyNode
    			= signet.newTreeNode
    					(adminOrgsTree, "BIOLOGY", "Biology");
    		humsciNode.addChild(biologyNode);
    		
    		TreeNode chemNode
    			= signet.newTreeNode
    					(adminOrgsTree, "CHEM", "Chemistry");
    		humsciNode.addChild(chemNode);
    		
    		TreeNode compcsiNode
    			= signet.newTreeNode
    					(adminOrgsTree, "COMPCSI", "Computer Science");
    		humsciNode.addChild(compcsiNode);
    		
    		TreeNode dramaNode
    			= signet.newTreeNode
    					(adminOrgsTree, "DRAMA", "Art Department");
    		humsciNode.addChild(dramaNode);
    		
    		TreeNode englishNode
    			= signet.newTreeNode
    					(adminOrgsTree, "ENGLISH", "Art Department");
    		humsciNode.addChild(englishNode);
    		
    		TreeNode litNode
    			= signet.newTreeNode
    					(adminOrgsTree, "LIT", "Literature");
    		humsciNode.addChild(litNode);
    		
    		TreeNode musicNode
    			= signet.newTreeNode
    					(adminOrgsTree, "MUSIC", "Music Department");
    		humsciNode.addChild(musicNode);
    		
    		TreeNode physicsNode
    			= signet.newTreeNode
    					(adminOrgsTree, "PHYSICS", "Physics");
    		humsciNode.addChild(physicsNode);
    		
    	
    	TreeNode lawNode
    		= signet.newTreeNode
    				(adminOrgsTree, "LAW", "Law School");
    	provostNode.addChild(lawNode);
    	
    	TreeNode medicineNode
    		= signet.newTreeNode
    				(adminOrgsTree, "MEDICINE", "School of Medicine");
    	provostNode.addChild(medicineNode);
    	
          
  
  signet.save(adminOrgsTree);
  
  return adminOrgsTree;
}

private static void createSampleSubsystems
	(Signet signet,
	 Tree		tree)
{
  Subsystem authoritySubsystem
    = signet.newSubsystem
        ("authority", "authority system", "authority system");
  Category proxyCategory
    = signet.newCategory
        (authoritySubsystem, "proxy", "Authority Proxy", Status.ACTIVE);
  Function granting_proxyFunction
  	= signet.newFunction
  			(proxyCategory,
  			 "granting_proxy",
  			 "Authority Granting Proxy",
  			 Status.ACTIVE,
  			 "Authority Granting Proxy");
  authoritySubsystem.setTree(tree);
  
  signet.save(authoritySubsystem);
  
  
  	
  Subsystem financialSubsystem
    = signet.newSubsystem
    	 ("financial", "Financial System GL", "financial system");
  Category apCategory
  	= signet.newCategory
  			(financialSubsystem, "ap", "Accounts Payable", Status.ACTIVE);
  Function ap_taxFunction = signet.newFunction(apCategory, "ap_tax", "AP Tax", Status.ACTIVE, "Tax TBD");
  Function ap_managerFunction = signet.newFunction(apCategory, "ap_manager", "AP Manager", Status.ACTIVE, "Mgr TBD");
  Function ap_tr_buyerFunction = signet.newFunction(apCategory, "ap_tr_buyer", "AP T&R Buyer", Status.ACTIVE, "AP T&R Buyer");
  Function ap_screeningFunction = signet.newFunction(apCategory, "ap_screening", "AP Screening", Status.ACTIVE, "Screening TBD");
  Function ap_supplierFunction = signet.newFunction(apCategory, "ap_supplier", "AP Supplier Coordinator", Status.ACTIVE, "SC TBD");
  Function ap_interfacesFunction = signet.newFunction(apCategory, "ap_interfaces", "AP Interfaces", Status.ACTIVE, "AP Interfaces");
  Function ap_accountantFunction = signet.newFunction(apCategory, "ap_accountant", "AP Accountant", Status.ACTIVE, "Accountant TBD");
  Function ap_supervisorFunction = signet.newFunction(apCategory, "ap_supervisor", "AP Supervisor", Status.ACTIVE, "Supervisor TBD");
  Function ap_tr_managerFunction = signet.newFunction(apCategory, "ap_tr_manager", "AP T&R Manager", Status.ACTIVE, "AP T&R Manager");
  Function ap_cardFunction = signet.newFunction(apCategory, "ap_card", "AP Card Coordinator", Status.ACTIVE, "PCard setup and administration");
  Function ap_purgeFunction = signet.newFunction(apCategory, "ap_purge", "AP Payables Purge", Status.ACTIVE, "Purge records.  Submit request");
  Function ap_inquiryFunction = signet.newFunction(apCategory, "ap_inquiry", "AP Payables Inquiry", Status.ACTIVE, "See invoice batches.  View req");
  Function ap_invoiceFunction = signet.newFunction(apCategory, "ap_invoice", "AP Payables Invoice Processor", Status.ACTIVE, "Invoice entry. Invoice inquiry");
  Function ap_paymentFunction = signet.newFunction(apCategory, "ap_payment", "AP Payables Payment Processor", Status.ACTIVE, "Invoice inquiry.  Payment entr");

  Category apprCategory
		= signet.newCategory
				(financialSubsystem, "appr", "Approvals", Status.ACTIVE);
  Function appr_DPAFunction = signet.newFunction(apprCategory, "appr_DPA", "DPA Screening", Status.ACTIVE, "DPA screening");
  Function appr_sub_awardsFunction = signet.newFunction(apprCategory, "appr_sub_awards", "Sub Awards", Status.ACTIVE, "Sub Awards");
  Function appr_lab_animalsFunction = signet.newFunction(apprCategory, "appr_lab_animals", "Lab Animals", Status.ACTIVE, "Lab Animals");
  Function acting_approverFunction = signet.newFunction(apprCategory, "acting_approver", "Acting Approver", Status.ACTIVE, "Acting Approver");
  Function appr_requisitionsFunction = signet.newFunction(apprCategory, "appr_requisitions", "Requisitions", Status.ACTIVE, "Approve requisitions");
  Function appr_budget_controlFunction = signet.newFunction(apprCategory, "appr_budget_control", "Budget control", Status.ACTIVE, "Approve budget changes");
  Function appr_expense_journalFunction = signet.newFunction(apprCategory, "appr_expense_journal", "Expense Journal", Status.ACTIVE, "Approve expense journals");
  Function appr_revenue_journalFunction = signet.newFunction(apprCategory, "appr_revenue_journal", "GL Journal", Status.ACTIVE, "Approve general ledger journal");
  Function appr_alcohol_tax_exemptFunction = signet.newFunction(apprCategory, "appr_alcohol_tax_exempt", "Tax Exempt Alcohol", Status.ACTIVE, "Approve Tax Exempt Alcohol");
  Function appr_labor_distributionFunction = signet.newFunction(apprCategory, "appr_labor_distribution", "Labor Distribution", Status.ACTIVE, "Approve Labor Dist Adjustment");

  Category arCategory 
  	= signet.newCategory
  			(financialSubsystem, "ar", "Accounts Receivable", Status.ACTIVE);
  Function ar_interfacesFunction = signet.newFunction(arCategory, "ar_interfaces", "SU AR Interfaces", Status.ACTIVE, "TBD");
  Function ar_collectorFunction = signet.newFunction(arCategory, "ar_collector", "SU AR Collector", Status.ACTIVE, "Collections");
  Function ar_managerFunction = signet.newFunction(arCategory, "ar_manager", "SU AR Manager", Status.ACTIVE, "Transactions;Receipts;Collecti");
  Function ar_inquiryFunction = signet.newFunction(arCategory, "ar_inquiry", "SU AR Receivables Inquiry", Status.ACTIVE, "Receivables inquiry");
  Function ar_receipt_specialistFunction = signet.newFunction(arCategory, "ar_receipt_specialist", "SU AR Receipt Specialist", Status.ACTIVE, "Receipts");
  Function ar_billing_specialistFunction = signet.newFunction(arCategory, "ar_billing_specialist", "SU AR Billing Specialist", Status.ACTIVE, "Transactions; some listing rep");
  Function ar_customer_setup_specialistFunction = signet.newFunction(arCategory, "ar_customer_setup_specialist", "SU AR Customer Setup Specialist", Status.ACTIVE, "Customer Setup");

  Category cmCategory 
  	= signet.newCategory
  			(financialSubsystem, "cm", "Cash Management", Status.ACTIVE);
  Function cm_inquiryFunction = signet.newFunction(cmCategory, "cm_inquiry", "SU CM Inquiry", Status.ACTIVE, "Inquiries");
  Function cm_setupFunction = signet.newFunction(cmCategory, "cm_setup", "SU CM Setup", Status.ACTIVE, "Bank setups; bank functions");
  Function cm_userFunction = signet.newFunction(cmCategory, "cm_user", "SU CM User", Status.ACTIVE, "Cash forecasts; bank statement");
  Function cm_managerFunction = signet.newFunction(cmCategory, "cm_manager", "SU CM Manager", Status.ACTIVE, "Cash forecasts;bank statements");

  Category faCategory 
  	= signet.newCategory
  			(financialSubsystem, "fa", "Fixed Assets", Status.ACTIVE);
  Function fa_user_coFunction = signet.newFunction(faCategory, "fa_user_co", "User CO", Status.ACTIVE, "TBD");
  Function fa_manager_coFunction = signet.newFunction(faCategory, "fa_manager_co", "Manager CO", Status.ACTIVE, "TBD");
  Function fa_interfacesFunction = signet.newFunction(faCategory, "fa_interfaces", "FA Interfaces", Status.ACTIVE, "TBD");
  Function fa_manager_pmoFunction = signet.newFunction(faCategory, "fa_manager_pmo", "Manager PMO", Status.ACTIVE, "TBD");
  Function fa_as_it_clerkFunction = signet.newFunction(faCategory, "fa_as_it_clerk", "SF IT Clerk", Status.ACTIVE, "IT Clerk");
  Function fa_power_user_coFunction = signet.newFunction(faCategory, "fa_power_user_co", "Power User CO", Status.ACTIVE, "TBD");
  Function fa_as_it_managerFunction = signet.newFunction(faCategory, "fa_as_it_manager", "SF IT Manager", Status.ACTIVE, "IT Manager");
  Function fa_as_catalogerFunction = signet.newFunction(faCategory, "fa_as_cataloger", "SF Cataloger", Status.ACTIVE, "Catalog assets");
  Function fa_as_query_onlyFunction = signet.newFunction(faCategory, "fa_as_query_only", "SF Query Only", Status.ACTIVE, "Query asset records");
  Function fa_financial_analyst_pmoFunction = signet.newFunction(faCategory, "fa_financial_analyst_pmo", "Financial Analyst PMO", Status.ACTIVE, "TBD");
  Function fa_as_domain_adminFunction = signet.newFunction(faCategory, "fa_as_domain_admin", "SF Domain Admin", Status.ACTIVE, "Manage Sunflower tables");
  Function fa_as_excess_clerkFunction = signet.newFunction(faCategory, "fa_as_excess_clerk", "SF Excess Clerk", Status.ACTIVE, "Manage retirement of asset");
  Function fa_property_administrator_pmoFunction = signet.newFunction(faCategory, "fa_property_administrator_pmo", "Property Admin PMO", Status.ACTIVE, "TBD");
  Function fa_as_review_clerkFunction = signet.newFunction(faCategory, "fa_as_review_clerk", "SF Review Clerk", Status.ACTIVE, "Manage inventory of assets; Ma");
  Function fa_as_excess_managerFunction = signet.newFunction(faCategory, "fa_as_excess_manager", "SF Excess Manager", Status.ACTIVE, "Manage retirement of asset");
  Function fa_as_administratorFunction = signet.newFunction(faCategory, "fa_as_administrator", "SF Administrator", Status.ACTIVE, "Manage asset records; Manage S");
  Function fa_as_web_report_userFunction = signet.newFunction(faCategory, "fa_as_web_report_user", "SF Web Report User", Status.ACTIVE, "Run limited asset reports");
  Function fa_as_inactive_clerkFunction = signet.newFunction(faCategory, "fa_as_inactive_clerk", "SF Inactive Clerk", Status.ACTIVE, "Manage all inactive asset reco");
  Function fa_as_review_managerFunction = signet.newFunction(faCategory, "fa_as_review_manager", "SF Review Manager", Status.ACTIVE, "Manage inventory of assets; Ma");
  Function fa_as_agreement_clerkFunction = signet.newFunction(faCategory, "fa_as_agreement_clerk", "SF Agreement Clerk", Status.ACTIVE, "Manage all agreement asset rec");
  Function fa_as_inventory_clerkFunction = signet.newFunction(faCategory, "fa_as_inventory_clerk", "SF Inventory Clerk", Status.ACTIVE, "Manage all asset records; Tran");
  Function fa_as_inactive_managerFunction = signet.newFunction(faCategory, "fa_as_inactive_manager", "SF Inactive Manager", Status.ACTIVE, "Manage all inactive asset reco");
  Function fa_as_agreement_managerFunction = signet.newFunction(faCategory, "fa_as_agreement_manager", "SF Agreement Manager", Status.ACTIVE, "Manage all agreement asset rec");
  Function fa_as_inventory_managerFunction = signet.newFunction(faCategory, "fa_as_inventory_manager", "SF Inventory Manager", Status.ACTIVE, "Manage all asset records; Tran");
  Function fa_as_asset_center_representativeFunction = signet.newFunction(faCategory, "fa_as_asset_center_representative", "SF Asset Center Rep", Status.ACTIVE, "Manage asset records for ownin");

  Category fyiCategory 
  	= signet.newCategory(financialSubsystem, "fyi", "FYIs", Status.ACTIVE);
  Function fyi_budgetFunction = signet.newFunction(fyiCategory, "fyi_budget", "FYI Budget", Status.ACTIVE, "FYI Budget");
  Function fyi_reqFunction = signet.newFunction(fyiCategory, "fyi_req", "FYI Requisitions", Status.ACTIVE, "FYI Requisitions");
  Function fyi_LD_DAFunction = signet.newFunction(fyiCategory, "fyi_LD_DA", "FYI LD Dist Adj", Status.ACTIVE, "FYI Approved Dist Adj");
  Function fyi_LD_LSFunction = signet.newFunction(fyiCategory, "fyi_LD_LS", "FYI LD Labor Sched", Status.ACTIVE, "FYI Labor Sched Change");
  Function fyi_expense_journalsFunction = signet.newFunction(fyiCategory, "fyi_expense_journals", "FYI Expense Journals", Status.ACTIVE, "FYI Expense Journals");
  Function fyi_revenue_journalsFunction = signet.newFunction(fyiCategory, "fyi_revenue_journals", "FYI Revenue Journals", Status.ACTIVE, "FYI Revenue Journals");

  Category gaCategory 
  	= signet.newCategory
  			(financialSubsystem, "ga", "Grants Accounting", Status.ACTIVE);
  Function ga_inquiryFunction = signet.newFunction(gaCategory, "ga_inquiry", "SU GA Inquiry", Status.ACTIVE, "TBD");
  Function ga_asset_mgrFunction = signet.newFunction(gaCategory, "ga_asset_mgr", "Capital Asset Manager", Status.ACTIVE, "TBD");
  Function ga_dept_adminFunction = signet.newFunction(gaCategory, "ga_dept_admin", "GA Departmental Administrator", Status.ACTIVE, "TBD");
  Function glsu_workflow_adminFunction = signet.newFunction(gaCategory, "glsu_workflow_admin", "Workflow Adminstrator", Status.ACTIVE, "TBD");
  Function ga_adminFunction = signet.newFunction(gaCategory, "ga_admin", "SU GA Administrator", Status.ACTIVE, "Awards, Projects; Budgets, Exp");
  Function ga_billingFunction = signet.newFunction(gaCategory, "ga_billing", "SU AR Billing and Reporting", Status.ACTIVE, "Expenditure Inquiry; Billing; ");
  Function ga_controller_setupFunction = signet.newFunction(gaCategory, "ga_controller_setup", "SU GA Controller Setup", Status.ACTIVE, "Expenditure Setup.");
  Function ga_funds_acctFunction = signet.newFunction(gaCategory, "ga_funds_acct", "SU GA Funds Accountant", Status.ACTIVE, "Awards, Projects.  Budgets, Ex");
  Function ga_complianceFunction = signet.newFunction(gaCategory, "ga_compliance", "SU GA ORA Compliance Setup", Status.ACTIVE, "Burden; Billing; Proj Status C");
  Function ga_research_acctFunction = signet.newFunction(gaCategory, "ga_research_acct", "SU GA Research Accountant", Status.ACTIVE, "Awards, Projects;  Budgets, so");
  Function ga_research_assocFunction = signet.newFunction(gaCategory, "ga_research_assoc", "SU GA Research Associate", Status.ACTIVE, "Awards, Projects;  Budgets, so");
  Function ga_internal_complianceFunction = signet.newFunction(gaCategory, "ga_internal_compliance", "SU GA Internal Compliance Setup", Status.ACTIVE, "Project Templates; Awards code");
  Function ga_service_ctrFunction = signet.newFunction(gaCategory, "ga_service_ctr", "SU GA Service Centers, Aux, & Operations Specialist", Status.ACTIVE, "Projects, Expenditures, Status");

  Category glconCategory 
  	= signet.newCategory
  			(financialSubsystem,
  			 "glcon",
  			 "General Ledger - Consolidated Set of Books",
  			 Status.ACTIVE);
  Function glcon_userFunction = signet.newFunction(glconCategory, "glcon_user", "CS GL User", Status.ACTIVE, "Consolidated set of books: new");
  Function glcon_support_setupFunction = signet.newFunction(glconCategory, "glcon_support_setup", "Consolidated Support Setup", Status.ACTIVE, " ");
  Function glcon_inquiryFunction = signet.newFunction(glconCategory, "glcon_inquiry", "CS GL Inquiry", Status.ACTIVE, "Consolidated set of books: acc");
  Function glcon_gl_administratorFunction = signet.newFunction(glconCategory, "glcon_gl_administrator", "Consolidated GL Administrator", Status.ACTIVE, " ");
  Function glcon_controllerFunction = signet.newFunction(glconCategory, "glcon_controller", "CS GL Controller", Status.ACTIVE, "Consolidated set of books: con");

  Category glenCategory 
  	= signet.newCategory
  			(financialSubsystem,
  			 "glen",
  			 "General Ledger - Endowment Set of Books",
  			 Status.ACTIVE);
  Function glen_support_setupFunction = signet.newFunction(glenCategory, "glen_support_setup", "EN Support Setup", Status.ACTIVE, " ");
  Function glen_userFunction = signet.newFunction(glenCategory, "glen_user", "EN GL User", Status.ACTIVE, "Encumbrance set of books: new ");
  Function glen_gl_administratorFunction = signet.newFunction(glenCategory, "glen_gl_administrator", "EN GL Administrator", Status.ACTIVE, " ");
  Function glen_inquiryFunction = signet.newFunction(glenCategory, "glen_inquiry", "EN GL Inquiry", Status.ACTIVE, "Encumbrance set of books: acco");
  Function glen_controllerFunction = signet.newFunction(glenCategory, "glen_controller", "EN GL Controller", Status.ACTIVE, "Encumbrance set of books: cont");

  Category glerCategory 
  	= signet.newCategory
  			(financialSubsystem,
  			 "gler",
  			 "General Ledger - Reporting Set of Books",
  			 Status.ACTIVE);
  Function gler_gl_inquiryFunction = signet.newFunction(glerCategory, "gler_gl_inquiry", "ER GL Inquiry", Status.ACTIVE, " ");
  Function gler_support_setupFunction = signet.newFunction(glerCategory, "gler_support_setup", "ER Support Setup", Status.ACTIVE, " ");
  Function gler_userFunction = signet.newFunction(glerCategory, "gler_user", "ER GL User", Status.ACTIVE, "Encum. Reptg. set of books: ne");
  Function gler_controllerFunction = signet.newFunction(glerCategory, "gler_controller", "ER GL Controller", Status.ACTIVE, "ER GL Controller");
  Function gler_gl_administratorFunction = signet.newFunction(glerCategory, "gler_gl_administrator", "ER GL Administrator", Status.ACTIVE, " ");

  Category glsuCategory 
  	= signet.newCategory
  			(financialSubsystem,
  			 "glsu",
  			 "General Ledger - Stanford Univ Set of Books",
  			 Status.ACTIVE);
  Function glsu_userFunction = signet.newFunction(glsuCategory, "glsu_user", "SU GL User", Status.ACTIVE, "New journals; inquiry");
  Function glsu_support_setupFunction = signet.newFunction(glsuCategory, "glsu_support_setup", "SU Support Setup", Status.ACTIVE, " ");
  Function glsu_interfacesFunction = signet.newFunction(glsuCategory, "glsu_interfaces", "General Ledger Interfaces", Status.ACTIVE, " ");
  Function glsu_gl_administratorFunction = signet.newFunction(glsuCategory, "glsu_gl_administrator", "SU GL Administrator", Status.ACTIVE, " ");
  Function glsu_inquiryFunction = signet.newFunction(glsuCategory, "glsu_inquiry", "SU GL Inquiry", Status.ACTIVE, "Account inquiry; journal inqui");

  Category memCategory 
  	= signet.newCategory
  			(financialSubsystem, "mem", "Membership", Status.ACTIVE);
  Category perCategory 
  	= signet.newCategory
  			(financialSubsystem, "per", "Human Resources", Status.ACTIVE);
  Category poCategory 
  	= signet.newCategory
  			(financialSubsystem, "po", "Purchasing", Status.ACTIVE);
  Category pspCategory 
  	= signet.newCategory
  			(financialSubsystem, "psp", "Labor Distribution", Status.ACTIVE);
  Category repCategory 
  	= signet.newCategory
  			(financialSubsystem, "rep", "Reporting", Status.ACTIVE);
  Category ssCategory 
  	= signet.newCategory
  			(financialSubsystem, "ss", "Self Service", Status.ACTIVE);

  financialSubsystem.setTree(tree);
  signet.save(financialSubsystem);
  
  
  	
  Subsystem hrSubsystem
    = signet.newSubsystem
    	("hr", "Human Resources", "human resources system");
  Category hr_payroll_benCategory
    = signet.newCategory
        (hrSubsystem,
         "hr_payroll_ben",
         "HR, Payroll, Benefits",
         Status.ACTIVE);
  hrSubsystem.setTree(tree);
  signet.save(hrSubsystem);
  	
  Subsystem researchSubsystem
    = signet.newSubsystem
  	  ("research", "Research", "Research");
  Category coiCategory
  	= signet.newCategory
  			(researchSubsystem, "coi", "Conflict of Interest", Status.ACTIVE);
  researchSubsystem.setTree(tree);
  signet.save(researchSubsystem);
  	
  Subsystem spacemgmtSubsystem
    = signet.newSubsystem
    	("spacemgmt", "Space Management", "Space Management");
  Category ispaceCategory
  	= signet.newCategory
  			(spacemgmtSubsystem, "ispace", "iSpace", Status.ACTIVE);
  spacemgmtSubsystem.setTree(tree);
  signet.save(spacemgmtSubsystem);
  	
  Subsystem studentSubsystem
    = signet.newSubsystem
    	("student", "Student Systems & Data", "student system");
  Category admin_finaidCategory 
  	= signet.newCategory
  			(studentSubsystem, "admin_finaid", "Financial Aid", Status.ACTIVE);
  Category approvalsCategory 
  	= signet.newCategory
  			(studentSubsystem, "approvals", "Approvals", Status.ACTIVE);
  Category manage_admissCategory 
  	= signet.newCategory
  			(studentSubsystem, "manage_admiss", "Admissions", Status.ACTIVE);
  Category manage_financialsCategory 
  	= signet.newCategory
  			(studentSubsystem,
  			 "manage_financials",
  			 "Student Financials",
  			 Status.ACTIVE);
  Category manage_recordsCategory 
  	= signet.newCategory
  			(studentSubsystem,
  			 "manage_records",
  			 "Student Records",
  			 Status.ACTIVE);
  
  studentSubsystem.setTree(tree);
  signet.save(studentSubsystem);
}

private static SubjectType createPersonSubjectType(Signet signet)
throws ObjectNotFoundException
{
  SubjectType personSubjectType
    = signet.newSubjectType
  	    (PERSON_SUBJECT_TYPE_ID,
  	     PERSON_SUBJECT_TYPE_NAME);
  
  signet.save(personSubjectType);
  
  return personSubjectType;
}

private static void createAndSaveSubject
	(Signet				signet,
	 SubjectType 	subjectType,
	 String 			id,
	 String				name,
	 String				description,
	 String				displayId,
	 String				firstName,
	 String				middleName,
	 String				lastName)
throws OperationNotSupportedException
{
  Subject newSubject;
  
  newSubject
  	= signet.newSubject
  			(subjectType, id, name, description, displayId);
  
  newSubject.addAttribute(signet.ATTR_FIRSTNAME, firstName);
  newSubject.addAttribute(signet.ATTR_MIDDLENAME, middleName);
  newSubject.addAttribute(signet.ATTR_LASTNAME, lastName);
  
  signet.save(newSubject);
}

private static void createSampleSubjects
	(Signet 			signet,
	 SubjectType 	personSubjectType)
throws OperationNotSupportedException
{
  createAndSaveSubject
  	(signet,
  	 personSubjectType,
  	 "K0000001",
  	 "McRae, Lynn", 
  	 "Chair, Signet Working Group", 
  	 "lmcrae",
  	 "Lynn",		// ~firstname
  	 null,			// ~middlename
  	 "McRae");	// ~lastname
  
  createAndSaveSubject
  	(signet,
     	 personSubjectType,
     "K0000002",
     "Nguyen, Minh", 
     "Signet Applicaiton Architect", 
     "mnguyen",
  	 "Minh",		// ~firstname
  	 null,			// ~middlename
  	 "Nguyen");	// ~lastname
  
  createAndSaveSubject
  	(signet,
     	 personSubjectType,
     "K0000003",
     "Cohen, Andy", 
     "Signet, Principal Developer", 
     "acohen",
  	 "Andy",		// ~firstname
  	 null,			// ~middlename
  	 "Cohen");	// ~lastname
  
  createAndSaveSubject
  	(signet,
     	 personSubjectType,
     "K0000004",
     "Vine, Jennifer", 
     "Signet, UI Design", 
     "jvine",
  	 "Jennifer",	// ~firstname
  	 null,				// ~middlename
  	 "Vine");			// ~lastname
  
  createAndSaveSubject
  	(signet,
     	 personSubjectType,
     "K0000005",
     "Morgan, R.L. 'Bob'", 
     "Washington, Wise guy", 
     "morgan",
  	 "R.",				// ~firstname
  	 "L. 'Bob'",	// ~middlename
  	 "Morgan");		// ~lastname
  
  createAndSaveSubject
  	(signet,
     	 personSubjectType,
     "K0000006",
     "Barton, Tom", 
     "Chicago, Grouper project", 
     "tbarton",
  	 "Tom",			// ~firstname
  	 null,			// ~middlename
  	 "Barton");	// ~lastname
  
  createAndSaveSubject
  	(signet,
     	 personSubjectType,
     "K0000007",
     "Hazelton, Keith", 
     "Wisconson, MACE-DIR", 
     "hazelton",
  	 "Keith",			// ~firstname
  	 null,				// ~middlename
  	 "Hazelton");	// ~lastname
  
  createAndSaveSubject
  	(signet,
     	 personSubjectType,
     "K0000008",
     "Jones, Wendy", 
     "ITSS, Development Manager", 
     "wjones",
  	 "Wendy",		// ~firstname
  	 null,			// ~middlename
  	 "Jones");	// ~lastname
  
  createAndSaveSubject
  	(signet,
     	 personSubjectType,
     "K0000009",
     "Cramer, Tom", 
     "ITSS, Applied Strategy", 
     "tcramer",
  	 "Tom",			// ~firstname
  	 null,			// ~middlename
  	 "Cramer");	// ~lastname
  
  createAndSaveSubject
  	(signet,
     	 personSubjectType,
     "K0000010",
     "Klemm, John Norman", 
     "KM Video Production", 
     "jklemm",
  	 "John",		// ~firstname
  	 "Norman",	// ~middlename
  	 "Klemm");	// ~lastname
  
  createAndSaveSubject
  	(signet,
     	 personSubjectType,
     "K0000011",
     "Katz, P. \"Kitty\"", 
     "Founder, KITN Cat Network", 
     "kkatz",
  	 "P.",				// ~firstname
  	 "\"Kitty\"",	// ~middlename
  	 "Katz");			// ~lastname
  
}

private static void createTestData(Signet signet)
throws ObjectNotFoundException
{
  Subsystem subsystem0
  	= signet.newSubsystem("my_id0", "my_name0", "my_helptext0");
  Subsystem subsystem1
		= signet.newSubsystem("my_id1", "my_name1", "my_helptext1");
  Subsystem subsystem2
		= signet.newSubsystem("my_id2", "my_name2", "my_helptext2");

  Category category00 
  	= signet.newCategory(subsystem0, "my_id00", "my_name00", Status.ACTIVE);
  Category category01 
  	= signet.newCategory(subsystem0, "my_id01", "my_name01", Status.ACTIVE);
  Category category02 
  	= signet.newCategory(subsystem0, "my_id02", "my_name02", Status.ACTIVE);
 
  Function function000
             = signet.newFunction
             		(category00, 
             		 "my_id000", 
             		 "my_name000", 
             		 Status.ACTIVE,
             		 "my_helptext000");
  Function function001
             = signet.newFunction
             		(category00, 
             		 "my_id001", 
             		 "my_name001", 
             		 Status.ACTIVE,
             		 "my_helptext001");
  Function function002
             = signet.newFunction
             		(category00, 
             		 "my_id002", 
             		 "my_name002", 
             		 Status.ACTIVE,
             		 "my_helptext002");
  
  function000.addPermission
  	(signet.newPermission("my_id0000", subsystem0, Status.ACTIVE));
  function000.addPermission
  	(signet.newPermission("my_id0001", subsystem0, Status.ACTIVE));
  function000.addPermission
  	(signet.newPermission("my_id0002", subsystem0, Status.ACTIVE));

  Tree tree00
  	= signet.newTree
  			("my_id00", "my_name00");
  subsystem0.setTree(tree00);
  TreeNode treeNode00
  	= signet.newTreeNode(tree00, "my_id00", "my_name00");
  tree00.addRoot(treeNode00);
  
  TreeNode treeNode000
  	= signet.newTreeNode(tree00, "my_id000", "my_name000");
  TreeNode treeNode001
		= signet.newTreeNode(tree00, "my_id001", "my_name001");
  TreeNode treeNode002
		= signet.newTreeNode(tree00, "my_id002", "my_name002");
  
  treeNode00.addChild(treeNode000);
  treeNode00.addChild(treeNode001);
  treeNode00.addChild(treeNode002);
  
  TreeNode treeNode0000 
  	= signet.newTreeNode
  			(tree00, "my_id0000", "my_name0000");
  TreeNode treeNode0001
		= signet.newTreeNode
				(tree00, "my_id0001", "my_name0001");
  TreeNode treeNode0002
		= signet.newTreeNode
				(tree00, "my_id0002", "my_name0002");
  
  treeNode000.addChild(treeNode0000);
  treeNode000.addChild(treeNode0001);
  treeNode000.addChild(treeNode0002);
  
  TreeNode treeNode00000
  	= signet.newTreeNode
  			(tree00,
  			 "my_id00000",
  			 "my_name00000");
  TreeNode treeNode00001
		= signet.newTreeNode
				(tree00,
				 "my_id00001",
				 "my_name00001");
  TreeNode treeNode00002
		= signet.newTreeNode
				(tree00,
				 "my_id00002",
				 "my_name00002");
  
  treeNode0000.addChild(treeNode00000);
  treeNode0000.addChild(treeNode00001);
  treeNode0000.addChild(treeNode00002);
  
  Subject subject0 = null;
  Subject subject1 = null;
  
//  try
//  {
    subject0
    	= signet.newSubject
    			("my_id0", "my_name0", "Test Subject", "subject0");
    subject1
    	= signet.newSubject
    			("my_id1", "my_name1", "Test Subject", "subject1");
//  }
//  catch (OperationNotSupportedException onse)
//  {
//    System.out.println
//    	("<<ERROR>> - tried to create a new Subject in a read-only"
//    	 + " Subject repository: "
//    	 + onse);
//  }
  
  subsystem0.setTree(tree00);
  
  signet.save(subsystem0);
  signet.save(subsystem1);
  signet.save(subsystem2);
  signet.save(subject0);
  signet.save(subject1);
  
  if ((treeNode000.isAncestorOf(treeNode00000))
      && !(treeNode00000.isAncestorOf(treeNode000)))
  {
    System.out.println("TreeNode.isAncestorOf works properly.");
  }
  else
  {
    System.out.println("<<ERROR>> - TreeNode.isAncestorOf FAILED");
  }
  
  if ((treeNode00000.isDescendantOf(treeNode000))
      && !(treeNode000.isDescendantOf(treeNode000)))
  {
    System.out.println("TreeNode.isDescendantOf works properly.");
  }
  else
  {
    System.out.println("<<ERROR>> - TreeNode.isDescendantOf FAILED");
  }
}
}
