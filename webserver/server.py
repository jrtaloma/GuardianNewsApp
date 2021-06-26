import modules
from flask import Flask
from flask_restful import Api, Resource, reqparse, request

app = Flask(__name__)
api = Api(app)

PATH = '/'
DATABASE = '/home/jrtaloma/webserver/database.db'
SCHEMA = '/home/jrtaloma/webserver/schema.sql'

class Server(Resource):

    @app.route(PATH, methods=['GET'])
    def helloWorld():
        return {'message': 'Hello World!'}, 200

    @app.route(PATH+'accounts', methods=['GET'])
    def getAccounts():
        connection = modules.get_db_connection(DATABASE)
        accounts = connection.execute('SELECT * FROM accounts;').fetchall()
        connection.commit()
        connection.close()

        res = []
        for account in accounts:
            account = dict(account)
            res.append(account)

        return {'accounts': res}, 200


    # Check signed-in Google account's tokenID
    @app.route(PATH+'tokensignin', methods=['POST'])
    def checkTokenID():
        data = request.get_json()
        if data:
            print(data)
            try:
                if 'tokenID' in data:
                    userid, email = modules.checkTokenID(data['tokenID'])
                    if userid != "" and email != "":
                        modules.insertAccount(DATABASE, userid, data['tokenID'], email)
                        print('Signed-in account: '+userid+', '+email)
                        return {'error': False}, 201
                else:
                    # Bad request
                    return {'error': True}, 400
            except ValueError:
                # Invalid token
                pass
        return {'error': True}, 403


    # Check if there exists an account with a given email
    @app.route(PATH+'checkemail', methods=['GET'])
    def checkEmail():
        parser = reqparse.RequestParser()
        parser.add_argument('email', type=str, required=True)
        args = parser.parse_args()
        email = args['email']

        # TokenID verification
        if 'tokenID' in request.headers:
            tokenid = request.headers['tokenID']
            accountInDatabase = modules.checkIfAccountExists(DATABASE, tokenid)
            if accountInDatabase == 1:
                emailInDatabase = modules.checkIfEmailExists(DATABASE, email)
                if emailInDatabase == 1:
                    return {'error': False}, 200
                else:
                    return {'error': True}, 404
            # Forbidden request
            return {'error': True}, 403
        # Bad request
        return {'error': True}, 400


    # Every valid Google user can get news stored on server
    @app.route(PATH+'news', methods=['GET'])
    def getNews():
        # TokenID verification
        if 'tokenID' in request.headers:
            userid, _ = modules.checkTokenID(request.headers['tokenID'])
            if userid != "":
                connection = modules.get_db_connection(DATABASE)
                news = connection.execute('SELECT * FROM news;').fetchall()
                connection.commit()
                connection.close()

                res = []
                for n in news:
                    n = dict(n)
                    res.append(n)

                return {'news': res}, 200

        return {'error': True}, 403


    # Delete all records in news
    @app.route(PATH+'deletenews', methods=['DELETE'])
    def deleteNews():
        # TokenID verification
        if 'tokenID' in request.headers:
            userid, _ = modules.checkTokenID(request.headers['tokenID'])
            if userid != "":
                connection = modules.get_db_connection(DATABASE)
                news = connection.execute('DELETE FROM news;').fetchall()
                connection.commit()
                connection.close()

                return {'error': False}, 200

        return {'error': True}, 403


    # Every valid Google user can get its favorites stored on server:
    # users not registered yet will get 0 favorites.
    @app.route(PATH+'favorites', methods=['GET'])
    def getFavorites():
        # TokenID verification
        if 'tokenID' in request.headers:
            tokenid = request.headers['tokenID']
            accountInDatabase = modules.checkIfAccountExists(DATABASE, tokenid)
            if accountInDatabase == 1:
                connection = modules.get_db_connection(DATABASE)
                userid = connection.execute("SELECT userID FROM accounts WHERE tokenID='"+tokenid+"';").fetchall()
                userid = dict(userid[0])['userID']
                favorites = connection.execute("SELECT id, webPublicationDate, webTitle, webUrl FROM favorites JOIN news on content=id WHERE account='"+userid+"' ORDER BY favorites.created DESC;").fetchall()
                connection.commit()
                connection.close()

                res = []
                for favorite in favorites:
                    favorite = dict(favorite)
                    res.append(favorite)

                return {'favorites': res}, 200

        return {'error': True}, 403


    # Create a new favorite resource
    @app.route(PATH+'createfavorite', methods=['POST'])
    def createFavorite():
        data = request.get_json()
        if data:
            print(data)
            try:
                # TokenID verification
                if 'tokenID' in request.headers:
                    tokenid = request.headers['tokenID']
                    accountInDatabase = modules.checkIfAccountExists(DATABASE, tokenid)
                    if accountInDatabase == 1:
                        connection = modules.get_db_connection(DATABASE)
                        userid = connection.execute("SELECT userID FROM accounts WHERE tokenID='"+tokenid+"';").fetchall()
                        userid = dict(userid[0])['userID']
                        connection.commit()
                        connection.close()
                        # News verification
                        if 'news' in data:
                            news = data['news']
                            if 'id' in news and 'webPublicationDate' in news and 'webTitle' in news and 'webUrl' in news:
                                resourceCreated = modules.insertFavorite(DATABASE, userid, news)
                                if resourceCreated:
                                    print('Created favorite: ('+userid+', '+news['id']+')')
                                    return {'error': False}, 201
                                else:
                                    # Duplicate
                                    return {'error': True}, 403
                            else:
                                # Bad request
                                return {'error': True}, 400
                        else:
                            # Bad request
                            return {'error': True}, 400
                else:
                    # Bad request
                    return {'error': True}, 400
            except ValueError:
                # Invalid tokenID or forbidden insertion
                pass
        return {'error': True}, 403


    # Delete a favorite resource
    @app.route(PATH+'deletefavorite', methods=['DELETE'])
    def deleteFavorite():
        parser = reqparse.RequestParser()
        parser.add_argument('newsid', type=str, required=True)
        args = parser.parse_args()
        news = args['newsid']
        # TokenID verification
        if 'tokenID' in request.headers:
            tokenid = request.headers['tokenID']
            accountInDatabase = modules.checkIfAccountExists(DATABASE, tokenid)
            connection = modules.get_db_connection(DATABASE)
            if accountInDatabase == 1:
                userid = connection.execute("SELECT userID FROM accounts WHERE tokenID='"+tokenid+"';").fetchall()
                userid = dict(userid[0])['userID']
                connection.commit()
                connection.close()
                resourceDeleted = modules.deleteFavorite(DATABASE, userid, news)
                if resourceDeleted:
                    print('Deleted favorite: ('+userid+', '+news+')')
                    return {'error': False}, 200
                else:
                    # Not found
                    return {'error': True}, 404
            else:
                # Close connection anyway
                connection.commit()
                connection.close()

        return {'error': True}, 403


    # Delete an account resource
    @app.route(PATH+'deleteaccount', methods=['DELETE'])
    def deleteAccount():
        # TokenID verification
        if 'tokenID' in request.headers:
            userid, _ = modules.checkTokenID(request.headers['tokenID'])
            if userid != "":
                resourceDeleted = modules.deleteAccount(DATABASE, userid)
                if resourceDeleted:
                    print('Deleted account: '+userid)
                    return {'error': False}, 200
                else:
                    # Not found
                    return {'error': True}, 404

        return {'error': True}, 403


api.add_resource(Server, PATH)


if __name__=='__main__':
    print('Initializing database...')
    modules.init_db(DATABASE, SCHEMA)
    print('Starting web server...')
    app.run()
