var CSS = {
    TABLE: 'table table-striped table-hover',
    TABLE_DARK: 'table-dark',
    TABLE_SECONDARY: 'table-secondary',
    TABLE_LIGHT: 'table-light',
    BTN_PRIMARY: 'btn btn-sm btn-primary',
    BTN_SECONDARY: 'btn btn-sm btn-secondary',
    BTN_OUTLINE: 'btn btn-sm btn-outline-secondary',
    FORM_SM: 'form-control form-control-sm',
    SELECT_SM: 'form-select form-select-sm',
    PAGE_ITEM: 'page-item',
    PAGE_LINK: 'page-link'
};

function attr(obj) {
    var parts = [];
    for (var k in obj) {
        if (obj[k] === true) parts.push(k);
        else if (obj[k]) parts.push(`${k}="${obj[k]}"`);
    }
    return parts.length ? ' ' + parts.join(' ') : '';
}

function toggleNullFilter(checkedId, otherId) {
    var checked = getElement(checkedId);
    var other = getElement(otherId);
    if (checked && checked.checked && other) other.checked = false;
}

var NavbarBuilder = {
    pages: [
        { href: 'main.html', label: 'Работники' },
        { href: 'organizations.html', label: 'Организации' },
        { href: 'persons.html', label: 'Персоны' },
        { href: 'addresses.html', label: 'Адреса' },
        { href: 'locations.html', label: 'Локации' }
    ],

    render: function(containerId, activePage) {
        var container = getElement(containerId);
        if (!container) return;

        var currentPage = activePage || window.location.pathname.split('/').pop();
        var navItems = this.pages.map(function(page) {
            var active = currentPage === page.href ? ' active' : '';
            return `<li class="nav-item"><a class="nav-link${active}" href="${page.href}">${page.label}</a></li>`;
        }).join('');

        container.innerHTML = `
            <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
                <div class="container-fluid">
                    <a class="navbar-brand" href="#">HRMS</a>
                    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                        <span class="navbar-toggler-icon"></span>
                    </button>
                    <div class="collapse navbar-collapse" id="navbarNav">
                        <ul class="navbar-nav me-auto">${navItems}</ul>
                        <button class="btn btn-outline-light" onclick="handleLogout()">Выход</button>
                    </div>
                </div>
            </nav>`;
    }
};

class ModalBuilder {
    constructor(modalId) {
        this.modalElement = getElement(modalId);
        this.bsModal = this.modalElement ? new bootstrap.Modal(this.modalElement) : null;
    }

    show() { if (this.bsModal) this.bsModal.show(); }
    hide() { if (this.bsModal) this.bsModal.hide(); }
}

class TableBuilder {
    constructor(config) {
        this.config = config;
        this.state = {
            page: 0,
            pageSize: config.constants.DEFAULT_PAGE_SIZE,
            totalPages: 0,
            sortFields: [],      // массив полей для мультисорта
            sortDirections: {},  // направление для каждого поля
            filters: [],
            collapsedGroups: config.collapsedGroups || []  // группы скрытые по умолчанию
        };
    }

    getGroups() {
        var groups = {};
        this.config.columns.forEach(function(col) {
            var g = col.group || col.groupHeader;
            if (g && !groups[g]) {
                groups[g] = col.groupLabel || g;
            }
        });
        return groups;
    }

    toggleGroup(groupName) {
        var idx = this.state.collapsedGroups.indexOf(groupName);
        if (idx === -1) {
            this.state.collapsedGroups.push(groupName);
        } else {
            this.state.collapsedGroups.splice(idx, 1);
        }
        this.updateGroupVisibility();
    }

