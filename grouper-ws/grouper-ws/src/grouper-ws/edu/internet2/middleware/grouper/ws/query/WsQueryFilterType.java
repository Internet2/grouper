/*
 * @author mchyzer $Id: WsQueryFilterType.java,v 1.6 2009-03-15 08:15:37 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.query;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.filter.ComplementFilter;
import edu.internet2.middleware.grouper.filter.GroupAnyAttributeFilter;
import edu.internet2.middleware.grouper.filter.GroupAttributeExactFilter;
import edu.internet2.middleware.grouper.filter.GroupAttributeFilter;
import edu.internet2.middleware.grouper.filter.GroupNameExactFilter;
import edu.internet2.middleware.grouper.filter.GroupNameFilter;
import edu.internet2.middleware.grouper.filter.GroupTypeFilter;
import edu.internet2.middleware.grouper.filter.GroupUuidFilter;
import edu.internet2.middleware.grouper.filter.GroupsInStemFilter;
import edu.internet2.middleware.grouper.filter.IntersectionFilter;
import edu.internet2.middleware.grouper.filter.QueryFilter;
import edu.internet2.middleware.grouper.filter.UnionFilter;
import edu.internet2.middleware.grouper.ws.coresoap.WsQueryFilter;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * type of find group queries
 */
public enum WsQueryFilterType {

  /**
   * find by uuid.  pass the uuid in.
   */
  FIND_BY_GROUP_UUID {

    /**
     * make sure that based on the inputs, that this is a valid query
     * @param wsQueryFilter is the query params to validate based on type
     * @throws WsInvalidQueryException if invalid
     */
    @Override
    public void validate(WsQueryFilter wsQueryFilter) throws WsInvalidQueryException {

      //for a uuid, needs the uuid and nothing else
      wsQueryFilter.validateNoTypeOfGroups();
      wsQueryFilter.validateNoGroupName();
      wsQueryFilter.validateHasGroupUuid();
      wsQueryFilter.validateNoGroupAttributeName();
      wsQueryFilter.validateNoGroupAttributeValue();
      wsQueryFilter.validateNoQueryFilter0();
      wsQueryFilter.validateNoQueryFilter1();
      wsQueryFilter.validateNoStemName();
      wsQueryFilter.validateNoStemNameScope();
      wsQueryFilter.validateNoGroupTypeName();
      wsQueryFilter.validateShouldHavePagingSorting(false);

    }

    /**
     * return the query filter
     * @param wsQueryFilter
     * @return the query filter
     */
    @Override
    public QueryFilter<Group> retrieveQueryFilter(WsQueryFilter wsQueryFilter) {
      return new GroupUuidFilter(wsQueryFilter.getGroupUuid());
    }

  },

  /**
   * find by exact name, pass the name in
   */
  FIND_BY_GROUP_NAME_EXACT {

    /**
     * make sure that based on the inputs, that this is a valid query
     * @param wsQueryFilter is the query params to validate based on type
     * @throws WsInvalidQueryException if invalid
     */
    @Override
    public void validate(WsQueryFilter wsQueryFilter) throws WsInvalidQueryException {

      //for a name, needs the name and nothing else
      wsQueryFilter.validateNoTypeOfGroups();
      wsQueryFilter.validateHasGroupName();
      wsQueryFilter.validateNoGroupUuid();
      wsQueryFilter.validateNoGroupAttributeName();
      wsQueryFilter.validateNoGroupAttributeValue();
      wsQueryFilter.validateNoQueryFilter0();
      wsQueryFilter.validateNoQueryFilter1();
      wsQueryFilter.validateNoStemName();
      wsQueryFilter.validateNoStemNameScope();
      wsQueryFilter.validateNoGroupTypeName();
      wsQueryFilter.validateShouldHavePagingSorting(false);

    }

    /**
     * return the query filter
     * @param wsQueryFilter
     * @return the query filter
     */
    @Override
    public QueryFilter retrieveQueryFilter(WsQueryFilter wsQueryFilter) {
      return new GroupNameExactFilter(wsQueryFilter.getGroupName());
    }

  },

