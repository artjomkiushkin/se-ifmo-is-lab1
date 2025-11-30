var stompClient = null;
var wsSubscriptions = {};
var wsIgnoreUntil = 0;
var reconnectAttempts = 0;

function connectWebSocket(subscriptions) {
    var token = getToken();
    if (!token) return;
    
    var socket = new SockJS(WS_URL);
    stompClient = Stomp.over(socket);
    stompClient.debug = null;
    
    stompClient.connect({ Authorization: 'Bearer ' + token }, function() {
        if (subscriptions) {
            subscriptions.forEach(function(sub) {
                subscribeToEntity(sub.entity, sub.onUpdate, sub.tableBuilder);
            });
        }
    }, function() {
        // reconnectAttempts++;
        setTimeout(function() { connectWebSocket(subscriptions); }, 5000);
    });
}

function subscribeToEntity(entity, onUpdate, tableBuilder) {
    var topics = ['created', 'updated', 'deleted', 'refresh'];
    
    topics.forEach(function(action) {
        var topic = '/topic/' + entity + '/' + action;
        stompClient.subscribe(topic, function(message) {
            var data = null;
            try { data = JSON.parse(message.body); } catch(e) {}
            var id = data?.id || data;
            handleWsUpdate(entity, onUpdate, tableBuilder, id, action);
        });
    });
    
    wsSubscriptions[entity] = { onUpdate: onUpdate, tableBuilder: tableBuilder };
}

function handleWsUpdate(entity, onUpdate, tableBuilder, id, action) {
    if (Date.now() < wsIgnoreUntil) return;
    
    var viewOpen = ViewModalBuilder.isOpen() && ViewModalBuilder.entity === entity;
    var sameId = viewOpen && ViewModalBuilder.entityId == id;
    
    if (sameId && action === 'deleted') {
        ViewModalBuilder.close();
        NotificationManager.warning('Сущность была удалена другим пользователем');
        if (onUpdate) onUpdate();
        return;
    }
    
    if (sameId) {
        NotificationManager.showViewUpdate(function() {
            if (ViewModalBuilder.config?.loadOne) ViewModalBuilder.config.loadOne();
            if (onUpdate) onUpdate();
        });
        return;
    }
    
    var canSilentUpdate = true;
    
    if (tableBuilder) {
        var hasFilters = tableBuilder.state.filters.length > 0;
        var hasSort = tableBuilder.state.sortFields.length > 0;
        var notFirstPage = tableBuilder.state.page > 0;
        
        if (hasFilters || hasSort || notFirstPage) {
            canSilentUpdate = false;
        }
    }
    
    if (canSilentUpdate) {
        if (onUpdate) onUpdate();
    } else {
        NotificationManager.showPendingUpdate(entity, onUpdate);
    }
}

function wsIgnore(ms) {
    wsIgnoreUntil = Date.now() + (ms || 2000);
}

function disconnectWebSocket() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
}