    updateGroupVisibility() {
        var self = this;
        // скрыть/показать колонки
        this.state.collapsedGroups.forEach(function(group) {
            document.querySelectorAll(`[data-group="${group}"]`).forEach(function(cell) { cell.style.display = 'none'; });
        });
        Object.keys(this.getGroups()).forEach(function(group) {
            if (self.state.collapsedGroups.indexOf(group) === -1) {
                document.querySelectorAll(`[data-group="${group}"]`).forEach(function(cell) { cell.style.display = ''; });
            }
        });
        // перестроить мега-хедер с новыми colspan и иконками
        var groupHeaderRow = document.querySelector('.group-header-row');
        if (groupHeaderRow) {
            groupHeaderRow.outerHTML = this.renderGroupHeaderRow();
        }
    }


    calculateOptimalPageSize() {
        return Math.max(5, Math.min(Math.floor((window.innerHeight - 600) / 45), 50));
    }

    // синкает инпут pageSize с оптимальным значением
    syncPageSizeInput() {
        this.state.pageSize = this.calculateOptimalPageSize();
        var input = getElement(this.config.elementIds.PAGE_SIZE_INPUT);
        if (input) input.value = this.state.pageSize;
    }

    // собирает всю таблицу: мега-хедер + хедер + фильтры + null фильтры + tbody
    renderTableStructure(containerId) {
        var container = getElement(containerId);
        if (!container) return;

        var g = this.renderGroupHeaderRow();
        var h = this.renderHeaderRow();
        var f = this.renderFilterRow();
        var n = this.renderNullFilterRow();
        container.innerHTML = `
            <div class="table-responsive">
                <table class="${CSS.TABLE}">
                    <thead class="${CSS.TABLE_DARK}">${g}${h}${f}${n}</thead>
                    <tbody id="${this.config.elementIds.TABLE_BODY}"></tbody>
                </table>
            </div>`;
        
        this.updateGroupVisibility();
    }

    // мега-хедер с названиями групп (кликабельный для toggle)
    // groupHeader - только для мега-хедера, group - для мега-хедера И скрытия
    renderGroupHeaderRow() {
        var groups = this.getGroups();
        if (Object.keys(groups).length === 0) return '';

        var self = this;
        var toggleFunc = this.config.toggleGroupFunction || 'toggleGroup';
        var cells = [];
        var currentGroup = null;
        var colspan = 0;

        // считаем только видимые колонки (не скрытые группой)
        this.config.columns.forEach(function(col, idx) {
            var colGroup = col.groupHeader || col.group;
            var isHidden = col.group && self.state.collapsedGroups.indexOf(col.group) !== -1;
            var isLast = idx === self.config.columns.length - 1;
            
            if (colGroup !== currentGroup) {
                if (colspan > 0) {
                    var isCollapsed = currentGroup && self.state.collapsedGroups.indexOf(currentGroup) !== -1;
                    var icon = currentGroup ? (isCollapsed ? 'plus-square' : 'dash-square') : '';
                    var label = currentGroup ? `<i class="bi bi-${icon} me-1"></i>${groups[currentGroup]}` : '';
                    var a = attr({
                        colspan: colspan > 1 ? colspan : null,
                        class: currentGroup ? 'text-center group-header group-toggle' : '',
                        onclick: currentGroup && `${toggleFunc}('${currentGroup}')`
                    });
                    cells.push(`<th${a}>${label}</th>`);
                }
                currentGroup = colGroup;
                colspan = isHidden ? 0 : 1;
            } else {
                if (!isHidden) colspan++;
            }

            if (isLast && colspan > 0) {
                var isCollapsed = currentGroup && self.state.collapsedGroups.indexOf(currentGroup) !== -1;
                var icon = currentGroup ? (isCollapsed ? 'plus-square' : 'dash-square') : '';
                var label = currentGroup ? `<i class="bi bi-${icon} me-1"></i>${groups[currentGroup]}` : '';
                var a = attr({
                    colspan: colspan > 1 ? colspan : null,
                    class: currentGroup ? 'text-center group-header group-toggle' : '',
                    onclick: currentGroup && `${toggleFunc}('${currentGroup}')`
                });
                cells.push(`<th${a}>${label}</th>`);
            }
        });

        if (this.config.actions) cells.push('<th></th>');
        return `<tr class="group-header-row">${cells.join('')}</tr>`;
    }

