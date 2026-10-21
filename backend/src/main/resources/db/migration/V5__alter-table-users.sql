ALTER TABLE users
    ALTER COLUMN username TYPE VARCHAR(30),
    ALTER COLUMN email TYPE VARCHAR(254);

-- e-mail único sem diferenciar maiúsculas
CREATE UNIQUE INDEX IF NOT EXISTS users_email_lower_idx ON users (lower(email));
