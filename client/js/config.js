var _host = window.location.hostname || 'localhost';
var _port = '8888';

var _protocol = 'https:';

const API_BASE_URL = _protocol + '//' + _host + ':' + _port + '/api';
const WS_URL = _protocol + '//' + _host + ':' + _port + '/ws';
const JWT_STORAGE_KEY = 'hrms_jwt_token';
