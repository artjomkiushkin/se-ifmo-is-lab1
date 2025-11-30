var API = {
    ADDRESSES: '/addresses',
    ADDRESSES_BY_ID: '/addresses/{id}',
    ADDRESSES_RELATED: '/addresses/{id}/related',
    ADDRESSES_FILTER: '/addresses/filter',
    ORGANIZATIONS: '/organizations',
    ORGANIZATIONS_FILTER: '/organizations/filter',
    ORGANIZATIONS_BATCH: '/organizations/batch'
};

var ENTITY_CONFIGS = {
    addresses: {
        endpoint: API.ADDRESSES_FILTER,
        formatOption: function(a) {
            return { value: a.id, text: 'ID:{id} ({zipCode})'.fmt(a), data: a };
        },
        placeholder: 'Поиск адреса...',
        allowEmpty: true,
        preload: 'focus',
        buildFilter: function(query) {
            return [{ field: 'zipCode', operator: 'contains_any', value: query }];
        }
    }
};

var addressConfig = {
    endpoint: API.ADDRESSES,
    entityName: 'Адрес',
    entityNamePlural: 'адресов',
    gender: 'm',
    constants: { DEFAULT_PAGE_SIZE: 10, MIN_PAGE_SIZE: 1, MAX_PAGE_SIZE: 1000 },
    elementIds: {
        TABLE_BODY: 'addressesTableBody',
        PAGINATION: 'addressesPagination',
        PAGE_SIZE_INPUT: 'pageSizeInput'
    },
    sortPrefix: 'sort-address-',
    sortFunction: 'addAddressSort',
    applyFiltersFunction: 'applyAddressFilters',
    clearFiltersFunction: 'clearAddressFilters',
    clearSortFunction: 'clearAddressSort',
    columns: [
        { field: 'id', label: 'ID', type: 'number', sortable: true, filterable: true },
        { field: 'zipCode', label: 'Почтовый индекс', type: 'text', sortable: true, filterable: true }
    ],
    actions: function(item) {
        return ('<button class="btn btn-sm btn-info compact-btn" onclick="viewAddress({id})" title="Просмотр"><i class="bi bi-eye"></i></button>' +
                '<button class="btn btn-sm btn-primary compact-btn" onclick="editAddress({id})" title="Изменить"><i class="bi bi-pencil"></i></button>' +
                '<button class="btn btn-sm btn-danger compact-btn" onclick="deleteAddress({id})" title="Удалить"><i class="bi bi-trash"></i></button>').fmt(item);
    }
};

var addressViewFields = [
    { field: 'id', label: 'ID' },
    { field: 'zipCode', label: 'Почтовый индекс' }
];

async function viewAddress(id) {
    try {
        var data = await addressCrud.loadOne(id);
        ViewModalBuilder.show('addresses', id, data, {
            title: 'Просмотр адреса #' + id,
            fields: addressViewFields,
            onEdit: editAddress,
            loadOne: function() { viewAddress(id); }
        });
    } catch (e) {
        showError('Ошибка загрузки: ' + e.message);
    }
}

var addressTable, addressCrud, addressModal, addressDeleteModal;
var deleteState = { id: null, organizations: [], tomSelects: [] };

document.addEventListener('DOMContentLoaded', function() {
    checkAuth();
    initAddresses();
});

function initAddresses() {
    addressModal = new ModalBuilder('addressModal');
    addressDeleteModal = new ModalBuilder('deleteConfirmModal');
    
    addressTable = new TableBuilder(addressConfig);
    addressTable.renderTableStructure('tableContainer');
    addressTable.initDynamicFilters();
    addressTable.syncPageSizeInput();
    addressTable.config.onLoad = loadAddresses;
    
    addressCrud = new CrudManager({
        endpoint: addressConfig.endpoint,
        entityName: addressConfig.entityName,
        entityNamePlural: addressConfig.entityNamePlural,
        gender: addressConfig.gender,
        tableBuilder: addressTable,
        modal: addressModal,
        deleteModal: addressDeleteModal,
        useWebSocket: true
    });
    
    loadAddresses();
    connectWebSocket([{ entity: 'addresses', onUpdate: loadAddresses, tableBuilder: addressTable }]);
}

function loadAddresses() { addressCrud.load(); }
function changePage(page) { addressTable.changePage(page); }
function applyPageSize() { addressTable.applyPageSize(); }
function addAddressSort(field) { addressTable.addSort(field); }
function clearAddressSort() { addressTable.clearSort(); }
function applyAddressFilters() { addressTable.applyFilters(); }
function clearAddressFilters() { addressTable.clearFilters(); }

