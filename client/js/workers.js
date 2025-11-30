var API = {
    WORKERS: '/workers',
    WORKERS_BY_ID: '/workers/{id}',
    ORGANIZATIONS: '/organizations',
    PERSONS: '/persons',
    LOCATIONS: '/locations',
    ADDRESSES: '/addresses',
    UNIQUE_DATES: '/special/workers/unique-start-dates',
    DELETE_BY_DATE: '/special/workers/by-end-date'
};

var workerConfig = {
    endpoint: API.WORKERS,
    entityName: 'Работник',
    entityNamePlural: 'работников',
    gender: 'm',
    constants: { DEFAULT_PAGE_SIZE: 10, MIN_PAGE_SIZE: 1, MAX_PAGE_SIZE: 1000 },
    elementIds: {
        TABLE_BODY: 'workersTableBody',
        PAGINATION: 'pagination',
        PAGE_SIZE_INPUT: 'pageSizeInput'
    },
    sortPrefix: 'sort-',
    sortFunction: 'addSort',
    applyFiltersFunction: 'applyColumnFilters',
    clearFiltersFunction: 'clearColumnFilters',
    clearSortFunction: 'clearSort',
    collapsedGroups: ['person', 'organization'],
    toggleGroupFunction: 'toggleGroup',
    columns: [
        // Основные поля работника (всегда видны)
        { field: 'id', label: 'ID', type: 'number', sortable: true, filterable: true },
        { field: 'coordinates.x', label: 'X', type: 'number', sortable: true, filterable: true,
          format: function(v, item) { return item.coordinates?.x ?? '-'; } },
        { field: 'coordinates.y', label: 'Y', type: 'number', sortable: true, filterable: true,
          format: function(v, item) { return item.coordinates?.y ?? '-'; } },
        { field: 'creationDate', label: 'Создан', type: 'datetime', sortable: true, filterable: true,
          format: function(v) { return formatDateTime(v); } },
        { field: 'position', label: 'Должность', type: 'enum', sortable: true, filterable: true, enumName: 'positions',
          format: function(v) { return EnumCache.getDisplayName('positions', v); } },
        { field: 'salary', label: 'Зарплата', type: 'number', sortable: true, filterable: true,
          format: function(v) { return v ? v.toLocaleString() + ' ₽' : '-'; } },
        { field: 'rating', label: 'Рейтинг', type: 'number', sortable: true, filterable: true },
        { field: 'startDate', label: 'Устройство', type: 'date', sortable: true, filterable: true,
          format: function(v) { return formatDate(v); } },
        { field: 'endDate', label: 'Окончание', type: 'datetime', sortable: true, filterable: true, nullable: true,
          format: function(v) { return v ? formatDateTimeWithTz(v) : '-'; } },
        // Персона: основное поле (под мега-хедером, но всегда видно)
        { field: 'person.name', label: 'ФИО', type: 'text', sortable: true, filterable: true, groupHeader: 'person', groupLabel: 'Персона',
          format: function(v, item) { return item.person?.name ?? '-'; } },
        // Персона: детали (группа person - скрываются)
        { field: 'person.id', label: 'ID', type: 'number', sortable: true, filterable: true, group: 'person', groupLabel: 'Персона',
          format: function(v, item) { return item.person?.id ?? '-'; } },
        { field: 'person.eyeColor', label: 'Глаза', type: 'enum', sortable: true, filterable: true, enumName: 'colors', group: 'person',
          format: function(v, item) { return EnumCache.getDisplayName('colors', item.person?.eyeColor); } },
        { field: 'person.hairColor', label: 'Волосы', type: 'enum', sortable: true, filterable: true, enumName: 'colors', group: 'person',
          format: function(v, item) { return EnumCache.getDisplayName('colors', item.person?.hairColor); } },
        { field: 'person.height', label: 'Рост', type: 'number', sortable: true, filterable: true, group: 'person',
          format: function(v, item) { return item.person?.height ?? '-'; } },
        { field: 'person.nationality', label: 'Нац.', type: 'enum', sortable: true, filterable: true, nullable: true, enumName: 'countries', group: 'person',
          format: function(v, item) { return EnumCache.getDisplayName('countries', item.person?.nationality); } },
        // Локация персоны (тоже в группе person)
        { field: 'person.location.id', label: 'Лок. ID', type: 'number', sortable: false, filterable: false, nullable: true, group: 'person',
          format: function(v, item) { return item.person?.location?.id ?? '-'; } },
        { field: 'person.location.name', label: 'Лок. имя', type: 'text', sortable: false, filterable: false, group: 'person',
          format: function(v, item) { return item.person?.location?.name || '-'; } },
        { field: 'person.location.x', label: 'Лок. X', type: 'number', sortable: false, filterable: false, group: 'person',
          format: function(v, item) { return item.person?.location?.x ?? '-'; } },
        { field: 'person.location.y', label: 'Лок. Y', type: 'number', sortable: false, filterable: false, group: 'person',
          format: function(v, item) { return item.person?.location?.y ?? '-'; } },
        { field: 'person.location.z', label: 'Лок. Z', type: 'number', sortable: false, filterable: false, group: 'person',
          format: function(v, item) { return item.person?.location?.z ?? '-'; } },
        // Организация: основное поле (под мега-хедером, но всегда видно)
        { field: 'organization.fullName', label: 'Название', type: 'text', sortable: true, filterable: true, groupHeader: 'organization', groupLabel: 'Организация',
          format: function(v, item) { return item.organization?.fullName ?? '-'; } },
        // Организация: детали (группа organization - скрываются)
        { field: 'organization.id', label: 'ID', type: 'number', sortable: true, filterable: true, group: 'organization', groupLabel: 'Организация',
          format: function(v, item) { return item.organization?.id ?? '-'; } },
        { field: 'organization.type', label: 'Тип', type: 'enum', sortable: true, filterable: true, nullable: true, enumName: 'organizationTypes', group: 'organization',
          format: function(v, item) { return EnumCache.getDisplayName('organizationTypes', item.organization?.type); } },
        { field: 'organization.annualTurnover', label: 'Оборот', type: 'number', sortable: true, filterable: true, group: 'organization',
          format: function(v, item) { return item.organization?.annualTurnover?.toLocaleString() ?? '-'; } },
        { field: 'organization.employeesCount', label: 'Сотр.', type: 'number', sortable: true, filterable: true, nullable: true, group: 'organization',
          format: function(v, item) { return item.organization?.employeesCount ?? '-'; } },
        { field: 'organization.rating', label: 'Рейтинг', type: 'number', sortable: true, filterable: true, group: 'organization',
          format: function(v, item) { return item.organization?.rating ?? '-'; } },
        // Адрес организации (тоже в группе organization)
        { field: 'organization.officialAddress.id', label: 'Адр. ID', type: 'number', sortable: false, filterable: false, nullable: true, group: 'organization',
          format: function(v, item) { return item.organization?.officialAddress?.id ?? '-'; } },
        { field: 'organization.officialAddress.zipCode', label: 'Индекс', type: 'text', sortable: false, filterable: false, group: 'organization',
          format: function(v, item) { return item.organization?.officialAddress?.zipCode || '-'; } }
    ],
    actions: function(item) {
        return ('<button class="btn btn-sm btn-info compact-btn" onclick="viewWorker({id})" title="Просмотр"><i class="bi bi-eye"></i></button>' +
                '<button class="btn btn-sm btn-primary compact-btn" onclick="editWorker({id})" title="Изменить"><i class="bi bi-pencil"></i></button>' +
                '<button class="btn btn-sm btn-danger compact-btn" onclick="deleteWorker({id})" title="Удалить"><i class="bi bi-trash"></i></button>').fmt(item);
    }
};

