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
 * $Id: StemSave.java,v 1.5 2009-11-17 02:52:29 mchyzer Exp $
 */
package edu.internet2.middleware.grouper;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * Use this class to insert or update a composite
 */
public class CompositeSave {
  
  /**
   * create a new stem save
   * @param theGrouperSession
   */
  public CompositeSave() {
  }

  /**
   * type is intersection or complement
   */
  private String type;

  /**
   * if should delete composite
   */
  private boolean deleteComposite;
  
  /**
   * if should delete composite
   * @param theRemoveComposite
   * @return this for chaining
   */
  public CompositeSave assignDeleteComposite(boolean theRemoveComposite) {
    this.deleteComposite = theRemoveComposite;
    return this;
  }
  
  /**
   * uuid of composite on insert
   */
  private String id;

  /**
   * id of composite on insert
   * @param theId
   * @return this for chaining
   */
  public CompositeSave assignId(String theId) {
    this.id = theId;
    return this;
  }
  
  /**
   * 
   * @param theType
   * @return this for chaining
   */
  public CompositeSave assignType(String theType) {
    this.type = theType;
    return this;
  }
  
  /** name to change to */
  private String ownerName;

  /**
   * 
   * @param theLeftFactor
   * @return this for chaining
   */
  public CompositeSave assignLeftFactorName(String theLeftFactor) {
    this.leftFactorName = theLeftFactor;
    return this;
  }
  
  /** leftFactor name to change to */
  private String leftFactorName;

  /**
   * rightFactorName
   */
  private String rightFactorName;
  
  /**
   * 
   * @param theRightFactor
   * @return this for chaining
   */
  public CompositeSave assignRightFactorName(String theRightFactor) {
    this.rightFactorName = theRightFactor;
    return this;
  }
  
  /**
   * name
   * @param name1
   * @return name
   */
  public CompositeSave assignOwnerName(String name1) {
    this.ownerName = name1;
    return this;
  }
  
  /** save mode */
  private SaveMode saveMode;

  /**
   * asssign save mode
   * @param theSaveMode
   * @return this for chaining
   */
  public CompositeSave assignSaveMode(SaveMode theSaveMode) {
    this.saveMode = theSaveMode;
    return this;
  }

  /** save type after the save */
  private SaveResultType saveResultType = null;

  /**
   * get the save type
   * @return save type
   */
  public SaveResultType getSaveResultType() {
    return this.saveResultType;
  }
  
  /**
   * <pre>
   * create or update or delete a composite
   * </pre>
   * @return the composite that was updated or created or deleted
   */
  public Composite save() throws InsufficientPrivilegeException, GroupNotFoundException {

    GrouperUtil.assertion(!StringUtils.isBlank(this.ownerName), "ownerName is required");
    GrouperUtil.assertion(!StringUtils.isBlank(this.leftFactorName), "leftFactorName is required");
    GrouperUtil.assertion(!StringUtils.isBlank(this.rightFactorName), "rightFactorName is required");
    if (!this.deleteComposite) {
      GrouperUtil.assertion(!StringUtils.isBlank(this.type), "type is required");
    }
    
    return (Composite)GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
    
        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {
          
          grouperTransaction.setCachingEnabled(false);
          
          Group ownerGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), CompositeSave.this.ownerName, true, new QueryOptions().secondLevelCache(false));
          Group leftFactorGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), CompositeSave.this.leftFactorName, true, new QueryOptions().secondLevelCache(false));
          Group rightFactorGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), CompositeSave.this.rightFactorName, true, new QueryOptions().secondLevelCache(false));
          CompositeType compositeType = CompositeType.valueOfIgnoreCase(CompositeSave.this.type);

          boolean hasComposite = ownerGroup.isHasComposite();

          Composite composite = hasComposite ? ownerGroup.getComposite(true) : null;
          
          //default to insert or update
          saveMode = (SaveMode)ObjectUtils.defaultIfNull(saveMode, SaveMode.INSERT_OR_UPDATE);

          if (saveMode == SaveMode.INSERT && hasComposite) {
            throw new RuntimeException("Inserting composite but it already exists!");
          }
          if (saveMode == SaveMode.UPDATE && !hasComposite) {
            throw new RuntimeException("Updating composite but it doesnt exist!");
          }

          // delete
          if (CompositeSave.this.deleteComposite) {
            if (!hasComposite) {
              CompositeSave.this.saveResultType = SaveResultType.NO_CHANGE;
              return null;
            }
            GrouperDAOFactory.getFactory().getComposite().delete(composite);
            CompositeSave.this.saveResultType = SaveResultType.DELETE;
            return composite;
          }

          // insert
          if (!hasComposite) {
            composite = ownerGroup.internal_addCompositeMember(GrouperSession.staticGrouperSession(), compositeType, leftFactorGroup, rightFactorGroup, CompositeSave.this.id);
            CompositeSave.this.saveResultType = SaveResultType.INSERT;
            return composite;
          }
          
          // it has a composite
          if (StringUtils.equals(composite.getLeftFactorUuid(), leftFactorGroup.getId())
              && StringUtils.equals(composite.getRightFactorUuid(), rightFactorGroup.getId())
              && composite.getType() == compositeType) {
            CompositeSave.this.saveResultType = SaveResultType.NO_CHANGE;
            return composite;
          }
          
          //its wrong
          ownerGroup.deleteCompositeMember();
          composite = ownerGroup.addCompositeMember(compositeType, leftFactorGroup, rightFactorGroup);
          CompositeSave.this.saveResultType = SaveResultType.UPDATE;
          return composite;
        }
      });
  }
}
