var API = {
    PERSONS: '/persons',
    PERSONS_BY_ID: '/persons/{id}',
    PERSONS_RELATED: '/persons/{id}/related-workers',
    PERSONS_FILTER: '/persons/filter',
    WORKERS: '/workers',
    WORKERS_FILTER: '/workers/filter',
    WORKERS_BATCH: '/workers/batch',
    LOCATIONS: '/locations',
    LOCATIONS_FILTER: '/locations/filter',
    COUNT_BY_HEIGHT: '/special/workers/count-by-height'
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
            return [{ field: 'name,x,y,z', operator: 'contains_any', value: query || '' }];
        }
    },
    persons: {
        endpoint: API.PERSONS_FILTER,
        formatOption: function(p) {
            var loc = p.location ? '({x}, {y}, {z})'.fmt(p.location) : 'без локации';
            return { 
                value: p.id, 
                text: '{name} — {nat}, рост {h}см, {loc}'.fmt({ name: p.name, nat: EnumCache.getDisplayName('countries', p.nationality) || 'N/A', h: p.height, loc: loc }),
                data: p
            };
        },
        placeholder: 'Поиск персоны...',
        allowEmpty: false,
        preload: 'focus',
        buildFilter: function(query) {
            return [{ field: 'name,nationality', operator: 'contains_any', value: query || '' }];
        }
    }
};

var personConfig = {
    endpoint: API.PERSONS,
    entityName: 'Персона',
    entityNamePlural: 'персон',
    gender: 'f',
    constants: { DEFAULT_PAGE_SIZE: 10, MIN_PAGE_SIZE: 1, MAX_PAGE_SIZE: 1000 },
    elementIds: {
        TABLE_BODY: 'personsTableBody',
        PAGINATION: 'personPagination',
        PAGE_SIZE_INPUT: 'personPageSizeInput'
    },
    sortPrefix: 'sort-person-',
    sortFunction: 'addPersonSort',
    applyFiltersFunction: 'applyPersonFilters',
    clearFiltersFunction: 'clearPersonFilters',
    clearSortFunction: 'clearPersonSort',
    collapsedGroups: ['location'],
    toggleGroupFunction: 'togglePersonGroup',
    columns: [
        // Основные поля персоны (всегда видны)
        { field: 'id', label: 'ID', type: 'number', sortable: true, filterable: true },
        { field: 'name', label: 'Имя', type: 'text', sortable: true, filterable: true },
        { field: 'eyeColor', label: 'Цвет глаз', type: 'enum', sortable: true, filterable: true, enumName: 'colors',
          format: function(v) { return EnumCache.getDisplayName('colors', v); } },
        { field: 'hairColor', label: 'Цвет волос', type: 'enum', sortable: true, filterable: true, enumName: 'colors',
          format: function(v) { return EnumCache.getDisplayName('colors', v); } },
        { field: 'height', label: 'Рост', type: 'number', sortable: true, filterable: true },
        { field: 'nationality', label: 'Национальность', type: 'enum', sortable: true, filterable: true, nullable: true, nullLabel: 'Пустая', enumName: 'countries',
          format: function(v) { return EnumCache.getDisplayName('countries', v); } },
        // Локация: основное поле (под мега-хедером, но всегда видно)
        { field: 'location.name', label: 'Название', type: 'text', sortable: true, filterable: true, nullable: true, nullLabel: 'Нет', groupHeader: 'location', groupLabel: 'Локация',
          format: function(v, item) { return item.location?.name || (item.location ? 'ID:' + item.location.id : '-'); } },
        // Локация: детали (группа location - скрываются)
        { field: 'location.id', label: 'ID', type: 'number', sortable: true, filterable: true, group: 'location', groupLabel: 'Локация',
          format: function(v, item) { return item.location?.id ?? '-'; } },
        { field: 'location.x', label: 'X', type: 'number', sortable: true, filterable: true, group: 'location',
          format: function(v, item) { return item.location?.x ?? '-'; } },
        { field: 'location.y', label: 'Y', type: 'number', sortable: true, filterable: true, group: 'location',
          format: function(v, item) { return item.location?.y ?? '-'; } },
        { field: 'location.z', label: 'Z', type: 'number', sortable: true, filterable: true, group: 'location',
          format: function(v, item) { return item.location?.z ?? '-'; } }
    ],
    actions: function(item) {
        return ('<button class="btn btn-sm btn-info compact-btn" onclick="viewPerson({id})" title="Просмотр"><i class="bi bi-eye"></i></button>' +
                '<button class="btn btn-sm btn-primary compact-btn" onclick="editPerson({id})" title="Изменить"><i class="bi bi-pencil"></i></button>' +
                '<button class="btn btn-sm btn-danger compact-btn" onclick="deletePerson({id})" title="Удалить"><i class="bi bi-trash"></i></button>').fmt(item);
    }
};

