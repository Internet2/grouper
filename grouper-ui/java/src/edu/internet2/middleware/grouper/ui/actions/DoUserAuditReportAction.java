/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2005 The University Of Bristol

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

package edu.internet2.middleware.grouper.ui.actions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.UserAuditQuery;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.GroupOrStem;
import edu.internet2.middleware.grouper.ui.util.CollectionPager;
import edu.internet2.middleware.grouper.ui.util.ObjectAsMap;
import edu.internet2.middleware.subject.Subject;

/**
 * Top level Strut's action which displays audit log entries. 
 * <p/>
<table width="75%" border="1">
  <tr bgcolor="#CCCCCC"> 
    <td width="51%"><strong><font face="Arial, Helvetica, sans-serif">Request 
      Parameter</font></strong></td>
    <td width="12%"><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td width="37%"><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
   <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">schemaChangesOnly</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">true/false - if true show audit entries for all groupType nad field changes</font></td>
  </tr>
  <tr> 
    <td><p><font face="Arial, Helvetica, sans-serif">groupId</font></p></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">If present show audit entries 
      for actions on group</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">stemId</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">If present show audit entries 
      for actions on stem</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">memberId</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">If present show audit entries 
      for actions on member</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">subjectId</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">If present show audit entries 
      for actions on subject</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">subjectType</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Helps uniquely identify subject</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">sourceId</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Helps uniquely identify subject</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">filterType</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">If subjectId or memberId specifies 
      whether to show actions performed by entity, membership changes or privilege 
      changes for the entity</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">sort</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Most recent first/date order</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">dateQualifier</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">on, after, before, between</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">date1</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN/OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Defaults to 7 days ago - or 
      value of audit.query.default-since in media.properties</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">date2</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Only used if 'between' selected 
      as dateQualifier</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">callerPageId</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Id for current page - so that 
      can return to this page if user follows subject/group summary link</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">origCallerPageId</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Id for page which had link to 
      the audit log</font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">pageSize</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">How many entries to show per 
      page </font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">start</font></td>
    <td><font face="Arial, Helvetica, sans-serif">IN</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Show entries starting from this 
      position </font></td>
  </tr>
  <tr> 
    <td><font face="Arial, Helvetica, sans-serif">&nbsp;</font></td>
    <td><font face="Arial, Helvetica, sans-serif">&nbsp;</font></td>
    <td><font face="Arial, Helvetica, sans-serif">&nbsp;</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Request Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">title=audit.query.title</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Key resolved in nav ResourceBundle 
      </font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">auditInfoKey</font></td>
    <td>OUT</td>
    <td><font face="Arial, Helvetica, sans-serif">Key resolved in nav ResourceBundle 
      </font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">auditInfoEntity</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Key resolved in nav ResourceBundle 
      </font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">forceCallerPageId </font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Allows return to page before 
      audit log</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">pager</font></td>
    <td><font face="Arial, Helvetica, sans-serif">OUT</font></td>
    <td><font face="Arial, Helvetica, sans-serif">Pages audit log entries</font></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td><font face="Arial, Helvetica, sans-serif">&nbsp;</font></td>
    <td><font face="Arial, Helvetica, sans-serif">&nbsp;</font></td>
    <td><font face="Arial, Helvetica, sans-serif">&nbsp;</font></td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Session Attribute</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr bgcolor="#FFFFFF"> 
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
  </tr>
  <tr bgcolor="#CCCCCC"> 
    <td><strong><font face="Arial, Helvetica, sans-serif">Strut's Action Parameter</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Direction</font></strong></td>
    <td><strong><font face="Arial, Helvetica, sans-serif">Description</font></strong></td>
  </tr>
  <tr> 
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
  </tr>
</table>

 * @author Gary Brown.
 * @version $Id: DoUserAuditReportAction.java,v 1.6 2009-10-01 13:43:13 isgwb Exp $
 */
public class DoUserAuditReportAction extends GrouperCapableAction {

	//------------------------------------------------------------ Local
	// Forwards
	static final private String FORWARD_AuditQuery = "AuditQuery";

	//------------------------------------------------------------ Action
	// Methods

