CREATE TABLE tasks (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(100) NOT NULL,
    description     TEXT,
    priority        VARCHAR(10)  NOT NULL DEFAULT 'NORMAL'
                    CONSTRAINT chk_tasks_priority CHECK (priority IN ('LOW', 'NORMAL', 'HIGH')),
    due_date        DATE,
    completed       BOOLEAN      NOT NULL DEFAULT FALSE,
    owner_id        VARCHAR(255) NOT NULL,
    owner_username  VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_tasks_owner_id        ON tasks (owner_id);
CREATE INDEX idx_tasks_owner_completed ON tasks (owner_id, completed);
CREATE INDEX idx_tasks_due_date        ON tasks (due_date);
