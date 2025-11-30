async function apiCall(endpoint, options = {}) {
    var token = getToken();
    // var startTime = Date.now();
    var headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };
    
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    var response = await fetch(`${API_BASE_URL}${endpoint}`, {
        ...options,
        headers
    });
    
    if (response.status === 401 || response.status === 403) {
        logout();
        return;
    }
    
    if (!response.ok) {
        var error = await response.json().catch(() => ({}));
        throw new Error(error.error || 'Ошибка выполнения запроса');
    }
    
    if (response.status === 204) {
        return null;
    }
    
    var text = await response.text();
    // console.log('response', endpoint, Date.now() - startTime, 'ms');
    return text ? JSON.parse(text) : null;

}
