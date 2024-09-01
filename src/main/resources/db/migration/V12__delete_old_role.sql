DELETE FROM role_authorities ra WHERE ra.role_id = 'id_member';
DELETE FROM role r WHERE r.id = 'id_member';
 DELETE FROM role_authorities ra WHERE ra.role_id = 'id_admin';
DELETE FROM role r WHERE r.id = 'id_admin';