var workerTable, workerCrud, workerModal, workerDeleteModal, deleteByEndDateModal, uniqueDatesModal;
var currentDeleteId = null;

async function initWorkers() {
    // console.log("init workers");
    await EnumCache.load();
    EnumCache.populateSelect('position', 'positions', 'Выберите должность');
    EnumCache.populateSelect('newPersonEyeColor', 'colors', 'Глаза *');
    EnumCache.populateSelect('newPersonHairColor', 'colors', 'Волосы *');
    EnumCache.populateSelect('editPersonEyeColor', 'colors', 'Глаза *');
    EnumCache.populateSelect('editPersonHairColor', 'colors', 'Волосы *');

    workerModal = new ModalBuilder('workerModal');
    workerDeleteModal = new ModalBuilder('deleteConfirmModal');
    deleteByEndDateModal = new ModalBuilder('deleteByEndDateModal');
    uniqueDatesModal = new ModalBuilder('uniqueDatesModal');

    workerTable = new TableBuilder(workerConfig);
    workerTable.renderTableStructure('tableContainer');
    workerTable.initDynamicFilters();
    workerTable.syncPageSizeInput();
    workerTable.config.onLoad = loadWorkers;

    workerCrud = new CrudManager({
        endpoint: workerConfig.endpoint,
        entityName: workerConfig.entityName,
        entityNamePlural: workerConfig.entityNamePlural,
        gender: workerConfig.gender,
        tableBuilder: workerTable,
        modal: workerModal,
        deleteModal: workerDeleteModal
    });

    loadWorkers();
    connectWebSocket([{ entity: 'workers', onUpdate: loadWorkers, tableBuilder: workerTable }]);
}

