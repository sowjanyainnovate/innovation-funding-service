ALTER TABLE process_role ADD CONSTRAINT process_role_unique UNIQUE (application_id, organisation_id, role_id, user_id);