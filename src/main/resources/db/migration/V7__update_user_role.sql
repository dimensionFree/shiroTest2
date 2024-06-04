UPDATE user
SET role_id='role_id_member'
WHERE role_id = 'id_member';
UPDATE user
SET role_id='role_id_admin'
WHERE role_id = 'id_admin';