    // хедер с названиями колонок, кликабельные если sortable
    renderHeaderRow() {
        var self = this;
        var sortFunc = this.config.sortFunction || 'addSort';

        var headers = this.config.columns.map(function(col) {
            var a = attr({
                class: self.getCellClass(col),
                'data-group': col.group,
                onclick: col.sortable && `${sortFunc}('${col.field}')`,
                style: col.sortable && 'cursor:pointer'
            });
            var sortSpan = col.sortable ? `<span id="${self.config.sortPrefix}${col.field}"></span>` : '';
            return `<th${a}>${col.label}${sortSpan}</th>`;
        });

        if (this.config.actions) headers.push('<th class="td-narrow">Действия</th>');
        return `<tr>${headers.join('')}</tr>`;
    }

    // строка с инпутами/селектами для фильтрации + кнопки
    renderFilterRow() {
        var self = this;
        var filters = this.config.columns.map(function(col) {
            var a = attr({ class: self.getCellClass(col), 'data-group': col.group });
            var input = col.filterable ? self.createFilterInput(col) : '';
            return `<td${a}>${input}</td>`;
        });

        if (this.config.actions) {
            var af = this.config.applyFiltersFunction || 'applyFilters';
            var cf = this.config.clearFiltersFunction || 'clearFilters';
            var cs = this.config.clearSortFunction || 'clearSort';
            filters.push(`<td class="text-nowrap">
                <button class="${CSS.BTN_PRIMARY} me-1" onclick="${af}()" title="Применить"><i class="bi bi-check-lg"></i></button>
                <button class="${CSS.BTN_SECONDARY} me-1" onclick="${cf}()" title="Сбросить фильтры"><i class="bi bi-funnel"></i></button>
                <button class="${CSS.BTN_OUTLINE}" onclick="${cs}()" title="Сбросить сортировку"><i class="bi bi-arrow-down-up"></i></button>
            </td>`);
        }

        return `<tr class="${CSS.TABLE_SECONDARY}">${filters.join('')}</tr>`;
    }

    // создаёт инпут/селект для фильтра колонки
    createFilterInput(col) {
        var id = this.generateFilterId(col.field);
        var af = this.config.applyFiltersFunction || 'applyFilters';
        var onEnter = `onkeydown="if(event.key==='Enter')${af}()"`;

        switch (col.type) {
            case 'number':
                return `<input type="number" class="${CSS.FORM_SM}" id="${id}" placeholder="${col.label}" ${onEnter}>`;
            case 'date':
                return `<input type="date" class="${CSS.FORM_SM}" id="${id}" ${onEnter}>`;
            case 'datetime':
                return `<input type="datetime-local" class="${CSS.FORM_SM}" id="${id}" ${onEnter}>`;
            case 'enum':
                var opts = (col.enumName ? EnumCache.getOptions(col.enumName) : col.options.map(function(o) { return {name: o, displayName: o}; }))
                    .map(function(o) { return `<option value="${o.name}">${o.displayName}</option>`; }).join('');
                return `<select class="${CSS.SELECT_SM}" id="${id}" onchange="${af}()"><option value="">-- ${col.label} --</option>${opts}</select>`;
            default:
                return `<input type="text" class="${CSS.FORM_SM}" id="${id}" placeholder="${col.label}" ${onEnter}>`;
        }
    }

