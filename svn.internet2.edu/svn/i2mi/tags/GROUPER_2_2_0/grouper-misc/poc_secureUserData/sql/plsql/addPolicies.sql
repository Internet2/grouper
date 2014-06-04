BEGIN
dbms_rls.add_policy (object_schema => 'FASTDEV1',
 object_name => 'secureuserdata_user',
 policy_name => 'sud_fgac_select_rows',
 function_schema => 'FASTDEV1',
 policy_function => 'secureuserdata_fgac_pkg.userdata_select_rows_predicate',
 statement_types => 'SELECT',
 policy_type     => dbms_rls.DYNAMIC
);
END;
/

BEGIN
dbms_rls.add_policy (object_schema => 'FASTDEV1',
 object_name => 'secureuserdata_user',
 policy_name => 'sud_fgac_update_rows',
 function_schema => 'FASTDEV1',
 policy_function => 'secureuserdata_fgac_pkg.userdata_update_rows_predicate',
 statement_types => 'UPDATE',
 update_check    => TRUE,
 policy_type     => dbms_rls.DYNAMIC
);
END;
/

BEGIN
dbms_rls.add_policy (object_schema => 'FASTDEV1',
 object_name => 'secureuserdata_user',
 policy_name => 'sud_fgac_select_cols_ids',
 function_schema => 'FASTDEV1',
 policy_function => 'secureuserdata_fgac_pkg.userdata_select_cols_ids',
 statement_types => 'SELECT',
 policy_type     => dbms_rls.DYNAMIC,
 sec_relevant_cols => 'PERSONID,NETID',
 sec_relevant_cols_opt => DBMS_RLS.ALL_ROWS

);
END;
/

BEGIN
dbms_rls.add_policy (object_schema => 'FASTDEV1',
 object_name => 'secureuserdata_user',
 policy_name => 'sud_fgac_update_cols_ids',
 function_schema => 'FASTDEV1',
 policy_function => 'secureuserdata_fgac_pkg.userdata_update_cols_ids',
 statement_types => 'UPDATE',
 update_check    => TRUE,
 policy_type     => dbms_rls.DYNAMIC,
 sec_relevant_cols => 'PERSONID,NETID'

);
END;
/

BEGIN
dbms_rls.add_policy (object_schema => 'FASTDEV1',
 object_name => 'secureuserdata_user',
 policy_name => 'sud_fgac_select_cols_name',
 function_schema => 'FASTDEV1',
 policy_function => 'secureuserdata_fgac_pkg.userdata_select_cols_name',
 statement_types => 'SELECT',
 policy_type     => dbms_rls.DYNAMIC,
 sec_relevant_cols => 'FIRST_NAME,LAST_NAME',
 sec_relevant_cols_opt => DBMS_RLS.ALL_ROWS

);
END;
/

BEGIN
dbms_rls.add_policy (object_schema => 'FASTDEV1',
 object_name => 'secureuserdata_user',
 policy_name => 'sud_fgac_update_cols_name',
 function_schema => 'FASTDEV1',
 policy_function => 'secureuserdata_fgac_pkg.userdata_update_cols_name',
 statement_types => 'UPDATE',
 update_check    => TRUE,
 policy_type     => dbms_rls.DYNAMIC,
 sec_relevant_cols => 'FIRST_NAME,LAST_NAME'
);
END;
/

BEGIN
dbms_rls.add_policy (object_schema => 'FASTDEV1',
 object_name => 'secureuserdata_user',
 policy_name => 'sud_fgac_select_cols_contact',
 function_schema => 'FASTDEV1',
 policy_function => 'secureuserdata_fgac_pkg.userdata_select_cols_contact',
 statement_types => 'SELECT',
 policy_type     => dbms_rls.DYNAMIC,
 sec_relevant_cols => 'EMAIL,WORK_PHONE,HOME_PHONE',
 sec_relevant_cols_opt => DBMS_RLS.ALL_ROWS

);
END;
/

BEGIN
dbms_rls.add_policy (object_schema => 'FASTDEV1',
 object_name => 'secureuserdata_user',
 policy_name => 'sud_fgac_update_cols_contact',
 function_schema => 'FASTDEV1',
 policy_function => 'secureuserdata_fgac_pkg.userdata_update_cols_contact',
 statement_types => 'UPDATE',
 update_check    => TRUE,
 policy_type     => dbms_rls.DYNAMIC,
 sec_relevant_cols => 'EMAIL,WORK_PHONE,HOME_PHONE'
);
END;
/

