package edu.internet2.middleware.grouper.shibboleth.dataConnector.field;

import java.util.Arrays;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.SchemaException;

public abstract class BaseMembershipField extends BaseField {

  /** the field */
  private Field field;

  /** the member filter */
  private FieldMemberFilter memberFilter;

  /**
   * @see edu.internet2.middleware.grouper.shibboleth.dataConnector.field.BaseField#constructor(String id)
   */
  public BaseMembershipField(String id) throws GrouperException {
    super(id);

    // by default return all members
    memberFilter = FieldMemberFilter.all;

    // if filter element exists
    if (this.getIdElements().size() > 1) {
      try {
        memberFilter = FieldMemberFilter.valueOf(this.getSecondIdElement());
      } catch (IllegalArgumentException e) {
        throw new GrouperException("Unknown filter value, should be one of "
            + Arrays.asList(FieldMemberFilter.values()), e);
      }
    }

    // by default the field is "members"
    String fieldName = GrouperConfig.LIST;

    // if field name exists
    if (this.getIdElements().size() == 3) {
      fieldName = this.getThirdIdElement();
    }

    try {
      field = FieldFinder.find(fieldName, true);
    } catch (SchemaException e) {
      throw new GrouperException("Unknown field '" + fieldName + "'", e);
    }
  }

  /**
   * Get the underlying {@link Field}.
   * 
   * @return the field
   */
  public Field getField() {
    return field;
  }

  /**
   * Get the filter responsible for returning members or groups.
   * 
   * @return the member filter
   */
  public FieldMemberFilter getMemberFilter() {
    return memberFilter;
  }

}