  /**
   * find by approx name, pass the name in, and optionally a stem name
   */
  FIND_BY_GROUP_NAME_APPROXIMATE {

    /**
     * make sure that based on the inputs, that this is a valid query
     * @param wsQueryFilter is the query params to validate based on type
     * @throws WsInvalidQueryException if invalid
     */
    @Override
    public void validate(WsQueryFilter wsQueryFilter) throws WsInvalidQueryException {

      //for a name, needs the name and nothing else
      //optional wsQueryFilter.validateNoTypeOfGroup();
      wsQueryFilter.validateHasGroupName();
      wsQueryFilter.validateNoGroupUuid();
      wsQueryFilter.validateNoGroupAttributeName();
      wsQueryFilter.validateNoGroupAttributeValue();
      wsQueryFilter.validateNoQueryFilter0();
      wsQueryFilter.validateNoQueryFilter1();
      //optional wsQueryFilter.validateNoStemName();
      wsQueryFilter.validateNoStemNameScope();
      wsQueryFilter.validateNoGroupTypeName();
      wsQueryFilter.validateShouldHavePagingSorting(true);

    }

    /**
     * return the query filter
     * @param wsQueryFilter
     * @return the query filter
     */
    @Override
    public QueryFilter retrieveQueryFilter(WsQueryFilter wsQueryFilter) {
      
      Stem stem = wsQueryFilter.retrieveStem();
      if (stem == null) {
        //if not passed in, then use root
        stem = StemFinder.findRootStem(wsQueryFilter.retrieveGrouperSession());
      }
      String groupName = wsQueryFilter.getGroupName();
      String sortString = wsQueryFilter.getSortString();
      Boolean retrieveAscending = wsQueryFilter.retrieveAscending();
      Integer retrievePageNumber = wsQueryFilter.retrievePageNumber();
      Integer retrievePageSize = wsQueryFilter.retrievePageSize();
      return new GroupNameFilter(groupName, stem, 
          sortString, retrieveAscending, 
          retrievePageNumber, retrievePageSize, wsQueryFilter.retrieveTypeOfGroups());

    }

  },

  /**
   * find by children of stem.  pass the stem in
   */
  FIND_BY_STEM_NAME {

    /**
     * make sure that based on the inputs, that this is a valid query
     * @param wsQueryFilter is the query params to validate based on type
     * @throws WsInvalidQueryException if invalid
     */
    @Override
    public void validate(WsQueryFilter wsQueryFilter) throws WsInvalidQueryException {

      //for a stem name, needs the stem name and optional scope
      //optional wsQueryFilter.validateNoTypeOfGroup();
      wsQueryFilter.validateNoGroupName();
      wsQueryFilter.validateNoGroupUuid();
      wsQueryFilter.validateNoGroupAttributeName();
      wsQueryFilter.validateNoGroupAttributeValue();
      wsQueryFilter.validateNoQueryFilter0();
      wsQueryFilter.validateNoQueryFilter1();
      wsQueryFilter.validateHasStemName();
      // optional wsQueryFilter.validateNoStemNameScope();
      wsQueryFilter.validateNoGroupTypeName();
      wsQueryFilter.validateShouldHavePagingSorting(true);

    }

    /**
     * return the query filter
     * @param wsQueryFilter
     * @return the query filter
     */
    @Override
    public QueryFilter retrieveQueryFilter(WsQueryFilter wsQueryFilter) {

      Scope scope = wsQueryFilter.retrieveStemScope(StemScope.ONE_LEVEL).convertToScope();

      //fail if the stem is not found, that is probably bad
      return new GroupsInStemFilter(wsQueryFilter.getStemName(), scope, false, 
          wsQueryFilter.getSortString(), wsQueryFilter.retrieveAscending(), 
          wsQueryFilter.retrievePageNumber(), wsQueryFilter.retrievePageSize(), 
          wsQueryFilter.retrieveTypeOfGroups());
    }

  },

