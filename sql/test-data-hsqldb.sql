
-- 
-- $Id: test-data-hsqldb.sql,v 1.1 2005-02-16 20:39:54 blair Exp $
-- 

INSERT INTO grouper_subject (subjectID, subjectTypeID)
  VALUES ('blair', 'person');
INSERT INTO grouper_subject (subjectID, subjectTypeID)
  VALUES ('notblair', 'person');

COMMIT;
SHUTDOWN COMPACT; 

