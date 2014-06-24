/*******************************************************************************
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.ui.text;

/**
 * bean for bundles
 */
public class TextBundleBean {

  /** country e.g. us */
  private String country;

  /** language e.g. en */
  private String language;

  /** filename in the package tfText that is before the .base.properties, and .properties */
  private String fileNamePrefix;

  /**
   * country e.g. us
   * @return the country
   */
  public String getCountry() {
    return this.country;
  }

  /**
   * country e.g. us
   * @param country1 the country to set
   */
  public void setCountry(String country1) {
    this.country = country1;
  }

  /**
   * language e.g. en
   * @return the language
   */
  public String getLanguage() {
    return this.language;
  }

  /**
   * language e.g. en
   * @param language1 the language to set
   */
  public void setLanguage(String language1) {
    this.language = language1;
  }

  /**
   * filename in the package tfText that is before the .base.properties, and .properties
   * @return the fileNamePrefix
   */
  public String getFileNamePrefix() {
    return this.fileNamePrefix;
  }

  /**
   * filename in the package tfText that is before the .base.properties, and .properties
   * @param fileNamePrefix1 the fileNamePrefix to set
   */
  public void setFileNamePrefix(String fileNamePrefix1) {
    this.fileNamePrefix = fileNamePrefix1;
  }

}
