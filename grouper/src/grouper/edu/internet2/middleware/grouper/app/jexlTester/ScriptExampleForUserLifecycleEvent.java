package edu.internet2.middleware.grouper.app.jexlTester;


public enum ScriptExampleForUserLifecycleEvent implements ScriptExample {

  /** Plain text only — passes through verbatim, no JEXL evaluation. */
  PLAIN_TEXT {

    @Override
    public Object expectedOutput() {
      return "Job loss";
    }
  },

  /** groupUserAdd or groupUserRemove trigger — simple variable interpolation. */
  GROUP_USER_ADD_REMOVE_SIMPLE {

    @Override
    public Object expectedOutput() {
      return "Job loss from Accounting";
    }
  },

  /** groupUserAdd or groupUserRemove trigger — conditional output based on group metadata. */
  GROUP_USER_ADD_REMOVE_CONDITIONAL {

    @Override
    public Object expectedOutput() {
      return "Job loss from Accounting (sensitive)";
    }
  },

  /** groupUserAdd or groupUserRemove trigger — five group string variables, HTML-escaped. */
  GROUP_USER_ADD_REMOVE {

    @Override
    public Object expectedOutput() {
      return "Job loss from Accounting";
    }
  },

  /** groupUserRemoveFromFolder trigger — group plus stem variables. */
  GROUP_USER_REMOVE_FROM_FOLDER {

    @Override
    public Object expectedOutput() {
      return "Removed from Accounting in Departments";
    }
  },

  /** dataFieldRemove trigger — configId and value variables. */
  DATA_FIELD_REMOVE {

    @Override
    public Object expectedOutput() {
      return "Lost position access (value: manager)";
    }
  },

  /** dataRowRemove trigger — configId variable. */
  DATA_ROW_REMOVE {

    @Override
    public Object expectedOutput() {
      return "Lost pursual row";
    }
  };

  @Override
  public ScriptType retrieveScriptType() {
    return ScriptType.USER_LIFECYCLE_EVENT;
  }

  @Override
  public abstract Object expectedOutput();

}
