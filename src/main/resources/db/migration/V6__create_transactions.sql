CREATE TABLE transactions (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                  UUID NOT NULL REFERENCES users(id),
    category_id              UUID NOT NULL REFERENCES categories(id),
    recurring_transaction_id UUID REFERENCES recurring_transactions(id) ON DELETE SET NULL,
    type                     transaction_type NOT NULL,
    amount                   DECIMAL(9,2) NOT NULL CHECK (amount > 0 AND amount <= 99000),
    description              VARCHAR(100),
    date                     DATE NOT NULL,
    created_at               TIMESTAMP NOT NULL DEFAULT now(),
    updated_at               TIMESTAMP NOT NULL DEFAULT now()
);