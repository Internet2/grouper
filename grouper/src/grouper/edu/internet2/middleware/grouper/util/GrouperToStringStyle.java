/**
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
 */
/*
 * @author mchyzer
 * $Id: GrouperToStringStyle.java,v 1.2 2008-10-27 21:27:46 mchyzer Exp $
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
    String className = value.getClass().getName();
    if (!(className.startsWith("java.lang") || className.startsWith("java.util"))) {
      buffer.append("\n  ");
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
    buffer.append("\n");
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
