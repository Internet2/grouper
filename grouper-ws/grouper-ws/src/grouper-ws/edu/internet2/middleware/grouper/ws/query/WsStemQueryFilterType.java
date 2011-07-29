/*
 * @author mchyzer $Id: WsStemQueryFilterType.java,v 1.4 2009-03-15 08:15:37 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.query;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.filter.ComplementFilter;
import edu.internet2.middleware.grouper.filter.IntersectionFilter;
import edu.internet2.middleware.grouper.filter.QueryFilter;
import edu.internet2.middleware.grouper.filter.StemAnyAttributeFilter;
import edu.internet2.middleware.grouper.filter.StemAttributeFilter;
import edu.internet2.middleware.grouper.filter.StemNameAnyFilter;
import edu.internet2.middleware.grouper.filter.StemNameExactFilter;
import edu.internet2.middleware.grouper.filter.StemUuidFilter;
import edu.internet2.middleware.grouper.filter.StemsInStemFilter;
import edu.internet2.middleware.grouper.filter.UnionFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemQueryFilter;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * type of find group queries
 */
public enum WsStemQueryFilterType {

  /**
   * find by uuid.  pass the uuid in.
   * uuid is requried, all other params are forbidden
   */
  FIND_BY_STEM_UUID {

    /**
     * make sure that based on the inputs, that this is a valid query
     * @param wsStemQueryFilter is the query params to validate based on type
     * @throws WsInvalidQueryException if invalid
     */
    @Override
    public void validate(WsStemQueryFilter wsStemQueryFilter)
        throws WsInvalidQueryException {

      //for a uuid, needs the uuid and nothing else
      wsStemQueryFilter.validateNoParentStemName();
      wsStemQueryFilter.validateNoParentStemNameScope();
      wsStemQueryFilter.validateNoStemAttributeName();
      wsStemQueryFilter.validateNoStemAttributeValue();
      wsStemQueryFilter.validateNoStemName();
      wsStemQueryFilter.validateNoStemQueryFilter0();
      wsStemQueryFilter.validateNoStemQueryFilter1();
      wsStemQueryFilter.validateHasStemUuid();

    }

    /**
     * return the query filter
     * @param wsStemQueryFilter
     * @return the query filter
     */
    @Override
    public QueryFilter retrieveQueryFilter(WsStemQueryFilter wsStemQueryFilter) {
      return new StemUuidFilter(wsStemQueryFilter.getStemUuid());
    }

  },

  /**
   * find by exact name, pass the name in.
   * stem name is required, all others forbidden
   */
  FIND_BY_STEM_NAME {

    /**
     * make sure that based on the inputs, that this is a valid query
     * @param wsStemQueryFilter is the query params to validate based on type
     * @throws WsInvalidQueryException if invalid
     */
    @Override
    public void validate(WsStemQueryFilter wsStemQueryFilter)
        throws WsInvalidQueryException {

      //for a name, needs the name and nothing else
      wsStemQueryFilter.validateNoParentStemName();
      wsStemQueryFilter.validateNoParentStemNameScope();
      wsStemQueryFilter.validateNoStemAttributeName();
      wsStemQueryFilter.validateNoStemAttributeValue();
      wsStemQueryFilter.validateHasStemName();
      wsStemQueryFilter.validateNoStemQueryFilter0();
      wsStemQueryFilter.validateNoStemQueryFilter1();
      wsStemQueryFilter.validateNoStemUuid();

    }

    /**
     * return the query filter
     * @param wsStemQueryFilter
     * @return the query filter
     */
    @Override
    public QueryFilter retrieveQueryFilter(WsStemQueryFilter wsStemQueryFilter) {
      return new StemNameExactFilter(wsStemQueryFilter.getStemName());
    }

  },

  /**
   * find by approx name, pass the name in.
   * stem name is required, optionally pass in parentStem name
   */
  FIND_BY_STEM_NAME_APPROXIMATE {

    /**
     * make sure that based on the inputs, that this is a valid query
     * @param wsStemQueryFilter is the query params to validate based on type
     * @throws WsInvalidQueryException if invalid
     */
    @Override
    public void validate(WsStemQueryFilter wsStemQueryFilter)
        throws WsInvalidQueryException {

      //for a name, needs the name and optionally parent stem name
      // optional wsStemQueryFilter.validateNoParentStemName();
      wsStemQueryFilter.validateNoParentStemNameScope();
      wsStemQueryFilter.validateNoStemAttributeName();
      wsStemQueryFilter.validateNoStemAttributeValue();
      wsStemQueryFilter.validateHasStemName();
      wsStemQueryFilter.validateNoStemQueryFilter0();
      wsStemQueryFilter.validateNoStemQueryFilter1();
      wsStemQueryFilter.validateNoStemUuid();

    }

    /**
     * return the query filter
     * @param wsStemQueryFilter
     * @return the query filter
     */
    @Override
    public QueryFilter retrieveQueryFilter(WsStemQueryFilter wsStemQueryFilter) {
      
      Stem stem = wsStemQueryFilter.retrieveParentStem();
      if (stem == null) {
        //if not passed in, then use root
        stem = StemFinder.findRootStem(wsStemQueryFilter.retrieveGrouperSession());
      }
      
      return new StemNameAnyFilter(wsStemQueryFilter.getStemName(), stem);
    }

  },

