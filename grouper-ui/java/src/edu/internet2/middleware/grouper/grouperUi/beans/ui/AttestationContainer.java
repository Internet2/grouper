/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class AttestationContainer {

  /**
   * attribute def name for attestation assignment
   */
  private AttributeDefName attributeDefNameBase = null;
  
  /**
   * 
   */
  public AttestationContainer() {
  }

  /**
   * note this is not protected by security
   * @return attibute def name
   */
  public AttributeDefName getAttributeDefName() {
    
    if (this.attributeDefNameBase == null) {

      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          AttestationContainer.this.attributeDefNameBase = AttributeDefNameFinder.findByName(
              GrouperAttestationJob.attestationStemName() + ":" + GrouperAttestationJob.ATTESTATION_VALUE_DEF, false);
          return null;
        }
      });

    }
    
    return this.attributeDefNameBase;
  }
  
  /**
   * if the attestation assignment is directly assigned to the group
   */
  private boolean directGroupAttestationAssignment;
  
  /**
   * if the attestation assignment is directly assigned to the stem
   */
  private boolean directStemAttestationAssignment;

  /**
   * attribute assign to the group object
   */
  private AttributeAssign groupAttributeAssignable = null;
  
  /**
   * attribute assign to stem object
   */
  private AttributeAssign stemAttributeAssignable = null;

  /**
   * 
   * @return true if can read
   */
  public boolean isCanReadAttestation() {
    
    GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
    
    if (guiGroup != null) {
      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().isCanRead()) {
        return true;
      }
    }
    
    GuiStem guiStem = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getGuiStem();
    
    if (guiStem != null) {
      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().isCanAdminPrivileges()) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * 
   * @return true if can write
   */
  public boolean isCanWriteAttestation() {
    
    GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
    
    if (guiGroup != null) {
      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().isCanUpdate()) {
        return true;
      }
    }
    
    GuiStem guiStem = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getGuiStem();
    
    if (guiStem != null) {
      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().isCanAdminPrivileges()) {
        return true;
      }
    }
    return false;
  }

  /**
   * 
   */
  private void attributeAssignableHelper() {

    if (!this.isCanReadAttestation() )  {
      return;
    }
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
        
        if (guiGroup != null) {
          Group group = guiGroup.getGroup();
          AttestationContainer.this.groupAttributeAssignable = 
            group.getAttributeDelegate().retrieveAssignment(null, AttestationContainer.this.getAttributeDefName(), false, false);
          String attestationDirectAssignment = AttestationContainer.this.groupAttributeAssignable
              .getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.attestationStemName() 
                  + ":" + GrouperAttestationJob.ATTESTATION_DIRECT_ASSIGNMENT);
          if (GrouperUtil.booleanValue(attestationDirectAssignment, false)) { 
            // group has direct attestation, don't use stem attributes at all.
            AttestationContainer.this.directGroupAttestationAssignment = true;
          }
        }
        
        GuiStem guiStem = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getGuiStem();
        
        if (guiStem != null) {
          if (GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().isCanAdminPrivileges()) {
            return true;
          }
        }

        
        return null;
      }
    });

//    if (guiStem != null) {
//      Stem stem = null;
//      
//      stem = guiStem.getStem();
//    }
//
//
//    AttributeAssign attributeAssign = null;
//
//    
//    AttributeAssign attributeAssign = 
  }
  
  /**
   * 
   * @return if direct to group
   */
  public boolean isDirectGroupAttestationAssignment() {
    //TODO
    return false;
  }
  
}
