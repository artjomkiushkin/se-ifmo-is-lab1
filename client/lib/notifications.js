var TOAST_STYLES = {
    success: { bg: 'bg-success text-white', icon: '<i class="bi bi-check-circle"></i>', closeWhite: true },
    error: { bg: 'bg-danger text-white', icon: '<i class="bi bi-exclamation-circle"></i>', closeWhite: true },
    warning: { bg: 'bg-warning text-dark', icon: '<i class="bi bi-exclamation-triangle"></i>', closeWhite: false },
    info: { bg: 'bg-info text-white', icon: '<i class="bi bi-info-circle"></i>', closeWhite: true }
    // debug: { bg: 'bg-secondary text-white', icon: '<i class="bi bi-bug"></i>', closeWhite: true }
};

var NotificationManager = {
    container: null,
    pending: {},
    
    init: function() {
        if (this.container) return;
        this.container = document.createElement('div');
        this.container.className = 'toast-container position-fixed top-0 end-0 p-3';
        this.container.style.zIndex = '9999';
        document.body.appendChild(this.container);
    },
    
    show: function(message, type, ttl) {
        this.init();
        type = type || 'info';
        ttl = ttl !== undefined ? ttl : 5000;
        var style = TOAST_STYLES[type];
        
        var el = document.createElement('div');
        el.className = 'toast';
        el.innerHTML = 
            '<div class="toast-header ' + style.bg + '">' +
                '<strong class="me-auto"><span class="me-2">' + style.icon + '</span>' + 
                type.charAt(0).toUpperCase() + type.slice(1) + '</strong>' +
                '<button type="button" class="btn-close' + (style.closeWhite ? ' btn-close-white' : '') + '" data-bs-dismiss="toast"></button>' +
            '</div>' +
            '<div class="toast-body">' + message + '</div>';
        
        this.container.appendChild(el);
        new bootstrap.Toast(el, { autohide: ttl > 0, delay: ttl }).show();
        el.addEventListener('hidden.bs.toast', function() { el.remove(); });
        return el;
    },
    
    success: function(msg, ttl) { return this.show(msg, 'success', ttl); },
    error: function(msg, ttl) { return this.show(msg, 'error', ttl); },
    warning: function(msg, ttl) { return this.show(msg, 'warning', ttl); },
    info: function(msg, ttl) { return this.show(msg, 'info', ttl); },
    
    showPendingUpdate: function(entity, callback) {
        if (this.pending[entity]) return;
        var self = this;
        
        var el = document.createElement('div');
        el.className = 'toast show position-fixed';
        el.style.cssText = 'bottom: 20px; right: 20px; z-index: 1055;';
        el.innerHTML = 
            '<div class="toast-header bg-info text-white">' +
                '<strong class="me-auto"><i class="bi bi-arrow-repeat me-2"></i>Данные изменены</strong>' +
                '<button type="button" class="btn-close btn-close-white"></button>' +
            '</div>' +
            '<div class="toast-body"><button class="btn btn-sm btn-primary"><i class="bi bi-arrow-clockwise me-1"></i>Обновить</button></div>';
        
        el.querySelector('.btn-close').onclick = function() { self.dismissPendingUpdate(entity); };
        el.querySelector('.btn-primary').onclick = function() { self.applyPendingUpdate(entity); };
        
        document.body.appendChild(el);
        this.pending[entity] = { el: el, callback: callback };
    },
    
    applyPendingUpdate: function(entity) {
        var p = this.pending[entity];
        if (p && p.callback) p.callback();
        this.dismissPendingUpdate(entity);
    },
    
    dismissPendingUpdate: function(entity) {
        var p = this.pending[entity];
        if (p && p.el) p.el.remove();
        delete this.pending[entity];
    },
    
    viewUpdateEl: null,
    
    showViewUpdate: function(callback) {
        if (this.viewUpdateEl) return;
        var self = this;
        
        var el = document.createElement('div');
        el.className = 'toast show position-fixed';
        el.style.cssText = 'bottom: 100px; right: 20px; z-index: 1060;';
        el.innerHTML = 
            '<div class="toast-header bg-warning text-dark">' +
                '<strong class="me-auto"><i class="bi bi-exclamation-triangle me-2"></i>Данные изменены</strong>' +
                '<button type="button" class="btn-close"></button>' +
            '</div>' +
            '<div class="toast-body">Сущность была изменена другим пользователем.<br>' +
            '<button class="btn btn-sm btn-primary mt-2"><i class="bi bi-arrow-clockwise me-1"></i>Обновить</button></div>';
        
        el.querySelector('.btn-close').onclick = function() { self.dismissViewUpdate(); };
        el.querySelector('.btn-primary').onclick = function() {
            if (callback) callback();
            self.dismissViewUpdate();
        };
        
        document.body.appendChild(el);
        this.viewUpdateEl = el;
    },
    
    dismissViewUpdate: function() {
        if (this.viewUpdateEl) {
            this.viewUpdateEl.remove();
            this.viewUpdateEl = null;
        }
    }
};

function showSuccess(msg) { NotificationManager.success(msg); }
function showError(msg) { NotificationManager.error(msg); }
function showAlert(msg) { NotificationManager.info(msg); }
function showWarning(msg) { NotificationManager.warning(msg); }