  /**
   * find by children of stem.
   * parentStemName is required, parentStemNameScope is optional.
   * all others forbidden.
   */
  FIND_BY_PARENT_STEM_NAME {

    /**
     * make sure that based on the inputs, that this is a valid query
     * @param wsStemQueryFilter is the query params to validate based on type
     * @throws WsInvalidQueryException if invalid
     */
    @Override
    public void validate(WsStemQueryFilter wsStemQueryFilter)
        throws WsInvalidQueryException {

      //for a stem name, needs the stem name and optional scope
      wsStemQueryFilter.validateHasParentStemName();
      // optional wsStemQueryFilter.validateNoParentStemNameScope();
      wsStemQueryFilter.validateNoStemAttributeName();
      wsStemQueryFilter.validateNoStemAttributeValue();
      wsStemQueryFilter.validateNoStemName();
      wsStemQueryFilter.validateNoStemQueryFilter0();
      wsStemQueryFilter.validateNoStemQueryFilter1();
      wsStemQueryFilter.validateNoStemUuid();

    }

    /**
     * return the query filter
     * @param wsStemQueryFilter
     * @return the query filter
     */
    @Override
    public QueryFilter retrieveQueryFilter(WsStemQueryFilter wsStemQueryFilter) {

      StemScope stemScope = StemScope.valueOfIgnoreCase(wsStemQueryFilter.getParentStemNameScope());
      
      stemScope = GrouperUtil.defaultIfNull(stemScope, StemScope.ONE_LEVEL);
      
      Scope scope = wsStemQueryFilter.retrieveStemScope(stemScope)
          .convertToScope();

      //fail if the stem is not found, that is probably bad
      return new StemsInStemFilter(wsStemQueryFilter.getParentStemName(), scope, false);
    }

  },

  /**
   * find by query, configure all the query params.
   * attribute value is required, parentStemName is optional, 
   * attributeName is optional (defaults to all)
   */
  FIND_BY_APPROXIMATE_ATTRIBUTE {

    /**
     * make sure that based on the inputs, that this is a valid query
     * @param wsStemQueryFilter is the query params to validate based on type
     * @throws WsInvalidQueryException if invalid
     */
    @Override
    public void validate(WsStemQueryFilter wsStemQueryFilter)
        throws WsInvalidQueryException {

      //for a attribute, need value, optional name, and optional stem name
      // optional wsStemQueryFilter.validateNoParentStemName();
      wsStemQueryFilter.validateNoParentStemNameScope();
      // optional wsStemQueryFilter.validateNoStemAttributeName();
      wsStemQueryFilter.validateHasStemAttributeValue();
      wsStemQueryFilter.validateNoStemName();
      wsStemQueryFilter.validateNoStemQueryFilter0();
      wsStemQueryFilter.validateNoStemQueryFilter1();
      wsStemQueryFilter.validateNoStemUuid();
    }

    /**
     * return the query filter
     * @param wsStemQueryFilter
     * @return the query filter
     */
    @Override
    public QueryFilter retrieveQueryFilter(WsStemQueryFilter wsStemQueryFilter) {
      Stem stem = wsStemQueryFilter.retrieveParentStem();
      if (stem == null) {
        //if not passed in, then use root
        stem = StemFinder.findRootStem(wsStemQueryFilter.retrieveGrouperSession());
      }

      if (!StringUtils.isBlank(wsStemQueryFilter.getStemAttributeName())) {
        return new StemAttributeFilter(wsStemQueryFilter.getStemAttributeName(),
            wsStemQueryFilter.getStemAttributeValue(), stem);
      }
      return new StemAnyAttributeFilter(wsStemQueryFilter.getStemAttributeValue(), stem);
    }

  },

