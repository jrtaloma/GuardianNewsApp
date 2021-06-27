from copy import error
import time
from flask import Flask
from flask_restful import Api, Resource, reqparse, request

# https://www.digitalocean.com/community/tutorials/processing-incoming-request-data-in-flask

app = Flask(__name__)
api = Api(app)

parser = reqparse.RequestParser()

PATH = '/'

STATE = 0

class Server(Resource):

    @app.route(PATH+'search', methods=['GET'])
    def get():
        parser.remove_argument('state')
        parser.add_argument('req', type=int, required=True)
        args = parser.parse_args()
        return {'error':False, 'msg':'ok', 'state':STATE}

    #def post(self):
        #global STATE
        #parser.add_argument('req', type=int, required=True)
        #parser.add_argument('state', type=int, required=True)
        #args = parser.parse_args()
        #req = args['req']
        #state = args['state']
        #if state >= 0:
            #STATE = state
            #return {'error':False, 'msg':'NEW_STATE'} 
        #return {'error':True}

    @app.route(PATH+'json-example', methods=['POST'])
    def json_example():
        request_data = request.get_json()
        if request_data:
            if 'language' in request_data:
                language = request_data['language']

            if 'framework' in request_data:
                framework = request_data['framework']

            if 'version_info' in request_data:
                if 'python' in request_data['version_info']:
                    python_version = request_data['version_info']['python']

            if 'examples' in request_data:
                if (type(request_data['examples']) == list) and (len(request_data['examples']) > 0):
                    example = request_data['examples'][0]

            if 'boolean_test' in request_data:
                boolean_test = request_data['boolean_test']

        return '''
            The language value is: {}
            The framework value is: {}
            The Python version is: {}
            The item at index 0 in the example list is: {}
            The boolean value is: {}'''.format(language, framework, python_version, example, boolean_test)
    
    @app.route(PATH+'create', methods=['POST'])
    def post():
        global STATE
        request_data = request.get_json()
        if request_data:
            if 'info' in request_data:
                info = request_data['info']
                if type(info) == int:
                    STATE = info
                    return {'info': info}
        return {'error': True}, 403

api.add_resource(Server, PATH)

if __name__=='__main__':
    print('Starting server...')
    app.run(host='0.0.0.0', port=5000)