function loadWorkers() { workerCrud.load(); }
function changePage(page) { workerTable.changePage(page); }
function applyPageSize() { workerTable.applyPageSize(); }
function addSort(field) { workerTable.addSort(field); }
function clearSort() { workerTable.clearSort(); }
function applyColumnFilters() { workerTable.applyFilters(); }
function toggleGroup(group) { workerTable.toggleGroup(group); }
function clearColumnFilters() {
    getElement('searchInput').value = '';
    workerTable.clearFilters();
}

function handleSearch(query) {
    if (!query) { workerTable.clearFilters(); return; }
    workerTable.applyFilters();
}

var workerViewFields = [
    { field: 'id', label: 'ID' },
    { field: 'salary', label: 'Зарплата', format: function(v) { return v?.toLocaleString() ?? '-'; } },
    { field: 'rating', label: 'Рейтинг' },
    { field: 'position', label: 'Должность', format: function(v) { return EnumCache.getDisplayName('positions', v); } },
    { field: 'startDate', label: 'Дата устройства' },
    { field: 'endDate', label: 'Дата окончания работы', format: function(v) { return formatDateTimeWithTz(v); } },
    { field: 'creationDate', label: 'Дата создания', format: function(v) { return v ? new Date(v).toLocaleString() : '-'; } },
    { field: 'coordinates.x', label: 'Координата X' },
    { field: 'coordinates.y', label: 'Координата Y' },
    { field: 'person.id', label: 'Персона ID' },
    { field: 'person.name', label: 'Персона имя' },
    { field: 'person.eyeColor', label: 'Цвет глаз', format: function(v) { return EnumCache.getDisplayName('colors', v); } },
    { field: 'person.hairColor', label: 'Цвет волос', format: function(v) { return EnumCache.getDisplayName('colors', v); } },
    { field: 'person.height', label: 'Рост' },
    { field: 'person.nationality', label: 'Национальность', format: function(v) { return EnumCache.getDisplayName('countries', v); } },
    { field: 'person.location.name', label: 'Локация персоны' },
    { field: 'organization.id', label: 'Организация ID' },
    { field: 'organization.fullName', label: 'Организация' },
    { field: 'organization.type', label: 'Тип орг.', format: function(v) { return EnumCache.getDisplayName('organizationTypes', v); } },
    { field: 'organization.annualTurnover', label: 'Оборот', format: function(v) { return v?.toLocaleString() ?? '-'; } },
    { field: 'organization.rating', label: 'Рейтинг орг.' },
    { field: 'organization.officialAddress.zipCode', label: 'Адрес орг.' }
];

async function viewWorker(id) {
    try {
        var data = await workerCrud.loadOne(id);
        ViewModalBuilder.show('workers', id, data, {
            title: 'Просмотр работника #' + id,
            fields: workerViewFields,
            onEdit: editWorker,
            loadOne: function() { viewWorker(id); }
        });
    } catch (e) {
        showError('Ошибка загрузки: ' + e.message);
    }
}

var orgTomSelect = null, personTomSelect = null, newOrgAddrTomSelect = null, newPersonLocTomSelect = null;

function toggleOrgType() {
    var type = document.querySelector('input[name="orgType"]:checked').value;
    getElement('orgExistingBlock').style.display = type === 'existing' ? 'block' : 'none';
    getElement('orgNewBlock').style.display = type === 'new' ? 'block' : 'none';
    initOrgTomSelect();
    if (type === 'new') initNewOrgAddrTomSelect();
}

function toggleNewOrgAddrType() {
    var type = document.querySelector('input[name="newOrgAddrType"]:checked').value;
    getElement('newOrgAddrExistingBlock').style.display = type === 'existing' ? 'block' : 'none';
    getElement('newOrgAddrNewBlock').style.display = type === 'new' ? 'block' : 'none';
    if (type === 'existing') initNewOrgAddrTomSelect();
}

