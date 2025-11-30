var API = {
    ORGANIZATIONS: '/organizations',
    ORGANIZATIONS_BY_ID: '/organizations/{id}',
    ORGANIZATIONS_RELATED: '/organizations/{id}/related-workers',
    ORGANIZATIONS_FILTER: '/organizations/filter',
    WORKERS: '/workers',
    WORKERS_FILTER: '/workers/filter',
    WORKERS_BATCH: '/workers/batch',
    SALARY_INDEX: '/organizations/{id}/workers/salary/indexation'
};

var ENTITY_CONFIGS = {
    organizations: {
        endpoint: API.ORGANIZATIONS_FILTER,
        formatOption: function(o) {
            var addr = o.officialAddress ? o.officialAddress.zipCode : 'без адреса';
            return { 
                value: o.id, 
                text: '{name} — {type}, оборот {turn}, {addr}'.fmt({
                    name: o.fullName,
                    type: EnumCache.getDisplayName('organizationTypes', o.type) || 'без типа',
                    turn: o.annualTurnover ? o.annualTurnover.toLocaleString() : '-',
                    addr: addr
                }),
                data: o
            };
        },
        placeholder: 'Поиск организации...',
        allowEmpty: false,
        preload: 'focus',
        buildFilter: function(query) {
            return [{ field: 'fullName,type', operator: 'contains_any', value: query }];
        }
    }
};

var orgConfig = {
    endpoint: API.ORGANIZATIONS,
    entityName: 'Организация',
    entityNamePlural: 'организаций',
    gender: 'f',
    constants: { DEFAULT_PAGE_SIZE: 10, MIN_PAGE_SIZE: 1, MAX_PAGE_SIZE: 1000 },
    elementIds: {
        TABLE_BODY: 'orgsTableBody',
        PAGINATION: 'orgPagination',
        PAGE_SIZE_INPUT: 'orgPageSizeInput'
    },
    sortPrefix: 'sort-org-',
    sortFunction: 'addOrgSort',
    applyFiltersFunction: 'applyOrgFilters',
    clearFiltersFunction: 'clearOrgFilters',
    clearSortFunction: 'clearOrgSort',
    collapsedGroups: ['address'],
    toggleGroupFunction: 'toggleOrgGroup',
    columns: [
        // Основные поля организации (всегда видны)
        { field: 'id', label: 'ID', type: 'number', sortable: true, filterable: true },
        { field: 'fullName', label: 'Название', type: 'text', sortable: true, filterable: true },
        { field: 'type', label: 'Тип', type: 'enum', sortable: true, filterable: true, nullable: true, nullLabel: 'Нет', enumName: 'organizationTypes',
          format: function(v) { return EnumCache.getDisplayName('organizationTypes', v); } },
        { field: 'annualTurnover', label: 'Оборот', type: 'number', sortable: true, filterable: true,
          format: function(v) { return v ? v.toLocaleString() : '-'; } },
        { field: 'employeesCount', label: 'Сотр.', type: 'number', sortable: true, filterable: true, nullable: true, nullLabel: 'Нет' },
        { field: 'rating', label: 'Рейтинг', type: 'number', sortable: true, filterable: true,
          format: function(v) { return v ? v.toFixed(1) : '-'; } },
        // Адрес: основное поле (под мега-хедером, но всегда видно)
        { field: 'officialAddress.zipCode', label: 'Индекс', type: 'text', sortable: true, filterable: true, nullable: true, nullLabel: 'Нет', groupHeader: 'address', groupLabel: 'Адрес',
          format: function(v, item) { return item.officialAddress?.zipCode || '-'; } },
        // Адрес: детали (группа address - скрываются)
        { field: 'officialAddress.id', label: 'ID', type: 'number', sortable: true, filterable: true, group: 'address', groupLabel: 'Адрес',
          format: function(v, item) { return item.officialAddress?.id ?? '-'; } }
    ],
    actions: function(item) {
        return ('<button class="btn btn-sm btn-info compact-btn" onclick="viewOrg({id})" title="Просмотр"><i class="bi bi-eye"></i></button>' +
                '<button class="btn btn-sm btn-primary compact-btn" onclick="editOrg({id})" title="Изменить"><i class="bi bi-pencil"></i></button>' +
                '<button class="btn btn-sm btn-success compact-btn" onclick="showIndexModal({id})" title="Индексация"><i class="bi bi-currency-dollar"></i></button>' +
                '<button class="btn btn-sm btn-danger compact-btn" onclick="deleteOrg({id})" title="Удалить"><i class="bi bi-trash"></i></button>').fmt(item);
    }
};

