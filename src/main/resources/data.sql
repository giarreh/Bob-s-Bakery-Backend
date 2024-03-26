INSERT INTO roles (name)
SELECT x.name
FROM (
    VALUES
    ('ROLE_USER'),
    ('ROLE_MODERATOR'),
    ('ROLE_ADMIN')
) AS x(name)
WHERE NOT EXISTS (
    SELECT 1 FROM roles r WHERE r.name = x.name
);