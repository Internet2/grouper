/*
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.ldappc.spml.request;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openspml.v2.msg.spmlbatch.BatchRequest;
import org.openspml.v2.msg.spmlbatch.OnError;

/**
 *
 */
public abstract class BulkProvisioningRequest extends ProvisioningRequest {

  // TODO extend BatchRequest ?

  // TODO use the same dateTime implementation as SPML, e.g. UpdatesRequest, when
  // available

  /** The format for the updated since time. */
  public static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'_'HH:mm:ss");
  static {
    df.setLenient(true);
  }

  /** What to do on error, copied from {@link BatchRequest}. */
  private OnError m_onError = OnError.RESUME;

  /** The updated since time as a string. */
  private String m_updatedSince = null;

  /** The updated since time as a <code>Date</code>. */
  private Date updatedSinceDate = null;

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BulkProvisioningRequest)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    final BulkProvisioningRequest that = (BulkProvisioningRequest) o;

    if (m_onError != null ? !m_onError.equals(that.m_onError) : that.m_onError != null) {
      return false;
    }
    if (m_updatedSince != null ? !m_updatedSince.equals(that.m_updatedSince) : that.m_updatedSince != null) {
      return false;
    }

    return true;
  }

  public OnError getOnError() {
    return m_onError;
  }

  public String getUpdatedSince() {
    return m_updatedSince;
  }

  public Date getUpdatedSinceAsDate() {
    return updatedSinceDate;
  }

  public int hashCode() {
    int result = super.hashCode();
    result = 29 * result + (m_onError != null ? m_onError.hashCode() : 0);
    result = 29 * result + (m_updatedSince != null ? m_updatedSince.hashCode() : 0);
    return result;
  }

  public void setOnError(OnError onError) {
    m_onError = onError;
  }

  public void setUpdatedSince(Date updatedSince) {
    if (updatedSince != null) {
      m_updatedSince = df.format(updatedSince);
    }
    updatedSinceDate = updatedSince;
  }

  public void setUpdatedSince(String updatedSince) throws ParseException {
    if (updatedSince != null) {
      // TODO update date parsing code, from the old ldappc
      StringBuffer dateInput = new StringBuffer(updatedSince);
      int len = dateInput.length();
      if (len == 10) {
        dateInput.append("_00:00:00");
      } else if (len == 13) {
        dateInput.append(":00:00");
      } else if (len == 16) {
        dateInput.append(":00");
      }
      updatedSinceDate = df.parse(dateInput.toString());
    }
    m_updatedSince = updatedSince;
  }
  
  public String toString() {
    ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
    toStringBuilder.appendSuper(super.toString());
    toStringBuilder.append("onError", this.getOnError());
    toStringBuilder.append("updatedSince", this.getUpdatedSince());
    return toStringBuilder.toString();
  }
}