	public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, GrouperSession grouperSession)
			throws Exception {

          boolean isRoot = grouperSession.getSubject().equals(SubjectFinder.findRootSubject())
            || Boolean.TRUE.equals(request.getSession().getAttribute("activeWheelGroupMember"));

		request.setAttribute("title", "audit.query.title");
    session.setAttribute("subtitle","");
		
        DynaActionForm auditForm = (DynaActionForm) form;
        saveAsCallerPage(request,auditForm);
        Boolean schemaOnly = (Boolean)auditForm.get("schemaChangesOnly");
        if(schemaOnly==null) {
        	schemaOnly=Boolean.FALSE;
        }
        String displayDateFormat = null;
        
        try {
        	displayDateFormat = GrouperUiFilter.retrieveSessionMediaResourceBundle().getString("audit.query.display-date-format");
        }catch(Exception e) {
        	
        }
        String groupTypeId=auditForm.getString("groupTypeId");
        String groupId=auditForm.getString("groupId");
        String stemId=auditForm.getString("stemId");
        String memberId=auditForm.getString("memberId");
        String subjectId=auditForm.getString("subjectId");
        String subjectType=auditForm.getString("subjectType");
        String sourceId=auditForm.getString("sourceId");
        String sort=auditForm.getString("sort");
        String origCallerPageId=auditForm.getString("origCallerPageId");
        String filterType=auditForm.getString("filterType");
        String dateQualifier=auditForm.getString("dateQualifier");
        String date1=auditForm.getString("date1");
        String date2=auditForm.getString("date2");
        Boolean extendedResults=(Boolean)auditForm.get("extendedResults");
        Boolean viaForm=(Boolean)auditForm.get("viaForm");
        if(viaForm==null) {
        	viaForm=Boolean.FALSE;
        }
        if(extendedResults==null) {
        	if(!viaForm) {
        		extendedResults = (Boolean)session.getAttribute("extendedResults");
        	}
        	if(extendedResults==null) {
        		extendedResults=Boolean.FALSE;
        	}
        }
        session.setAttribute("extendedResults",extendedResults);
        auditForm.set("extendedResults", extendedResults);
        ResourceBundle mediaBundle = GrouperUiFilter.retrieveSessionMediaResourceBundle();
        boolean isEnabled = false;
        String dateFormat="mm/dd/yyyy";
        int noDays = 7;
        try {
        	isEnabled = "true".equals(mediaBundle.getString("audit.query.enabled"));
        }catch (MissingResourceException e) {
        	
        }

        try {
        	dateFormat = mediaBundle.getString("audit.query.date-format");
        }catch (MissingResourceException e) {
        	
        }
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        
        try {
        	noDays = Integer.parseInt(mediaBundle.getString("audit.query.default-since"));
        }catch (MissingResourceException e) {
        	
        }catch(NumberFormatException e) {
        	
        }
        
        if(!isEmpty(subjectId) && isEmpty(memberId)) {
        	Subject subj=null;
        	if(!isEmpty(subjectType) && !isEmpty(sourceId)) {
        		subj = SubjectFinder.findById(subjectId, subjectType, sourceId,true);
        	}else{
        		subj = SubjectFinder.findById(subjectId,true);
        	}
        	Member member = MemberFinder.findBySubject(grouperSession, subj,false);
        	memberId=member.getUuid();
        }
        UserAuditQuery query = new UserAuditQuery();
        QueryOptions options = new QueryOptions();
        Date startDate = null;
        Date endDate = null;
        try {
        	if(!isEmpty(date1)) {
        		startDate = sdf.parse(date1);
        	}
        }catch(Exception e) {
        	
        }
        
        try {
        	if(!isEmpty(date2)) {
        		startDate = sdf.parse(date2);
        	}
        }catch(Exception e) {
        	
        }
        
        if(startDate==null && endDate==null) {
        	Calendar cal = Calendar.getInstance();
        	cal.add(Calendar.DAY_OF_YEAR, -noDays);
        	cal.set(Calendar.HOUR, 0);
        	cal.set(Calendar.MINUTE,0);
        	cal.set(Calendar.SECOND,0);
        	startDate = cal.getTime();
        	auditForm.set("date1", sdf.format(startDate));
        	auditForm.set("dateQualifier","after"); 
        	dateQualifier="after";
        	
        }
        
        if("on".equals(dateQualifier) || startDate.equals(endDate)) {
        	query.setOnDate(startDate);
        }else {
        	if("before".equals(dateQualifier)) {
        		query.setToDate(startDate);
        	}else if("after".equals(dateQualifier) || "between".equals(dateQualifier)) {
        		query.setFromDate(startDate);
        	}
        	
        	if("between".equals(dateQualifier) && endDate!=null) {
        		query.setToDate(endDate);
        	}
        }
        
        
        String infoKey = null;
        String entity = null;
        query.setQueryOptions(options);
        List<AuditEntry> results = null;
        Map privs = null;
        if (!isEmpty(groupId)) {
          privs = GrouperHelper.hasAsMap(grouperSession, GroupOrStem.findByGroup(grouperSession, GroupFinder.findByUuid(grouperSession, groupId)));
        } else if (!isEmpty(stemId)) {
          privs = GrouperHelper.hasAsMap(grouperSession, GroupOrStem.findByStem(grouperSession, StemFinder.findByUuid(grouperSession, stemId)));
        }

        if(schemaOnly) {
			query=query.addAuditTypeCategory("groupType").addAuditTypeCategory("groupField");
			if(!isEmpty(groupTypeId)) {
				query=query.addAuditTypeFieldValue("groupTypeId", groupTypeId);
			}
			infoKey ="audit.query.info.schema";
			entity = "";
		}else if(!isEmpty(groupId) && privs.containsKey("admin")) {
			query=query.addAuditTypeFieldValue("groupId", groupId);
			infoKey ="audit.query.info.actions-on";
			Group group = GroupFinder.findByUuid(grouperSession, groupId,true);
			entity = group.getDisplayExtension();
		}else if(!isEmpty(stemId) && (privs.containsKey("create") || privs.containsKey("stem"))) {
			query=query.addAuditTypeFieldValue("stemId", stemId);
			infoKey ="audit.query.info.actions-on";
			Stem stem = StemFinder.findByUuid(grouperSession, stemId,true);
			entity = stem.getDisplayExtension();
		}else if(!isEmpty(memberId) && isRoot) {
			Member member=MemberFinder.findByUuid(grouperSession, memberId,false);
			if(filterType.equals("memberships")) {
			    query=query.addAuditTypeCategory("membership").addAuditTypeFieldValue("memberId", memberId);
			    infoKey="audit.query.info.membership";
			}else if(filterType.equals("actions")) {
				query=query.loggedInMember(member);
				query=query.actAsMember(member);
				infoKey="audit.query.info.actions-by";
			}else if(filterType.equals("privileges")) {
				query=query.addAuditTypeCategory("privilege").addAuditTypeFieldValue("memberId", memberId);
			    infoKey="audit.query.info.privilege";
			}
			entity = GrouperHelper.getMemberDisplayValue(member, GrouperUiFilter.retrieveSessionMediaResourceBundle());
		} else {
                  throw new RuntimeException("Unexpected.  Invalid URL for user audit logs or insufficient privileges.");
                }
		
		request.setAttribute("auditInfoKey",infoKey);
		request.setAttribute("auditInfoEntity", entity);
		
		
		//Set up CollectionPager for view
	    String startStr = request.getParameter("start");
	    if (startStr == null || "".equals(startStr))
	      startStr = "0";

	    int start = Integer.parseInt(startStr);
	    if(start <0) {
	    	start=0;
	    }
	    
	    int pageSize = getPageSize(session);
	    
        //QueryPaging paging = new QueryPaging();
        //paging.setPageSize(getPageSize(session));
        //paging.setDoTotalCount(true);
        //paging.setCacheTotalCount(true);
        //options.paging(paging);
	    int page= (int) ((start/pageSize)+1);
	    options.paging(pageSize,page,true);
	    if(!"asc".equals(sort) && !"desc".equals(sort)) {
	    	sort="desc";
	    }
	    if("asc".equals(sort)) {
	    	options.sortAsc("lastUpdatedDb");
	    }else{
	    	options.sortDesc("lastUpdatedDb");
	    }
		results=query.execute();
		
		int count = options.getQueryPaging().getTotalRecordCount();
		
		
	   
	    int end = start + pageSize;
	    List<ObjectAsMap> resultsAsMaps = new ArrayList<ObjectAsMap>();
	    ObjectAsMap auditMap;
	    for (AuditEntry entry : results) {
	    	auditMap=ObjectAsMap.getInstance("AuditEntryAsMap", entry,grouperSession);
	    	if(!isEmpty(displayDateFormat)) {
	    		auditMap.setDateFormat(displayDateFormat);
	    	}
			resultsAsMaps.add(auditMap);
		}
	    
	    CollectionPager pager = new CollectionPager(null, resultsAsMaps,count,
				null, start, null, pageSize);
		pager.setParam("groupId", groupId);
		pager.setTarget(mapping.getPath());
		pager.setParams(auditForm.getMap());
		request.setAttribute("pager", pager);
		
		request.setAttribute("forceCallerPageId",origCallerPageId);
		
		return mapping.findForward(FORWARD_AuditQuery);

	}

}
