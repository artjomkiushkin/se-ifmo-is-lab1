var API = {
    IMPORT: '/import',
    IMPORT_HISTORY: '/import/history',
    IMPORT_BY_ID: '/import/{id}'
};

var importConfig = {
    endpoint: API.IMPORT_HISTORY,
    entityName: 'Операция импорта',
    entityNamePlural: 'операций импорта',
    gender: 'f',
    constants: { DEFAULT_PAGE_SIZE: 10, MIN_PAGE_SIZE: 1, MAX_PAGE_SIZE: 100 },
    elementIds: {
        TABLE_BODY: 'historyTableBody',
        PAGINATION: 'historyPagination',
        PAGE_SIZE_INPUT: 'historyPageSizeInput'
    },
    sortPrefix: 'sort-import-',
    sortFunction: 'addImportSort',
    applyFiltersFunction: 'applyImportFilters',
    clearFiltersFunction: 'clearImportFilters',
    clearSortFunction: 'clearImportSort',
    columns: [
        { field: 'id', label: 'ID', type: 'number', sortable: true, filterable: true },
        { 
            field: 'status', 
            label: 'Статус', 
            type: 'enum', 
            sortable: true, 
            filterable: true,
            options: ['IN_PROGRESS', 'SUCCESS', 'FAILED'],
            format: formatStatus
        },
        { field: 'username', label: 'Пользователь', type: 'text', sortable: true, filterable: true },
        { field: 'addedCount', label: 'Объектов', type: 'number', sortable: true, filterable: true, format: formatCount },
        { field: 'createdAt', label: 'Дата', type: 'datetime', sortable: true, filterable: true, format: formatDateTime }
    ],
    actions: function(item) {
        return '<button class="btn btn-sm btn-info compact-btn" onclick="viewImportDetail({id})" title="Детали"><i class="bi bi-eye"></i></button>'.fmt(item);
    }
};

var importViewFields = [
    { field: 'id', label: 'ID' },
    { field: 'status', label: 'Статус', format: formatStatus },
    { field: 'username', label: 'Пользователь' },
    { field: 'addedCount', label: 'Добавлено объектов', format: formatCount },
    { field: 'createdAt', label: 'Начало', format: formatDateTime },
    { field: 'finishedAt', label: 'Завершение', format: formatDateTime },
    { field: 'errorMessage', label: 'Ошибка', format: function(v) { return v ? '<span class="text-danger">' + v + '</span>' : '-'; } }
];

var importTable, importCrud;

function initImport() {
    importTable = new TableBuilder(importConfig);
    importTable.renderTableStructure('historyTableContainer');
    importTable.initDynamicFilters();
    importTable.config.onLoad = loadHistory;
    
    importCrud = new CrudManager({
        endpoint: API.IMPORT_HISTORY,
        entityName: importConfig.entityName,
        entityNamePlural: importConfig.entityNamePlural,
        gender: importConfig.gender,
        tableBuilder: importTable
    });
    
    loadHistory();
    setupImportForm();
    connectWebSocket([{ entity: 'imports', onUpdate: loadHistory, tableBuilder: importTable }]);
}

function loadHistory() { importCrud.load(); }
function changePage(page) { importTable.changePage(page); }
function applyPageSize() { importTable.applyPageSize(); }
function addImportSort(field) { importTable.addSort(field); }
function clearImportSort() { importTable.clearSort(); }
function applyImportFilters() { importTable.applyFilters(); }
function clearImportFilters() { importTable.clearFilters(); }

function setupImportForm() {
    var form = getElement('importForm');
    if (form) {
        form.onsubmit = function(e) {
            e.preventDefault();
            doImport();
        };
    }
}

async function doImport() {
    var fileInput = getElement('importFile');
    var btn = getElement('importBtn');
    var result = getElement('importResult');
    
    if (!fileInput || !fileInput.files.length) {
        showWarning('Выберите файл для импорта');
        return;
    }
    
    var formData = new FormData();
    formData.append('file', fileInput.files[0]);
    
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Импорт...';
    result.style.display = 'none';
    
    try {
        var data = await apiUpload(API.IMPORT, formData);
        
        if (data.status === 'SUCCESS') {
            result.className = 'alert alert-success';
            result.innerHTML = '<i class="bi bi-check-circle me-2"></i>Импорт успешно завершён. Добавлено объектов: ' + data.addedCount;
            showSuccess('Импорт успешно завершён');
        } else {
            result.className = 'alert alert-danger';
            result.innerHTML = '<i class="bi bi-x-circle me-2"></i>Ошибка импорта: ' + (data.errorMessage || 'Неизвестная ошибка');
            showError('Ошибка импорта');
        }
        result.style.display = 'block';
        fileInput.value = '';
        loadHistory();
    } catch (e) {
        result.className = 'alert alert-danger';
        result.innerHTML = '<i class="bi bi-x-circle me-2"></i>Ошибка: ' + e.message;
        result.style.display = 'block';
        showError('Ошибка импорта: ' + e.message);
    } finally {
        btn.disabled = false;
        btn.innerHTML = '<i class="bi bi-cloud-upload me-1"></i>Импортировать';
    }
}

async function viewImportDetail(id) {
    try {
        var data = await apiCall(API.IMPORT_BY_ID.fmt({ id: id }));
        ViewModalBuilder.show('import', id, data, {
            title: 'Детали импорта #' + id,
            fields: importViewFields,
            loadOne: function() { viewImportDetail(id); }
        });
    } catch (e) {
        showError('Ошибка загрузки: ' + e.message);
    }
}

function formatStatus(val) {
    if (!val) return '-';
    var map = {
        'SUCCESS': { cls: 'text-success', icon: 'check-circle', text: 'Успешно' },
        'FAILED': { cls: 'text-danger', icon: 'x-circle', text: 'Ошибка' },
        'IN_PROGRESS': { cls: 'text-warning', icon: 'hourglass-split', text: 'В процессе' }
    };
    var s = map[val] || { cls: '', icon: 'question', text: val };
    return '<span class="' + s.cls + '"><i class="bi bi-' + s.icon + ' me-1"></i>' + s.text + '</span>';
}

function formatCount(val) {
    return val != null ? val : '-';
}

function formatDateTime(val) {
    if (!val) return '-';
    return new Date(val).toLocaleString('ru-RU');
}

function downloadExample() {
    var example = {
        items: [
            {
                type: "worker",
                coordinatesX: 1.5,
                coordinatesY: 2.5,
                salary: 50000,
                rating: 4.5,
                startDate: "2024-01-15",
                position: "MANAGER",
                newOrganization: { fullName: "Новая организация", annualTurnover: 500000, rating: 3.5 },
                newPerson: { name: "Иван Иванов", eyeColor: "BLUE", hairColor: "WHITE", height: 180 }
            },
            { type: "organization", fullName: "Отдельная организация", annualTurnover: 1000000, rating: 4.0 },
            { type: "person", name: "Артём Артёмов", eyeColor: "RED", hairColor: "WHITE", height: 175 }
        ]
    };
    
    var blob = new Blob([JSON.stringify(example, null, 2)], { type: 'application/json' });
    var url = URL.createObjectURL(blob);
    var a = document.createElement('a');
    a.href = url;
    a.download = 'import-example.json';
    a.click();
    URL.revokeObjectURL(url);
}
