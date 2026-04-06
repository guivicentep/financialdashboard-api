CREATE TABLE recurring_transactions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id),
    category_id     UUID NOT NULL REFERENCES categories(id),
    type            transaction_type NOT NULL,
    amount          DECIMAL(9,2) NOT NULL CHECK (amount > 0 AND amount <= 99000),
    description     VARCHAR(100),
    recurrence_type recurrence_type NOT NULL,
    recurrence_day  SMALLINT NOT NULL,
    start_date      DATE NOT NULL,
    end_date        DATE,
    is_active       BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);