var personTable, personCrud, personModal, personDeleteModal;
var deleteState = { id: null, workers: [], tomSelects: [] };
var locState = { select: null };

async function initPersons() {
    await EnumCache.load();
    EnumCache.populateSelect('eyeColor', 'colors', 'Выберите');
    EnumCache.populateSelect('hairColor', 'colors', 'Выберите');
    EnumCache.populateSelect('nationality', 'countries', 'Не выбрано');

    personModal = new ModalBuilder('personModal');
    personDeleteModal = new ModalBuilder('deleteConfirmModal');
    
    personTable = new TableBuilder(personConfig);
    personTable.renderTableStructure('tableContainer');
    personTable.initDynamicFilters();
    personTable.syncPageSizeInput();
    personTable.config.onLoad = loadPersons;
    
    personCrud = new CrudManager({
        endpoint: personConfig.endpoint,
        entityName: personConfig.entityName,
        entityNamePlural: personConfig.entityNamePlural,
        gender: personConfig.gender,
        tableBuilder: personTable,
        modal: personModal,
        deleteModal: personDeleteModal,
        useWebSocket: true
    });
    
    loadPersons();
    connectWebSocket([{ entity: 'persons', onUpdate: loadPersons, tableBuilder: personTable }]);
}

function loadPersons() { personCrud.load(); }
function changePage(page) { personTable.changePage(page); }
function applyPersonPageSize() { personTable.applyPageSize(); }
function addPersonSort(field) { personTable.addSort(field); }
function clearPersonSort() { personTable.clearSort(); }
function applyPersonFilters() { personTable.applyFilters(); }
function clearPersonFilters() { personTable.clearFilters(); }
function togglePersonGroup(group) { personTable.toggleGroup(group); }

var personViewFields = [
    { field: 'id', label: 'ID' },
    { field: 'name', label: 'Имя' },
    { field: 'eyeColor', label: 'Цвет глаз', format: function(v) { return EnumCache.getDisplayName('colors', v); } },
    { field: 'hairColor', label: 'Цвет волос', format: function(v) { return EnumCache.getDisplayName('colors', v); } },
    { field: 'height', label: 'Рост' },
    { field: 'nationality', label: 'Национальность', format: function(v) { return EnumCache.getDisplayName('countries', v); } },
    { field: 'location.id', label: 'Локация ID' },
    { field: 'location.name', label: 'Локация название' },
    { field: 'location.x', label: 'Локация X' },
    { field: 'location.y', label: 'Локация Y' },
    { field: 'location.z', label: 'Локация Z' }
];

async function viewPerson(id) {
    try {
        var data = await personCrud.loadOne(id);
        ViewModalBuilder.show('persons', id, data, {
            title: 'Просмотр персоны #' + id,
            fields: personViewFields,
            onEdit: editPerson,
            loadOne: function() { viewPerson(id); }
        });
    } catch (e) {
        showError('Ошибка загрузки: ' + e.message);
    }
}

function getLocationType() {
    return document.querySelector('input[name="locationType"]:checked').value;
}

function toggleLocationType() {
    var type = getLocationType();
    getElement('existingLocSection').style.display = type === 'existing' ? 'block' : 'none';
    getElement('editLocSection').style.display = type === 'edit' ? 'block' : 'none';
    getElement('newLocSection').style.display = type === 'new' ? 'block' : 'none';
}

