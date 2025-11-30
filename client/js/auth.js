async function login(username, password) {
    var response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({username, password})
    });
    
    if (!response.ok) {
        var data = await response.json().catch(() => ({}));
        throw new Error(data.error || 'Ошибка входа');
    }
    
    var data = await response.json();
    localStorage.setItem(JWT_STORAGE_KEY, data.token);
    return data.token;
}

async function register(username, password) {
    if (!username || !password) {
        throw new Error('Введите имя пользователя и пароль');
    }
    if (password.length < 4) {
        throw new Error('Пароль должен быть не менее 4 символов');
    }
    
    var response = await fetch(`${API_BASE_URL}/auth/register`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({username, password})
    });
    
    if (!response.ok) {
        var data = await response.json().catch(() => ({}));
        throw new Error(data.error || 'Ошибка регистрации');
    }
    
    var data = await response.json();
    localStorage.setItem(JWT_STORAGE_KEY, data.token);
    return data.token;
}

function logout() {
    localStorage.removeItem(JWT_STORAGE_KEY);
    window.location.href = 'login.html';
}

function getToken() {
    return localStorage.getItem(JWT_STORAGE_KEY);
}

function checkAuth() {
    if (!getToken()) {
        window.location.href = 'login.html';
        return false;
    }
    return true;
}

function handleLogout() {
    logout();
}
