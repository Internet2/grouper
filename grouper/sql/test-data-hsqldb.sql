
-- 
-- $Id: test-data-hsqldb.sql,v 1.2 2005-02-16 23:24:24 blair Exp $
-- 

INSERT INTO grouper_subject (subjectID, subjectTypeID)
  VALUES ('blair', 'person');
INSERT INTO grouper_subject (subjectID, subjectTypeID)
  VALUES ('notblair', 'person');

COMMIT;
SHUTDOWN COMPACT; 