function toggleNewPersonLocType() {
    var type = document.querySelector('input[name="newPersonLocType"]:checked').value;
    getElement('newPersonLocExistingBlock').style.display = type === 'existing' ? 'block' : 'none';
    getElement('newPersonLocNewBlock').style.display = type === 'new' ? 'block' : 'none';
    if (type === 'existing') initNewPersonLocTomSelect();
}

function togglePersonType() {
    var type = document.querySelector('input[name="personType"]:checked').value;
    getElement('personExistingBlock').style.display = type === 'existing' ? 'block' : 'none';
    getElement('personEditBlock').style.display = type === 'edit' ? 'block' : 'none';
    getElement('personNewBlock').style.display = type === 'new' ? 'block' : 'none';
    initPersonTomSelect();
    if (type === 'new') initNewPersonLocTomSelect();
}

function initOrgTomSelect() {
    if (orgTomSelect) return;
    orgTomSelect = TomSelectBuilder.create(getElement('organizationSelect'), {
        searchEndpoint: '/organizations/filter',
        formatOption: function(o) { return { value: o.id, text: o.fullName + ' — ' + (o.type ? EnumCache.getDisplayName('organizationTypes', o.type) : 'без типа'), data: o }; },
        placeholder: 'Поиск организации...',
        allowEmpty: false,
        preload: 'focus',
        buildFilter: function(q) { return [{ field: 'fullName,type', operator: 'contains_any', value: q || '' }]; }
    });
}

function initPersonTomSelect() {
    if (personTomSelect) return;
    personTomSelect = TomSelectBuilder.create(getElement('personSelect'), {
        searchEndpoint: '/persons/filter',
        formatOption: function(p) { return { value: p.id, text: p.name + ' — ' + EnumCache.getDisplayName('colors', p.eyeColor) + '/' + EnumCache.getDisplayName('colors', p.hairColor), data: p }; },
        placeholder: 'Поиск персоны...',
        allowEmpty: false,
        preload: 'focus',
        buildFilter: function(q) { return [{ field: 'name,eyeColor,hairColor', operator: 'contains_any', value: q || '' }]; }
    });
}

function initNewOrgAddrTomSelect() {
    if (newOrgAddrTomSelect) return;
    newOrgAddrTomSelect = TomSelectBuilder.create(getElement('newOrgAddrSelect'), {
        searchEndpoint: '/addresses/filter',
        formatOption: function(a) { return { value: a.id, text: a.zipCode, data: a }; },
        placeholder: 'Поиск адреса...',
        allowEmpty: false,
        preload: 'focus',
        buildFilter: function(q) { return [{ field: 'zipCode', operator: 'contains_any', value: q || '' }]; }
    });
}

function initNewPersonLocTomSelect() {
    if (newPersonLocTomSelect) return;
    newPersonLocTomSelect = TomSelectBuilder.create(getElement('newPersonLocSelect'), {
        searchEndpoint: '/locations/filter',
        formatOption: function(l) { return { value: l.id, text: (l.name || 'ID:' + l.id) + ' (' + l.x + ',' + l.y + ',' + l.z + ')', data: l }; },
        placeholder: 'Поиск локации...',
        allowEmpty: false,
        preload: 'focus',
        buildFilter: function(q) { return [{ field: 'name,x,y,z', operator: 'contains_any', value: q || '' }]; }
    });
}


function openCreateWorkerModal() {
    getElement('workerModalTitle').textContent = 'Создать работника';
    getElement('workerForm').reset();
    setElementValue('workerId', '');
    setElementValue('coordX', 0);
    setElementValue('coordY', 0);
    getElement('orgTypeExisting').checked = true;
    getElement('personTypeExisting').checked = true;
    getElement('newOrgAddrNone').checked = true;
    getElement('newPersonLocNone').checked = true;
    toggleOrgType();
    togglePersonType();
    toggleNewOrgAddrType();
    toggleNewPersonLocType();
    if (orgTomSelect) orgTomSelect.clear();
    if (personTomSelect) personTomSelect.clear();
    if (newOrgAddrTomSelect) newOrgAddrTomSelect.clear();
    if (newPersonLocTomSelect) newPersonLocTomSelect.clear();
    TimezoneUtil.populate('endDateTz');
    workerModal.show();
}

