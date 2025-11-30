var EnumCache = {
    data: {},
    loaded: false,
    // lastLoadTime: null,

    async load() {
        if (this.loaded) return this.data;
        try {
            this.data = await apiCall('/enums');
            this.loaded = true;
        } catch (e) {
            console.error('Failed to load enums', e);
        }
        return this.data;
    },

    get(enumName) {
        return this.data[enumName] || [];
    },

    getDisplayName(enumName, value) {
        if (!value) return '-';
        var item = this.get(enumName).find(function(e) { return e.name === value; });
        return item ? item.displayName : value;
    },

    getOptions(enumName) {
        return this.get(enumName);
    },

    populateSelect(selectId, enumName, placeholder) {
        var select = getElement(selectId);
        if (!select) return;
        var options = this.get(enumName);
        select.innerHTML = placeholder ? '<option value="">' + placeholder + '</option>' : '';
        options.forEach(function(o) {
            var opt = document.createElement('option');
            opt.value = o.name;
            opt.textContent = o.displayName;
            select.appendChild(opt);
        });
    }
};

