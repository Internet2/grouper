/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.identity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kim.bo.entity.dto.KimEntityDefaultInfo;
import org.kuali.rice.kim.bo.entity.dto.KimEntityInfo;
import org.kuali.rice.kim.bo.entity.dto.KimEntityNameInfo;
import org.kuali.rice.kim.bo.entity.dto.KimEntityNamePrincipalNameInfo;
import org.kuali.rice.kim.bo.entity.dto.KimEntityPrivacyPreferencesInfo;
import org.kuali.rice.kim.bo.entity.dto.KimPrincipalInfo;
import org.kuali.rice.kim.bo.reference.dto.AddressTypeInfo;
import org.kuali.rice.kim.bo.reference.dto.AffiliationTypeInfo;
import org.kuali.rice.kim.bo.reference.dto.CitizenshipStatusInfo;
import org.kuali.rice.kim.bo.reference.dto.EmailTypeInfo;
import org.kuali.rice.kim.bo.reference.dto.EmploymentStatusInfo;
import org.kuali.rice.kim.bo.reference.dto.EmploymentTypeInfo;
import org.kuali.rice.kim.bo.reference.dto.EntityNameTypeInfo;
import org.kuali.rice.kim.bo.reference.dto.EntityTypeInfo;
import org.kuali.rice.kim.bo.reference.dto.ExternalIdentifierTypeInfo;
import org.kuali.rice.kim.bo.reference.dto.PhoneTypeInfo;
import org.kuali.rice.kim.service.IdentityService;

import edu.internet2.middleware.grouperClient.api.GcGetSubjects;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperKimConnector.util.GrouperKimUtils;


/**
 * Implements the Kuali identity service to delegate to Grouper
 */
public class GrouperKimIdentityServiceImpl implements IdentityService {

  /**
   * logger
   */
  private static final Log LOG = GrouperClientUtils.retrieveLog(GrouperKimIdentityServiceImpl.class);

