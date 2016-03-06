/**
 * 
 */
package edu.internet2.middleware.grouperTierApiAuth.interfaces;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.tierApiAuthzServer.interfaces.AsasApiFolderInterface;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.AsasSaveMode;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.folders.AsasApiFolder;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.folders.AsasApiFolderDeleteParam;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.folders.AsasApiFolderDeleteResult;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.folders.AsasApiFolderLookup;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.folders.AsasApiFolderSaveParam;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.folders.AsasApiFolderSaveResult;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.entity.AsasApiEntityLookup;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.exception.StemAddAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperTierApiAuth.utils.GrouperAuthzApiUtils;
import edu.internet2.middleware.subject.Subject;


/**
 * Implement the folder interface
 * @author mchyzer
 *
 */
public class GaasFolderInterfaceImpl implements AsasApiFolderInterface {

  /**
   * save a folder
   * @see AsasApiFolderInterface
   */
  @Override
  public AsasApiFolderSaveResult save(AsasApiEntityLookup authenticatedSubject,
      AsasApiFolderSaveParam asasApiFolderSaveParam) {

    if (asasApiFolderSaveParam == null) {
      throw new NullPointerException();
    }
    
    Subject loggedInSubject = GrouperAuthzApiUtils.loggedInSubject(authenticatedSubject);
    
    //start a session
    GrouperSession grouperSession = GrouperSession.start(loggedInSubject);
    
    try {
    
      AsasApiFolderSaveResult result = new AsasApiFolderSaveResult();
      
      StemSave stemSave = new StemSave(grouperSession);
      
      if (asasApiFolderSaveParam.getCreateParentFoldersIfNotExist() != null && asasApiFolderSaveParam.getCreateParentFoldersIfNotExist()) {
        stemSave.assignCreateParentStemsIfNotExist(true);
      }
      
      AsasApiFolderLookup asasApiFolderLookup = asasApiFolderSaveParam.getFolderLookup();
      
      if (asasApiFolderLookup != null) {
        
        //TODO do handles (idIndex?)
        
        if (asasApiFolderLookup.getId() != null) {
          stemSave.assignUuid(asasApiFolderLookup.getId());
        }
        
        if (asasApiFolderLookup.getName() != null) {
          stemSave.assignStemNameToEdit(asasApiFolderLookup.getName());
        }
        
      }
      
      AsasApiFolder asasApiFolder = asasApiFolderSaveParam.getFolder();
      
      if (asasApiFolder != null) {
      
        if (asasApiFolder.getDescription() != null) {
          stemSave.assignDescription(asasApiFolder.getDescription());
        }
        
        if (asasApiFolder.getDisplayName() != null) {
          stemSave.assignDisplayName(asasApiFolder.getDisplayName());
        }
        
        if (asasApiFolder.getId() != null) {
          stemSave.assignUuid(asasApiFolder.getId());
        }
        
        if (asasApiFolder.getStatus() != null && !StandardApiServerUtils.equals("active", asasApiFolder.getStatus())) {
          throw new RuntimeException("Invalid status '" + asasApiFolder.getStatus() + "', expecting null or 'active'");
        }
        
        if (asasApiFolder.getName() != null) {
          stemSave.assignName(asasApiFolder.getName());
        }
      }
      
      AsasSaveMode saveMode = StandardApiServerUtils.defaultIfNull(asasApiFolderSaveParam.getSaveMode(), AsasSaveMode.INSERT_OR_UPDATE);
      
      switch(saveMode) {
        case INSERT:
          stemSave.assignSaveMode(SaveMode.INSERT);
          break;
        case UPDATE:
          stemSave.assignSaveMode(SaveMode.UPDATE);
          break;
        case INSERT_OR_UPDATE:
          stemSave.assignSaveMode(SaveMode.INSERT_OR_UPDATE);
          break;
      }
      
      Stem stem = null;
      try {
        stem = stemSave.save();
      } catch (StemNotFoundException snfe) {
        result.setUpdateDoesntExist(true);
        return result;
      } catch (StemAddAlreadyExistsException saaee) {
        result.setInsertAlreadyExists(true);
        return result;
      } catch (RuntimeException re) {
        if (asasApiFolderLookup != null && !StandardApiServerUtils.isBlank(asasApiFolderLookup.getName())) {
          
          Boolean parentFolderExists = GrouperAuthzApiUtils.folderParentExistsSafe(grouperSession, asasApiFolderLookup.getName());
          if (parentFolderExists != null && !parentFolderExists) {
            result.setParentFolderDoesntExist(true);
            return result;
          }
        }
        //its not this case so just rethrow
        throw re;
      }
      SaveResultType saveResultType = stemSave.getSaveResultType();

      // convert the folder
      AsasApiFolder folderToReturn = GrouperAuthzApiUtils.convertToFolder(stem);

      result.setFolder(folderToReturn);
      
      result.setCreated(saveResultType == SaveResultType.INSERT);
      result.setUpdated(saveResultType == SaveResultType.UPDATE);
      
      return result;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  @Override
  public AsasApiFolderDeleteResult delete(AsasApiEntityLookup authenticatedSubject,
      AsasApiFolderDeleteParam asasApiFolderDeleteParam) {

    if (asasApiFolderDeleteParam == null) {
      throw new NullPointerException();
    }
    
    Subject loggedInSubject = GrouperAuthzApiUtils.loggedInSubject(authenticatedSubject);
    
    //start a session
    GrouperSession grouperSession = GrouperSession.start(loggedInSubject);
    
    try {
    
      AsasApiFolderDeleteResult result = new AsasApiFolderDeleteResult();
      
      AsasApiFolderLookup asasApiFolderLookup = asasApiFolderDeleteParam.getFolderLookup();
      
      Stem stem = GrouperAuthzApiUtils.folderLookupConvertToStem(grouperSession, asasApiFolderLookup, false);

      result.setDeleted(false);
      
      if (stem == null && !StringUtils.isBlank(asasApiFolderLookup.getName())) {
        //lets see if we can check the parent folder
        String parentStemName = GrouperUtil.parentStemNameFromName(asasApiFolderLookup.getName(), true);
        boolean parentExists = false;
        if (parentStemName == null) {
          parentExists = true;
        } else {
          Stem parent = StemFinder.findByName(grouperSession, parentStemName, false);
          parentExists = parent != null;
        }
        result.setParentFolderExists(parentExists);
      } 
      
      if (stem != null) {
      
        boolean recursive = asasApiFolderDeleteParam.getRecursive() != null && asasApiFolderDeleteParam.getRecursive();
        
        if (recursive) {
          stem.obliterate(false, false);
        } else {
          stem.delete();
        }
        
        result.setDeleted(true);
      }
      
      return result;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }

  
}