    // строка с чекбоксами NULL / NOT NULL для nullable полей
    renderNullFilterRow() {
        if (!this.config.columns.some(function(c) { return c.nullable; })) return '';

        var self = this;
        var af = this.config.applyFiltersFunction || 'applyFilters';
        var cells = this.config.columns.map(function(col) {
            var a = attr({ class: self.getCellClass(col), 'data-group': col.group });
            if (!col.nullable) return `<td${a}></td>`;
            var nullId = self.generateNullFilterId(col.field);
            var notNullId = self.generateNotNullFilterId(col.field);
            return `<td${a}>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="checkbox" id="${nullId}" onchange="toggleNullFilter('${nullId}','${notNullId}'); ${af}()">
                    <label class="form-check-label small" for="${nullId}">NULL</label>
                </div>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="checkbox" id="${notNullId}" onchange="toggleNullFilter('${notNullId}','${nullId}'); ${af}()">
                    <label class="form-check-label small" for="${notNullId}">≠NULL</label>
                </div>
            </td>`;
        });

        if (this.config.actions) cells.push('<td class="td-narrow"></td>');
        return `<tr class="${CSS.TABLE_LIGHT}">${cells.join('')}</tr>`;
    }

    // person.name -> filterPersonName (camelCase)
    generateFilterId(field) {
        return 'filter' + field.split('.').map(function(p) { return p.charAt(0).toUpperCase() + p.slice(1); }).join('');
    }

    generateNullFilterId(field) { return this.generateFilterId(field) + 'Null'; }
    generateNotNullFilterId(field) { return this.generateFilterId(field) + 'NotNull'; }

    // генерит конфиги фильтров из columns: id, field, operator, parse
    generateFilterConfigs() {
        var self = this;
        return this.config.columns.filter(function(c) { return c.filterable; }).map(function(col) {
            var operator, parse = null;
            switch (col.type) {
                case 'number':
                    operator = 'EQUALS';
                    parse = col.field.includes('.') ? parseFloat : parseInt;
                    break;
                case 'date':
                case 'datetime':
                case 'enum':
                    operator = 'EQUALS';
                    break;
                default:
                    operator = 'CONTAINS';
            }
            return { id: self.generateFilterId(col.field), field: col.field, operator: operator, parse: parse };
        });
    }

    // конфиги для null/notNull чекбоксов
    generateNullFilterConfigs() {
        var self = this;
        var configs = [];
        this.config.columns.filter(function(c) { return c.nullable; }).forEach(function(col) {
            configs.push({ id: self.generateNullFilterId(col.field), field: col.field, operator: 'IS_NULL' });
            configs.push({ id: self.generateNotNullFilterId(col.field), field: col.field, operator: 'IS_NOT_NULL' });
        });
        return configs;
    }

    // вызывать после renderTableStructure - инитит filterConfigs и sortableFields
    initDynamicFilters() {
        this.config.filterConfigs = this.generateFilterConfigs();
        this.config.nullFilterConfigs = this.generateNullFilterConfigs();
        if (!this.config.sortableFields) {
            this.config.sortableFields = this.config.columns.filter(function(c) { return c.sortable; }).map(function(c) { return c.field; });
        }
    }

    // рендерит строки таблицы, customRowRenderer если нужна кастомная вёрстка
    renderTable(data, customRowRenderer) {
        var tbody = getElement(this.config.elementIds.TABLE_BODY);
        if (!tbody) return;

        tbody.innerHTML = '';
        var self = this;
        data.forEach(function(item) {
            var row = tbody.insertRow();
            row.innerHTML = customRowRenderer ? customRowRenderer(item) : self.defaultRowRenderer(item);
        });
    }

    // дефолтный рендер строки: значения колонок + actions
    defaultRowRenderer(item) {
        var self = this;
        var cells = this.config.columns.map(function(col) {
            var value = col.format ? col.format(self.getNestedValue(item, col.field), item) : self.getNestedValue(item, col.field);
            var hidden = col.group && self.state.collapsedGroups.indexOf(col.group) !== -1;
            var a = attr({
                class: self.getCellClass(col),
                'data-group': col.group,
                style: hidden && 'display:none'
            });
            return `<td${a}>${value != null ? value : '-'}</td>`;
        });
        if (this.config.actions) cells.push(`<td class="action-buttons">${this.config.actions(item)}</td>`);
        return cells.join('');
    }