  /**
     * find by query, configure all the query params
     */FIND_BY_APPROXIMATE_ATTRIBUTE{
  
      /**
       * make sure that based on the inputs, that this is a valid query
       * @param wsQueryFilter is the query params to validate based on type
       * @throws WsInvalidQueryException if invalid
       */
      @Override
      public void validate(WsQueryFilter wsQueryFilter) throws WsInvalidQueryException {
  
        //for a attribute, need value, optional name, and optional stem name
        wsQueryFilter.validateNoTypeOfGroups();
        wsQueryFilter.validateNoGroupName();
        wsQueryFilter.validateNoGroupUuid();
        // optional wsQueryFilter.validateHasGroupAttributeName();
        wsQueryFilter.validateHasGroupAttributeValue();
        wsQueryFilter.validateNoQueryFilter0();
        wsQueryFilter.validateNoQueryFilter1();
        // optional wsQueryFilter.validateNoStemName();
        wsQueryFilter.validateNoStemNameScope();
        wsQueryFilter.validateNoGroupTypeName();
        wsQueryFilter.validateShouldHavePagingSorting(false);
      }
  
      /**
       * return the query filter
       * @param wsQueryFilter
       * @return the query filter
       */
      @Override
      public QueryFilter retrieveQueryFilter(WsQueryFilter wsQueryFilter) {
        Stem stem = wsQueryFilter.retrieveStem();
        if (stem == null) {
          //if not passed in, then use root
          stem = StemFinder.findRootStem(wsQueryFilter.retrieveGrouperSession());
        }
        if (!StringUtils.isBlank(wsQueryFilter.getGroupAttributeName())) {
          return new GroupAttributeFilter(wsQueryFilter.getGroupAttributeName(),
              wsQueryFilter.getGroupAttributeValue(), stem);
        }
        return new GroupAnyAttributeFilter(wsQueryFilter.getGroupAttributeValue(), stem);
      }
  
    }, 
  /**
   * find by exact attribute, configure all the query params
   */
  FIND_BY_EXACT_ATTRIBUTE {

    /**
     * make sure that based on the inputs, that this is a valid query
     * @param wsQueryFilter is the query params to validate based on type
     * @throws WsInvalidQueryException if invalid
     */
    @Override
    public void validate(WsQueryFilter wsQueryFilter) throws WsInvalidQueryException {

      //for a attribute, need value, name, and optional stem name
      wsQueryFilter.validateNoTypeOfGroups();
      wsQueryFilter.validateNoGroupName();
      wsQueryFilter.validateNoGroupUuid();
      wsQueryFilter.validateHasGroupAttributeName();
      wsQueryFilter.validateHasGroupAttributeValue();
      wsQueryFilter.validateNoQueryFilter0();
      wsQueryFilter.validateNoQueryFilter1();
      // optional wsQueryFilter.validateNoStemName();
      wsQueryFilter.validateNoStemNameScope();
      wsQueryFilter.validateNoGroupTypeName();
      wsQueryFilter.validateShouldHavePagingSorting(false);
    }

    /**
     * return the query filter
     * @param wsQueryFilter
     * @return the query filter
     */
    @Override
    public QueryFilter retrieveQueryFilter(WsQueryFilter wsQueryFilter) {
      Stem stem = wsQueryFilter.retrieveStem();
      if (stem == null) {
        //if not passed in, then use root
        stem = StemFinder.findRootStem(wsQueryFilter.retrieveGrouperSession());
      }
      return new GroupAttributeExactFilter(wsQueryFilter.getGroupAttributeName(),
          wsQueryFilter.getGroupAttributeValue(), stem);
    }

  },

  /**
   * find by group type
   */
  FIND_BY_TYPE {

    /**
     * make sure that based on the inputs, that this is a valid query
     * @param wsQueryFilter is the query params to validate based on type
     * @throws WsInvalidQueryException if invalid
     */
    @Override
    public void validate(WsQueryFilter wsQueryFilter) throws WsInvalidQueryException {

      //for a type, needs type and optional stem
      wsQueryFilter.validateNoGroupName();
      wsQueryFilter.validateNoTypeOfGroups();
      wsQueryFilter.validateNoGroupUuid();
      wsQueryFilter.validateNoGroupAttributeName();
      wsQueryFilter.validateNoGroupAttributeValue();
      wsQueryFilter.validateNoQueryFilter0();
      wsQueryFilter.validateNoQueryFilter1();
      // optional wsQueryFilter.validateNoStemName();
      wsQueryFilter.validateNoStemNameScope();
      wsQueryFilter.validateHasGroupTypeName();
      wsQueryFilter.validateShouldHavePagingSorting(false);

    }

    /**
     * return the query filter
     * @param wsQueryFilter
     * @return the query filter
     */
    @Override
    public QueryFilter retrieveQueryFilter(WsQueryFilter wsQueryFilter) {

      Stem stem = wsQueryFilter.retrieveStem();
      if (stem == null) {
        //if not passed in, then use root
        stem = StemFinder.findRootStem(wsQueryFilter.retrieveGrouperSession());
      }
      GroupType groupType = wsQueryFilter.retrieveGroupType();

      return new GroupTypeFilter(groupType, stem);
    }

  },