async function editWorker(id) {
    try {
        var w = await workerCrud.loadOne(id);
        // console.log(w);
        
        getElement('workerModalTitle').textContent = 'Редактировать работника';
        setElementValue('workerId', w.id);
        setElementValue('salary', w.salary);
        setElementValue('rating', w.rating);
        setElementValue('position', w.position);
        setElementValue('startDate', w.startDate);
        setElementValue('coordX', w.coordinates.x);
        setElementValue('coordY', w.coordinates.y);
        
        TimezoneUtil.populate('endDateTz');
        if (w.endDate) setElementValue('endDate', new Date(w.endDate).toISOString().slice(0, 16));
        
        getElement('orgTypeExisting').checked = true;
        toggleOrgType();
        if (w.organization?.id && orgTomSelect) {
            orgTomSelect.addOption({ value: w.organization.id, text: w.organization.fullName });
            orgTomSelect.setValue(w.organization.id);
        }
        
        if (w.person) {
            getElement('personTypeEdit').checked = true;
            setElementValue('editPersonId', w.person.id);
            setElementValue('editPersonName', w.person.name);
            setElementValue('editPersonEyeColor', w.person.eyeColor);
            setElementValue('editPersonHairColor', w.person.hairColor);
            setElementValue('editPersonHeight', w.person.height);
            setElementValue('editPersonLocId', w.person.location?.id || '');
            setElementValue('editPersonLocX', w.person.location?.x || '');
            setElementValue('editPersonLocY', w.person.location?.y || '');
            setElementValue('editPersonLocZ', w.person.location?.z || '');
            setElementValue('editPersonLocName', w.person.location?.name || '');
        } else {
            getElement('personTypeExisting').checked = true;
        }
        togglePersonType();
        
        workerModal.show();
    } catch (e) {
        showError('Ошибка загрузки: {msg}'.fmt({ msg: e.message }));
    }
}

async function saveWorker() {
    var form = getElement('workerForm');
    if (!form.checkValidity()) { form.reportValidity(); return; }

    var orgType = document.querySelector('input[name="orgType"]:checked').value;
    var personType = document.querySelector('input[name="personType"]:checked').value;
    var startDate = getElementValue('startDate');
    var endDate = getElementValue('endDate');
    var endDateTz = getElementValue('endDateTz');
    
    if (endDate && startDate && new Date(endDate) < new Date(startDate)) {
        showError('Дата окончания не может быть раньше даты начала');
        return;
    }
    
    try {
        var data = {
            coordinatesX: parseFloat(getElementValue('coordX')),
            coordinatesY: parseFloat(getElementValue('coordY')),
            salary: parseFloat(getElementValue('salary')),
            rating: parseFloat(getElementValue('rating')),
            startDate: startDate,
            position: getElementValue('position'),
            endDate: TimezoneUtil.toISOWithOffset(endDate, endDateTz)
        };
        
        if (orgType === 'existing') {
            var orgId = orgTomSelect ? parseInt(orgTomSelect.getValue()) : null;
            if (!orgId) { showError('Выберите организацию'); return; }
            data.organizationId = orgId;
        } else {
            data.newOrganization = buildNewOrganization();
        }
        
        if (personType === 'existing') {
            var personId = personTomSelect ? parseInt(personTomSelect.getValue()) : null;
            if (!personId) { showError('Выберите персону'); return; }
            data.personId = personId;
        } else if (personType === 'edit') {
            data.editPerson = buildEditPerson();
        } else {
            data.newPerson = buildNewPerson();
        }

        await workerCrud.save(data, getElementValue('workerId'));
    } catch (e) {
        showError('Ошибка: {msg}'.fmt({ msg: e.message }));
    }
}

function buildNewOrganization() {
    var addrType = document.querySelector('input[name="newOrgAddrType"]:checked').value;
    var org = {
        fullName: getElementValue('newOrgName').trim(),
        annualTurnover: parseFloat(getElementValue('newOrgTurnover')),
        rating: parseFloat(getElementValue('newOrgRating'))
    };
    
    if (addrType === 'existing') {
        var addrId = newOrgAddrTomSelect ? parseInt(newOrgAddrTomSelect.getValue()) : null;
        if (addrId) org.officialAddressId = addrId;
    } else if (addrType === 'new') {
        var zip = getElementValue('newOrgAddrZip').trim();
        if (zip) org.newAddress = { zipCode: zip };
    }
    
    return org;
}

