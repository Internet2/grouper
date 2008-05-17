/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/dbpersist/HibernateQry.java,v 1.2 2008-05-17 20:54:09 ddonn Exp $

Copyright (c) 2007 Internet2, Stanford University

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
package edu.internet2.middleware.signet.dbpersist;

import edu.internet2.middleware.signet.AssignmentImpl;
import edu.internet2.middleware.signet.FunctionImpl;
import edu.internet2.middleware.signet.LimitImpl;
import edu.internet2.middleware.signet.PermissionImpl;
import edu.internet2.middleware.signet.ProxyImpl;
import edu.internet2.middleware.signet.SubsystemImpl;
import edu.internet2.middleware.signet.TreeImpl;
import edu.internet2.middleware.signet.TreeNodeRelationship;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;
import edu.internet2.middleware.signet.subjsrc.SignetSubjectAttr;

/**
 * HibernateQry - A container for the static Hibernate DB queries
 */
public class HibernateQry
{
	/** All proxies granted by the given grantor */
	static final String	Qry_proxiesGrantedAll =
			"from " + ProxyImpl.class.getName() +		//$NON-NLS-1$
			" as proxy " + 								//$NON-NLS-1$
			" where grantorKey = :grantorKey ";			//$NON-NLS-1$

	/** All proxies granted by the given grantor and the given status */
	static final String	Qry_proxiesGranted =
			Qry_proxiesGrantedAll +
			" and " +									//$NON-NLS-1$
			" status = :status ";						//$NON-NLS-1$

	/** All proxies granted to the given grantee */
	static final String	Qry_proxiesReceivedAll =
			"from " + ProxyImpl.class.getName() +		//$NON-NLS-1$
			" as proxy " +								//$NON-NLS-1$
			" where granteeKey = :granteeKey ";			//$NON-NLS-1$

	/** All proxies granted to the given grantee and the given status */
	static final String	Qry_proxiesReceived =
			Qry_proxiesReceivedAll + 
			" and " +									//$NON-NLS-1$
			" status = :status ";						//$NON-NLS-1$


	/** All assignments */
	static final String	Qry_assignmentsAll =
			"from " +									//$NON-NLS-1$
			AssignmentImpl.class.getName() +
			" as assignment ";							//$NON-NLS-1$

	/** All assignments having the given status */
	static final String	Qry_assignmentsAllByStatus =
			Qry_assignmentsAll +
			" where assignment.status = :status ";		//$NON-NLS-1$

	/** All assignments granted by the given grantor */
	static final String	Qry_assignmentsGrantedAll =
			Qry_assignmentsAll +
			" where grantorKey = :grantorKey ";			//$NON-NLS-1$

	/** All assignments granted by the given grantor with the given status */
	static final String	Qry_assignmentsGranted =
			Qry_assignmentsGrantedAll +
			" and " +									//$NON-NLS-1$
			" status = :status ";						//$NON-NLS-1$

	/** All assignments received by the given grantee */
	static final String	Qry_assignmentsReceivedAll =
			Qry_assignmentsAll +
			" where granteeKey = :granteeKey ";			//$NON-NLS-1$

	/** All assignments received by the given grantee with the given status */
	static final String	Qry_assignmentsReceived =
			Qry_assignmentsReceivedAll +
			" and " +									//$NON-NLS-1$
			" status = :status ";						//$NON-NLS-1$

	/** All assignments whose functions are within the given subsystem */
	static final String	Qry_assignmentsBySubsystemAll = 
			Qry_assignmentsAll +
			" where assignment.function.key in " +				//$NON-NLS-1$
			" (select function.id from " +						//$NON-NLS-1$
			FunctionImpl.class.getName() + " as function " +	//$NON-NLS-1$
			" where function.subsystem.id = :subsysId) ";		//$NON-NLS-1$

	/** All assignments whose functions are within the given subsystem and having the given status */
	static final String	Qry_assignmentsBySubsystem = 
			Qry_assignmentsBySubsystemAll +
			" and " +									//$NON-NLS-1$
			" assignment.status = :status ";			//$NON-NLS-1$

	/** All assignments for the given function and subsystem */
	static final String	Qry_assignmentsByFunctionAll = 
			Qry_assignmentsAll +
			" where assignment.function.funcId = :functionId " +//$NON-NLS-1$
			" and " +											//$NON-NLS_1$
			" assignment.function.subsystem.id = :subsysId ";	//$NON-NLS-1$

	/** All assignments for the given function and subsystem having the given status */
	static final String	Qry_assignmentsByFunction = 
			Qry_assignmentsByFunctionAll +
			" and " +									//$NON-NLS-1$
			" assignment.status = :status ";			//$NON-NLS-1$