  /**
   * and two queries together
   */
  AND {

    /**
     * make sure that based on the inputs, that this is a valid query
     * @param wsQueryFilter is the query params to validate based on type
     * @throws WsInvalidQueryException if invalid
     */
    @Override
    public void validate(WsQueryFilter wsQueryFilter) throws WsInvalidQueryException {

      //for an and, needs almost nothing, but needs the queryFilter0 and queryFilter1
      wsQueryFilter.validateNoTypeOfGroups();
      wsQueryFilter.validateNoGroupName();
      wsQueryFilter.validateNoGroupUuid();
      wsQueryFilter.validateNoGroupAttributeName();
      wsQueryFilter.validateNoGroupAttributeValue();
      wsQueryFilter.validateHasQueryFilter0();
      wsQueryFilter.validateHasQueryFilter1();
      wsQueryFilter.validateNoStemName();
      wsQueryFilter.validateNoStemNameScope();
      wsQueryFilter.validateNoGroupTypeName();
      wsQueryFilter.validateShouldHavePagingSorting(false);

    }

    /**
     * return the query filter
     * @param wsQueryFilter
     * @return the query filter
     */
    @Override
    public QueryFilter retrieveQueryFilter(WsQueryFilter wsQueryFilter) {
      return new IntersectionFilter(wsQueryFilter.getQueryFilter0().retrieveQueryFilter(),
          wsQueryFilter.getQueryFilter1().retrieveQueryFilter());
    }

  },

  /**
   * or two queries together
   */
  OR {

    /**
     * make sure that based on the inputs, that this is a valid query
     * @param wsQueryFilter is the query params to validate based on type
     * @throws WsInvalidQueryException if invalid
     */
    @Override
    public void validate(WsQueryFilter wsQueryFilter) throws WsInvalidQueryException {

      //for an or, needs nothing but queryFilter0 and queryFilter1
      wsQueryFilter.validateNoTypeOfGroups();
      wsQueryFilter.validateNoGroupName();
      wsQueryFilter.validateNoGroupUuid();
      wsQueryFilter.validateNoGroupAttributeName();
      wsQueryFilter.validateNoGroupAttributeValue();
      wsQueryFilter.validateHasQueryFilter0();
      wsQueryFilter.validateHasQueryFilter1();
      wsQueryFilter.validateNoStemName();
      wsQueryFilter.validateNoStemNameScope();
      wsQueryFilter.validateNoGroupTypeName();

      wsQueryFilter.validateShouldHavePagingSorting(false);
    }

    /**
     * return the query filter
     * @param wsQueryFilter
     * @return the query filter
     */
    @Override
    public QueryFilter retrieveQueryFilter(WsQueryFilter wsQueryFilter) {
      return new UnionFilter(
          wsQueryFilter.getQueryFilter0().retrieveQueryFilter(), wsQueryFilter
              .getQueryFilter1().retrieveQueryFilter());
    }

  },

  /**
   * complement one query, set A minus set B
   */
  MINUS {

    /**
     * make sure that based on the inputs, that this is a valid query
     * @param wsQueryFilter is the query params to validate based on type
     * @throws WsInvalidQueryException if invalid
     */
    @Override
    public void validate(WsQueryFilter wsQueryFilter) throws WsInvalidQueryException {

      //for a minus, needs nothing but queryFilter0 and queryFilter1
      wsQueryFilter.validateNoTypeOfGroups();
      wsQueryFilter.validateNoGroupName();
      wsQueryFilter.validateNoGroupUuid();
      wsQueryFilter.validateNoGroupAttributeName();
      wsQueryFilter.validateNoGroupAttributeValue();
      wsQueryFilter.validateHasQueryFilter0();
      wsQueryFilter.validateHasQueryFilter1();
      wsQueryFilter.validateNoStemName();
      wsQueryFilter.validateNoStemNameScope();
      wsQueryFilter.validateNoGroupTypeName();
      wsQueryFilter.validateShouldHavePagingSorting(false);

    }

    /**
     * return the query filter
     * @param wsQueryFilter
     * @return the query filter
     */
    @Override
    public QueryFilter retrieveQueryFilter(WsQueryFilter wsQueryFilter) {
      return new ComplementFilter(wsQueryFilter.getQueryFilter0().retrieveQueryFilter(),
          wsQueryFilter.getQueryFilter1().retrieveQueryFilter());
    }

  };

  /**
   * make sure that based on the inputs, that this is a valid query
   * @param wsGroupQuery is the query params to validate based on type
   * @throws WsInvalidQueryException if invalid
   */
  public abstract void validate(WsQueryFilter wsGroupQuery)
      throws WsInvalidQueryException;

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @return the enum or null or exception if not found
   */
  public static WsQueryFilterType valueOfIgnoreCase(String string) {
    return GrouperServiceUtils.enumValueOfIgnoreCase(WsQueryFilterType.class, string, false);
  }

  /**
   * return the query filter
   * @param wsQueryFilter
   * @return the query filter
   */
  public abstract QueryFilter retrieveQueryFilter(WsQueryFilter wsQueryFilter);

}
