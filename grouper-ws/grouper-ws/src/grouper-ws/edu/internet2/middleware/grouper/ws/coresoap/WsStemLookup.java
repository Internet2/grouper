/*******************************************************************************
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
 ******************************************************************************/
/**
 * 
 */
package edu.internet2.middleware.grouper.ws.coresoap;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemDeleteResult.WsStemDeleteResultCode;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouper.ws.util.GrouperWsToStringCompact;

/**
 * <pre>
 * Class to lookup a stem via web service
 * 
 * developers make sure each setter calls this.clearSubject();
 * TODO: add in extensions in the query
 * </pre>
 * @author mchyzer
 */
public class WsStemLookup implements GrouperWsToStringCompact {

  /**
   * integer ID for object
   */
  private String idIndex;
  
  /**
   * integer ID for object
   * @return the id
   */
  public String getIdIndex() {
    return this.idIndex;
  }

  /**
   * integer ID for object
   * @param idIndex1
   */
  public void setIdIndex(String idIndex1) {
    this.idIndex = idIndex1;
  }

  /**
   * see if blank
   * @return true if blank
   */
  public boolean blank() {
    return StringUtils.isBlank(this.stemName) && StringUtils.isBlank(this.uuid)
      && this.stem == null && this.stemFindResult == null && StringUtils.isBlank(this.idIndex);
  }
  