	/** All assignments for the given scopeId and scope-nodeId */
	static final String Qry_assignmentsByScopeAll =
			Qry_assignmentsAll +
			" where assignment.scope.fullyQualifiedId.treeId = :scopeId " +	//$NON-NLS-1$
			" and " +									//$NON-NLS-1$
			" assignment.scope.fullyQualifiedId.treeNodeId = :nodeId ";		//$NON-NLS-1$

	/** All assignments for the given scopeId and scope-nodeId and having the given status */
	static final String Qry_assignmentsByScope =
			Qry_assignmentsByScopeAll +
			" and " +									//$NON-NLS-1$
			" assignment.status = :status ";			//$NON-NLS-1$

	/** All assignments for the given grantee, function, scopetree, treenode, and assignmentId */
	static final String Qry_assignmentDuplicates =
			Qry_assignmentsAll +
			" where granteeKey = :granteeKey " +		//$NON-NLS-1$
			" and functionKey = :functionKey " +		//$NON-NLS-1$
			" and scopeID = :scopeId " +				//$NON-NLS-1$
			" and scopeNodeID = :scopeNodeId " +		//$NON-NLS-1$
			" and assignmentID != :assignmentId ";		//$NON-NLS-1$


	/** All subjects */
	static final String Qry_subjectAll =
			"from " +									//$NON-NLS-1$
			SignetSubject.class.getName() +
			" as subj ";								//$NON-NLS-1$

	/** One subject by primary key */
	static final String	Qry_subjectByPK =
			Qry_subjectAll +
			" where " +									//$NON-NLS-1$
			" subjectkey = :subjectkey ";				//$NON-NLS-1$

	/** All subjects for the given sourceId */
	static final String Qry_subjBySrc =
			Qry_subjectAll +
			" where " +									//$NON-NLS-1$
			" sourceID = :source_id ";					//$NON-NLS-1$

	/** All subjects for the given sourceId and subjectId*/
	static final String Qry_subjByIdSrc =
			Qry_subjBySrc +
			" and " +									//$NON-NLS-1$
			" subjectID = :subject_id ";				//$NON-NLS-1$

	/** All subjects whose attribute "subjAuthId" matches the given identifier */
	static final String Qry_subjectById =
			"from " +										//$NON-NLS-1$
			SignetSubject.class.getName() + " subj, " +		//$NON-NLS-1$
			SignetSubjectAttr.class.getName() + " attr " +	//$NON-NLS-1$
			" where " + 									//$NON-NLS-1$
			" attr.attrValue = :subjIdentifier " +			//$NON-NLS-1$
			" and " + 										//$NON-NLS-1$
			" attr.mappedName = 'subjectAuthId' " +			//$NON-NLS-1$
			" and " + 										//$NON-NLS-1$
			" attr.parent.subject_PK = subj.subject_PK " +	//$NON-NLS-1$
			" order by " + 									//$NON-NLS-1$
			" attr.parent.subject_PK, " + 					//$NON-NLS-1$
			" attr.mappedName, " +							//$NON-NLS-1$
			" attr.sequence ";								//$NON-NLS-1$

	static final String Qry_subjectByAttrValue =
			"from " +										//$NON-NLS-1$
			SignetSubject.class.getName() + " subj, " +		//$NON-NLS-1$
			SignetSubjectAttr.class.getName() + " attr " +	//$NON-NLS-1$
			" where " +										//$NON-NLS-1$
			" attr.attrValue = :attrValue " + 				//$NON-NLS-1$
			" and " + 										//$NON-NLS-1$
			" attr.mappedName = :attrName " +				//$NON-NLS-1$
			" and " +										//$NON-NLS-1$
			" attr.parent.subject_PK = subj.subject_PK ";	//$NON-NLS-1$

//	static final String Qry_subjectByAttrValue =
//			"from " +										//$NON-NLS-1$
//			SignetSubject.class.getName() + " subj, " +		//$NON-NLS-1$
//			SignetSubjectAttr.class.getName() + " attr " +	//$NON-NLS-1$
//			" where " +										//$NON-NLS-1$
//			" subj.subject_PK "	+ //$NON-NLS-1$
//			" in " + 
//			" (select attr.parent.subject_PK " +
//			" from " + 
//			SignetSubjectAttr.class.getName() + " attr " +
//			" where " +
//			" attr.mappedName = :attrName " +				//$NON-NLS-1$
//			" and " +										//$NON-NLS-1$
//			" attr.attrValue = :attrValue " + 				//$NON-NLS-1$
//			" ) ";								//$NON-NLS-1$

			
	/** All functions */
	static final String Qry_functionAll =
			"from " +									//$NON-NLS-1$
			FunctionImpl.class.getName() +
			" as function ";							//$NON-NLS-1$