function buildNewPerson() {
    var locType = document.querySelector('input[name="newPersonLocType"]:checked').value;
    var person = {
        name: getElementValue('newPersonName').trim(),
        eyeColor: getElementValue('newPersonEyeColor'),
        hairColor: getElementValue('newPersonHairColor'),
        height: parseInt(getElementValue('newPersonHeight'))
    };
    
    if (locType === 'existing') {
        var locId = newPersonLocTomSelect ? parseInt(newPersonLocTomSelect.getValue()) : null;
        if (locId) person.locationId = locId;
    } else if (locType === 'new') {
        var x = getElementValue('newPersonLocX');
        var y = getElementValue('newPersonLocY');
        var z = getElementValue('newPersonLocZ');
        if (x && y && z) {
            person.newLocation = {
                x: parseInt(x),
                y: parseFloat(y),
                z: parseInt(z),
                name: getElementValue('newPersonLocName').trim() || null
            };
        }
    }
    
    return person;
}

function buildEditPerson() {
    var person = {
        id: parseInt(getElementValue('editPersonId')),
        name: getElementValue('editPersonName').trim(),
        eyeColor: getElementValue('editPersonEyeColor'),
        hairColor: getElementValue('editPersonHairColor'),
        height: parseInt(getElementValue('editPersonHeight'))
    };
    var locId = getElementValue('editPersonLocId');
    var locZ = getElementValue('editPersonLocZ');
    if (locId && locZ) {
        person.locationId = parseInt(locId);
        person.locationX = parseInt(getElementValue('editPersonLocX') || 0);
        person.locationY = parseFloat(getElementValue('editPersonLocY') || 0);
        person.locationZ = parseInt(locZ);
        person.locationName = getElementValue('editPersonLocName').trim() || null;
    } else if (locZ) {
        person.newLocation = {
            x: parseInt(getElementValue('editPersonLocX') || 0),
            y: parseFloat(getElementValue('editPersonLocY') || 0),
            z: parseInt(locZ),
            name: getElementValue('editPersonLocName').trim() || null
        };
    }
    return person;
}

function deleteWorker(id) {
    currentDeleteId = id;
    getElement('deleteMessage').textContent = 'Вы уверены, что хотите удалить этого работника?';
    workerDeleteModal.show();
}

async function confirmDelete() {
    try {
        if (typeof wsIgnore === 'function') wsIgnore();
        await apiCall(API.WORKERS_BY_ID.fmt({ id: currentDeleteId }), { method: 'DELETE' });
        workerDeleteModal.hide();
        loadWorkers();
        showSuccess('Работник удален');
    } catch (e) {
        showError('Ошибка удаления: {msg}'.fmt({ msg: e.message }));
    }
}

async function showUniqueStartDates() {
    try {
        var dates = await apiCall(API.UNIQUE_DATES);
        getElement('uniqueDatesResult').textContent = dates.map(formatDate).join('\n') || 'Нет данных';
        uniqueDatesModal.show();
    } catch (e) {
        getElement('uniqueDatesResult').textContent = 'Ошибка: {msg}'.fmt({ msg: e.message });
        uniqueDatesModal.show();
    }
}

function showDeleteByEndDateModal() {
    getElement('deleteEndDateResult').innerHTML = '';
    setElementValue('deleteEndDate', '');
    TimezoneUtil.populate('deleteEndDateTz');
    deleteByEndDateModal.show();
}

async function executeDeleteByEndDate() {
    var endDate = getElementValue('deleteEndDate');
    var endDateTz = getElementValue('deleteEndDateTz');
    if (!endDate) {
        getElement('deleteEndDateResult').innerHTML = '<div class="alert alert-warning">Пожалуйста, выберите дату</div>';
        return;
    }
    var isoDate = TimezoneUtil.toISOWithOffset(endDate, endDateTz);

    try {
        await apiCall(API.DELETE_BY_DATE + '?endDate=' + encodeURIComponent(isoDate), { method: 'DELETE' });
        getElement('deleteEndDateResult').innerHTML = '<div class="alert alert-success">Работник успешно удалён</div>';
        setTimeout(function() { deleteByEndDateModal.hide(); loadWorkers(); }, 1500);
    } catch (e) {
        getElement('deleteEndDateResult').innerHTML = '<div class="alert alert-danger">Ошибка: {msg}</div>'.fmt({ msg: e.message });
    }
}