function initLocationSelect() {
    if (locState.select) locState.select.destroy();
    
    var el = getElement('existingLocSelect');
    if (!el) return;
    
    locState.select = TomSelectBuilder.create(el, {
        searchEndpoint: API.LOCATIONS_FILTER,
        formatOption: ENTITY_CONFIGS.locations.formatOption,
        placeholder: 'Поиск локации...',
        allowEmpty: true,
        preload: 'focus',
        buildFilter: function(query) {
            return [{ field: 'name,x,y,z', operator: 'contains_any', value: query || '' }];
        }
    });
}

function setLocationFields(loc) {
    setElementValue('editLocId', '');
    setElementValue('editLocX', '');
    setElementValue('editLocY', '');
    setElementValue('editLocZ', '');
    setElementValue('editLocName', '');
    setElementValue('locX', '');
    setElementValue('locY', '');
    setElementValue('locZ', '');
    setElementValue('locName', '');
    
    if (!loc) {
        getElement('locTypeNone').checked = true;
    } else if (loc.id) {
        getElement('locTypeEdit').checked = true;
        setElementValue('editLocId', loc.id);
        setElementValue('editLocX', loc.x);
        setElementValue('editLocY', loc.y);
        setElementValue('editLocZ', loc.z);
        setElementValue('editLocName', loc.name || '');
    } else {
        getElement('locTypeNew').checked = true;
        setElementValue('locX', loc.x);
        setElementValue('locY', loc.y);
        setElementValue('locZ', loc.z);
        setElementValue('locName', loc.name || '');
    }
    toggleLocationType();
}

function getLocationFromForm() {
    var type = getLocationType();
    
    if (type === 'existing' && locState.select) {
        var id = locState.select.getValue();
        if (!id) return null;
        var opt = locState.select.options[id];
        return opt ? opt.data : null;
    }
    
    if (type === 'edit') {
        var id = getElementValue('editLocId');
        var z = getElementValue('editLocZ');
        if (!id || !z) return null;
        return {
            id: parseInt(id),
            x: parseInt(getElementValue('editLocX') || 0),
            y: parseFloat(getElementValue('editLocY') || 0),
            z: parseInt(z),
            name: getElementValue('editLocName')?.trim() || null
        };
    }
    
    if (type === 'new') {
        var z = getElementValue('locZ');
        if (!z) return null;
        return {
            x: parseInt(getElementValue('locX') || 0),
            y: parseFloat(getElementValue('locY') || 0),
            z: parseInt(z),
            name: getElementValue('locName')?.trim() || null
        };
    }
    
    return null;
}

async function openCreateModal() {
    getElement('personForm').reset();
    setElementValue('personId', '');
    setLocationFields(null);
    getElement('personModalTitle').textContent = 'Создать персону';
    initLocationSelect();
    personModal.show();
}

async function editPerson(id) {
    try {
        var person = await personCrud.loadOne(id);
        setElementValue('personId', person.id);
        setElementValue('personName', person.name);
        setElementValue('eyeColor', person.eyeColor);
        setElementValue('hairColor', person.hairColor);
        setElementValue('height', person.height);
        setElementValue('nationality', person.nationality || '');
        
        initLocationSelect();
        setLocationFields(person.location);
        
        getElement('personModalTitle').textContent = 'Редактировать персону';
        personModal.show();
    } catch (e) {
        showError('Ошибка загрузки: {msg}'.fmt({ msg: e.message }));
    }
}

async function savePerson() {
    var form = getElement('personForm');
    if (!form.checkValidity()) { form.reportValidity(); return; }
    
    var loc = getLocationFromForm();
    await personCrud.save({
        name: getElementValue('personName'),
        eyeColor: getElementValue('eyeColor'),
        hairColor: getElementValue('hairColor'),
        height: parseInt(getElementValue('height')),
        nationality: getElementValue('nationality') || null,
        locationId: loc && loc.id ? loc.id : null,
        locationX: loc ? loc.x : null,
        locationY: loc ? loc.y : null,
        locationZ: loc ? loc.z : null,
        locationName: loc ? loc.name : null
    }, getElementValue('personId'));
}

