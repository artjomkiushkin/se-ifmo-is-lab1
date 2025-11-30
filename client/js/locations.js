var API = {
    LOCATIONS: '/locations',
    LOCATIONS_BY_ID: '/locations/{id}',
    LOCATIONS_RELATED: '/locations/{id}/related',
    LOCATIONS_FILTER: '/locations/filter',
    PERSONS: '/persons',
    PERSONS_FILTER: '/persons/filter',
    PERSONS_BATCH: '/persons/batch'
};

var ENTITY_CONFIGS = {
    locations: {
        endpoint: API.LOCATIONS_FILTER,
        formatOption: function(l) {
            return { value: l.id, text: l.name ? '{name} ({x}, {y}, {z})'.fmt(l) : 'ID:{id} ({x}, {y}, {z})'.fmt(l), data: l };
        },
        placeholder: 'Поиск локации...',
        allowEmpty: true,
        preload: 'focus',
        buildFilter: function(query) {
            return [{ field: 'name,x,y,z', operator: 'contains_any', value: query }];
        }
    }
};

var locationConfig = {
    endpoint: API.LOCATIONS,
    entityName: 'Локация',
    entityNamePlural: 'локаций',
    gender: 'f',
    constants: { DEFAULT_PAGE_SIZE: 10, MIN_PAGE_SIZE: 1, MAX_PAGE_SIZE: 1000 },
    elementIds: {
        TABLE_BODY: 'locationsTableBody',
        PAGINATION: 'locationsPagination',
        PAGE_SIZE_INPUT: 'pageSizeInput'
    },
    sortPrefix: 'sort-location-',
    sortFunction: 'addLocationSort',
    applyFiltersFunction: 'applyLocationFilters',
    clearFiltersFunction: 'clearLocationFilters',
    clearSortFunction: 'clearLocationSort',
    columns: [
        { field: 'id', label: 'ID', type: 'number', sortable: true, filterable: true },
        { field: 'x', label: 'X', type: 'number', sortable: true, filterable: true },
        { field: 'y', label: 'Y', type: 'number', sortable: true, filterable: true },
        { field: 'z', label: 'Z', type: 'number', sortable: true, filterable: true },
        { field: 'name', label: 'Название', type: 'text', sortable: true, filterable: true, nullable: true, nullLabel: 'Пустое' }
    ],
    actions: function(item) {
        return ('<button class="btn btn-sm btn-info compact-btn" onclick="viewLocation({id})" title="Просмотр"><i class="bi bi-eye"></i></button>' +
                '<button class="btn btn-sm btn-primary compact-btn" onclick="editLocation({id})" title="Изменить"><i class="bi bi-pencil"></i></button>' +
                '<button class="btn btn-sm btn-danger compact-btn" onclick="deleteLocation({id})" title="Удалить"><i class="bi bi-trash"></i></button>').fmt(item);
    }
};

var locationViewFields = [
    { field: 'id', label: 'ID' },
    { field: 'x', label: 'Координата X' },
    { field: 'y', label: 'Координата Y' },
    { field: 'z', label: 'Координата Z' },
    { field: 'name', label: 'Название' }
];

async function viewLocation(id) {
    try {
        var data = await locationCrud.loadOne(id);
        ViewModalBuilder.show('locations', id, data, {
            title: 'Просмотр локации #' + id,
            fields: locationViewFields,
            onEdit: editLocation,
            loadOne: function() { viewLocation(id); }
        });
    } catch (e) {
        showError('Ошибка загрузки: ' + e.message);
    }
}

var locationTable, locationCrud, locationModal, locationDeleteModal;
var deleteState = { id: null, persons: [], tomSelects: [] };

document.addEventListener('DOMContentLoaded', function() {
    checkAuth();
    initLocations();
});

function initLocations() {
    locationModal = new ModalBuilder('locationModal');
    locationDeleteModal = new ModalBuilder('deleteConfirmModal');
    
    locationTable = new TableBuilder(locationConfig);
    locationTable.renderTableStructure('tableContainer');
    locationTable.initDynamicFilters();
    locationTable.syncPageSizeInput();
    locationTable.config.onLoad = loadLocations;
    
    locationCrud = new CrudManager({
        endpoint: locationConfig.endpoint,
        entityName: locationConfig.entityName,
        entityNamePlural: locationConfig.entityNamePlural,
        gender: locationConfig.gender,
        tableBuilder: locationTable,
        modal: locationModal,
        deleteModal: locationDeleteModal,
        useWebSocket: true
    });
    
    loadLocations();
    connectWebSocket([{ entity: 'locations', onUpdate: loadLocations, tableBuilder: locationTable }]);
}

