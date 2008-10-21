/*
 * @author mchyzer
 * $Id: GrouperToStringStyle.java,v 1.1 2008-10-21 03:51:03 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.util;

import org.apache.commons.lang.builder.ToStringStyle;


/**
 * style for printing out objects
 */
@SuppressWarnings("serial")
public class GrouperToStringStyle extends ToStringStyle {

  /**
   * 
   * @see org.apache.commons.lang.builder.ToStringStyle#append(java.lang.StringBuffer, java.lang.String, boolean[], java.lang.Boolean)
   */
  @Override
  public void append(StringBuffer buffer, String fieldName, boolean[] array,
      Boolean fullDetail) {
    if (array == null) {
      return;
    }
    super.append(buffer, fieldName, array, fullDetail);
  }

  /**
   * 
   * @see org.apache.commons.lang.builder.ToStringStyle#append(java.lang.StringBuffer, java.lang.String, byte[], java.lang.Boolean)
   */
  @Override
  public void append(StringBuffer buffer, String fieldName, byte[] array,
      Boolean fullDetail) {
    if (array == null) {
      return;
    }
    super.append(buffer, fieldName, array, fullDetail);
  }

  /**
   * 
   * @see org.apache.commons.lang.builder.ToStringStyle#append(java.lang.StringBuffer, java.lang.String, char[], java.lang.Boolean)
   */
  @Override
  public void append(StringBuffer buffer, String fieldName, char[] array,
      Boolean fullDetail) {
    if (array == null) {
      return;
    }
    super.append(buffer, fieldName, array, fullDetail);
  }

  /**
   * 
   * @see org.apache.commons.lang.builder.ToStringStyle#append(java.lang.StringBuffer, java.lang.String, double[], java.lang.Boolean)
   */
  @Override
  public void append(StringBuffer buffer, String fieldName, double[] array,
      Boolean fullDetail) {
    if (array == null) {
      return;
    }
    super.append(buffer, fieldName, array, fullDetail);
  }

  /**
   * 
   * @see org.apache.commons.lang.builder.ToStringStyle#append(java.lang.StringBuffer, java.lang.String, float[], java.lang.Boolean)
   */
  @Override
  public void append(StringBuffer buffer, String fieldName, float[] array,
      Boolean fullDetail) {
    if (array == null) {
      return;
    }
    super.append(buffer, fieldName, array, fullDetail);
  }

  /**
   * 
   * @see org.apache.commons.lang.builder.ToStringStyle#append(java.lang.StringBuffer, java.lang.String, int[], java.lang.Boolean)
   */
  @Override
  public void append(StringBuffer buffer, String fieldName, int[] array,
      Boolean fullDetail) {
    if (array == null) {
      return;
    }
    super.append(buffer, fieldName, array, fullDetail);
  }

  /**
   * 
   * @see org.apache.commons.lang.builder.ToStringStyle#append(java.lang.StringBuffer, java.lang.String, long[], java.lang.Boolean)
   */
  @Override
  public void append(StringBuffer buffer, String fieldName, long[] array,
      Boolean fullDetail) {
    if (array == null) {
      return;
    }
    super.append(buffer, fieldName, array, fullDetail);
  }

  /**
   * 
   * @see org.apache.commons.lang.builder.ToStringStyle#append(java.lang.StringBuffer, java.lang.String, java.lang.Object, java.lang.Boolean)
   */
  @Override
  public void append(StringBuffer buffer, String fieldName, Object value,
      Boolean fullDetail) {
    if (value == null) {
      return;
    }
    super.append(buffer, fieldName, value, fullDetail);
  }

  /**
   * 
   * @see org.apache.commons.lang.builder.ToStringStyle#append(java.lang.StringBuffer, java.lang.String, java.lang.Object[], java.lang.Boolean)
   */
  @Override
  public void append(StringBuffer buffer, String fieldName, Object[] array,
      Boolean fullDetail) {
    if (array == null) {
      return;
    }
    super.append(buffer, fieldName, array, fullDetail);
  }

  /**
   * 
   * @see org.apache.commons.lang.builder.ToStringStyle#append(java.lang.StringBuffer, java.lang.String, short[], java.lang.Boolean)
   */
  @Override
  public void append(StringBuffer buffer, String fieldName, short[] array,
      Boolean fullDetail) {
    if (array == null) {
      return;
    }
    super.append(buffer, fieldName, array, fullDetail);
  }

  /**
   * 
   */
  public GrouperToStringStyle() {
    this.setUseShortClassName(true);
    this.setUseIdentityHashCode(false);
  }

}
