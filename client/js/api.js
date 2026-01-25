async function apiCall(endpoint, options = {}) {
    var token = getToken();
    var headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };
    
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }
    
    var response = await fetch(API_BASE_URL + endpoint, {
        ...options,
        headers: headers
    });
    
    return handleResponse(response);
}

async function apiUpload(endpoint, formData) {
    var token = getToken();
    var headers = {};
    
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }
    
    var response = await fetch(API_BASE_URL + endpoint, {
        method: 'POST',
        headers: headers,
        body: formData
    });
    
    return handleResponse(response);
}

async function handleResponse(response) {
    if (response.status === 401 || response.status === 403) {
        logout();
        return;
    }
    
    var text = await response.text();
    var data = text ? JSON.parse(text) : null;
    
    if (!response.ok) {
        throw new Error(data?.error || 'Ошибка выполнения запроса');
    }
    
    return data;
}
