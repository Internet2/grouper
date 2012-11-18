package edu.internet2.middleware.grouperStandardApi.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.authzStandardApiServer.interfaces.beans.groups.AsasApiGroup;
import edu.internet2.middleware.authzStandardApiServer.interfaces.beans.groups.AsasApiQueryParams;
import edu.internet2.middleware.authzStandardApiServer.interfaces.entity.AsasApiEntityLookup;
import edu.internet2.middleware.authzStandardApiServer.util.ExpirableCache;
import edu.internet2.middleware.authzStandardApiServer.util.StandardApiServerConfig;
import edu.internet2.middleware.authzStandardApiServer.util.StandardApiServerUtils;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.internal.dao.QuerySortField;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperStandardApi.config.GrouperAuthzApiServerConfig;
import edu.internet2.middleware.subject.Subject;


public class GrouperAuthzApiUtils {

  /**
   * successes cache
   */
  private static ExpirableCache<String, Subject> successesAuthnSubjectCache = null;
  
  /**
   * successes cache
   */
  private static ExpirableCache<String, Subject> successesAuthnSubjectCache() {
    
    //doesnt need synchronization.  I guess keep trying to init if not initted
    if (successesAuthnSubjectCache == null) {
      int successCacheMinutes = GrouperAuthzApiServerConfig.retrieveConfig().propertyValueInt(
          "grouperAuthzApiServer.loggedInSubject.cacheSubjectSuccessesForMinutes", -1);
      if (successCacheMinutes > 0) {
        successesAuthnSubjectCache = new ExpirableCache<String, Subject>(successCacheMinutes);
      }
    }
    return successesAuthnSubjectCache;
  }

  /**
   * failures cache
   */
  private static ExpirableCache<String, Boolean> failuresAuthnSubjectCache = null;
  
  /**
   * failures cache
   */
  private static ExpirableCache<String, Boolean> failuresAuthnSubjectCache() {
    
    //doesnt need synchronization.  I guess keep trying to init if not initted
    if (failuresAuthnSubjectCache == null) {
      int failuresCacheMinutes = GrouperAuthzApiServerConfig.retrieveConfig().propertyValueInt(
          "grouperAuthzApiServer.loggedInSubject.cacheSubjectFailuresForMinutes", -1);
      if (failuresCacheMinutes > 0) {
        failuresAuthnSubjectCache = new ExpirableCache<String, Boolean>(failuresCacheMinutes);
      }
    }
    return failuresAuthnSubjectCache;
  }

  /**
   * 
   * @param grouperGroups
   * @return the converted groups or null if none there
   */
  public static List<AsasApiGroup> convertToGroups(Collection<Group> grouperGroups) {

    List<AsasApiGroup> asasApiGroups = null;
    
    if (GrouperUtil.length(grouperGroups) > 0) {
      
      asasApiGroups = new ArrayList<AsasApiGroup>();
      for (Group grouperGroup : grouperGroups) {
        AsasApiGroup asasApiGroup = GrouperAuthzApiUtils.convertToGroup(grouperGroup);
        asasApiGroups.add(asasApiGroup);
      }
      
    }
    return asasApiGroups;
  }

