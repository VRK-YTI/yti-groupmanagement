-- Need to update manually Flyway schema_version table since we skipped a few major versions in Spring boot upgrade
DROP INDEX IF EXISTS "schema_version_vr_idx";
DROP INDEX IF EXISTS "schema_version_ir_idx";
ALTER TABLE "schema_version" DROP COLUMN IF EXISTS "version_rank";
ALTER TABLE "schema_version" DROP CONSTRAINT IF EXISTS "schema_version_pk";
ALTER TABLE "schema_version" ALTER COLUMN "version" DROP NOT NULL;
ALTER TABLE "schema_version" ADD CONSTRAINT "schema_version_pk" PRIMARY KEY ("installed_rank");
UPDATE "schema_version" SET "type"='BASELINE' WHERE "type"='INIT';