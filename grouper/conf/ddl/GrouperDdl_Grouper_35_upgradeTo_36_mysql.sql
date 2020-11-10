DROP INDEX grouper_sync_mship_gr_idx on grouper_sync_membership;
CREATE UNIQUE INDEX grouper_sync_mship_gr_idx ON grouper_sync_membership (grouper_sync_id, grouper_sync_group_id, grouper_sync_member_id);
update grouper_ddl set last_updated = date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), history = substring(concat(date_format(current_timestamp(), '%Y/%m/%d %H:%i:%s'), ': upgrade Grouper from V', db_version, ' to V36, ', history), 1, 3500), db_version = 36 where object_name = 'Grouper';
commit;