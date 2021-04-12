CREATE TABLE "user"
(
    id                VARCHAR(36) PRIMARY KEY  NOT NULL,
    username          VARCHAR(20)              NOT NULL,
    email             VARCHAR(30)              NOT NULL,
    "password"        TEXT                     NOT NULL,
    has_verified_mail BOOLEAN                  NOT NULL DEFAULT 'f',
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at        TIMESTAMP WITH TIME ZONE,
    CONSTRAINT "Unique user infos" UNIQUE (username, email)
);