function loadLocations() { locationCrud.load(); }
function changePage(page) { locationTable.changePage(page); }
function applyPageSize() { locationTable.applyPageSize(); }
function addLocationSort(field) { locationTable.addSort(field); }
function clearLocationSort() { locationTable.clearSort(); }
function applyLocationFilters() { locationTable.applyFilters(); }
function clearLocationFilters() { locationTable.clearFilters(); }

function openCreateModal() {
    getElement('locationForm').reset();
    setElementValue('locationId', '');
    getElement('locationModalLabel').textContent = 'Создать локацию';
    locationModal.show();
}

async function editLocation(id) {
    try {
        var loc = await locationCrud.loadOne(id);
        setElementValue('locationId', loc.id);
        setElementValue('locationX', loc.x);
        setElementValue('locationY', loc.y);
        setElementValue('locationZ', loc.z);
        setElementValue('locationName', loc.name);
        getElement('locationModalLabel').textContent = 'Редактировать локацию';
        locationModal.show();
    } catch (e) {
        showError('Ошибка загрузки: {msg}'.fmt({ msg: e.message }));
    }
}

async function saveLocation() {
    var form = getElement('locationForm');
    if (!form.checkValidity()) { form.reportValidity(); return; }
    
    var name = getElementValue('locationName').trim();
    await locationCrud.save({
        x: parseFloat(getElementValue('locationX')),
        y: parseFloat(getElementValue('locationY')),
        z: parseInt(getElementValue('locationZ')),
        name: name || null
    }, getElementValue('locationId'));
}

async function deleteLocation(id) {
    deleteState = { id: id, persons: [], tomSelects: [] };
    
    try {
        var relatedIds = await apiCall(API.LOCATIONS_RELATED.fmt({ id: id }));
        
        if (!relatedIds || relatedIds.length === 0) {
            getElement('deleteMessage').textContent = 'Вы уверены, что хотите удалить эту локацию?';
            getElement('replacementSection').style.display = 'none';
            locationDeleteModal.show();
            return;
        }
        
        var personsResp = await apiCall(API.PERSONS_FILTER, {
            method: 'POST',
            body: JSON.stringify([{ field: 'id', operator: 'in', value: relatedIds.join(',') }])
        });
        deleteState.persons = personsResp.content || personsResp;
        
        getElement('deleteMessage').innerHTML = 
            'Эта локация используется в <strong>{count}</strong> персонах.<br>Выберите новую локацию для каждой:'.fmt({ count: relatedIds.length });
        getElement('replacementSection').style.display = 'block';
        renderPersonsReplacementList();
        locationDeleteModal.show();
    } catch (e) {
        showError('Ошибка: {msg}'.fmt({ msg: e.message }));
    }
}

function renderPersonsReplacementList() {
    TomSelectBuilder.destroyAll(deleteState.tomSelects);
    deleteState.tomSelects = [];
    if (deleteState.persons.length === 0) return;
    
    var container = getElement('personsReplacementList');
    container.innerHTML = deleteState.persons.map(function(p) {
        return ('<div class="card mb-2"><div class="card-body py-2"><div class="row align-items-center">' +
            '<div class="col-md-4"><strong>{name}</strong><br><small class="text-muted">ID: {id}</small></div>' +
            '<div class="col-md-8"><select id="locSelect_{id}" data-person-id="{id}"></select></div>' +
            '</div></div></div>').fmt({ id: p.id, name: escapeHtml(p.name) });
    }).join('');
    
    deleteState.persons.forEach(function(p) {
        var el = getElement('locSelect_' + p.id);
        if (!el) return;
        
        var ts = TomSelectBuilder.create(el, {
            searchEndpoint: API.LOCATIONS_FILTER,
            formatOption: ENTITY_CONFIGS.locations.formatOption,
            placeholder: 'Поиск локации...',
            allowEmpty: true,
            preload: 'focus',
            buildFilter: function(q) {
                return [
                    { field: 'name,x,y,z', operator: 'contains_any', value: q || '' },
                    { field: 'id', operator: 'not_equals', value: deleteState.id }
                ];
            }
        });
        deleteState.tomSelects.push(ts);
    });
}

function buildReplacement(ts) {
    var personId = parseInt(ts.input.dataset.personId);
    var selected = ts.options[ts.getValue()];
    return {
        personId: personId,
        newLocationId: selected ? selected.data.id : null
    };
}

async function confirmDelete() {
    try {
        if (typeof wsIgnore === 'function') wsIgnore(5000);
        
        var replacements = deleteState.persons.length > 0 
            ? deleteState.tomSelects.map(buildReplacement) 
            : null;
        
        await apiCall(API.LOCATIONS_BY_ID.fmt({ id: deleteState.id }), {
            method: 'DELETE',
            body: replacements ? JSON.stringify({ replacements: replacements }) : null
        });
        locationDeleteModal.hide();
        loadLocations();
        showSuccess('Локация удалена');
    } catch (e) {
        showError('Ошибка: {msg}'.fmt({ msg: e.message }));
    }
}
