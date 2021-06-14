from sqlite3.dbapi2 import IntegrityError
import credentials
import sqlite3
from google.oauth2 import id_token
from google.auth.transport import requests

# Database

def init_db(database, schema):
    connection = sqlite3.connect(database)

    with open(schema) as f:
        connection.executescript(f.read())

    connection.commit()
    connection.close()


def get_db_connection(database):
    connection = sqlite3.connect(database)
    connection.row_factory = sqlite3.Row
    return connection


def insertAccount(database, userid, tokenid, email):
    connection = get_db_connection(database)
    cur = connection.cursor()

    try:
        cur.execute("INSERT INTO accounts (userID, tokenID, email) VALUES (?,?,?)", (userid, tokenid, email, ))
    except IntegrityError:
        # Account registered yet: update tokenID
        cur.execute("DELETE FROM accounts WHERE userID='"+userid+"';")
        cur.execute("INSERT INTO accounts (userID, tokenID, email) VALUES (?,?,?)", (userid, tokenid, email, ))
        pass

    connection.commit()
    connection.close()


def checkIfAccountExists(database, tokenid):
    connection = get_db_connection(database)

    # Check if there exists tokenid in accounts
    accountInDatabase = connection.execute('SELECT EXISTS(SELECT tokenID FROM accounts WHERE tokenID="'+tokenid+'");').fetchall()
    accountInDatabase = dict(accountInDatabase[0])['EXISTS(SELECT tokenID FROM accounts WHERE tokenID="'+tokenid+'")']

    connection.commit()
    connection.close()

    # Return 0 or 1
    return accountInDatabase


def checkIfEmailExists(database, email):
    connection = get_db_connection(database)

    # Check if there exists email in accounts
    emailInDatabase = connection.execute('SELECT EXISTS(SELECT email FROM accounts WHERE email="'+email+'");').fetchall()
    emailInDatabase = dict(emailInDatabase[0])['EXISTS(SELECT email FROM accounts WHERE email="'+email+'")']

    connection.commit()
    connection.close()

    # Return 0 or 1
    return emailInDatabase


def insertFavorite(database, userid, news):
    connection = get_db_connection(database)

    # Insert news
    resourceCreated = False
    cur = connection.cursor()
    try:
        cur.execute("INSERT INTO news (id, webPublicationDate, webTitle, webUrl) VALUES (?,?,?,?)", (news['id'], news['webPublicationDate'], news['webTitle'], news['webUrl'], ))
    except IntegrityError:
        # News registered yet
        pass
    finally:
        try:
            cur.execute("INSERT INTO favorites (account, content) VALUES (?,?)", (userid, news['id'], ))
            resourceCreated = True
        except IntegrityError:
            # Favorite registered yet
            pass

    connection.commit()
    connection.close()

    return resourceCreated


def deleteFavorite(database, userid, news):
    connection = get_db_connection(database)

    # Delete favorite
    resourceDeleted = False
    cur = connection.cursor()
    cur.execute("DELETE FROM favorites WHERE account='"+userid+"' and content='"+news+"';")
    numDeletedFavorites = cur.execute("SELECT changes();").fetchall()
    numDeletedFavorites = dict(numDeletedFavorites[0])['changes()']
    if numDeletedFavorites > 0:
        resourceDeleted = True

    connection.commit()
    connection.close()

    return resourceDeleted


def deleteAccount(database, userid):
    connection = get_db_connection(database)

    # Check if there exists userid in accounts
    accountInDatabase = connection.execute('SELECT EXISTS(SELECT userID FROM accounts WHERE userID="'+userid+'");').fetchall()
    accountInDatabase = dict(accountInDatabase[0])['EXISTS(SELECT userID FROM accounts WHERE userID="'+userid+'")']

    # Delete account
    resourceDeleted = False
    if accountInDatabase == 1:
        cur = connection.cursor()
        cur.execute("DELETE FROM accounts WHERE userID='"+userid+"';")
        numDeletedAccounts = cur.execute("SELECT changes();").fetchall()
        numDeletedAccounts = dict(numDeletedAccounts[0])['changes()']
        if numDeletedAccounts > 0:
            cur.execute("DELETE FROM favorites WHERE account='"+userid+"';")
            resourceDeleted = True

    connection.commit()
    connection.close()

    return resourceDeleted


# Web server

def checkTokenID(tokenID):
    try:
        # Specify the CLIENT_ID in credentials.py of the app that accesses the backend
        idinfo = id_token.verify_oauth2_token(tokenID, requests.Request(), credentials.CLIENT_ID)
        # ID token is valid. Get the user's Google Account ID from the decoded token
        userid = idinfo['sub']
        email = idinfo['email']
        return userid, email
    except ValueError:
        # Invalid token
        pass
    return "", ""