    // возвращает css класс для ячейки: narrow для id/number, wrap для text
    getCellClass(col) {
        if (col.field === 'id' || col.field.endsWith('.id')) return 'td-narrow';
        if (col.type === 'number' || col.type === 'date' || col.type === 'datetime' || col.type === 'enum') return 'td-narrow';
        if (col.type === 'text') return 'td-wrap';
        return '';
    }

    // достаёт значение по пути типа person.name из объекта
    getNestedValue(obj, path) {
        return path.split('.').reduce(function(cur, key) { return cur && cur[key] !== undefined ? cur[key] : null; }, obj);
    }

    renderPagination(totalPages) {
        this.state.totalPages = totalPages;
        var self = this;
        renderPagination(this.config.elementIds.PAGINATION, this.state.page, totalPages, function(p) { self.changePage(p); });
    }

    changePage(page) {
        if (page < 0 || page >= this.state.totalPages) return;
        this.state.page = page;
        if (this.config.onLoad) this.config.onLoad();
    }

    // добавляет/меняет/убирает сортировку: 1й клик asc, 2й desc, 3й убирает
    addSort(field) {
        toggleSort(field, this.state.sortFields, this.state.sortDirections);
        this.updateSortIndicators();
        this.state.page = 0;
        if (this.config.onLoad) this.config.onLoad();
    }

    clearSort() {
        this.state.sortFields = [];
        this.state.sortDirections = {};
        this.updateSortIndicators();
        this.state.page = 0;
        if (this.config.onLoad) this.config.onLoad();
    }

    updateSortIndicators() {
        updateSortIndicators(this.config.sortPrefix, this.config.sortableFields, this.state.sortFields, this.state.sortDirections);
    }

    // собирает url для GET запроса: endpoint?page=X&size=Y&sort=...
    buildUrl(baseEndpoint) {
        var url = `${baseEndpoint}?page=${this.state.page}&size=${this.state.pageSize}`;
        return this.state.sortFields.length > 0 ? appendSortParams(url, this.state.sortFields, this.state.sortDirections) : url + '&sort=id,asc';
    }

    applyFilters(customFilters) {
        this.state.filters = customFilters || this.buildFilters();
        this.state.page = 0;
        if (this.config.onLoad) this.config.onLoad();
    }

    // собирает фильтры из инпутов + null/notNull чекбоксов
    buildFilters() {
        var filters = buildFiltersFromConfigs(this.config.filterConfigs);
        if (this.config.nullFilterConfigs) {
            this.config.nullFilterConfigs.forEach(function(cfg) {
                var el = getElement(cfg.id);
                if (el && el.checked) filters.push({ field: cfg.field, operator: cfg.operator, value: null });
            });
        }
        return filters;
    }

    // сбрасывает все инпуты и чекбоксы
    clearFilters() {
        this.config.filterConfigs.forEach(function(f) {
            var el = getElement(f.id);
            if (el) el.type === 'checkbox' ? el.checked = false : el.value = '';
        });
        if (this.config.nullFilterConfigs) {
            this.config.nullFilterConfigs.forEach(function(f) {
                var el = getElement(f.id);
                if (el) el.checked = false;
            });
        }
        this.state.filters = [];
        this.state.page = 0;
        if (this.config.onLoad) this.config.onLoad();
    }

    applyPageSize() {
        var self = this;
        validateAndApplyPageSize(getElement(this.config.elementIds.PAGE_SIZE_INPUT), this.state, this.config.constants, function() {
            self.state.page = 0;
            if (self.config.onLoad) self.config.onLoad();
        });
    }

    // хелперы для проверки состояния (юзаются в вебсокет логике)
    hasFilters() { return this.state.filters.length > 0; }
    hasSorting() { return this.state.sortFields.length > 0; }
    isFirstPage() { return this.state.page === 0; }

