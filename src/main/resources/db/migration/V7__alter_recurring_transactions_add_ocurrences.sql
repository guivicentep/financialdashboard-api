ALTER TABLE recurring_transactions
    ADD COLUMN occurrences SMALLINT NOT NULL DEFAULT 1 CHECK (occurrences >= 1)