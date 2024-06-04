-- remove foreign index
SELECT @constraint_name := CONSTRAINT_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_NAME = 'role_authorities' AND COLUMN_NAME = 'role_id';

SET @sql = CONCAT('ALTER TABLE role_authorities DROP INDEX ', @constraint_name);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE role_authorities SET role_id = 'role_id_member' WHERE role_id= 'id_member';
UPDATE role_authorities SET role_id = 'role_id_admin' WHERE role_id= 'id_admin';
UPDATE role SET id = 'role_id_member' WHERE id= 'id_member';
UPDATE role SET id = 'role_id_admin' WHERE id= 'id_admin';
UPDATE user SET role_id = 'role_id_admin' WHERE role_id is null;

-- add index