var orgTable, orgCrud, orgModal, orgDeleteModal, indexModal;
var deleteState = { id: null, workers: [], tomSelects: [] };

async function initOrganizations() {
    await EnumCache.load();
    EnumCache.populateSelect('orgType', 'organizationTypes', 'Не выбрано');

    orgModal = new ModalBuilder('orgModal');
    orgDeleteModal = new ModalBuilder('deleteConfirmModal');
    indexModal = new ModalBuilder('indexSalariesModal');
    
    orgTable = new TableBuilder(orgConfig);
    orgTable.renderTableStructure('tableContainer');
    orgTable.initDynamicFilters();
    orgTable.syncPageSizeInput();
    orgTable.config.onLoad = loadOrganizations;
    
    orgCrud = new CrudManager({
        endpoint: orgConfig.endpoint,
        entityName: orgConfig.entityName,
        entityNamePlural: orgConfig.entityNamePlural,
        gender: orgConfig.gender,
        tableBuilder: orgTable,
        modal: orgModal,
        deleteModal: orgDeleteModal,
        useWebSocket: true
    });
    
    loadOrganizations();
    connectWebSocket([{ entity: 'organizations', onUpdate: loadOrganizations, tableBuilder: orgTable }]);
}

function loadOrganizations() { orgCrud.load(); }
function changePage(page) { orgTable.changePage(page); }
function applyOrgPageSize() { orgTable.applyPageSize(); }
function addOrgSort(field) { orgTable.addSort(field); }
function clearOrgSort() { orgTable.clearSort(); }
function applyOrgFilters() { orgTable.applyFilters(); }
function clearOrgFilters() { orgTable.clearFilters(); }
function toggleOrgGroup(group) { orgTable.toggleGroup(group); }

var orgViewFields = [
    { field: 'id', label: 'ID' },
    { field: 'fullName', label: 'Название' },
    { field: 'type', label: 'Тип', format: function(v) { return EnumCache.getDisplayName('organizationTypes', v); } },
    { field: 'annualTurnover', label: 'Годовой оборот', format: function(v) { return v?.toLocaleString() ?? '-'; } },
    { field: 'employeesCount', label: 'Кол-во сотрудников' },
    { field: 'rating', label: 'Рейтинг', format: function(v) { return v?.toFixed(1) ?? '-'; } },
    { field: 'officialAddress.id', label: 'Адрес ID' },
    { field: 'officialAddress.zipCode', label: 'Почтовый индекс' }
];

async function viewOrg(id) {
    try {
        var data = await orgCrud.loadOne(id);
        ViewModalBuilder.show('organizations', id, data, {
            title: 'Просмотр организации #' + id,
            fields: orgViewFields,
            onEdit: editOrg,
            loadOne: function() { viewOrg(id); }
        });
    } catch (e) {
        showError('Ошибка загрузки: ' + e.message);
    }
}

var addressTomSelect = null;

function toggleAddressType() {
    var type = document.querySelector('input[name="addressType"]:checked').value;
    getElement('addressExistingBlock').style.display = type === 'existing' ? 'block' : 'none';
    getElement('addressEditBlock').style.display = type === 'edit' ? 'block' : 'none';
    getElement('addressNewBlock').style.display = type === 'new' ? 'block' : 'none';
    
    if (type === 'existing' && !addressTomSelect) {
        addressTomSelect = TomSelectBuilder.create(getElement('addressSelect'), {
            searchEndpoint: '/addresses/filter',
            formatOption: function(a) { return { value: a.id, text: 'ID:' + a.id + ' — ' + a.zipCode, data: a }; },
            placeholder: 'Поиск адреса...',
            allowEmpty: true,
            preload: 'focus',
            buildFilter: function(q) { return [{ field: 'zipCode', operator: 'contains', value: q || '' }]; }
        });
    }
}