  /**
   * see if this stem lookup has data
   * @return true if it has data
   */
  public boolean hasData() {
    return !StringUtils.isBlank(this.stemName) || !StringUtils.isBlank(this.uuid)
        || !StringUtils.isBlank(this.idIndex);
  }
  

  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsSubjectLookup.class);

  /** find the stem */
  private Stem stem = null;

  /** result of stem find */
  public static enum StemFindResult {

    /** uuid doesnt match name or id index etc */
    STEM_UUID_NAME_IDINDEX_DONT_MATCH {

      /**
       * convert this code to a delete code
       * @return the code
       */
      @Override
      public WsStemDeleteResultCode convertToDeleteCode() {
        return WsStemDeleteResultCode.STEM_UUID_NAME_IDINDEX_DONT_MATCH;
      }

    },


    /** found the stem */
    SUCCESS {

      /**
       * convert this code to a delete code
       * @return the code
       */
      @Override
      public WsStemDeleteResultCode convertToDeleteCode() {
        return WsStemDeleteResultCode.SUCCESS;
      }

    },

    /** cant find the subject */
    STEM_NOT_FOUND {

      /**
       * convert this code to a delete code
       * @return the code
       */
      @Override
      public WsStemDeleteResultCode convertToDeleteCode() {
        return WsStemDeleteResultCode.SUCCESS_STEM_NOT_FOUND;
      }

    },

    /** incvalid query (e.g. if everything blank) */
    INVALID_QUERY {

      /**
       * convert this code to a delete code
       * @return the code
       */
      @Override
      public WsStemDeleteResultCode convertToDeleteCode() {
        return WsStemDeleteResultCode.INVALID_QUERY;
      }

    };

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS;
    }

    /**
     * convert this code to a delete code
     * @return the code
     */
    public abstract WsStemDeleteResultCode convertToDeleteCode();

    /**
     * null safe equivalent to convertToDeleteCode
     * @param stemFindResult to convert
     * @return the code
     */
    public static WsStemDeleteResultCode convertToDeleteCodeStatic(
        StemFindResult stemFindResult) {
      return stemFindResult == null ? WsStemDeleteResultCode.EXCEPTION : stemFindResult
          .convertToDeleteCode();
    }

  }

  /**
   * uuid of the stem to find
   */
  private String uuid;

  /**
   * <pre>
   * 
   * Note: this is not a javabean property because we dont want it in the web service
   * </pre>
   * @return the stem
   */
  public Stem retrieveStem() {
    return this.stem;
  }

  /**
   * <pre>
   * 
   * Note: this is not a javabean property because we dont want it in the web service
   * </pre>
   * @return the subjectFindResult, this is never null
   */
  public StemFindResult retrieveStemFindResult() {
    return this.stemFindResult;
  }

  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * pass in a grouper session
   * @param grouperSession 
   * @param exceptionOnNull excdeption if nothing in stem
   */
  public void retrieveStemIfNeeded(GrouperSession grouperSession, boolean exceptionOnNull) {
    //see if we already retrieved
    if (this.stemFindResult != null) {
      return;
    }
    try {
      //assume success (set otherwise if there is a problem)
      this.stemFindResult = StemFindResult.SUCCESS;

      boolean hasUuid = !StringUtils.isBlank(this.uuid);

      boolean hasName = !StringUtils.isBlank(this.stemName);

      boolean hasIdIndex = !StringUtils.isBlank(this.idIndex);

      //maybe nothing and thats ok
      if (!exceptionOnNull && !hasUuid && !hasName && !hasIdIndex) {
        return;
      }

      //must have a name or uuid
      if (!hasUuid && !hasName && !hasIdIndex) {
        this.stemFindResult = StemFindResult.INVALID_QUERY;
        String logMessage = "Invalid query: " + this;
        LOG.warn(logMessage);
        return;
      }

      Stem theStem = null;
      
      if (hasName) {
        theStem = StemFinder.findByName(grouperSession, this.stemName, true, new QueryOptions().secondLevelCache(false));
      } else if (hasUuid) {
        theStem = StemFinder.findByUuid(grouperSession, this.uuid, true, new QueryOptions().secondLevelCache(false));
      } else if (hasIdIndex) {
        theStem = StemFinder.findByIdIndex(GrouperUtil.longValue(this.idIndex), true, new QueryOptions().secondLevelCache(false));
      }

      //make sure matches
      if ((hasUuid && !StringUtils.equals(this.uuid, theStem.getUuid()))
          || (hasName && !StringUtils.equals(this.stemName, theStem.getName()))
          || (hasIdIndex && !GrouperUtil.equals(GrouperUtil.longValue(this.idIndex), theStem.getIdIndex()))){
        this.stemFindResult = StemFindResult.STEM_UUID_NAME_IDINDEX_DONT_MATCH;
        String error = "Stem name '" + this.stemName + "', uuid '" + this.uuid
            + "', idIndex: " + this.idIndex + " do not match";
        String logMessage = "Invalid query: " + error + ", " + this;
        LOG.warn(logMessage);
      }
      
      this.stem = theStem;
      
    } catch (StemNotFoundException gnf) {
      this.stemFindResult = StemFindResult.STEM_NOT_FOUND;
    }

  }

  /**
   * clear the subject if a setter is called
   */
  private void clearStem() {
    this.stem = null;
    this.stemFindResult = null;
  }

  /** name of the stem to find (includes stems, e.g. stem1:stem2:stemName */
  private String stemName;

  /** result of subject find */
  private StemFindResult stemFindResult = null;

  /**
   * uuid of the stem to find
   * @return the uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * uuid of the stem to find
   * @param uuid1 the uuid to set
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
    this.clearStem();
  }

  /**
   * name of the stem to find (includes stems, e.g. stem1:stem2:stemName
   * @return the theName
   */
  public String getStemName() {
    return this.stemName;
  }

  /**
   * name of the stem to find (includes stems, e.g. stem1:stem2:stemName
   * @param theName the theName to set
   */
  public void setStemName(String theName) {
    this.stemName = theName;
    this.clearStem();
  }

  /**
   * convert stem lookups to stem ids
   * @param grouperSession
   * @param wsStemLookups
   * @param errorMessage
   * @param lookupCount is an array of size one int where 1 will be added if there are records, and no change if not
   * @return the stem ids
   */
  public static Set<String> convertToStemIds(GrouperSession grouperSession, WsStemLookup[] wsStemLookups, StringBuilder errorMessage, int[] lookupCount) {
    //get all the stems
    //we could probably batch these to get better performance.
    Set<String> stemIds = null;
    if (!GrouperServiceUtils.nullArray(wsStemLookups)) {
      
      stemIds = new LinkedHashSet<String>();
      int i=0;

      boolean foundRecords = false;
      
      for (WsStemLookup wsStemLookup : wsStemLookups) {
        
        if (wsStemLookup == null || !wsStemLookup.hasData()) {
          continue;
        }

        if (!foundRecords) {
          lookupCount[0]++;
          foundRecords = true;
        }

        wsStemLookup.retrieveStemIfNeeded(grouperSession, false);
        Stem stem = wsStemLookup.retrieveStem();
        if (stem != null) {
          stemIds.add(stem.getUuid());
        } else {
          
          if (errorMessage.length() > 0) {
            errorMessage.append(", ");
          }

          errorMessage.append("Error on stem index: " + i + ", " + wsStemLookup.retrieveStemFindResult() + ", " + wsStemLookup.toStringCompact());
        }
        
        i++;
      }
      
    }
    return stemIds;
  }

  /**
   * make sure this is an explicit toString
   * @return return a compact to string
   */
  public String toStringCompact() {
    if (!StringUtils.isBlank(this.stemName)) {
      return "name: " + this.stemName;
    }
    if (!StringUtils.isBlank(this.uuid)) {
      return "id: " + this.uuid;
    }
    if (!StringUtils.isBlank(this.idIndex)) {
      return "idIndex: " + this.idIndex;
    }
    return "blank";
  }

  /**
   * 
   */
  public WsStemLookup() {
    //blank
  }

  /**
   * @param stemName1 
   * @param uuid1
   */
  public WsStemLookup(String stemName1, String uuid1) {
    this.uuid = uuid1;
    this.setStemName(stemName1);
  }

  /**
   * @param stemName1 
   * @param uuid1
   * @param idIndex1
   */
  public WsStemLookup(String stemName1, String uuid1, String idIndex1) {
    this.uuid = uuid1;
    this.setStemName(stemName1);
    this.idIndex = idIndex1;
  }

}
