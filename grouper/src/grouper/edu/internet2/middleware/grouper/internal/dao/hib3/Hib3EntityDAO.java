/**
 * Copyright 2014 Internet2
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
 */
/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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

package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.entity.Entity;
import edu.internet2.middleware.grouper.entity.EntityUtils;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.EntityDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * Basic Hibernate <code>Entity</code> DAO interface.
 * @author  chris hyzer.
 * @version $Id: Hib3GroupDAO.java,v 1.51 2009-12-10 08:54:15 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3EntityDAO extends Hib3DAO implements EntityDAO {

  /**
   * @see EntityDAO#findEntitiesSecure(GrouperSession, List, List, List, List, List, List, String, Set, QueryOptions)
   */
  public Set<Entity> findEntitiesSecure(GrouperSession grouperSession,
      List<String> ancestorFolderIds, List<String> ancestorFolderNames, List<String> ids,
      List<String> names, List<String> parentFolderIds, List<String> parentFolderNames,
      String terms,
      Set<Privilege> inPrivSet, QueryOptions queryOptions) {
    
    int parentFolderNamesLength = GrouperUtil.length(parentFolderNames);

    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("theGroup.displayNameDb");
    }
  
    StringBuilder sql = new StringBuilder(
        "select distinct theGroup from Group theGroup ");
  
    if (parentFolderNamesLength > 0) {
      sql.append(", Stem parentStem ");
    }
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(grouperSession.getSubject(), byHqlStatic,
        sql, "theGroup.uuid", inPrivSet);

    if (changedQuery) {
      sql.append(" and ");
    } else {
      sql.append(" where ");
    }
    
    sql.append(" theGroup.typeOfGroupDb = 'entity' ");

    if (parentFolderNamesLength > 0) {
      sql.append(" and theGroup.parentUuid = parentStem.uuid ");
    }
    
    int ancestorFolderIdsLength = GrouperUtil.length(ancestorFolderIds);
    int ancestorFolderNamesLength = GrouperUtil.length(ancestorFolderNames);
    int idsLength = GrouperUtil.length(ids);
    int namesLength = GrouperUtil.length(names);
    int parentFolderIdsLength = GrouperUtil.length(parentFolderIds);
    
    int bindVariableCount = ancestorFolderIdsLength + ancestorFolderNamesLength + idsLength + namesLength + parentFolderIdsLength + parentFolderNamesLength;
    
    if (bindVariableCount > 180) {
      throw new RuntimeException("Too many bind variables: " + bindVariableCount);
    }
    
    if (ancestorFolderIdsLength > 0) {
      
      //ancestorFolderIds
      //we need to convert these into names
      Set<Stem> ancestorFolders = GrouperDAOFactory.getFactory().getStem().findByUuids(ancestorFolderIds, null);
      
      if (ancestorFolderNames == null) {
        ancestorFolderNames = new ArrayList<String>();
      }
      for (Stem stem : ancestorFolders) {
        ancestorFolderNames.add(stem.getName());
      }
      ancestorFolderNamesLength = GrouperUtil.length(ancestorFolderNames);
    }
    
    //ancestorFolderNames
    if (ancestorFolderNamesLength > 0) {
      
      sql.append(" and ( ");
      
      int index = 0;
      for (String ancestorFolderName : ancestorFolderNames) {
        
        if (index > 0) {
          sql.append(" or ");
        }
        
        sql.append(" ( ");
        
        sql.append(" theGroup.nameDb like :theAncestorName").append(index).append(" ");
        byHqlStatic.setString("theAncestorName" + index, ancestorFolderName + ":%");
        
        sql.append(" ) ");
        index++;
      }
      
      sql.append(" ) ");
    }
    
    //ids
    if (idsLength > 0) {
      
      sql.append(" and theGroup.uuid in ( ");
      
      sql.append(HibUtils.convertToInClause(ids, byHqlStatic));
      
      sql.append(" ) ");
    }

    //names
    if (namesLength > 0) {
      
      sql.append(" and ( ");
      sql.append(" theGroup.nameDb in ( ");
      
      sql.append(HibUtils.convertToInClause(names, byHqlStatic));

      sql.append(" ) ");
      
      sql.append(" or theGroup.alternateNameDb in ( ");
      
      sql.append(HibUtils.convertToInClause(names, byHqlStatic));

      sql.append(" ) ");
      
      //also allow attribute value
      
      sql.append(" or exists ( select theAttributeAssignValue from AttributeAssign theAttributeAssign, " +
      		" AttributeAssignValue theAttributeAssignValue, AttributeDefName theAttributeDefName ");

      sql.append(" where theGroup.uuid = theAttributeAssign.ownerGroupId ");
      sql.append(" and theAttributeAssign.attributeDefNameId = theAttributeDefName.id ");

      sql.append(" and theAttributeDefName.nameDb = :entitySubjectIdDefName ");
      byHqlStatic.setString("entitySubjectIdDefName", EntityUtils.entitySubjectIdentifierName());

      sql.append(" and theAttributeAssignValue.attributeAssignId = theAttributeAssign.id ");

      sql.append(" and ");
      sql.append(" theAttributeAssignValue.valueString in ( ");
    
      sql.append(HibUtils.convertToInClause(names, byHqlStatic));

      sql.append(" ) ");

      sql.append(" ) ");
      sql.append(" ) ");
    }

    //parentFolderIds
    if (parentFolderIdsLength > 0) {
      
      sql.append(" and theGroup.parentUuid in ( ");
      
      sql.append(HibUtils.convertToInClause(parentFolderIds, byHqlStatic));
      
      sql.append(" ) ");
    }

    //parentFolderNames
    if (parentFolderNamesLength > 0) {
      
      sql.append(" and parentStem.nameDb in ( ");
      
      sql.append(HibUtils.convertToInClause(parentFolderNames, byHqlStatic));
      
      sql.append(" ) ");
    }

    
    //see if there is a scope
    if (!StringUtils.isBlank(terms)) {
      terms = terms.toLowerCase();

      String[] scopes = GrouperUtil.splitTrim(terms, " ");

      sql.append(" and ( ");

      if (GrouperUtil.length(scopes) == 1) {
        sql.append(" ( theGroup.id = :theGroupIdScope or ( ");
        byHqlStatic.setString("theGroupIdScope", terms);
      } else {
        sql.append(" ( ( ");
      }

      int index = 0;
      for (String theScope : scopes) {
        if (index != 0) {
          sql.append(" and ");
        }
        sql.append(" ( lower(theGroup.nameDb) like :scope" + index 
            + " or lower(theGroup.displayNameDb) like :scope" + index 
            + " or lower(theGroup.descriptionDb) like :scope" + index + " ) ");
        theScope = "%" + theScope + "%";
        byHqlStatic.setString("scope" + index, theScope);
        index++;
      }
      sql.append(" ) ) ");
      //also allow attribute value
      
      sql.append(" or exists ( select theAttributeAssignValue from AttributeAssign theAttributeAssign, " +
          " AttributeAssignValue theAttributeAssignValue, AttributeDefName theAttributeDefName ");

      sql.append(" where theGroup.uuid = theAttributeAssign.ownerGroupId ");
      sql.append(" and theAttributeAssign.attributeDefNameId = theAttributeDefName.id ");

      sql.append(" and theAttributeDefName.nameDb = :entitySubjectIdDefName ");
      byHqlStatic.setString("entitySubjectIdDefName", EntityUtils.entitySubjectIdentifierName());

      sql.append(" and theAttributeAssignValue.attributeAssignId = theAttributeAssign.id ");

      for (int i = 0; i < scopes.length; i++) {
        sql.append(" and lower(theAttributeAssignValue.valueString) like :scope").append(i).append(" ");
        index++;
      }

      sql.append(" ) ");

      sql.append(" ) ");
    }

    Set<Group> groups = byHqlStatic.createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindEntitiesSecure")
      .options(queryOptions)
      .listSet(Group.class);
    
    return (Set<Entity>)(Object)groups;
  }

  /** */
  private static final String KLASS = Hib3EntityDAO.class.getName();

  /**
   * @see EntityDAO#findEntitiesByGroupIds(List)
   */
  public List<Object[]> findEntitiesByGroupIds(Collection<String> groupIds) {

    List<Object[]> results = new ArrayList<Object[]>();
    
    if (GrouperUtil.length(groupIds) == 0) {
      return results;
    }

    List<String> groupIdList = groupIds instanceof List ? (List)groupIds : new ArrayList<String>(groupIds);
    //lets page through these
    int batchSize = 180;
    int pages = GrouperUtil.batchNumberOfBatches(groupIds, batchSize);

    for (int i=0; i<pages; i++) {
      List<String> groupIdPageList = GrouperUtil.batchList(groupIdList, batchSize, i);

      StringBuilder sql = new StringBuilder(
          "select distinct theGroup, theAttributeAssignValue from Group theGroup, AttributeAssign theAttributeAssign, AttributeAssignValue theAttributeAssignValue, AttributeDefName theAttributeDefName ");
    
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    
      sql.append(" where theGroup.typeOfGroupDb = 'entity' ");
      sql.append(" and theGroup.uuid = theAttributeAssign.ownerGroupId ");
      sql.append(" and theAttributeAssign.attributeDefNameId = theAttributeDefName.id ");
  
      sql.append(" and theAttributeDefName.nameDb = :entitySubjectIdDefName ");
      byHqlStatic.setString("entitySubjectIdDefName", EntityUtils.entitySubjectIdentifierName());
  
      sql.append(" and theAttributeAssignValue.attributeAssignId = theAttributeAssign.id ");

      sql.append(" and theGroup.uuid in ( ");
      
      sql.append(HibUtils.convertToInClause(groupIdPageList, byHqlStatic));

      sql.append(" ) ");
      
      List<Object[]> resultPage = byHqlStatic.createQuery(sql.toString())
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindEntitiesByGroupIds")
        .list(Object[].class);
      results.addAll(GrouperUtil.nonNull(resultPage));
    }

    return results;
  }
} 

