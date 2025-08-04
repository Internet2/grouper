COMMENT ON VIEW grouper_data_field_assign_v IS 'Data field assign view';

COMMENT ON COLUMN grouper_data_field_assign_v.subject_source_id IS 'subject_source_id: subject source id';

COMMENT ON COLUMN grouper_data_field_assign_v.member_id IS 'member_id: member id';

COMMENT ON COLUMN grouper_data_field_assign_v.data_field_config_id IS 'data_field_config_id: data field config id';

COMMENT ON COLUMN grouper_data_field_assign_v.subject_id IS 'subject_id: subject id of subject';

COMMENT ON COLUMN grouper_data_field_assign_v.value_text IS 'value_text: value text';

COMMENT ON COLUMN grouper_data_field_assign_v.value_integer IS 'value_integer: value integer';

COMMENT ON COLUMN grouper_data_field_assign_v.data_field_internal_id IS 'data_field_internal_id: data field internal id';

COMMENT ON COLUMN grouper_data_field_assign_v.data_field_assign_internal_id IS 'data_field_assign_internal_id: data field assign internal id';

COMMENT ON VIEW grouper_data_row_assign_v IS 'Data row assign view';

COMMENT ON COLUMN grouper_data_row_assign_v.data_row_config_id IS 'data_row_config_id: data row config id';

COMMENT ON COLUMN grouper_data_row_assign_v.subject_id IS 'subject_id: subject id of subject';

COMMENT ON COLUMN grouper_data_row_assign_v.data_row_internal_id IS 'data_row_internal_id: data row internal id';

COMMENT ON COLUMN grouper_data_row_assign_v.data_row_assign_internal_id IS 'data_row_assign_internal_id: data row assign internal id';

COMMENT ON COLUMN grouper_data_row_assign_v.subject_source_id IS 'subject_source_id: subject source id';

COMMENT ON COLUMN grouper_data_row_assign_v.member_id IS 'member_id: member id';


COMMENT ON VIEW grouper_data_row_field_asgn_v IS 'Data row field assign view';

COMMENT ON COLUMN grouper_data_row_field_asgn_v.data_row_config_id IS 'data_row_config_id: data row config id';

COMMENT ON COLUMN grouper_data_row_field_asgn_v.data_field_config_id IS 'data_field_config_id: data field config id';

COMMENT ON COLUMN grouper_data_row_field_asgn_v.subject_id IS 'subject_id: subject id of subject';

COMMENT ON COLUMN grouper_data_row_field_asgn_v.value_text IS 'value_text: value text';

COMMENT ON COLUMN grouper_data_row_field_asgn_v.value_integer IS 'value_integer: value integer';

COMMENT ON COLUMN grouper_data_row_field_asgn_v.subject_source_id IS 'subject_source_id: subject source id';

COMMENT ON COLUMN grouper_data_row_field_asgn_v.member_id IS 'member_id: member id';

COMMENT ON COLUMN grouper_data_row_field_asgn_v.data_field_internal_id IS 'data_field_internal_id: data field internal id';

COMMENT ON COLUMN grouper_data_row_field_asgn_v.data_field_assign_internal_id IS 'data_field_assign_internal_id: data field assign internal id';

COMMENT ON COLUMN grouper_data_row_field_asgn_v.data_row_internal_id IS 'data_row_internal_id: data row internal id';

COMMENT ON COLUMN grouper_data_row_field_asgn_v.data_row_assign_internal_id IS 'data_row_assign_internal_id: data row assign internal id';


update grouper_ddl set last_updated = to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS'), history = substring((to_char(current_timestamp, 'YYYY/MM/DD HH12:MI:SS') || ': upgrade Grouper from V' || db_version || ' to V46, ' || history) from 1 for 3500), db_version = 46 where object_name = 'Grouper';
commit;