    // url для POST /filter endpoint
    getFilterUrl(baseEndpoint) {
        var url = `${baseEndpoint}/filter?page=${this.state.page}&size=${this.state.pageSize}`;
        return this.state.sortFields.length > 0 ? appendSortParams(url, this.state.sortFields, this.state.sortDirections) : url + '&sort=id,asc';
    }
}

// менеджер CRUD операций - load/save/delete
class CrudManager {
    constructor(config) {
        this.config = config;
        this.tableBuilder = config.tableBuilder;
        this.modal = config.modal;
        this.deleteModal = config.deleteModal;
    }

    getGenderSuffix() {
        return this.config.gender === 'f' ? 'а' : this.config.gender === 'n' ? 'о' : '';
    }

    // грузит данные: если есть фильтры POST /filter, иначе GET
    async load() {
        try {
            var hasFilters = this.tableBuilder.hasFilters();
            var url = hasFilters ? this.tableBuilder.getFilterUrl(this.config.endpoint) : this.tableBuilder.buildUrl(this.config.endpoint);
            var opts = hasFilters ? { method: 'POST', body: JSON.stringify(this.tableBuilder.state.filters) } : { method: 'GET' };
            // console.log('loading', url);

            var data = await apiCall(url, opts);
            this.tableBuilder.renderTable(data.content, this.config.rowRenderer);
            this.tableBuilder.renderPagination(data.totalPages);
            this.tableBuilder.updateSortIndicators();
            return data;
        } catch (e) {
            showError(`Ошибка загрузки ${this.config.entityNamePlural}: ${e.message}`);
            throw e;
        }
    }

    async loadOne(id) {
        try {
            return await apiCall(`${this.config.endpoint}/${id}`);
        } catch (e) {
            showError(`Ошибка загрузки ${this.config.entityName}: ${e.message}`);
            throw e;
        }
    }

    // сохраняет сущность: POST если новая, PUT если есть id
    async save(data, id) {
        var url = id ? `${this.config.endpoint}/${id}` : this.config.endpoint;
        try {
            if (typeof wsIgnore === 'function') wsIgnore(); // хак: игнорим вебсокеты от своего апдейта в течение некоторое времени
            var result = await apiCall(url, {
                method: id ? 'PUT' : 'POST',
                body: JSON.stringify(data)
            });
            if (this.modal) this.modal.hide();
            await this.load();
            var suffix = this.getGenderSuffix();
            showSuccess(`${this.config.entityName} ${id ? 'обновлен' : 'создан'}${suffix}`);
            return result;
        } catch (e) {
            showError(`Ошибка сохранения: ${e.message}`);
            throw e;
        }
    }

    // удаляет с опциональным replacementId (для замены связей)
    async delete(id, replacementId) {
        var q = replacementId ? `?replacementId=${replacementId}` : '';
        var url = `${this.config.endpoint}/${id}${q}`;
        try {
            if (typeof wsIgnore === 'function') wsIgnore();
            await apiCall(url, { method: 'DELETE' });
            if (this.deleteModal) this.deleteModal.hide();
            await this.load();
            showSuccess(`${this.config.entityName} удален${this.getGenderSuffix()}`);
        } catch (e) {
            showError(`Ошибка удаления: ${e.message}`);
            throw e;
        }
    }
}