function openCreateModal() {
    getElement('addressForm').reset();
    setElementValue('addressId', '');
    getElement('addressModalLabel').textContent = 'Создать адрес';
    addressModal.show();
}

async function editAddress(id) {
    try {
        var addr = await addressCrud.loadOne(id);
        setElementValue('addressId', addr.id);
        setElementValue('addressZipCode', addr.zipCode);
        getElement('addressModalLabel').textContent = 'Редактировать адрес';
        addressModal.show();
    } catch (e) {
        showError('Ошибка загрузки: {msg}'.fmt({ msg: e.message }));
    }
}

async function saveAddress() {
    var form = getElement('addressForm');
    if (!form.checkValidity()) { form.reportValidity(); return; }
    
    await addressCrud.save({ zipCode: getElementValue('addressZipCode') }, getElementValue('addressId'));
}

async function deleteAddress(id) {
    deleteState = { id: id, organizations: [], tomSelects: [] };
    
    try {
        var relatedIds = await apiCall(API.ADDRESSES_RELATED.fmt({ id: id }));
        
        if (!relatedIds || relatedIds.length === 0) {
            getElement('deleteMessage').textContent = 'Вы уверены, что хотите удалить этот адрес?';
            getElement('replacementSection').style.display = 'none';
            addressDeleteModal.show();
            return;
        }
        
        var orgsResp = await apiCall(API.ORGANIZATIONS_FILTER, {
            method: 'POST',
            body: JSON.stringify([{ field: 'id', operator: 'in', value: relatedIds.join(',') }])
        });
        deleteState.organizations = orgsResp.content || orgsResp;
        
        getElement('deleteMessage').innerHTML = 
            'Этот адрес используется в <strong>{count}</strong> организациях.<br>Выберите новый адрес для каждой:'.fmt({ count: relatedIds.length });
        getElement('replacementSection').style.display = 'block';
        renderOrganizationsReplacementList();
        addressDeleteModal.show();
    } catch (e) {
        showError('Ошибка: {msg}'.fmt({ msg: e.message }));
    }
}

function renderOrganizationsReplacementList() {
    TomSelectBuilder.destroyAll(deleteState.tomSelects);
    deleteState.tomSelects = [];
    if (deleteState.organizations.length === 0) return;
    
    var container = getElement('organizationsReplacementList');
    container.innerHTML = deleteState.organizations.map(function(o) {
        return ('<div class="card mb-2"><div class="card-body py-2"><div class="row align-items-center">' +
            '<div class="col-md-4"><strong>{name}</strong><br><small class="text-muted">ID: {id}</small></div>' +
            '<div class="col-md-8"><select id="addrSelect_{id}" data-org-id="{id}"></select></div>' +
            '</div></div></div>').fmt({ id: o.id, name: escapeHtml(o.fullName || o.name || 'Организация') });
    }).join('');
    
    deleteState.organizations.forEach(function(o) {
        var el = getElement('addrSelect_' + o.id);
        if (!el) return;
        
        var ts = TomSelectBuilder.create(el, {
            searchEndpoint: API.ADDRESSES_FILTER,
            formatOption: function(a) { return { value: a.id, text: 'ID:' + a.id + ' (' + a.zipCode + ')' }; },
            placeholder: 'Поиск адреса...',
            allowEmpty: true,
            preload: 'focus',
            buildFilter: function(query) {
                return [
                    { field: 'zipCode', operator: 'contains_any', value: query || '' },
                    { field: 'id', operator: 'not_equals', value: deleteState.id }
                ];
            }
        });
        deleteState.tomSelects.push(ts);
    });
}

function buildReplacement(ts) {
    var orgId = parseInt(ts.input.dataset.orgId);
    var newAddrId = ts.getValue();
    return {
        organizationId: orgId,
        newAddressId: newAddrId ? parseInt(newAddrId) : null
    };
}

async function confirmDelete() {
    try {
        if (typeof wsIgnore === 'function') wsIgnore(5000);
        
        var replacements = deleteState.organizations.length > 0 
            ? deleteState.tomSelects.map(buildReplacement) 
            : null;
        
        await apiCall(API.ADDRESSES_BY_ID.fmt({ id: deleteState.id }), {
            method: 'DELETE',
            body: replacements ? JSON.stringify({ replacements: replacements }) : null
        });
        addressDeleteModal.hide();
        loadAddresses();
        showSuccess('Адрес удален');
    } catch (e) {
        showError('Ошибка: {msg}'.fmt({ msg: e.message }));
    }
}