  /**
   * 
   * @param group
   * @return the converted group
   */
  public static AsasApiGroup convertToGroup(Group group) {
    
    if (group == null) {
      return null;
    }
    
    AsasApiGroup asasApiGroup = new AsasApiGroup();
    asasApiGroup.setDescription(group.getDescription());
    asasApiGroup.setId(group.getId());
    
    asasApiGroup.setName(StandardApiServerUtils.convertPathToUseSeparatorAndEscape(group.getName(), ":", 
        StandardApiServerConfig.retrieveConfig().configItemPathSeparatorChar()));
    asasApiGroup.setDisplayName(StandardApiServerUtils.convertPathToUseSeparatorAndEscape(group.getDisplayName(), ":", 
        StandardApiServerConfig.retrieveConfig().configItemPathSeparatorChar()));
    
    asasApiGroup.setStatus("enabled");
    return asasApiGroup;
  }
  
  
  /**
   * 
   * @param asasApiPaging
   * @return query options
   */
  public static QueryOptions convertToQueryOptions(AsasApiQueryParams asasApiQueryParams) {
    
    if (asasApiQueryParams == null) {
      return null;
    }
    
    QueryOptions queryOptions = new QueryOptions();
    
    if (!StringUtils.isBlank(asasApiQueryParams.getSortField())) {
      
      boolean ascending = true;
      if (asasApiQueryParams.getAscending() != null) {
        ascending = asasApiQueryParams.getAscending();
      }
      if (ascending) {
        queryOptions.sortAsc(asasApiQueryParams.getSortField());
      } else {
        queryOptions.sortDesc(asasApiQueryParams.getSortField());
      }
    }
    
    if (asasApiQueryParams.getLimit() != null) {
      long offset = 0L;
      //calculate the page number
      int pageNumber = 1;
      if (asasApiQueryParams.getOffset() != null) {
        offset = asasApiQueryParams.getOffset();
        
        //this is 1 indexed
        pageNumber = 1 + (int)(offset / asasApiQueryParams.getLimit());
        
        //needs to be an exact page boundary...
        if ((pageNumber-1) * asasApiQueryParams.getLimit() != offset) {
          
          throw new RuntimeException("Querying for offset of " + offset 
              + " which is not a page boundary when limit is: " + asasApiQueryParams.getLimit()
              + ", pageNumber: " + pageNumber);
          
        }
        
      }
      boolean doTotalCount = false;
      if (asasApiQueryParams.getDoTotalCount() != null) {
        doTotalCount = asasApiQueryParams.getDoTotalCount();
      }
      queryOptions.paging((int)(long)asasApiQueryParams.getLimit(), 
          pageNumber, doTotalCount);
    }
    
    return queryOptions;
  }
  
