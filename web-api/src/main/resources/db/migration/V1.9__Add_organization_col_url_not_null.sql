UPDATE organization SET url = '' WHERE url IS NULL;
ALTER TABLE organization ALTER COLUMN url SET NOT NULL;