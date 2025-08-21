package edu.internet2.middleware.grouper.hooks.examples;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapUtils;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hooks.AttributeAssignHooks;
import edu.internet2.middleware.grouper.hooks.beans.GrouperContextTypeBuiltIn;
import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfigInApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.subject.Subject;

/**
 * grouper.properties:
 * hooks.attributeAssign.class=edu.internet2.middleware.grouper.hooks.examples.LoaderAttributeVetoHook
 */
public class LoaderAttributeVetoHook extends AttributeAssignHooks {

  private static ExpirableCache<Boolean, Set<String>> loaderAttributeDefNameUuidCache = new ExpirableCache<Boolean, Set<String>>(60);

  public static void validateAttributeDefNameId(HooksContext hooksContext,
      String attributeDefNameId) {

    if (hooksContext == null || attributeDefNameId == null) {
      return;
    }
    
    if (hooksContext.getGrouperContextType() != GrouperContextTypeBuiltIn.GROUPER_UI) {
      return;
    }
    
    Subject loggedInSubject = hooksContext.getSubjectLoggedIn();
    
    if (loggedInSubject == null) {
      return;
    }
    
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return;
    }
    
    Set<String> attributeDefNameUuids = loaderAttributeDefNameUuidCache.get(Boolean.TRUE);
    
    if (attributeDefNameUuids == null) {
      synchronized (loaderAttributeDefNameUuidCache) {
        // double check in case another thread got here first
        attributeDefNameUuids = loaderAttributeDefNameUuidCache.get(Boolean.TRUE);
        
        if (attributeDefNameUuids == null) {
          
          Set<String> attributeDefNameUuidsNew = new HashSet<String>();
          
          
          List<String> attributeDefNameUuidsList = new GcDbAccess()
            .sql("""
                select gadn.id 
                from grouper_attribute_def_name gadn, grouper_attribute_def gad
                where gad.id = gadn.attribute_def_id and gad.name in (?, ?, ?, ?)
                """)
            .addBindVar(GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":legacy:attribute:legacyAttributeDef_grouperLoader")
            .addBindVar(GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":legacy:attribute:legacyGroupTypeDef_grouperLoader")
            .addBindVar(LoaderLdapUtils.attributeLoaderLdapStemName() + ":" + LoaderLdapUtils.LOADER_LDAP_DEF)
            .addBindVar(LoaderLdapUtils.attributeLoaderLdapStemName() + ":" + LoaderLdapUtils.LOADER_LDAP_VALUE_DEF)
            .selectList(String.class);
          
          attributeDefNameUuidsNew.addAll(attributeDefNameUuidsList);
          
          loaderAttributeDefNameUuidCache.put(Boolean.TRUE, attributeDefNameUuids);
          
          attributeDefNameUuids = attributeDefNameUuidsNew;
        }
      }
    }

    if (!attributeDefNameUuids.contains(attributeDefNameId)) {
      return;
    }
    
    String canEditLoaderIfInGroup = GrouperUiConfigInApi.retrieveConfig().propertyValueString("uiV2.loader.edit.if.in.group");
    if (!StringUtils.isBlank(canEditLoaderIfInGroup)) {
      GrouperSession grouperSession = null;
      
      //get a session, close it if you started it
      boolean startedSession = false;
      try {
        grouperSession = GrouperSession.staticGrouperSession(false);
        if (grouperSession == null) {
          grouperSession = GrouperSession.startRootSession();
          startedSession = true;
        }
        if (!PrivilegeHelper.isWheelOrRoot(grouperSession.getSubject())) {
          grouperSession = grouperSession.internal_getRootSession();
        }
        Group group = GroupFinder.findByName(grouperSession, canEditLoaderIfInGroup, true);
        if (group.hasMember(loggedInSubject)) {
          return;
        }
      } catch (Exception e) {
        throw new RuntimeException("Problem with user: " + GrouperUtil.subjectToString(loggedInSubject) + ", " + canEditLoaderIfInGroup, e);
      } finally {
        if (startedSession) {
          GrouperSession.stopQuietly(grouperSession);
        }
      }

    }
    
    throw new HookVeto("cannotAssignLoaderAttribute", "Cannot assign loader attribute");
    
  }
  
  @Override
  public void attributeAssignPreUpdate(HooksContext hooksContext,
      HooksAttributeAssignBean hooksAttributeAssignBean) {
    if (hooksAttributeAssignBean.getAttributeAssign() == null) {
      return;
    }
    validateAttributeDefNameId(hooksContext, hooksAttributeAssignBean.getAttributeAssign().getAttributeDefNameId());
  }

  @Override
  public void attributeAssignPreInsert(HooksContext hooksContext,
      HooksAttributeAssignBean hooksAttributeAssignBean) {
    if (hooksAttributeAssignBean.getAttributeAssign() == null) {
      return;
    }
    validateAttributeDefNameId(hooksContext, hooksAttributeAssignBean.getAttributeAssign().getAttributeDefNameId());
  }

  @Override
  public void attributeAssignPreDelete(HooksContext hooksContext,
      HooksAttributeAssignBean hooksAttributeAssignBean) {
    if (hooksAttributeAssignBean.getAttributeAssign() == null) {
      return;
    }
    validateAttributeDefNameId(hooksContext, hooksAttributeAssignBean.getAttributeAssign().getAttributeDefNameId());
  }

}
