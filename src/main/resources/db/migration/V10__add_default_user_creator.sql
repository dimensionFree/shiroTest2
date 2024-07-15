UPDATE user
SET created_by = COALESCE(created_by, id)
WHERE created_by IS NULL;