// рендерит пагинацию с многоточием если страниц много
function renderPagination(paginationId, currentPage, totalPages, onPageChange) {
    var pagination = getElement(paginationId);
    if (!pagination) return;
    
    // var debug = false;

    pagination.innerHTML = '';
    if (totalPages <= 1) return;

    var createPageItem = function(page, text, disabled, active, isEllipsis) {
        var li = document.createElement('li');
        li.className = CSS.PAGE_ITEM + (disabled ? ' disabled' : '') + (active ? ' active' : '');
        var a = document.createElement('a');
        a.className = CSS.PAGE_LINK;
        a.href = '#';
        a.textContent = text;
        if (!disabled && !isEllipsis) a.onclick = function(e) { e.preventDefault(); onPageChange(page); };
        li.appendChild(a);
        return li;
    };

    // стрелка влево
    pagination.appendChild(createPageItem(currentPage - 1, '‹', currentPage === 0, false));

    // номера страниц с ... если много
    var maxVisible = 7, pages = [];
    if (totalPages <= maxVisible) {
        for (var i = 0; i < totalPages; i++) pages.push(i);
    } else {
        pages.push(0); // первая всегда
        var start = Math.max(1, currentPage - 2), end = Math.min(totalPages - 2, currentPage + 2);
        if (start > 1) pages.push(-1); // ... слева
        for (var i = start; i <= end; i++) pages.push(i);
        if (end < totalPages - 2) pages.push(-1); // ... справа
        pages.push(totalPages - 1); // последняя всегда
    }

    pages.forEach(function(i) {
        pagination.appendChild(i === -1 ? createPageItem(0, '...', true, false, true) : createPageItem(i, i + 1, false, i === currentPage));
    });

    // стрелка вправо
    pagination.appendChild(createPageItem(currentPage + 1, '›', currentPage === totalPages - 1, false));
}

// собирает массив фильтров из конфигов (читает значения из инпутов)
function buildFiltersFromConfigs(filterConfigs) {
    var filters = [];
    filterConfigs.forEach(function(cfg) {
        var value = getElementValue(cfg.id);
        if (value) filters.push({ field: cfg.field, operator: cfg.operator, value: cfg.parse ? cfg.parse(value) : value });
    });
    return filters;
}

// добавляет sort параметры к url
function appendSortParams(url, sortFields, sortDirections) {
    sortFields.forEach(function(f) {
        url += `&sort=${f},${sortDirections[f] || 'asc'}`;
    });
    return url;
}

// обновляет индикаторы сортировки в хедере (стрелочки с номером)
function updateSortIndicators(prefix, sortableFields, sortFields, sortDirections) {
    sortableFields.forEach(function(field) {
        var indicator = getElement(prefix + field);
        if (!indicator) return;
        var index = sortFields.indexOf(field);
        indicator.textContent = index === -1 ? '' : ' ' + (index + 1) + (sortDirections[field] === 'asc' ? '↑' : '↓');
    });
}

// переключает сортировку: нет -> asc -> desc -> нет
function toggleSort(field, sortFields, sortDirections) {
    var index = sortFields.indexOf(field);
    if (index === -1) {
        sortFields.push(field);
        sortDirections[field] = 'asc';
    } else if (sortDirections[field] === 'asc') {
        sortDirections[field] = 'desc';
    } else {
        sortFields.splice(index, 1);
        delete sortDirections[field];
    }
}

// валидирует и применяет pageSize из инпута
function validateAndApplyPageSize(input, state, constants, loadFunction) {
    if (!input) return;

    var value = parseInt(input.value);
    if (isNaN(value) || value < constants.MIN_PAGE_SIZE) {
        showWarning(`Введите число больше ${constants.MIN_PAGE_SIZE - 1}`);
        input.value = state.pageSize;
        return;
    }
    if (value > constants.MAX_PAGE_SIZE) {
        showWarning(`Максимальное количество элементов: ${constants.MAX_PAGE_SIZE}`);
        input.value = constants.MAX_PAGE_SIZE;
        value = constants.MAX_PAGE_SIZE;
    }
    state.pageSize = value;
    loadFunction(0);
}