  /**
   * Gets the address type for the given address type code.
   * @see org.kuali.rice.kim.service.IdentityService#getAddressType(java.lang.String)
   */
  public AddressTypeInfo getAddressType(String code) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "getAddressType");
    debugMap.put("code", code);
    debugMap.put("result", "null");
    if (LOG.isDebugEnabled()) {
      LOG.debug(GrouperKimUtils.mapForLog(debugMap));
    }
    return null;
  }

  /**
   * Gets the affiliation type for the given affiliation type code.
   * @see org.kuali.rice.kim.service.IdentityService#getAffiliationType(java.lang.String)
   */
  public AffiliationTypeInfo getAffiliationType(String code) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "getAffiliationType");
    debugMap.put("code", code);
    debugMap.put("result", "null");
    if (LOG.isDebugEnabled()) {
      LOG.debug(GrouperKimUtils.mapForLog(debugMap));
    }
    return null;
  }

  /**
   * Gets the citizenship status for the given citizenship status code.
   * @see org.kuali.rice.kim.service.IdentityService#getCitizenshipStatus(java.lang.String)
   */
  public CitizenshipStatusInfo getCitizenshipStatus(String code) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "getCitizenshipStatus");
    debugMap.put("code", code);
    debugMap.put("result", "null");
    if (LOG.isDebugEnabled()) {
      LOG.debug(GrouperKimUtils.mapForLog(debugMap));
    }
    return null;
  }

  /**
   * Gets the names for the entities with ids in the given list.
   * @see org.kuali.rice.kim.service.IdentityService#getDefaultNamesForEntityIds(java.util.List)
   */
  public Map<String, KimEntityNameInfo> getDefaultNamesForEntityIds(List<String> entityIds) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "getDefaultNamesForEntityIds");
    int entityIdsSize = GrouperClientUtils.length(entityIds);
    debugMap.put("entityIds.size", entityIdsSize);
    Map<String, KimEntityNameInfo> result = new LinkedHashMap<String, KimEntityNameInfo>();
    boolean hadException = false;

    try {
    
      if (entityIdsSize == 0) {
        return result;
      }
      
      int index = 0;

      //log some of these
      for (String entityId : entityIds) {
        
        //dont log all...
        if (index > 20) {
          break;
        }
        
        entityId = GrouperKimUtils.translateEntityId(entityId);
        debugMap.put("entityIds." + index, entityId);


        index++;
      }


      GcGetSubjects gcGetSubjects = new GcGetSubjects();
      
      for (String entityId : entityIds) {
        entityId = GrouperKimUtils.translateEntityId(entityId);

        gcGetSubjects.addWsSubjectLookup(new WsSubjectLookup(entityId,null, null));
        
      }
      
      gcGetSubjects.assignIncludeSubjectDetail(true);
      
      WsGetSubjectsResults wsGetSubjectsResults = gcGetSubjects.execute();
      
      //we did one assignment, we have one result
      WsSubject[] wsSubjects = wsGetSubjectsResults.getWsSubjects();
      
      debugMap.put("resultNumberOfSubjects", GrouperClientUtils.length(wsSubjects));
      
            //map of subject id to ws subject object just to make sure they are in order
      Map<String, WsSubject> wsSubjectMap = new LinkedHashMap<String, WsSubject>();

      for (WsSubject wsSubject : GrouperClientUtils.nonNull(wsSubjects, WsSubject.class)) {
        wsSubjectMap.put(wsSubject.getId(), wsSubject);
      }

      index = 0;
      for (String entityId : entityIds) {
        
        WsSubject wsSubject = wsSubjectMap.get(entityId);
        
        if (wsSubject == null) {
          throw new RuntimeException("Cant find subject for entity: " + entityId);
        }
        
        String subjectId = wsSubject.getId();

        if (index < 20) {
          debugMap.put("subjectResult." + index, subjectId + ", " + wsSubject.getName());
        }
        
        KimEntityNameInfo kimEntityNameInfo = GrouperKimUtils.convertWsSubjectToEntityNameInfo(wsSubject, wsGetSubjectsResults.getSubjectAttributeNames());
        
        if (!StringUtils.equals(entityId, subjectId)) {
          throw new RuntimeException("Why is entityId: " + entityId + " not equal to " + subjectId);
        }
        
        result.put(entityId, kimEntityNameInfo);
        index++;
      }
      
      return result;

    } catch (RuntimeException re) {
      String errorPrefix = GrouperKimUtils.mapForLog(debugMap) + ", ";
      LOG.error(errorPrefix, re);
      GrouperClientUtils.injectInException(re, errorPrefix);
      hadException = true;
      throw re;
    } finally {
      if (LOG.isDebugEnabled() && !hadException) {
        LOG.debug(GrouperKimUtils.mapForLog(debugMap));
      }
    }
    
  }

  /**
   * Gets the name for the principals with ids in the given List.
   * 
   * <p>The resulting Map contains the principalId as the key and the name information as the value.
   * When fetching names by principal id, the resulting name info contains the entity's name info
   * as well as the principal's name info.
   * @see org.kuali.rice.kim.service.IdentityService#getDefaultNamesForPrincipalIds(java.util.List)
   */
  public Map<String, KimEntityNamePrincipalNameInfo> getDefaultNamesForPrincipalIds(
      List<String> principalIds) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("operation", "getDefaultNamesForPrincipalIds");
    int principalIdsSize = GrouperClientUtils.length(principalIds);
    debugMap.put("principalIds.size", principalIdsSize);
    Map<String, KimEntityNamePrincipalNameInfo> result = new LinkedHashMap<String, KimEntityNamePrincipalNameInfo>();
    boolean hadException = false;

    try {
    
      if (principalIdsSize == 0) {
        return result;
      }
      
      int index = 0;

      //log some of these
      for (String principalId : principalIds) {
        
        //dont log all...
        if (index > 20) {
          break;
        }
        
        principalId = GrouperKimUtils.translatePrincipalId(principalId);
        debugMap.put("principalIds." + index, principalId);


        index++;
      }


      GcGetSubjects gcGetSubjects = new GcGetSubjects();
      
      for (String principalId : principalIds) {
        principalId = GrouperKimUtils.translateEntityId(principalId);

        gcGetSubjects.addWsSubjectLookup(new WsSubjectLookup(null,null,principalId));
        
      }
      
      gcGetSubjects.assignIncludeSubjectDetail(true);
      
      WsGetSubjectsResults wsGetSubjectsResults = gcGetSubjects.execute();
      
      //we did one assignment, we have one result
      WsSubject[] wsSubjects = wsGetSubjectsResults.getWsSubjects();
      
      debugMap.put("resultNumberOfSubjects", GrouperClientUtils.length(wsSubjects));
      
            //map of subject id to ws subject object just to make sure they are in order
      Map<String, WsSubject> wsSubjectMap = new LinkedHashMap<String, WsSubject>();

      for (WsSubject wsSubject : GrouperClientUtils.nonNull(wsSubjects, WsSubject.class)) {
        wsSubjectMap.put(wsSubject.getIdentifierLookup(), wsSubject);
      }

      index = 0;
      for (String principalId : principalIds) {
        
        WsSubject wsSubject = wsSubjectMap.get(principalId);
        
        if (wsSubject == null) {
          throw new RuntimeException("Cant find subject for principal: " + principalId);
        }
        
        String subjectIdentifier = wsSubject.getIdentifierLookup();

        if (index < 20) {
          debugMap.put("subjectResult." + index, subjectIdentifier + ", " + wsSubject.getName());
        }
        
        KimEntityNamePrincipalNameInfo kimEntityNamePrincipalNameInfo = GrouperKimUtils.convertWsSubjectToPrincipalNameInfo(
            wsSubject, wsGetSubjectsResults.getSubjectAttributeNames());
        
        if (!StringUtils.equals(principalId, subjectIdentifier)) {
          throw new RuntimeException("Why is principalId: " + principalId + " not equal to " + subjectIdentifier);
        }
        
        result.put(principalId, kimEntityNamePrincipalNameInfo);
        index++;
      }
      
      return result;

    } catch (RuntimeException re) {
      String errorPrefix = GrouperKimUtils.mapForLog(debugMap) + ", ";
      LOG.error(errorPrefix, re);
      GrouperClientUtils.injectInException(re, errorPrefix);
      hadException = true;
      throw re;
    } finally {
      if (LOG.isDebugEnabled() && !hadException) {
        LOG.debug(GrouperKimUtils.mapForLog(debugMap));
      }
    }
  }

  /**
   * @see org.kuali.rice.kim.service.IdentityService#getEmailType(java.lang.String)
   */
  public EmailTypeInfo getEmailType(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.IdentityService#getEmploymentStatus(java.lang.String)
   */
  public EmploymentStatusInfo getEmploymentStatus(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.IdentityService#getEmploymentType(java.lang.String)
   */
  public EmploymentTypeInfo getEmploymentType(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.IdentityService#getEntityDefaultInfo(java.lang.String)
   */
  public KimEntityDefaultInfo getEntityDefaultInfo(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.IdentityService#getEntityDefaultInfoByPrincipalId(java.lang.String)
   */
  public KimEntityDefaultInfo getEntityDefaultInfoByPrincipalId(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.IdentityService#getEntityDefaultInfoByPrincipalName(java.lang.String)
   */
  public KimEntityDefaultInfo getEntityDefaultInfoByPrincipalName(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.IdentityService#getEntityInfo(java.lang.String)
   */
  public KimEntityInfo getEntityInfo(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.IdentityService#getEntityInfoByPrincipalId(java.lang.String)
   */
  public KimEntityInfo getEntityInfoByPrincipalId(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.IdentityService#getEntityInfoByPrincipalName(java.lang.String)
   */
  public KimEntityInfo getEntityInfoByPrincipalName(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.IdentityService#getEntityNameType(java.lang.String)
   */
  public EntityNameTypeInfo getEntityNameType(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.IdentityService#getEntityPrivacyPreferences(java.lang.String)
   */
  public KimEntityPrivacyPreferencesInfo getEntityPrivacyPreferences(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.IdentityService#getEntityType(java.lang.String)
   */
  public EntityTypeInfo getEntityType(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.IdentityService#getExternalIdentifierType(java.lang.String)
   */
  public ExternalIdentifierTypeInfo getExternalIdentifierType(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.IdentityService#getMatchingEntityCount(java.util.Map)
   */
  public int getMatchingEntityCount(Map<String, String> arg0) {
    return 0;
  }

  /**
   * @see org.kuali.rice.kim.service.IdentityService#getPhoneType(java.lang.String)
   */
  public PhoneTypeInfo getPhoneType(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.IdentityService#getPrincipal(java.lang.String)
   */
  public KimPrincipalInfo getPrincipal(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.IdentityService#getPrincipalByPrincipalName(java.lang.String)
   */
  public KimPrincipalInfo getPrincipalByPrincipalName(String arg0) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.IdentityService#getPrincipalByPrincipalNameAndPassword(java.lang.String, java.lang.String)
   */
  public KimPrincipalInfo getPrincipalByPrincipalNameAndPassword(String arg0, String arg1) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.IdentityService#lookupEntityDefaultInfo(java.util.Map, boolean)
   */
  public List<KimEntityDefaultInfo> lookupEntityDefaultInfo(Map<String, String> arg0,
      boolean arg1) {
    return null;
  }

  /**
   * @see org.kuali.rice.kim.service.IdentityService#lookupEntityInfo(java.util.Map, boolean)
   */
  public List<KimEntityInfo> lookupEntityInfo(Map<String, String> arg0, boolean arg1) {
    throw new RuntimeException("Not implemented");
  }

}
