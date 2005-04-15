
-- 
-- Grouper Test Data
-- 

DELETE FROM grouper_subject WHERE subjectID='member 0';
INSERT INTO grouper_subject (subjectID, subjectTypeID)
  VALUES ('member 0', 'person');
DELETE FROM grouper_subject WHERE subjectID='member 1';
INSERT INTO grouper_subject (subjectID, subjectTypeID)
  VALUES ('member 1', 'person');

COMMIT;


