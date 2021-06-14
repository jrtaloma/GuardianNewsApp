DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS news;
DROP TABLE IF EXISTS favorites;

CREATE TABLE accounts (
    userID TEXT PRIMARY KEY,
    tokenID TEXT NOT NULL,
    email TEXT UNIQUE,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE news (
    id TEXT PRIMARY KEY,
    webPublicationDate TEXT NOT NULL,
    webTitle TEXT NOT NULL,
    webUrl TEXT NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE favorites (
    account TEXT NOT NULL,
    content TEXT NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (account, content),
    FOREIGN KEY (account) REFERENCES accounts (userID),
    FOREIGN KEY (content) REFERENCES news (id)
);