package edu.internet2.middleware.grouperClient.jdbc;

/**
 * Whether fields should be persisted to the database or not.
 * @author harveycg
 *
 */
public enum GcPersist {
	/**
	 * Do persist them.
	 */
	doPersist{
		@Override
		boolean shouldPersist(GcPersist parentPersist, GcPersistableField gcPersistableField) {
			return true;
		}

    @Override
    boolean shouldSelect(GcPersist parentPersist, GcPersistableField gcPersistableField) {
      return true;
    }
	},

	/**
	 * Don't persist them.
	 */
	dontPersist{
		@Override
		boolean shouldPersist(GcPersist parentPersist, GcPersistableField gcPersistableField) {
			return false;
		}
		
    @Override
    boolean shouldSelect(GcPersist parentPersist, GcPersistableField gcPersistableField) {
      return false;
    }
	},
	
	
	 /**
   * Don't persist them.
   */
  selectButDontPersist{
    @Override
    boolean shouldPersist(GcPersist parentPersist, GcPersistableField gcPersistableField) {
      return false;
    }
    
    @Override
    boolean shouldSelect(GcPersist parentPersist, GcPersistableField gcPersistableField) {
      return true;
    }
  },

	/**
	 * Default to the class level behavior.
	 */
	defaultToPersistableClassDefaultFieldPersist() {
		@Override
		boolean shouldPersist(GcPersist parentPersist, GcPersistableField gcPersistableField) {
			if (parentPersist == null){
				throw new RuntimeException("A default to parent Persist was set on a field but no parent Persist has been set on the class.");
			}
			return parentPersist.shouldPersist(null, gcPersistableField);
		}

    @Override
    boolean shouldSelect(GcPersist parentPersist, GcPersistableField gcPersistableField) {
      if (parentPersist == null){
        throw new RuntimeException("A default to parent Persist was set on a field but no parent Persist has been set on the class.");
      }
      return parentPersist.shouldSelect(null, gcPersistableField);
    }
		
	}, 
	/**
	 * persist the field if it has the persistable field annotation
	 */
	persistIfPersistableField() {

	  /**
	   * 
	   * @see edu.internet2.middleware.grouperClient.jdbc.GcPersist#shouldPersist(edu.internet2.middleware.grouperClient.jdbc.GcPersist, GcPersistableField)
	   */
    @Override
    boolean shouldPersist(GcPersist parentPersist, GcPersistableField gcPersistableField) {
      return gcPersistableField != null;
    }

    /**
     * 
     * @see edu.internet2.middleware.grouperClient.jdbc.GcPersist#shouldSelect(edu.internet2.middleware.grouperClient.jdbc.GcPersist, GcPersistableField)
     */
    @Override
    boolean shouldSelect(GcPersist parentPersist, GcPersistableField gcPersistableField) {
      return gcPersistableField != null;
    }
	  
	}
	
	;

	/**
	 * Whether fields should be persisted to the database or not.
	 * @param parentPersist is the class level persist object to check, if any.
	 * @param gcPersistableField 
	 * @return true if so.
	 */
	abstract boolean shouldPersist(GcPersist parentPersist, GcPersistableField gcPersistableField);

  /**
   * Whether fields should be persisted to the database or not.
   * @param parentPersist is the class level persist object to check, if any.
   * @param gcPersistableField is the field level persist to check if any
   * @return true if so.
   */
  abstract boolean shouldSelect(GcPersist parentPersist, GcPersistableField gcPersistableField);
}
