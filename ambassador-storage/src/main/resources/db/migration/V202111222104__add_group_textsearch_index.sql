CREATE INDEX CONCURRENTLY group_textsearch_idx ON "group" USING GIN (textsearch);