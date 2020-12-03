-- this guy doesn't play ball. need to do it once in db postgres, once in db crobie. or maybe just need to wait x min for install?
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE ROLE crobie WITH LOGIN SUPERUSER;

CREATE DATABASE crobie;

GRANT ALL PRIVILEGES ON DATABASE crobie TO crobie;
