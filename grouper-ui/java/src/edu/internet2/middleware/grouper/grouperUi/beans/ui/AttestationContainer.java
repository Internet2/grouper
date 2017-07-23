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
   * 
   * @return attribute assignable
   */
  public AttributeAssignable getAttributeAssignable() {
    
    GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();
    
    Group group = null;
    
    if (guiGroup != null) {
      group = guiGroup.getGroup();
    }

    GuiStem guiStem = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemContainer().getGuiStem();
    
    Stem stem = null;
    
    if (guiStem != null) {
      stem = guiStem.getStem();
    }

    AttributeAssign attributeAssign = null;

// TODO
//    Gr
//    if (group != null) {
//      stem.getAttributeDelegate().retrieveAssignment(null, attributeDefName, false, false);
//    }
//    
//    AttributeAssign attributeAssign = 
return null;
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