// билдер для tom-select с серверным поиском
// TODO: вынести в отдельный файл?
var TomSelectBuilder = {
    create: function(element, config) {
        var el = typeof element === 'string' ? getElement(element) : element;
        if (!el) return null;

        var options = {
            valueField: 'value',
            labelField: 'text',
            searchField: ['text'],
            maxOptions: config.maxOptions || 100,
            placeholder: config.placeholder || 'Поиск...',
            allowEmptyOption: config.allowEmpty !== false,
            preload: config.preload || false,
            render: config.render || {}
        };

        // если есть endpoint - делаем серверный поиск
        if (config.searchEndpoint) {
            options.load = function(query, callback) {
                var filter = config.buildFilter ? config.buildFilter(query) : [{
                    field: config.searchField || 'name',
                    operator: 'contains',
                    value: query
                }];
                apiCall(config.searchEndpoint, {
                    method: 'POST',
                    body: JSON.stringify(filter)
                }).then(function(resp) {
                    var items = (resp.content || resp).map(config.formatOption).filter(function(x) { return x != null; });
                    callback(items);
                }).catch(function(e) {
                    console.error('TomSelect load error:', e);
                    callback();
                });
            };
        } else if (config.options) {
            options.options = config.options;
        }

        return new TomSelect(el, options);
    },

    // шорткат для создания tom-select по entityConfig
    createForEntity: function(element, entityConfig) {
        return this.create(element, {
            searchEndpoint: `${entityConfig.endpoint}/filter`,
            searchField: entityConfig.searchField || 'name',
            formatOption: entityConfig.formatOption,
            placeholder: entityConfig.placeholder || 'Поиск...',
            allowEmpty: entityConfig.allowEmpty,
            maxOptions: entityConfig.maxOptions || 100,
            preload: entityConfig.preload || 'focus',
            buildFilter: entityConfig.buildFilter
        });
    },

    // уничтожает массив инстансов (чтоб не текла память)
    destroyAll: function(instances) {
        if (!instances) return;
        instances.forEach(function(ts) {
            if (ts && ts.destroy) ts.destroy();
        });
    }
};

var ViewModalBuilder = {
    entity: null,
    entityId: null,
    data: null,
    modal: null,
    config: null,
    
    init: function(modalId) {
        var el = getElement(modalId || 'viewModal');
        if (el) this.modal = new bootstrap.Modal(el);
    },
    
    show: function(entity, id, data, config) {
        this.entity = entity;
        this.entityId = id;
        this.data = data;
        this.config = config;
        
        if (!this.modal) this.init();
        
        var title = getElement('viewModalTitle');
        var body = getElement('viewModalBody');
        var editBtn = getElement('viewModalEditBtn');
        
        if (title) title.textContent = config.title || 'Просмотр';
        if (body) body.innerHTML = this.renderContent(data, config.fields);
        if (editBtn) {
            editBtn.onclick = function() {
                ViewModalBuilder.close();
                if (config.onEdit) config.onEdit(id);
            };
        }
        
        this.modal.show();
    },
    
    renderContent: function(data, fields) {
        if (!fields || !fields.length) {
            return '<pre>' + JSON.stringify(data, null, 2) + '</pre>';
        }
        
        var rows = fields.map(function(f) {
            var val = ViewModalBuilder.getNestedValue(data, f.field);
            if (f.format) val = f.format(val, data);
            else if (val === null || val === undefined) val = '-';
            return `<tr><th class="text-muted" style="width:40%">${f.label}</th><td>${val}</td></tr>`;
        }).join('');
        
        return `<table class="table table-sm table-borderless"><tbody>${rows}</tbody></table>`;
    },
    
    getNestedValue: function(obj, path) {
        if (!obj || !path) return null;
        return path.split('.').reduce(function(o, k) { return o ? o[k] : null; }, obj);
    },
    
    refresh: function(newData) {
        if (newData) this.data = newData;
        var body = getElement('viewModalBody');
        if (body && this.config) {
            body.innerHTML = this.renderContent(this.data, this.config.fields);
        }
    },
    
    onWsUpdate: function(entity, updatedId) {
        if (this.entity === entity && this.entityId == updatedId && this.isOpen()) {
            NotificationManager.showViewUpdate(this.config.loadOne);
        }
    },
    
    isOpen: function() {
        var el = getElement('viewModal');
        return el && el.classList.contains('show');
    },
    
    close: function() {
        if (this.modal) this.modal.hide();
        this.entity = null;
        this.entityId = null;
        this.data = null;
    }
};