  /**
   * and two queries together
   */
  AND {

    /**
     * make sure that based on the inputs, that this is a valid query
     * stemQueryFilter0 and stemQueryFilter1 are required, all others forbidden
     * @param wsStemQueryFilter is the query params to validate based on type
     * @throws WsInvalidQueryException if invalid
     */
    @Override
    public void validate(WsStemQueryFilter wsStemQueryFilter)
        throws WsInvalidQueryException {

      //for an and, needs almost nothing, but needs the queryFilter0 and queryFilter1
      wsStemQueryFilter.validateNoParentStemName();
      wsStemQueryFilter.validateNoParentStemNameScope();
      wsStemQueryFilter.validateNoStemAttributeName();
      wsStemQueryFilter.validateNoStemAttributeValue();
      wsStemQueryFilter.validateNoStemName();
      wsStemQueryFilter.validateHasStemQueryFilter0();
      wsStemQueryFilter.validateHasStemQueryFilter1();
      wsStemQueryFilter.validateNoStemUuid();

    }

    /**
     * return the query filter
     * @param wsStemQueryFilter
     * @return the query filter
     */
    @Override
    public QueryFilter retrieveQueryFilter(WsStemQueryFilter wsStemQueryFilter) {
      return new IntersectionFilter(wsStemQueryFilter.getStemQueryFilter0()
          .retrieveQueryFilter(), wsStemQueryFilter.getStemQueryFilter1()
          .retrieveQueryFilter());
    }

  },

  /**
   * or two queries together
   */
  OR {

    /**
     * make sure that based on the inputs, that this is a valid query.
     * stemQueryFilter0 and stemQueryFilter1 are required, all others forbidden
     * @param wsStemQueryFilter is the query params to validate based on type
     * @throws WsInvalidQueryException if invalid
     */
    @Override
    public void validate(WsStemQueryFilter wsStemQueryFilter)
        throws WsInvalidQueryException {

      //for an or, needs nothing but queryFilter0 and queryFilter1
      wsStemQueryFilter.validateNoParentStemName();
      wsStemQueryFilter.validateNoParentStemNameScope();
      wsStemQueryFilter.validateNoStemAttributeName();
      wsStemQueryFilter.validateNoStemAttributeValue();
      wsStemQueryFilter.validateNoStemName();
      wsStemQueryFilter.validateHasStemQueryFilter0();
      wsStemQueryFilter.validateHasStemQueryFilter1();
      wsStemQueryFilter.validateNoStemUuid();

    }

    /**
     * return the query filter
     * @param wsStemQueryFilter
     * @return the query filter
     */
    @Override
    public QueryFilter retrieveQueryFilter(WsStemQueryFilter wsStemQueryFilter) {
      return new UnionFilter(wsStemQueryFilter.getStemQueryFilter0()
          .retrieveQueryFilter(), wsStemQueryFilter.getStemQueryFilter1()
          .retrieveQueryFilter());
    }

  },

  /**
   * complement one query, set A minus set B
   */
  MINUS {

    /**
     * make sure that based on the inputs, that this is a valid query
     * stemQueryFilter0 and stemQueryFilter1 are required, all others forbidden
     * @param wsStemQueryFilter is the query params to validate based on type
     * @throws WsInvalidQueryException if invalid
     */
    @Override
    public void validate(WsStemQueryFilter wsStemQueryFilter)
        throws WsInvalidQueryException {

      //for a minus, needs nothing but queryFilter0 and queryFilter1
      wsStemQueryFilter.validateNoParentStemName();
      wsStemQueryFilter.validateNoParentStemNameScope();
      wsStemQueryFilter.validateNoStemAttributeName();
      wsStemQueryFilter.validateNoStemAttributeValue();
      wsStemQueryFilter.validateNoStemName();
      wsStemQueryFilter.validateHasStemQueryFilter0();
      wsStemQueryFilter.validateHasStemQueryFilter1();
      wsStemQueryFilter.validateNoStemUuid();

    }

    /**
     * return the query filter
     * @param wsStemQueryFilter
     * @return the query filter
     */
    @Override
    public QueryFilter retrieveQueryFilter(WsStemQueryFilter wsStemQueryFilter) {
      return new ComplementFilter(wsStemQueryFilter.getStemQueryFilter0()
          .retrieveQueryFilter(), wsStemQueryFilter.getStemQueryFilter1()
          .retrieveQueryFilter());
    }

  };

  /**
   * make sure that based on the inputs, that this is a valid query
   * @param wsStemQuery is the query params to validate based on type
   * @throws WsInvalidQueryException if invalid
   */
  public abstract void validate(WsStemQueryFilter wsStemQuery)
      throws WsInvalidQueryException;

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @return the enum or null or exception if not found
   */
  public static WsStemQueryFilterType valueOfIgnoreCase(String string) {
    return GrouperServiceUtils.enumValueOfIgnoreCase(WsStemQueryFilterType.class, 
        string, false);
  }

  /**
   * return the query filter
   * @param wsStemQueryFilter
   * @return the query filter
   */
  public abstract QueryFilter retrieveQueryFilter(WsStemQueryFilter wsStemQueryFilter);

}