  /**
   * get the subject from the authenticated subject object
   * @param authenticatedSubject
   * @return the subject or throw an exception if not found
   */
  public static Subject loggedInSubject(AsasApiEntityLookup authenticatedSubject) {
      
    if (authenticatedSubject == null) {
      throw new NullPointerException("Why is authenticatedSubject null?");
    }
  
    if (StringUtils.isBlank(authenticatedSubject.getLookupString())) {
      throw new NullPointerException("Why is there no logged in subject???");
    }
    
    String subjectString = authenticatedSubject.getLookupString();
    
    ExpirableCache<String, Subject> successCache = successesAuthnSubjectCache();
    
    Subject loggedInSubject = successCache == null ? null : successCache.get(subjectString);
    
    if (loggedInSubject != null) {
      return loggedInSubject;
    }
    
    ExpirableCache<String, Boolean> failureCache = failuresAuthnSubjectCache();
    
    Boolean hadFailure = failureCache == null ? null : failureCache.get(subjectString);
    
    Subject subject = null;
    
    String additionalErrorMessage = " had trouble being resolved";
    
    // if we dont know that it is not resolvable or not in a group
    if (hadFailure == null) {
    
      //lets resolve
      String lookupBy = GrouperAuthzApiServerConfig.retrieveConfig().propertyValueString("grouperAuthzApiServer.loggedInSubject.lookupBy", "subjectIdOrIdentifier");
      
      String sourceId = GrouperAuthzApiServerConfig.retrieveConfig().propertyValueString("grouperAuthzApiServer.loggedInSubject.sourceId", "subjectIdOrIdentifier");
      
      if (StringUtils.equalsIgnoreCase("subjectId", lookupBy)) {
        
        if (!StringUtils.isBlank(sourceId)) {
          subject = SubjectFinder.findByIdAndSource(subjectString, sourceId, false);        
        } else {
          subject = SubjectFinder.findById(subjectString, false);        
        }
        
      } else if (StringUtils.equalsIgnoreCase("subjectIdentifier", lookupBy)) {
        
        if (!StringUtils.isBlank(sourceId)) {
          subject = SubjectFinder.findByIdentifierAndSource(subjectString, sourceId, false);        
        } else {
          subject = SubjectFinder.findByIdentifier(subjectString, false);        
        }
        
        
      } else if (StringUtils.equalsIgnoreCase("subjectIdOrIdentifier", lookupBy) || StringUtils.isBlank(lookupBy)) {
        
        if (!StringUtils.isBlank(sourceId)) {
          subject = SubjectFinder.findByIdOrIdentifierAndSource(subjectString, sourceId, false);        
        } else {
          subject = SubjectFinder.findByIdOrIdentifier(subjectString, false);        
        }
        
      } else {
        throw new RuntimeException("Not expecting value: '" + lookupBy + "', expecting subjectId, subjectIdentifier, or subjectIdOrIdentifier");
      }
      
      //see if we are checking a group
      if (subject != null) {
        
        final String requireGroup = GrouperAuthzApiServerConfig.retrieveConfig().propertyValueString(
            "grouperAuthzApiServer.loggedInSubject.requireInGroup");
        
        if (!StringUtils.isBlank(requireGroup)) {
          
          GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
          boolean startedSession = grouperSession == null;
          grouperSession = grouperSession == null ? GrouperSession.startRootSession() : grouperSession.internal_getRootSession();
          try {
            
            final Subject SUBJECT = subject;
            
            boolean hasMember = (Boolean)GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {
              
              @Override
              public Object callback(GrouperSession grouperSessionAdmin) throws GrouperSessionException {
                
                Group group = GroupFinder.findByName(grouperSessionAdmin, requireGroup, true);
                
                return group.hasMember(SUBJECT);
                
              }
            });
            
            if (!hasMember) {
              additionalErrorMessage = ", subject '" + GrouperUtil.subjectToString(subject) + "' is not in group: " + requireGroup;
              subject = null;
            }
            
          } finally {
            if (startedSession) {
              GrouperSession.stopQuietly(grouperSession);
            }
          }
        }        
      }
      
      //add to cache
      if (subject == null && failureCache != null) {
        failureCache.put(subjectString, true);
      }
      
      if (subject != null && successCache != null) {
        successCache.put(subjectString, subject);
      }
      
    }
    
    if (subject == null) {
      throw new RuntimeException("Subject: '" + subjectString 
          + additionalErrorMessage);
    }
    return subject;
  }

  /**
   * 
   * @param queryOptions
   * @return query params
   */
  public static AsasApiQueryParams convertToQueryParams(QueryOptions queryOptions) {
    
    if (queryOptions == null) {
      return null;
    }
    
    AsasApiQueryParams asasApiQueryParams = new AsasApiQueryParams();
    
    if (queryOptions.getQuerySort() != null 
        && GrouperUtil.length(queryOptions.getQuerySort().getQuerySortFields()) > 0) {
      
      //not sure if we should get the first or last, oh well, get the first
      QuerySortField querySortField = queryOptions.getQuerySort().getQuerySortFields().get(0);
      
      asasApiQueryParams.setAscending(querySortField.isAscending());
      asasApiQueryParams.setSortField(querySortField.getColumn());
      
    }

    QueryPaging queryPaging = queryOptions.getQueryPaging();
    
    if (queryPaging != null) {
      
      asasApiQueryParams.setLimit(Long.valueOf(queryPaging.getPageSize()));
      
      //note, this is calculated in the queryPaging object, but lets just calculate here
      asasApiQueryParams.setOffset(Long.valueOf((queryPaging.getPageNumber()-1) * queryPaging.getPageSize()));

      if (queryPaging.isDoTotalCount()) {
        asasApiQueryParams.setDoTotalCount(true);
        asasApiQueryParams.setTotalCount(Long.valueOf(queryPaging.getTotalRecordCount()));
      } else {
        asasApiQueryParams.setDoTotalCount(false);
      }
      
    }
    
    return asasApiQueryParams;
  }
  
}
