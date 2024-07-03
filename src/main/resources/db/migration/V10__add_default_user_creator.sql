UPDATE user
SET create_by = COALESCE(create_by, id)
WHERE create_by IS NULL;