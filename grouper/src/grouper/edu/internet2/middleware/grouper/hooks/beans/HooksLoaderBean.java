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
 * @author mchyzer
 * $Id: HooksLoaderBean.java,v 1.2 2009-04-28 20:08:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import java.util.Set;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.app.loader.LoaderJobBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * bean to hold objects for group low level hooks
 */
@GrouperIgnoreDbVersion
public class HooksLoaderBean extends HooksBean {
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: loaderJobBean */
  public static final String FIELD_LOADER_JOB_BEAN = "loaderJobBean";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_LOADER_JOB_BEAN);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /**
   * loader data
   */
  private LoaderJobBean loaderJobBean;

  /**
   * loader data 
   * @return loader data
   */
  public LoaderJobBean getLoaderJobBean() {
    return this.loaderJobBean;
  }

  /**
   * 
   */
  public HooksLoaderBean() {
    super();
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksLoaderBean clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }


  /**
   * @param loaderJobBean1
   */
  public HooksLoaderBean(LoaderJobBean loaderJobBean1) {
    this.loaderJobBean = loaderJobBean1;
  }
}
