CREATE OR REPLACE PACKAGE secureuserdata_fgac_pkg 
IS
   c_context CONSTANT VARCHAR2(30) := 'secureuserdata_fgac';
   
   c_schema CONSTANT VARCHAR2(30) := 'secureuserdata_schema';
   
   c_context_created CONSTANT VARCHAR2(30) := 'secureuserdata_created';
   
   c_schema_owner CONSTANT VARCHAR2(30) := 'FASTDEV1';
   
   --the action underscore col set will be on the end, the value is T for allowed
   c_col_prefix CONSTANT VARCHAR2(30) := 'sud_col_';

   --these will be T if the user can select or update all rows, to short circuit the where clause
   c_row_select_all CONSTANT VARCHAR2(30) := 'sud_row_select_all';
   c_row_update_all CONSTANT VARCHAR2(30) := 'sud_row_update_all';
   
   c_1970 constant date := TO_DATE('01/01/1970 00:00:00', 'MM-DD-YYYY HH24:MI:SS');
   
   --expire context every X minutes to get up to date security information
   c_seconds_context_expires constant number := 60*1;
   
   PROCEDURE show_context;

   PROCEDURE set_context_if_needed;

   PROCEDURE set_context_backdoor(as_schema varchar2);

   FUNCTION userdata_select_rows_predicate (
      schema_in VARCHAR2, 
      name_in VARCHAR2)
   RETURN VARCHAR2; 

   FUNCTION user_schema
   RETURN VARCHAR2; 

   FUNCTION userdata_update_rows_predicate (
      schema_in VARCHAR2, 
      name_in VARCHAR2)
   RETURN VARCHAR2; 

   FUNCTION userdata_select_cols_ids (
      schema_in VARCHAR2, 
      name_in VARCHAR2)
   RETURN VARCHAR2; 

   FUNCTION userdata_update_cols_ids (
      schema_in VARCHAR2, 
      name_in VARCHAR2)
   RETURN VARCHAR2; 

   FUNCTION userdata_select_cols_name (
      schema_in VARCHAR2, 
      name_in VARCHAR2)
   RETURN VARCHAR2; 

   FUNCTION userdata_update_cols_name (
      schema_in VARCHAR2, 
      name_in VARCHAR2)
   RETURN VARCHAR2; 

   FUNCTION userdata_select_cols_contact (
      schema_in VARCHAR2, 
      name_in VARCHAR2)
   RETURN VARCHAR2; 

   FUNCTION userdata_update_cols_contact (
      schema_in VARCHAR2, 
      name_in VARCHAR2)
   RETURN VARCHAR2; 



END secureuserdata_fgac_pkg;
/


GRANT EXECUTE ON SECUREUSERDATA_FGAC_PKG TO FASTDEV2;

GRANT EXECUTE ON SECUREUSERDATA_FGAC_PKG TO FASTDEV3;