	/** All functions for the given functionId and subsystemId */
	static final String Qry_functionByIdAndSubsys =
			Qry_functionAll +
			" where " +									//$NON-NLS-1$
			" functionID = :function_id " +				//$NON-NLS-1$
			" and " +									//$NON-NLS-1$
			" subsystemID = :subsys_id ";				//$NON-NLS-1$


	/** All limits */
	static final String Qry_limitAll =
			"from " +									//$NON-NLS-1$
			LimitImpl.class.getName() +
			" as limit ";								//$NON-NLS-1$

	/** One limit by primary key */
	static final String Qry_limitByPK =
			Qry_limitAll +
			" where " +									//$NON-NLS-1$
			" limitKey = :limit_key";					//$NON-NLS-1$

	/** All limits for the given subsystem */
	static final String Qry_limitBySubsys =
			Qry_limitAll +
			" where " +									//$NON-NLS-1$
			" subsystemID = :subsystemId";				//$NON-NLS-1$

	/** All limits for the given subsystem and limitId */
	static final String Qry_limitBySubsysAndId =
			Qry_limitBySubsys +
			" and " +									//$NON-NLS-1$
			" limitID = :limitId";						//$NON-NLS-1$


	/** All scopetrees */
	static final String Qry_treesAll =
			"from " +									//$NON-NLS-1$
			TreeImpl.class.getName() +
			" as treeImpl ";							//$NON-NLS-1$

	/** All scopetrees for the given treeId */
	static final String Qry_treeById =
			Qry_treesAll +
			" where treeID = :treeId";					//$NON-NLS-1$

	/** This is a stub and cannot be used directly for queries */
	static final String Qry_treeBySubsys_STUB =
			Qry_treesAll +
			" where treeID = " +						//$NON-NLS-1$
			" (select treeID from " +					//$NON-NLS-1$
			  SubsystemImpl.class.getName() +
			  " as subsysImpl ";						//$NON-NLS-1$

	/** One tree matching the given subsystem Id */
	static final String Qry_treeBySubsysId =
			Qry_treeBySubsys_STUB +
			  " where subsystemID = :subsystemID " +	//$NON-NLS-1$
			  " ) ";									//$NON-NLS-1$

	/** One tree matching the given subsystem name */
	static final String Qry_treeBySubsysName =
			Qry_treeBySubsys_STUB +
			  " where name = :name " +					//$NON-NLS-1$
			  " ) ";									//$NON-NLS-1$


	/** All treeNodeRelationships */
	static final String Qry_treeNodeRelationsAll =
			"from " +									//$NON-NLS-1$
			TreeNodeRelationship.class.getName() +
			" as treeNodeRelationship ";				//$NON-NLS-1$

	/** All treeNodeRelationships for the given treeId and matching the given child node id */
	static final String Qry_treeNodeParents =
			Qry_treeNodeRelationsAll +
			" where treeID = :treeId" +					//$NON-NLS-1$
			" and nodeID = :childNodeId";				//$NON-NLS-1$

	/** All treeNodeRelationships for the given treeId and matching the given parend node id */
	static final String Qry_treeNodeChildren =
			Qry_treeNodeRelationsAll +
			" where treeID = :treeId" +					//$NON-NLS-1$
			" and parentNodeID = :parentNodeId";		//$NON-NLS-1$


	/** All subsystems */
	static final String Qry_subsystemsAll =
			"from " +									//$NON-NLS-1$
			SubsystemImpl.class.getName() +
			" as subsystem ";							//$NON-NLS-1$

	/** This is a stub and cannot be used directly for queries */
	static final String Qry_subsystemByStatus_STUB =
			" and subsystem.status = :status ";			//$NON-NLS-1$

	/** All subsystems with the given status */
	static final String Qry_subsystemByStatus =
			Qry_subsystemsAll +
			" where subsystem.status = :status ";		//$NON-NLS-1$

	/** All subsystems with the given name */
	static final String Qry_subsystemByName =
			Qry_subsystemsAll +
			" where subsystem.name = :name ";			//$NON-NLS-1$

	/** All subsystems with the given subsystemId */
	static final String Qry_subsystemById =
			Qry_subsystemsAll +
			" where subsystem.subsystemID = :subsystemid "; //$NON-NLS-1$

	/** All subsystems matching the given scopetree id */
	static final String Qry_subsystemByScopeTreeId =
			Qry_subsystemsAll +
			" where subsystem.tree.id = :scopetreeid "; //$NON-NLS-1$
//			" where subsystem.scopetreeID = :scopetreeid "; //$NON-NLS-1$


	/** All permissions */
	static final String Qry_permissionAll =
			"from " +									//$NON-NLS-1$
			PermissionImpl.class.getName() +
			" as permission ";							//$NON-NLS-1$

	/** All permissions having the given subsystem id */
	static final String Qry_permissionBySubsys =
			Qry_permissionAll +
			" where subsystemID = :subsystemId";		//$NON-NLS-1$


}