async function deletePerson(id) {
    deleteState = { id: id, workers: [], tomSelects: [] };
    
    try {
        var relatedIds = await apiCall(API.PERSONS_RELATED.fmt({ id: id }));
        
        if (!relatedIds || relatedIds.length === 0) {
            getElement('deleteMessage').textContent = 'Вы уверены, что хотите удалить эту персону?';
            getElement('replacementSection').style.display = 'none';
            personDeleteModal.show();
            return;
        }
        
        var workersResp = await apiCall(API.WORKERS_FILTER, {
            method: 'POST',
            body: JSON.stringify([{ field: 'id', operator: 'in', value: relatedIds.join(',') }])
        });
        deleteState.workers = workersResp.content || workersResp;
        
        var personName = deleteState.workers[0]?.person?.name || 'Персона';
        getElement('deleteMessage').innerHTML = 
            '<strong>{name}</strong> назначена на <strong>{count}</strong> должностях.<br>Выберите кого назначить вместо:'.fmt({ name: escapeHtml(personName), count: relatedIds.length });
        getElement('replacementSection').style.display = 'block';
        renderWorkersReplacementList();
        personDeleteModal.show();
    } catch (e) {
        showError('Ошибка: {msg}'.fmt({ msg: e.message }));
    }
}

function formatWorkerInfo(w) {
    return '<strong>{pos}</strong> в <em>{org}</em><br><small>Зарплата: {sal}</small>'.fmt({
        pos: EnumCache.getDisplayName('positions', w.position) || 'Без должности',
        org: escapeHtml(w.organization?.fullName || 'Без организации'),
        sal: w.salary ? w.salary.toLocaleString() + ' ₽' : '-'
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
            '<div class="col-md-7"><label class="form-label mb-1">Назначить вместо:</label>' +
            '<select id="personSelect_{id}" data-worker-id="{id}"></select></div></div></div></div>').fmt({ id: w.id, info: formatWorkerInfo(w) });
    }).join('');
    
    deleteState.workers.forEach(function(w) {
        var el = getElement('personSelect_' + w.id);
        if (!el) return;
        
        var ts = TomSelectBuilder.create(el, {
            searchEndpoint: API.PERSONS_FILTER,
            formatOption: ENTITY_CONFIGS.persons.formatOption,
            placeholder: 'Поиск персоны...',
            allowEmpty: false,
            preload: 'focus',
            buildFilter: function(query) {
                return [
                    { field: 'name,nationality', operator: 'contains_any', value: query || '' },
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
        newPersonId: parseInt(ts.getValue())
    };
}

async function confirmDelete() {
    if (deleteState.workers.length > 0) {
        var hasEmpty = deleteState.tomSelects.some(function(ts) { return !ts.getValue(); });
        if (hasEmpty) { showError('Выберите замену для всех должностей'); return; }
    }
    
    try {
        if (typeof wsIgnore === 'function') wsIgnore(5000);
        
        var replacements = deleteState.workers.length > 0 
            ? deleteState.tomSelects.map(buildReplacement) 
            : null;
        
        await apiCall(API.PERSONS_BY_ID.fmt({ id: deleteState.id }), {
            method: 'DELETE',
            body: replacements ? JSON.stringify({ replacements: replacements }) : null
        });
        personDeleteModal.hide();
        loadPersons();
        showSuccess('Персона удалена');
    } catch (e) {
        showError('Ошибка: {msg}'.fmt({ msg: e.message }));
    }
}

async function countByHeight() {
    var height = getElementValue('heightCountInput');
    if (!height) { showWarning('Введите рост'); return; }
    try {
        var count = await apiCall(API.COUNT_BY_HEIGHT + '?height=' + height);
        showSuccess('Работников с ростом > ' + height + ': ' + count);
    } catch (e) {
        showError('Ошибка: ' + e.message);
    }
}