function openCreateModal() {
    getElement('orgForm').reset();
    setElementValue('orgId', '');
    getElement('addrTypeNone').checked = true;
    toggleAddressType();
    getElement('orgModalTitle').textContent = 'Создать организацию';
    orgModal.show();
}

async function editOrg(id) {
    try {
        var org = await orgCrud.loadOne(id);
        setElementValue('orgId', org.id);
        setElementValue('fullName', org.fullName);
        setElementValue('annualTurnover', org.annualTurnover);
        setElementValue('employeesCount', org.employeesCount || '');
        setElementValue('orgRating', org.rating);
        setElementValue('orgType', org.type || '');
        
        setElementValue('editAddressId', '');
        setElementValue('editZipCode', '');
        
        if (org.officialAddress) {
            getElement('addrTypeEdit').checked = true;
            setElementValue('editAddressId', org.officialAddress.id);
            setElementValue('editZipCode', org.officialAddress.zipCode);
            toggleAddressType();
        } else {
            getElement('addrTypeNone').checked = true;
            toggleAddressType();
        }
        
        getElement('orgModalTitle').textContent = 'Редактировать организацию';
        orgModal.show();
    } catch (e) {
        showError('Ошибка загрузки: {msg}'.fmt({ msg: e.message }));
    }
}

async function saveOrg() {
    var form = getElement('orgForm');
    if (!form.checkValidity()) { form.reportValidity(); return; }
    
    var employees = getElementValue('employeesCount');
    var type = getElementValue('orgType');
    var addrType = document.querySelector('input[name="addressType"]:checked').value;
    
    var data = {
        fullName: getElementValue('fullName').trim(),
        annualTurnover: parseFloat(getElementValue('annualTurnover')),
        rating: parseFloat(getElementValue('orgRating')),
        employeesCount: employees ? parseInt(employees) : null,
        type: type || null
    };
    
    if (addrType === 'none') {
        data.officialAddressId = null;
        data.zipCode = null;
    } else if (addrType === 'existing') {
        data.officialAddressId = addressTomSelect ? parseInt(addressTomSelect.getValue()) : null;
        data.zipCode = null;
    } else if (addrType === 'edit') {
        data.officialAddressId = parseInt(getElementValue('editAddressId')) || null;
        data.zipCode = getElementValue('editZipCode').trim() || null;
    } else {
        data.officialAddressId = null;
        data.zipCode = getElementValue('zipCode').trim() || null;
    }
    
    await orgCrud.save(data, getElementValue('orgId'));
}

async function deleteOrg(id) {
    deleteState = { id: id, workers: [], tomSelects: [] };
    
    try {
        var relatedIds = await apiCall(API.ORGANIZATIONS_RELATED.fmt({ id: id }));
        
        if (!relatedIds || relatedIds.length === 0) {
            getElement('deleteMessage').textContent = 'Вы уверены, что хотите удалить эту организацию?';
            getElement('replacementSection').style.display = 'none';
            orgDeleteModal.show();
            return;
        }
        
        var workersResp = await apiCall(API.WORKERS_FILTER, {
            method: 'POST',
            body: JSON.stringify([{ field: 'id', operator: 'in', value: relatedIds.join(',') }])
        });
        deleteState.workers = workersResp.content || workersResp;
        
        getElement('deleteMessage').innerHTML = 
            'В организации работают <strong>{count}</strong> сотрудников.<br>Выберите куда перевести каждого:'.fmt({ count: relatedIds.length });
        getElement('replacementSection').style.display = 'block';
        renderWorkersReplacementList();
        orgDeleteModal.show();
    } catch (e) {
        showError('Ошибка: {msg}'.fmt({ msg: e.message }));
    }
}

