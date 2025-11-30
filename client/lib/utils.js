String.prototype.fmt = function(params) {
    return this.replace(/\{(\w+)\}/g, function(_, key) { return params[key]; });
};

function getElement(id) {
    return document.getElementById(id);
}

function formatDate(dateString) {
    if (!dateString) return "N/A";
    var date = new Date(dateString);
    return date.toLocaleDateString();
}

function formatDateTime(dateString) {
    if (!dateString) return 'N/A';
    var date = new Date(dateString);
    return date.toLocaleString();
}

function formatDateTimeWithTz(dateString) {
    if (!dateString) return '-';
    var date = new Date(dateString);
    var tz = '';
    if (dateString.endsWith('Z')) {
        tz = 'UTC';
    } else {
        var match = dateString.match(/([+-]\d{2}:\d{2})$/);
        if (match) tz = 'UTC' + match[1];
    }
    return date.toLocaleString() + (tz ? ' (' + tz + ')' : '');
}

function escapeHtml(text) {
    var div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function getElementValue(id) {
    var el = getElement(id);
    return el ? el.value : '';
}

function setElementValue(id, value) {
    var el = getElement(id);
    if (el) el.value = value || '';
}

var TimezoneUtil = {
    offsets: [
        { value: '-12:00', label: 'UTC-12:00' },
        { value: '-11:00', label: 'UTC-11:00' },
        { value: '-10:00', label: 'UTC-10:00 (Гавайи)' },
        { value: '-09:00', label: 'UTC-9:00 (Аляска)' },
        { value: '-08:00', label: 'UTC-8:00 (Лос-Анджелес)' },
        { value: '-07:00', label: 'UTC-7:00 (Денвер)' },
        { value: '-06:00', label: 'UTC-6:00 (Чикаго)' },
        { value: '-05:00', label: 'UTC-5:00 (Нью-Йорк)' },
        { value: '-04:00', label: 'UTC-4:00' },
        { value: '-03:00', label: 'UTC-3:00' },
        { value: '-02:00', label: 'UTC-2:00' },
        { value: '-01:00', label: 'UTC-1:00' },
        { value: '+00:00', label: 'UTC+0:00 (Лондон)' },
        { value: '+01:00', label: 'UTC+1:00 (Париж)' },
        { value: '+02:00', label: 'UTC+2:00 (Киев)' },
        { value: '+03:00', label: 'UTC+3:00 (Москва)' },
        { value: '+04:00', label: 'UTC+4:00 (Дубай)' },
        { value: '+05:00', label: 'UTC+5:00 (Ташкент)' },
        { value: '+05:30', label: 'UTC+5:30 (Дели)' },
        { value: '+06:00', label: 'UTC+6:00 (Алматы)' },
        { value: '+07:00', label: 'UTC+7:00 (Бангкок)' },
        { value: '+08:00', label: 'UTC+8:00 (Пекин)' },
        { value: '+09:00', label: 'UTC+9:00 (Токио)' },
        { value: '+10:00', label: 'UTC+10:00 (Сидней)' },
        { value: '+11:00', label: 'UTC+11:00' },
        { value: '+12:00', label: 'UTC+12:00' }
    ],
    
    getBrowserOffset: function() {
        var offset = -new Date().getTimezoneOffset();
        var sign = offset >= 0 ? '+' : '-';
        var hours = Math.floor(Math.abs(offset) / 60);
        var mins = Math.abs(offset) % 60;
        return sign + String(hours).padStart(2, '0') + ':' + String(mins).padStart(2, '0');
    },
    
    populate: function(selectId) {
        var select = getElement(selectId);
        if (!select) return;
        var browserOffset = this.getBrowserOffset();
        select.innerHTML = this.offsets.map(function(tz) {
            var selected = tz.value === browserOffset ? ' selected' : '';
            return '<option value="' + tz.value + '"' + selected + '>' + tz.label + '</option>';
        }).join('');
    },
    
    toISOWithOffset: function(datetimeLocalValue, offsetValue) {
        if (!datetimeLocalValue) return null;
        return datetimeLocalValue + ':00' + offsetValue;
    }
};
