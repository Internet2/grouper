package edu.internet2.middleware.grouper.app.membershipRequire;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperDaemonUtils;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;

/**
 * utils and cache for membership require
 * @author mchyzer
 *
 */
public class MembershipRequireEngine {

  public static void clearCaches() {
    
    attributeDefNameNameToConfigBean.clear();
    attributeDefNameNameToGroupNames.clear();
    attributeDefNameNameToStemNames.clear();
    groupNameToConfigBeanAssigned.clear();
    membershipRequireConfigBeansCache.clear();
    requiredGroupNameToConfigBean.clear();
    stemNameToConfigBeanAssigned.clear();
    
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(MembershipRequireEngine.class);


  /**
   * 
   */
  public MembershipRequireEngine() {
  }

  /**
   * membership require config beans, cache them for 5 minutes
   */
  private static ExpirableCache<Boolean, List<MembershipRequireConfigBean>> membershipRequireConfigBeansCache = new ExpirableCache<Boolean, List<MembershipRequireConfigBean>>(5);

  /**
   * attribute def name name to stem names
   */
  private static ExpirableCache<String, Set<String>> attributeDefNameNameToStemNames = new ExpirableCache<String, Set<String>>(5);

  /**
   * get the attribute def name name to stem names where they are assigned
   * @param attributeDefNameName 
   * @return the beans
   */
  public static Set<String> attributeDefNameNameToStemNames(String attributeDefNameName) {
    Set<String> stemNames = attributeDefNameNameToStemNames.get(attributeDefNameName);
    
    if (stemNames == null) {
      synchronized(attributeDefNameNameToStemNames) {
        stemNames = attributeDefNameNameToStemNames.get(attributeDefNameName);
        
        if (stemNames == null) {
          
          List<MembershipRequireConfigBean> membershipRequireConfigBeansInConfig = membershipRequireConfigBeans();

          stemNames = new HashSet<String>();

          // are there any configured?
          if (GrouperUtil.length(membershipRequireConfigBeansInConfig) > 0) {

            GcDbAccess gcDbAccess = new GcDbAccess();
            StringBuilder sql = new StringBuilder("select gaasv.attribute_def_name_name, gaasv.stem_name from grouper_attr_asn_stem_v gaasv where gaasv.enabled = 'T'");
            sql.append(" and  ");
            sql.append(" gaasv.attribute_def_name_name = ? ");
            gcDbAccess.sql(sql.toString());
            gcDbAccess.addBindVar(attributeDefNameName);
            List<Object[]> results = gcDbAccess.selectList(Object[].class);
            
            for (Object[] row : GrouperUtil.nonNull(results)) {
              //String attributeDefName = (String)row[0];
              String currentStemName = (String)row[1];
              stemNames.add(currentStemName);
            }
          }
          attributeDefNameNameToStemNames.put(attributeDefNameName, stemNames);
        }
      }
    }

    return stemNames;
  }  

  /**
   * attribute def name name to group names
   */
  private static ExpirableCache<String, Set<String>> attributeDefNameNameToGroupNames = new ExpirableCache<String, Set<String>>(5);

  /**
   * get the attribute def name name to config bean
   * @param attributeDefNameName 
   * @return the bean
   */
  public static Set<MembershipRequireConfigBean> attributeDefNameNameToConfigBean(String attributeDefNameName) {
    
    // get configs to cache
    membershipRequireConfigBeans();
    
    return attributeDefNameNameToConfigBean.get(attributeDefNameName);
    
  }

  /**
   * get the required group name to config bean
   * @param groupName 
   * @return the bean
   */
  public static Set<MembershipRequireConfigBean> requiredGroupNameToConfigBean(String groupName) {
    
    // get configs to cache
    membershipRequireConfigBeans();
    
    return requiredGroupNameToConfigBean.get(groupName);
    
  }

  /**
   * get the attribute def name name to group names where they are assigned
   * @param attributeDefNameName 
   * @return the beans
   */
  public static Set<String> attributeDefNameNameToGroupNames(String attributeDefNameName) {
    Set<String> groupNames = attributeDefNameNameToGroupNames.get(attributeDefNameName);
    
    if (groupNames == null) {
      synchronized(attributeDefNameNameToGroupNames) {
        groupNames = attributeDefNameNameToGroupNames.get(attributeDefNameName);
        
        if (groupNames == null) {
          
          List<MembershipRequireConfigBean> membershipRequireConfigBeansInConfig = membershipRequireConfigBeans();

          groupNames = new HashSet<String>();

          // are there any configured?
          if (GrouperUtil.length(membershipRequireConfigBeansInConfig) > 0) {

            GcDbAccess gcDbAccess = new GcDbAccess();
            StringBuilder sql = new StringBuilder("select gaagv.attribute_def_name_name, gaagv.group_name from grouper_attr_asn_group_v gaagv where gaagv.enabled = 'T'");
            sql.append(" and gaagv.attribute_def_name_name = ? ");
            sql.append(" and not exists (select 1 from grouper_attr_asn_group_v gaagv2 where gaagv2.enabled = 'T' and gaagv.group_name = gaagv2.group_name and gaagv2.attribute_def_name_name = ? ) ");
            gcDbAccess.sql(sql.toString());
            gcDbAccess.addBindVar(attributeDefNameName);
            gcDbAccess.addBindVar(GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects") + ":etc:attribute:loaderMetadata:loaderMetadata");
            List<Object[]> results = gcDbAccess.selectList(Object[].class);
            
            for (Object[] row : GrouperUtil.nonNull(results)) {
              //String attributeDefName = (String)row[0];
              String currentGroupName = (String)row[1];
              groupNames.add(currentGroupName);
            }
          }
          
          // get the stems
          Set<String> stemNames = attributeDefNameNameToStemNames(attributeDefNameName);

          groupNames.addAll(groupsInStems(stemNames));
          
          attributeDefNameNameToGroupNames.put(attributeDefNameName, groupNames);
        }
      }
    }

    return groupNames;
  }

  /**
   * groups in stems that are eligible
   * @param stemNames
   * @return the set
   */
  public static Set<String> groupsInStems(Set<String> stemNames) {
    Set<String> groupNames = new HashSet<String>();
    if (GrouperUtil.length(stemNames) > 0) {
      GcDbAccess gcDbAccess = new GcDbAccess();
      StringBuilder sql = new StringBuilder("select distinct g.name from grouper_groups g, grouper_stems s where g.enabled = 'T' and g.parent_stem = s.id ");
      sql.append(" and not exists (select 1 from grouper_attr_asn_group_v gaagv2 where gaagv2.enabled = 'T' and g.name = gaagv2.group_name and gaagv2.attribute_def_name_name = ? ) ");
      gcDbAccess.addBindVar(GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects") + ":etc:attribute:loaderMetadata:loaderMetadata");
      sql.append(" and ( ");
      boolean first = true;
      for (String stemName : stemNames) {
        if (!first) {
          sql.append(" or ");
        }
        sql.append(" s.name = ? ");
        gcDbAccess.addBindVar(stemName);
        first = false;
      }
      sql.append(" ) ");
      List<String> results = gcDbAccess.sql(sql.toString()).selectList(String.class);
      groupNames.addAll(GrouperUtil.nonNull(results));
    }
    return groupNames;
  }  


  /**
   * after cache is retrieved, get the attribute def name name to config bean
   */
  private static Map<String, Set<MembershipRequireConfigBean>> attributeDefNameNameToConfigBean = new HashMap<String, Set<MembershipRequireConfigBean>>();
  
  /**
   * after cache is retrieved, get the group name to config bean
   */
  private static Map<String, Set<MembershipRequireConfigBean>> requiredGroupNameToConfigBean = new HashMap<String, Set<MembershipRequireConfigBean>>();
  
  /**
   * pattern to get config ids from config
   */
  private static Pattern configPattern = Pattern.compile("^grouper\\.membershipRequirement\\.([^.]+)\\.uiKey$");
  
  /**
   * get config beans from cache or config file
   * @return the beans
   */
  public static List<MembershipRequireConfigBean> membershipRequireConfigBeans() {
    List<MembershipRequireConfigBean> result = membershipRequireConfigBeansCache.get(Boolean.TRUE);
    if (result == null) {
      synchronized(membershipRequireConfigBeansCache) {
        result = membershipRequireConfigBeansCache.get(Boolean.TRUE);
        if (result == null) {
          result = new ArrayList<MembershipRequireConfigBean>();
          
          Map<String, Set<MembershipRequireConfigBean>> resultAttributeDefNameNameToConfigBean = new HashMap<String, Set<MembershipRequireConfigBean>>();
          
          Map<String, Set<MembershipRequireConfigBean>> resultGroupNameToConfigBean = new HashMap<String, Set<MembershipRequireConfigBean>>();
          
          Set<String> configIds = GrouperConfig.retrieveConfig().propertyConfigIds(configPattern);
          
          for (String configId : GrouperUtil.nonNull(configIds)) {
            

            //  # ui key to externalize text
            //  # {valueType: "string", regex: "^grouper\\.membershipRequirement\\.[^.]+\\.uiKey$"}
            //  #grouper.membershipRequirement.someConfigId.uiKey = customVetoCompositeRequireEmployee
            //
            //  # attribute name that signifies this requirement
            //  # {valueType: "string", regex: "^grouper\\.membershipRequirement\\.[^.]+\\.attributeName$"}
            //  #grouper.membershipRequirement.someConfigId.attributeName = etc:attribute:customComposite:requireEmployee
            //
            //  # group name which is the population group
            //  # {valueType: "group", regex: "^grouper\\.membershipRequirement\\.[^.]+\\.groupName$"}
            //  #grouper.membershipRequirement.someConfigId.requireGroupName = org:centralIt:staff:itStaff

            String uiKey = GrouperConfig.retrieveConfig().propertyValueString("grouper.membershipRequirement." + configId + ".uiKey");
            if (StringUtils.isBlank(uiKey)) {
              LOG.error("Invalid config for membershipRequirement uiKey '" + configId + "'");
              continue;
            }
            
            String attributeName = GrouperConfig.retrieveConfig().propertyValueString("grouper.membershipRequirement." + configId + ".attributeName");
            if (StringUtils.isBlank(attributeName)) {
              LOG.error("Invalid config for membershipRequirement attributeName '" + configId + "'");
              continue;
            }

            String groupName = GrouperConfig.retrieveConfig().propertyValueString("grouper.membershipRequirement." + configId + ".requireGroupName");
            if (StringUtils.isBlank(groupName)) {
              LOG.error("Invalid config for membershipRequirement requireGroupName '" + configId + "'");
              continue;
            }
            
            boolean hookEnable = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.membershipRequirement." + configId + ".hookEnable", true);
            
            MembershipRequireConfigBean membershipRequireConfigBean = new MembershipRequireConfigBean();
            membershipRequireConfigBean.setUiKey(uiKey);
            membershipRequireConfigBean.setAttributeName(attributeName);
            membershipRequireConfigBean.setRequireGroupName(groupName);
            membershipRequireConfigBean.setConfigId(configId);
            membershipRequireConfigBean.setHookEnable(hookEnable);
            
            AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(attributeName, false);
            if (attributeDefName == null) {
              LOG.error("cant find attribute def name: '" + attributeName + "'");
              continue;
            }
            membershipRequireConfigBean.setAttributeDefNameId(attributeDefName.getId());

            Group requireGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, false);
            if (requireGroup == null) {
              LOG.error("cant find group: '" + groupName + "'");
              continue;
            }
            membershipRequireConfigBean.setRequireGroupId(requireGroup.getId());
            
            result.add(membershipRequireConfigBean);
            
            Set<MembershipRequireConfigBean> theSet = resultAttributeDefNameNameToConfigBean.get(attributeName);
            if (theSet == null) {
              theSet = new HashSet<MembershipRequireConfigBean>();
              resultAttributeDefNameNameToConfigBean.put(attributeName, theSet);
            }
            theSet.add(membershipRequireConfigBean);
            
            theSet = resultGroupNameToConfigBean.get(attributeName);
            if (theSet == null) {
              theSet = new HashSet<MembershipRequireConfigBean>();
              resultGroupNameToConfigBean.put(groupName, theSet);
            }
            theSet.add(membershipRequireConfigBean);
            
          }
          membershipRequireConfigBeansCache.put(Boolean.TRUE, result);
          attributeDefNameNameToConfigBean = resultAttributeDefNameNameToConfigBean;
          requiredGroupNameToConfigBean = resultGroupNameToConfigBean;
          
        }
      }
    }
    return result;
  }

  /**
   * stems with attribute assignments that link to membership require config beans, cache them for 5 minutes
   */
  private static ExpirableCache<String, Set<MembershipRequireConfigBean>> stemNameToConfigBeanAssigned = new ExpirableCache<String, Set<MembershipRequireConfigBean>>(5);

  /**
   * get the membership require config beans for a stem
   * @param stemName 
   * @return the beans
   */
  public static Set<MembershipRequireConfigBean> stemNameToConfigBeanAssigned(String stemName) {
    
    Set<MembershipRequireConfigBean> membershipRequireConfigBeans = stemNameToConfigBeanAssigned.get(stemName);
    if (membershipRequireConfigBeans != null) {
      return membershipRequireConfigBeans;
    }

    List<MembershipRequireConfigBean> membershipRequireConfigBeansInConfig = membershipRequireConfigBeans();
    
    // are there any configured?
    if (GrouperUtil.length(membershipRequireConfigBeansInConfig) == 0) {
      membershipRequireConfigBeans = new HashSet<MembershipRequireConfigBean>();
      stemNameToConfigBeanAssigned.put(stemName, membershipRequireConfigBeans);
      return membershipRequireConfigBeans;
    }

    List<String> stemNamesToQuery = new ArrayList<String>(GrouperUtil.findParentStemNames(stemName));

    Map<String, Set<MembershipRequireConfigBean>> stemNameToConfigSet = new HashMap<String, Set<MembershipRequireConfigBean>>();

    Iterator<String> iterator = stemNamesToQuery.iterator();
    while (iterator.hasNext()) {
      String parentStemName = iterator.next();
      Set<MembershipRequireConfigBean> theList = stemNameToConfigBeanAssigned.get(parentStemName);
      if (theList != null) {
        stemNameToConfigSet.put(parentStemName, theList);
        iterator.remove();
      }
    }
    stemNamesToQuery.add(stemName);
    
    // init the results
    for (String stemNameToQuery : stemNamesToQuery) {
      Set<MembershipRequireConfigBean> theSet = stemNameToConfigSet.get(stemNameToQuery);
      if (theSet == null) {
        stemNameToConfigSet.put(stemNameToQuery, new HashSet<MembershipRequireConfigBean>());
      }
    }
    
    GcDbAccess gcDbAccess = new GcDbAccess();
    StringBuilder sql = new StringBuilder("select gaasv.attribute_def_name_name, gaasv.stem_name from grouper_attr_asn_stem_v gaasv where gaasv.enabled = 'T'");
    sql.append(" and ( ");
    boolean first = true;
    for (MembershipRequireConfigBean membershipRequireConfigBean : membershipRequireConfigBeansInConfig) {
      if (!first) {
        sql.append(" or ");
      }
      sql.append(" gaasv.attribute_def_name_name = ? ");
      gcDbAccess.addBindVar(membershipRequireConfigBean.getAttributeName());
      first = false;
    }
    sql.append(" ) ");
    sql.append(" and ( ");
    first = true;
    for (String stemNameToQuery : stemNamesToQuery) {
      if (!first) {
        sql.append(" or ");
      }
      sql.append(" gaasv.stem_name = ? ");
      gcDbAccess.addBindVar(stemNameToQuery);
      first = false;
    }
    sql.append(" ) ");
    List<Object[]> results = gcDbAccess.sql(sql.toString()).selectList(Object[].class);
    
    for (Object[] row : GrouperUtil.nonNull(results)) {
      String attributeDefName = (String)row[0];
      String currentStemName = (String)row[1];
      Set<MembershipRequireConfigBean> attributeBeans = attributeDefNameNameToConfigBean.get(attributeDefName);
      stemNameToConfigSet.get(currentStemName).addAll(GrouperUtil.nonNull(attributeBeans));
    }
    
    // now we have the attribute configs for each stem, add all from parent stems
    Set<MembershipRequireConfigBean> previousBeans = new HashSet<MembershipRequireConfigBean>();
    for (String theStem : stemNamesToQuery) {
      Set<MembershipRequireConfigBean> theBeans = stemNameToConfigSet.get(theStem);
      theBeans.addAll(previousBeans);
      previousBeans = theBeans;
    }
    
    // add the new ones
    for (String theStemName : stemNamesToQuery) {
      stemNameToConfigBeanAssigned.put(theStemName, stemNameToConfigSet.get(theStemName));
    }

    return stemNameToConfigSet.get(stemName);
  }

  /**
   * membership require config beans, cache them for 5 minutes
   */
  private static ExpirableCache<String, Set<MembershipRequireConfigBean>> groupNameToConfigBeanAssigned = new ExpirableCache<String, Set<MembershipRequireConfigBean>>(5);

  /**
   * get the membership require config beans for a group
   * @param groupName 
   * @return the beans
   */
  public static Set<MembershipRequireConfigBean> groupNameToConfigBeanAssigned(String groupName) {
    Set<MembershipRequireConfigBean> membershipRequireConfigBeans = groupNameToConfigBeanAssigned.get(groupName);
    if (membershipRequireConfigBeans != null) {
      return membershipRequireConfigBeans;
    }

    List<MembershipRequireConfigBean> membershipRequireConfigBeansInConfig = membershipRequireConfigBeans();
    
    // are there any configured?
    if (GrouperUtil.length(membershipRequireConfigBeansInConfig) == 0) {
      membershipRequireConfigBeans = new HashSet<MembershipRequireConfigBean>();
      groupNameToConfigBeanAssigned.put(groupName, membershipRequireConfigBeans);
      return membershipRequireConfigBeans;
    }

    // get the stem configs
    Set<MembershipRequireConfigBean> membershipRequireConfigBeansForStem = stemNameToConfigBeanAssigned(GrouperUtil.parentStemNameFromName(groupName));
    membershipRequireConfigBeans = new HashSet<MembershipRequireConfigBean>();
    membershipRequireConfigBeans.addAll(membershipRequireConfigBeansForStem);
    
    GcDbAccess gcDbAccess = new GcDbAccess();
    StringBuilder sql = new StringBuilder("select gaagv.attribute_def_name_name, gaagv.group_name from grouper_attr_asn_group_v gaagv where gaagv.enabled = 'T'");
    sql.append(" and not exists (select 1 from grouper_attr_asn_group_v gaagv2 where gaagv2.enabled = 'T' and gaagv.group_name = gaagv2.group_name and gaagv2.attribute_def_name_name = ? ) ");
    gcDbAccess.addBindVar(GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects") + ":etc:attribute:loaderMetadata:loaderMetadata");
    sql.append(" and ( ");
    boolean first = true;
    for (MembershipRequireConfigBean membershipRequireConfigBean : membershipRequireConfigBeansInConfig) {
      if (!first) {
        sql.append(" or ");
      }
      sql.append(" gaagv.attribute_def_name_name = ? ");
      gcDbAccess.addBindVar(membershipRequireConfigBean.getAttributeName());
      first = false;
    }
    sql.append(" ) ");
    sql.append(" and gaagv.group_name = ? ");
    gcDbAccess.addBindVar(groupName);
    List<Object[]> results = gcDbAccess.sql(sql.toString()).selectList(Object[].class);
    
    for (Object[] row : GrouperUtil.nonNull(results)) {
      String attributeDefName = (String)row[0];
      // String currentGroupName = (String)row[1];
      Set<MembershipRequireConfigBean> attributeBeans = attributeDefNameNameToConfigBean.get(attributeDefName);
      membershipRequireConfigBeans.addAll(GrouperUtil.nonNull(attributeBeans));
    }
    
    groupNameToConfigBeanAssigned.put(groupName, membershipRequireConfigBeans);

    return membershipRequireConfigBeans;
  }  
  
  /**
   * remove invalid members
   * @param groupName
   * @param membershipRequireConfigBean
   * @param memberId optional member id
   * @param membershipRequireEngineEnum 
   * @return number of members removed
   */
  public static int removeInvalidMembers(String groupName, MembershipRequireConfigBean membershipRequireConfigBean, String memberId, MembershipRequireEngineEnum membershipRequireEngineEnum) {
    GcDbAccess gcDbAccess = new GcDbAccess().sql("select gm.id, gm.subject_id, gm.subject_source "
      + " from grouper_memberships gms, grouper_members gm, grouper_fields gf, grouper_groups gg where gg.name = ? "
      + (StringUtils.isBlank(memberId) ? "" : " and gm.id = ? ")
      + " and gg.id = gms.owner_group_id and gms.enabled = 'T' and gms.member_id = gm.id and gg.enabled = 'T' "
      + " and gms.mship_type = 'immediate' and gm.subject_source != 'g:gsa' and gms.field_id = gf.id and gf.name = 'members' "
      + " and not exists (select 1 from grouper_memberships_all_v gmav2, grouper_groups gg2, grouper_fields gf2 "
      + " where gmav2.member_id = gm.id and gmav2.owner_group_id = gg2.id and gg2.name = ? "
      + " and gmav2.field_id = gf2.id and gf2.name = 'members' and gg2.enabled = 'T' and gmav2.immediate_mship_enabled = 'T')");
    gcDbAccess.addBindVar(groupName);
    if (!StringUtils.isBlank(memberId)) { 
      gcDbAccess.addBindVar(memberId);
    }
    gcDbAccess.addBindVar(membershipRequireConfigBean.getRequireGroupName());
    List<Object[]> rows = gcDbAccess.selectList(Object[].class);
    GrouperDaemonUtils.stopProcessingIfJobPaused();

    if (GrouperUtil.length(rows) == 0) {
      return 0;
    }
    Group group = GroupFinder.findByName(groupName, false);
    if (group == null) {
      return 0;
    }
    int count = 0;
    for (Object[] row : GrouperUtil.nonNull(rows)) {
      GrouperDaemonUtils.stopProcessingIfJobPaused();

      String currentMemberId = (String)row[0];
      String subjectId = (String)row[1];
      String subjectSourceId = (String)row[2];
      Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), currentMemberId, false);
      if (member == null) {
        continue;
      }
      if (group.deleteMember(member, false)) {
        GrouperMembershipRequireChange grouperMembershipRequireChange = new GrouperMembershipRequireChange();
        grouperMembershipRequireChange.setEngine(membershipRequireEngineEnum);
        grouperMembershipRequireChange.setGroupId(group.getId());
        grouperMembershipRequireChange.setMemberId(member.getId());
        grouperMembershipRequireChange.setAttributeDefNameId(membershipRequireConfigBean.getAttributeDefNameId());
        grouperMembershipRequireChange.setRequireGroupId(membershipRequireConfigBean.getRequireGroupId());
        grouperMembershipRequireChange.setConfigId(membershipRequireConfigBean.getConfigId());
        grouperMembershipRequireChange.store();
        count++;
      }
    }
    return count;
  }

  /**
   * remove invalid members
   * @param groupName
   * @param membershipRequireConfigBean
   * @param memberId optional member id
   * @return number of members removed
   */
  public static boolean validMember(String groupName, MembershipRequireConfigBean membershipRequireConfigBean, String memberId) {
    GcDbAccess gcDbAccess = new GcDbAccess().sql("select gmav2.member_id from grouper_memberships_all_v gmav2, grouper_groups gg2, grouper_fields gf2 "
      + " where gmav2.owner_group_id = gg2.id and gg2.name = ? and gmav2.member_id = ? "
      + " and gmav2.field_id = gf2.id and gf2.name = 'members' and gg2.enabled = 'T' and gmav2.immediate_mship_enabled = 'T'");
    gcDbAccess.addBindVar(membershipRequireConfigBean.getRequireGroupName());
    gcDbAccess.addBindVar(memberId);
    List<String> rows = gcDbAccess.selectList(String.class);
    if (GrouperUtil.length(rows) == 0) {
      // make sure right source
      Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), memberId, false);
      if (member == null || StringUtils.equals("g:gsa", member.getSubjectSourceId())) {
        return true;
      }
      return false;
    }
    return true;
  }

}