function formatWorkerInfo(w) {
    var person = w.person ? w.person.name : 'N/A';
    var salary = w.salary ? w.salary.toLocaleString() + ' ₽' : '-';
    return '<strong>{person}</strong> — {pos}<br><small>Зарплата: {sal}, Рейтинг: {rating}</small>'.fmt({
        person: escapeHtml(person),
        pos: EnumCache.getDisplayName('positions', w.position) || 'Без должности',
        sal: salary,
        rating: w.rating || '-'
    });
}

function renderWorkersReplacementList() {
    TomSelectBuilder.destroyAll(deleteState.tomSelects);
    deleteState.tomSelects = [];
    if (deleteState.workers.length === 0) return;
    
    var container = getElement('workersReplacementList');
    container.innerHTML = deleteState.workers.map(function(w) {
        return ('<div class="card mb-2"><div class="card-body py-2"><div class="row align-items-center">' +
            '<div class="col-md-5">{info}<br><small class="text-muted">ID: {id}</small></div>' +
            '<div class="col-md-7"><label class="form-label mb-1">Перевести в:</label>' +
            '<select id="orgSelect_{id}" data-worker-id="{id}"></select></div></div></div></div>').fmt({ id: w.id, info: formatWorkerInfo(w) });
    }).join('');
    
    deleteState.workers.forEach(function(w) {
        var el = getElement('orgSelect_' + w.id);
        if (!el) return;
        
        var ts = TomSelectBuilder.create(el, {
            searchEndpoint: API.ORGANIZATIONS_FILTER,
            formatOption: ENTITY_CONFIGS.organizations.formatOption,
            placeholder: 'Поиск организации...',
            allowEmpty: false,
            preload: 'focus',
            buildFilter: function(query) {
                return [
                    { field: 'fullName,type', operator: 'contains_any', value: query || '' },
                    { field: 'id', operator: 'not_equals', value: deleteState.id }
                ];
            }
        });
        deleteState.tomSelects.push(ts);
    });
}

function buildReplacement(ts) {
    return {
        workerId: parseInt(ts.input.dataset.workerId),
        newOrganizationId: parseInt(ts.getValue())
    };
}

async function confirmDelete() {
    if (deleteState.workers.length > 0) {
        var hasEmpty = deleteState.tomSelects.some(function(ts) { return !ts.getValue(); });
        if (hasEmpty) { showError('Выберите организацию для всех сотрудников'); return; }
    }
    
    try {
        if (typeof wsIgnore === 'function') wsIgnore(5000);
        
        var replacements = deleteState.workers.length > 0 
            ? deleteState.tomSelects.map(buildReplacement) 
            : null;
        
        await apiCall(API.ORGANIZATIONS_BY_ID.fmt({ id: deleteState.id }), {
            method: 'DELETE',
            body: replacements ? JSON.stringify({ replacements: replacements }) : null
        });
        orgDeleteModal.hide();
        loadOrganizations();
        showSuccess('Организация удалена');
    } catch (e) {
        showError('Ошибка: {msg}'.fmt({ msg: e.message }));
    }
}

function showIndexModal(id) {
    setElementValue('indexOrgId', id);
    setElementValue('coefficient', '1.1');
    getElement('indexResult').innerHTML = '';
    indexModal.show();
}

async function executeIndexSalaries() {
    var orgId = getElementValue('indexOrgId');
    var coef = parseFloat(getElementValue('coefficient'));
    
    if (!coef || coef <= 1) { showWarning('Коэффициент должен быть больше 1'); return; }
    
    try {
        await apiCall(API.SALARY_INDEX.fmt({ id: orgId }), { 
            method: 'PUT', 
            body: JSON.stringify({ coefficient: coef }) 
        });
        showSuccess('Зарплаты проиндексированы');
        indexModal.hide();
        loadOrganizations();
    } catch (e) {
        showError('Ошибка: {msg}'.fmt({ msg: e.message }));
    }
}
