package edu.internet2.middleware.grouper.stem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class StemSaveBatch {

  
  private boolean makeChangesIfExist = true;

  public StemSaveBatch assignMakeChangesIfExist(boolean theMakeChangesIfExist) {
    this.makeChangesIfExist = theMakeChangesIfExist;
    return this;
  }
  
  private List<StemSave> stemSaves = new ArrayList<StemSave>();
  
  public StemSaveBatch addStemSaves(Collection<StemSave> theStemSaves) {
    if (theStemSaves != null) {
      this.stemSaves.addAll(theStemSaves);
    }
    return this;
  }

  public Map<String, Stem> save() {
    
    Set<String> stemNames = new HashSet<String>();

    for (StemSave stemSave : stemSaves) {
      if (!StringUtils.isBlank(stemSave.getName())) {
        stemNames.add(stemSave.getName());
      }
      if (!StringUtils.isBlank(stemSave.getStemNameToEdit())) {
        stemNames.add(stemSave.getStemNameToEdit());
      }
    }
    
    Set<Stem> stems = StemFinder.findByNames(stemNames, false);
    
    Map<String, Stem> stemNameToStem = new HashMap<String, Stem>();
    
    for (Stem stem : GrouperUtil.nonNull(stems)) {
      stemNameToStem.put(stem.getName(), stem);
    }
    
    for (StemSave stemSave : stemSaves) {
      if (!StringUtils.isBlank(stemSave.getName())) {
        Stem stem = stemNameToStem.get(stemSave.getName());
        if (stem != null) {
          if (this.makeChangesIfExist) {
            stem = stemSave.save();
          }
        } else {
          stem = stemSave.save();
        }
        if (stem != null) {
          stemNameToStem.put(stem.getName(), stem);
        }
      }
    }
    return stemNameToStem;
  }
}
