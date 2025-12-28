let nodes = [];
let connections = [];
let nodeIdCounter = 0;
let currentLang = 'en';

const translations = {
    en: {
        "export_json": "Export JSON",
        "export_parsed": "Gen Parsed",
        "reset_view": "Reset View",
        "hint": "Right click to add. Drag ports to connect.",
        "categories": {
            "Events": "Events",
            "Function": "Function",
            "Variables": "Variables",
            "Math": "Math",
            "Logic": "Logic",
            "Appearance": "Appearance"
        },
        "nodes": {
            "on_called": { title: "On Called", label: "On Called" },
            "on_mgrun": { title: "On MGRUN", label: "On MGRUN" },
            "print_string": { title: "Print String", label: "Print String" },
            "print_chat": { title: "Print to Chat", label: "Print to Chat" },
            "get_arg": { title: "Get Parameter", label: "Get Parameter" },
            "player_health": { title: "Player Health", label: "Player Health" },
            "add_float": { title: "Add", label: "Add (Float)" },
            "branch": { title: "Branch", label: "Branch" },
            "make_color": { title: "Make Color", label: "Make Color" }
        },
        "pins": {
            "exec": "Exec",
            "in_string": "In String",
            "message": "Message",
            "index": "Index",
            "name": "Name",
            "value": "Value",
            "a": "A",
            "b": "B",
            "result": "Result",
            "condition": "Condition",
            "true": "True",
            "false": "False",
            "op": "Op",
            "base": "Base",
            "color": "Color"
        },
        "context": {
            "delete_node": "Delete Node",
            "break_links": "Break Links"
        }
    },
    zh: {
        "export_json": "导出 JSON",
        "export_parsed": "生成解析",
        "reset_view": "重置视角",
        "hint": "右键添加节点。拖拽端口连接。",
        "categories": {
            "Events": "事件",
            "Function": "函数",
            "Variables": "变量",
            "Math": "数学",
            "Logic": "逻辑",
            "Appearance": "外观"
        },
        "nodes": {
            "on_called": { title: "事件 被调用时", label: "被调用时" },
            "on_mgrun": { title: "事件 当运行MGRUN时", label: "当运行MGRUN时" },
            "print_string": { title: "打印字符串", label: "打印字符串" },
            "print_chat": { title: "输出到聊天", label: "输出到聊天" },
            "get_arg": { title: "获取参数", label: "获取参数" },
            "player_health": { title: "玩家生命值", label: "玩家生命值" },
            "add_float": { title: "加法", label: "加法 (浮点)" },
            "branch": { title: "分支", label: "分支" },
            "make_color": { title: "创建颜色", label: "创建颜色" }
        },
        "pins": {
            "exec": "执行",
            "in_string": "输入字符串",
            "message": "内容",
            "index": "索引",
            "name": "名称",
            "value": "值",
            "a": "A",
            "b": "B",
            "result": "结果",
            "condition": "条件",
            "true": "真",
            "false": "假",
            "op": "操作符",
            "base": "基色",
            "color": "颜色"
        },
        "context": {
            "delete_node": "删除节点",
            "break_links": "断开连接"
        }
    }
};

function t(key, type = null, id = null) {
    const lang = translations[currentLang];
    if (type === 'category') return lang.categories[key] || key;
    if (type === 'node_title' && id) return lang.nodes[id]?.title || key;
    if (type === 'node_label' && id) return lang.nodes[id]?.label || key;
    if (type === 'pin') return lang.pins[key] || key;
    if (type === 'context') return lang.context[key] || key;
    return lang[key] || key;
}

const canvas = document.getElementById('canvas');
const world = document.getElementById('world');
const svgLayer = document.getElementById('svg-layer');
const contextMenu = document.getElementById('context-menu');
const nodeContextMenu = document.getElementById('node-context-menu');

// Minimap refs
const minimapEl = document.getElementById('minimap');
const miniCanvas = document.getElementById('mini-canvas');
const miniCtx = miniCanvas.getContext('2d');
const miniViewport = document.getElementById('mini-viewport');

let activeNodeId = null;

// Viewport State
let panX = 0;
let panY = 0;
let zoomLevel = 1.0;
const MIN_ZOOM = 0.1;
const MAX_ZOOM = 3.0;

let isPanning = false;
let panStartX = 0;
let panStartY = 0;
let initialPanX = 0;
let initialPanY = 0;

// Minimap Dragging
let isDraggingMinimap = false;

// Update World Transform & Culling
function updateTransform() {
    world.style.transform = `translate(${panX}px, ${panY}px) scale(${zoomLevel})`;
    document.body.style.backgroundPosition = `${panX}px ${panY}px`;
    document.body.style.backgroundSize = `${20 * zoomLevel}px ${20 * zoomLevel}px`;
    
    updateVisibleNodes(); 
    requestAnimationFrame(renderMinimap);
}

function updateVisibleNodes() {
    const margin = 200; 
    const vMinX = (-panX - margin) / zoomLevel;
    const vMinY = (-panY - margin) / zoomLevel;
    const vMaxX = (-panX + window.innerWidth + margin) / zoomLevel;
    const vMaxY = (-panY + window.innerHeight + margin) / zoomLevel;
    
    const isLowLOD = zoomLevel <= 0.25;
    svgLayer.style.display = isLowLOD ? 'none' : 'block';

    for (let i = 0; i < nodes.length; i++) {
        const n = nodes[i];
        const nodeRight = n.x + 200;
        const nodeBottom = n.y + 150;
        
        const isVisible = (nodeRight > vMinX && n.x < vMaxX && nodeBottom > vMinY && n.y < vMaxY);
        
        if (isVisible) {
            mountNode(n, isLowLOD);
        } else {
            unmountNode(n);
        }
    }
    
    if (!isLowLOD) {
        updateConnections(); 
    }
}

// Node Definitions
const nodeDefinitions = [
    {
        id: 'on_mgrun',
        category: 'Events',
        label: 'On MGRUN',
        title: 'On MGRUN',
        color: '#800',
        inputs: [],
        outputs: [
            {id: 'exec', label: 'Exec', type: 'exec'},
            {id: 'name', label: 'Name', type: 'string'}
        ]
    },
    {
        id: 'print_chat',
        category: 'Function',
        label: 'Print to Chat',
        title: 'Print to Chat',
        color: '#48f',
        inputs: [
            {id: 'exec', label: 'Exec', type: 'exec'},
            {id: 'message', label: 'Message', type: 'string', hasInput: true, defaultValue: 'Hello Chat'}
        ],
        outputs: [{id: 'exec', label: 'Exec', type: 'exec'}]
    },
    {
        id: 'get_arg',
        category: 'Function',
        label: 'Get Parameter',
        title: 'Get Parameter',
        color: '#4d4',
        inputs: [
            {id: 'index', label: 'Index', type: 'number', hasInput: true, defaultValue: 0}
        ],
        outputs: [{id: 'value', label: 'Value', type: 'string'}]
    },
    {
        id: 'branch',
        category: 'Logic',
        label: 'Branch',
        title: 'Branch',
        color: '#888',
        inputs: [{id: 'exec', label: 'Exec', type: 'exec'}, {id: 'condition', label: 'Condition', type: 'boolean', hasInput: true, defaultValue: true}],
        outputs: [{id: 'true', label: 'True', type: 'exec'}, {id: 'false', label: 'False', type: 'exec'}]
    }
];

function buildContextMenu() {
    contextMenu.innerHTML = '';
    const categories = {};
    
    nodeDefinitions.forEach(def => {
        if (!categories[def.category]) categories[def.category] = [];
        categories[def.category].push(def);
    });

    for (const [cat, items] of Object.entries(categories)) {
        const catItem = document.createElement('div');
        catItem.className = 'menu-item menu-category';
        catItem.textContent = t(cat, 'category');
        
        const submenu = document.createElement('div');
        submenu.className = 'submenu';
        
        items.forEach(def => {
            const item = document.createElement('div');
            item.className = 'menu-item';
            item.textContent = t(def.label, 'node_label', def.id);
            item.onclick = (e) => {
                e.stopPropagation();
                addNode(def.id);
                contextMenu.style.display = 'none';
            };
            submenu.appendChild(item);
        });
        
        catItem.appendChild(submenu);
        contextMenu.appendChild(catItem);
    }
}

buildContextMenu();

let isDraggingNode = false;
let dragNode = null;
let dragOffsetX = 0;
let dragOffsetY = 0;

let isDraggingWire = false;
let dragStartSocket = null;
let activeWire = null; 

let createX = 0, createY = 0;

document.addEventListener('contextmenu', (e) => {
    e.preventDefault();
    contextMenu.style.display = 'none';
    nodeContextMenu.style.display = 'none';
    
    const node = e.target.closest('.node');
    
    if (node) {
        activeNodeId = node.id;
        const deleteBtn = nodeContextMenu.querySelector('div:nth-child(1)');
        const breakBtn = nodeContextMenu.querySelector('div:nth-child(2)');
        if (deleteBtn) deleteBtn.textContent = t('delete_node', 'context');
        if (breakBtn) breakBtn.textContent = t('break_links', 'context');

        nodeContextMenu.style.display = 'flex';
        nodeContextMenu.style.left = e.clientX + 'px';
        nodeContextMenu.style.top = e.clientY + 'px';
    } else {
        createX = e.clientX;
        createY = e.clientY;
        contextMenu.style.display = 'flex';
        contextMenu.style.left = e.clientX + 'px';
        contextMenu.style.top = e.clientY + 'px';
    }
});

document.addEventListener('click', (e) => {
    contextMenu.style.display = 'none';
    nodeContextMenu.style.display = 'none';
});

function deleteActiveNode() {
    if (activeNodeId) {
        removeNode(activeNodeId);
        activeNodeId = null;
    }
}

function breakActiveNodeLinks() {
    if (activeNodeId) {
        const nodeConnections = connections.filter(c => c.fromNode === activeNodeId || c.toNode === activeNodeId);
        nodeConnections.forEach(c => deleteConnection(c));
    }
}

function addNode(defId, x = null, y = null) {
    if (nodes.find(n => n.defId === defId)) {
        return;
    }

    const def = nodeDefinitions.find(d => d.id === defId);
    if (!def) return;

    let posX, posY;
    
    if (x !== null) {
        posX = x;
        posY = y;
    } else {
        posX = (createX - panX) / zoomLevel;
        posY = (createY - panY) / zoomLevel;
    }

    const id = 'node_' + (++nodeIdCounter);
    const nodeData = { id, defId, x: posX, y: posY, inputs: def.inputs, outputs: def.outputs, inputValues: {} };
    nodes.push(nodeData);
    mountNode(nodeData);
    renderMinimap();
}

function mountNode(n, isLowLOD) {
    let nodeEl = document.getElementById(n.id);
    
    if (!nodeEl) {
        nodeEl = document.createElement('div');
        nodeEl.className = 'node';
        nodeEl.id = n.id;
        nodeEl.dataset.defId = n.defId;
        nodeEl.style.left = n.x + 'px';
        nodeEl.style.top = n.y + 'px';

        const def = nodeDefinitions.find(d => d.id === n.defId);
        if (def) {
            renderNodeContent(nodeEl, def, n.id, n);
        }

        world.appendChild(nodeEl);

        const header = nodeEl.querySelector('.header');
        header.addEventListener('mousedown', (e) => {
            if (e.button !== 0) return;
            isDraggingNode = true;
            dragNode = nodeEl;
            const rect = nodeEl.getBoundingClientRect();
            dragOffsetX = e.clientX - rect.left;
            dragOffsetY = e.clientY - rect.top;
            e.stopPropagation();
            nodeEl.style.zIndex = 100;
        });
    }
    
    if (isLowLOD) {
        nodeEl.classList.add('lod-simple');
    } else {
        nodeEl.classList.remove('lod-simple');
    }
}

function unmountNode(n) {
    const el = document.getElementById(n.id);
    if (el) el.remove();
}

function renderNodeContent(nodeEl, def, id, nodeData = {}) {
    let inputHtml = '';
    def.inputs.forEach(i => {
        const iName = t(i.id, 'pin');
        let inputField = '';
        if (i.hasInput) {
            const commonClass = "inline-input";
            const stopProp = 'onmousedown="event.stopPropagation()"';
            const savedVal = nodeData.inputValues && nodeData.inputValues[i.id];
            const val = savedVal !== undefined ? savedVal : i.defaultValue;
            const changeHandler = `oninput="updateNodeValue('${id}', '${i.id}', this.type === 'checkbox' ? this.checked : this.value)"`;

            if (i.inputType === 'select' && i.options) {
                const opts = i.options.map(o => `<option value="${o}" ${o === val ? 'selected' : ''}>${o}</option>`).join('');
                inputField = `<select class="${commonClass}" ${stopProp} ${changeHandler}>${opts}</select>`;
            } else if (i.inputType === 'color') {
                inputField = `<input type="color" class="${commonClass}" value="${val || '#000000'}" ${stopProp} ${changeHandler}>`;
            } else if (i.type === 'boolean') {
                inputField = `<input type="checkbox" class="${commonClass}" ${val ? 'checked' : ''} ${stopProp} ${changeHandler}>`;
            } else {
                inputField = `<input type="${i.type === 'float' ? 'number' : 'text'}" value="${val !== undefined ? val : ''}" class="${commonClass}" ${stopProp} ${changeHandler}>`;
            }
        }

        inputHtml += `
            <div class="socket-row">
                <div class="socket input" 
                     data-node="${id}" 
                     data-socket="${i.id}" 
                     data-type="input"
                     data-prop="${i.type}"
                     title="${i.type}"></div>
                <span>${iName}</span>
                ${inputField}
            </div>`;
    });

    let outputHtml = '';
    def.outputs.forEach(o => {
        const oName = t(o.id, 'pin');
        outputHtml += `
            <div class="socket-row">
                <span>${oName}</span>
                <div class="socket output" 
                     data-node="${id}" 
                     data-socket="${o.id}" 
                     data-type="output"
                     data-prop="${o.type}"
                     title="${o.type}"></div>
            </div>`;
    });

    const displayTitle = t(def.title, 'node_title', def.id);

    nodeEl.innerHTML = `
        <div class="header" style="background-color: ${def.color};">
            <span>${displayTitle}</span>
        </div>
        <div class="content">
            <div class="inputs">${inputHtml}</div>
            <div class="outputs">${outputHtml}</div>
        </div>
    `;
    
    nodeEl.querySelectorAll('.socket.input').forEach(socket => {
        updateSocketInputState(socket);
    });
}

window.updateNodeValue = function(nodeId, inputId, value) {
    const n = nodes.find(node => node.id === nodeId);
    if (n) {
        if (!n.inputValues) n.inputValues = {};
        n.inputValues[inputId] = value;
    }
};

function setLanguage(lang) {
    currentLang = lang;
    document.getElementById('btn-export').textContent = t('export_json');
    document.getElementById('btn-export-parsed').textContent = t('export_parsed');
    document.getElementById('btn-reset').textContent = t('reset_view');
    document.getElementById('txt-hint').textContent = t('hint');
    
    buildContextMenu();
    
    nodes.forEach(n => {
        const el = document.getElementById(n.id);
        if (el) {
            const def = nodeDefinitions.find(d => d.id === n.defId);
            if (def) renderNodeContent(el, def, n.id, n);
        }
    });
}

function removeNode(id) {
    unmountNode({id}); 
    nodes = nodes.filter(n => n.id !== id);
    const nodeConnections = connections.filter(c => c.fromNode === id || c.toNode === id);
    nodeConnections.forEach(c => deleteConnection(c));
    renderMinimap();
}

function updateSocketInputState(socketEl) {
    if (!socketEl || socketEl.dataset.type !== 'input') return;
    const row = socketEl.parentNode;
    const input = row.querySelector('.inline-input');
    if (!input) return;
    const isConnected = connections.some(c => 
        c.toNode === socketEl.dataset.node && 
        c.toSocket === socketEl.dataset.socket
    );
    input.disabled = isConnected;
}

function deleteConnection(conn) {
    conn.path.remove();
    connections = connections.filter(c => c !== conn);
    const targetNode = document.getElementById(conn.toNode);
    if (targetNode) {
        const targetSocket = targetNode.querySelector(`.socket[data-socket="${conn.toSocket}"][data-type="input"]`);
        if (targetSocket) updateSocketInputState(targetSocket);
    }
}

document.addEventListener('wheel', (e) => {
    e.preventDefault();
    const zoomSensitivity = 0.001;
    const delta = -e.deltaY * zoomSensitivity;
    const oldZoom = zoomLevel;
    let newZoom = oldZoom + delta;
    newZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, newZoom));
    if (newZoom === oldZoom) return;
    const mouseX = e.clientX;
    const mouseY = e.clientY;
    const worldMouseX = (mouseX - panX) / oldZoom;
    const worldMouseY = (mouseY - panY) / oldZoom;
    panX = mouseX - worldMouseX * newZoom;
    panY = mouseY - worldMouseY * newZoom;
    zoomLevel = newZoom;
    updateTransform();
}, { passive: false });

document.addEventListener('mousedown', (e) => {
    if (e.button === 2) return;
    if (e.target.closest('#menu-bar') || 
        e.target.closest('.inline-input') || 
        e.target.closest('#minimap') || 
        e.target.closest('#context-menu') || 
        e.target.closest('#node-context-menu') ||
        e.target.closest('textarea')) {
        return;
    }
    if (e.target.closest('.header')) return;
    if (e.target.classList.contains('socket')) {
        isDraggingWire = true;
        dragStartSocket = e.target;
        activeWire = document.createElementNS('http://www.w3.org/2000/svg', 'path');
        activeWire.setAttribute('class', 'cable');
        activeWire.style.stroke = getComputedStyle(dragStartSocket).borderColor; 
        activeWire.style.pointerEvents = 'none'; 
        svgLayer.appendChild(activeWire);
        e.stopPropagation();
        e.preventDefault();
        return;
    }
    if (e.button === 0 || e.button === 1) { 
        isPanning = true;
        panStartX = e.clientX;
        panStartY = e.clientY;
        initialPanX = panX;
        initialPanY = panY;
        contextMenu.style.display = 'none';
        nodeContextMenu.style.display = 'none';
        e.preventDefault(); 
    }
});

document.addEventListener('mousemove', (e) => {
    if (isPanning) {
        const dx = e.clientX - panStartX;
        const dy = e.clientY - panStartY;
        panX = initialPanX + dx;
        panY = initialPanY + dy;
        updateTransform();
        return;
    }
    if (isDraggingNode && dragNode) {
        const x = ((e.clientX - dragOffsetX) - panX) / zoomLevel;
        const y = ((e.clientY - dragOffsetY) - panY) / zoomLevel;
        dragNode.style.left = x + 'px';
        dragNode.style.top = y + 'px';
        const nodeData = nodes.find(n => n.id === dragNode.id);
        if (nodeData) { nodeData.x = x; nodeData.y = y; }
        updateConnections();
        requestAnimationFrame(renderMinimap);
    }
    if (isDraggingWire && activeWire && dragStartSocket) {
        const rect = dragStartSocket.getBoundingClientRect();
        const startX = ((rect.left + rect.width / 2) - panX) / zoomLevel;
        const startY = ((rect.top + rect.height / 2) - panY) / zoomLevel;
        const mouseWorldX = (e.clientX - panX) / zoomLevel;
        const mouseWorldY = (e.clientY - panY) / zoomLevel;
        updatePath(activeWire, startX, startY, mouseWorldX, mouseWorldY);
    }
});

document.addEventListener('mouseup', (e) => {
    isPanning = false;
    if (isDraggingNode) {
        if (dragNode) dragNode.style.zIndex = '';
        isDraggingNode = false;
        dragNode = null;
        renderMinimap();
    }
    if (isDraggingWire) {
        if (e.target.classList.contains('socket') && e.target !== dragStartSocket) {
            const startType = dragStartSocket.dataset.type;
            const endType = e.target.dataset.type;
            const startNode = dragStartSocket.dataset.node;
            const endNode = e.target.dataset.node;
            const startProp = dragStartSocket.dataset.prop;
            const endProp = e.target.dataset.prop;
            if (startType !== endType && startNode !== endNode && startProp === endProp) {
                let source = startType === 'output' ? dragStartSocket : e.target;
                let target = startType === 'input' ? dragStartSocket : e.target;
                createConnection(source, target, getComputedStyle(dragStartSocket).borderColor);
            }
        }
        if (activeWire) {
            activeWire.remove();
            activeWire = null;
        }
        isDraggingWire = false;
        dragStartSocket = null;
    }
});

function createConnection(sourceEl, targetEl, color) {
    const fromNode = sourceEl.dataset.node;
    const fromSocket = sourceEl.dataset.socket;
    const toNode = targetEl.dataset.node;
    const toSocket = targetEl.dataset.socket;
    const exists = connections.some(c => 
        c.fromNode === fromNode && 
        c.fromSocket === fromSocket &&
        c.toNode === toNode && 
        c.toSocket === toSocket
    );
    if (exists) return;
    const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
    path.setAttribute('class', 'cable');
    path.style.stroke = color || '#fff'; 
    const conn = { fromNode, fromSocket, toNode, toSocket, path: path };
    path.addEventListener('click', (e) => {
        if (confirm('Delete connection?')) {
            deleteConnection(conn);
        }
    });
    svgLayer.appendChild(path);
    connections.push(conn);
    updateSocketInputState(targetEl);
    updateConnections();
}

function updateConnections() {
    connections.forEach(conn => {
        const sourceNode = document.getElementById(conn.fromNode);
        const targetNode = document.getElementById(conn.toNode);
        if (!sourceNode || !targetNode) {
            conn.path.style.display = 'none';
            return;
        }
        conn.path.style.display = '';
        const sourceEl = sourceNode.querySelector(`.socket[data-socket="${conn.fromSocket}"][data-type="output"]`);
        const targetEl = targetNode.querySelector(`.socket[data-socket="${conn.toSocket}"][data-type="input"]`);
        if (!sourceEl || !targetEl) return;
        const r1 = sourceEl.getBoundingClientRect();
        const r2 = targetEl.getBoundingClientRect();
        const x1 = ((r1.left + r1.width / 2) - panX) / zoomLevel;
        const y1 = ((r1.top + r1.height / 2) - panY) / zoomLevel;
        const x2 = ((r2.left + r2.width / 2) - panX) / zoomLevel;
        const y2 = ((r2.top + r2.height / 2) - panY) / zoomLevel;
        updatePath(conn.path, x1, y1, x2, y2);
    });
}

function updatePath(pathEl, x1, y1, x2, y2) {
    const dist = Math.abs(x2 - x1) * 0.5;
    const cp1x = x1 + dist;
    const cp1y = y1;
    const cp2x = x2 - dist;
    const cp2y = y2;
    const d = `M ${x1} ${y1} C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${x2} ${y2}`;
    pathEl.setAttribute('d', d);
}

function showOutput() {
    const outputData = {
        nodes: nodes.map(n => {
            const def = nodeDefinitions.find(d => d.id === n.defId);
            return {
                id: n.id,
                type: n.defId, 
                title: def ? def.title : '',
                x: n.x,
                y: n.y,
                inputValues: n.inputValues || {}
            };
        }),
        connections: connections.map(c => ({
            from: c.fromNode,
            fromPort: c.fromSocket,
            to: c.toNode,
            toPort: c.toSocket
        }))
    };
    const textArea = document.getElementById('output');
    textArea.value = JSON.stringify(outputData, null, 2);
    textArea.style.display = 'block';
}

function showParsedOutput() {
    const parsedData = getParsedNodes();
    const textArea = document.getElementById('output');
    textArea.value = JSON.stringify(parsedData, null, 2);
    textArea.style.display = 'block';
}

function getParsedNodes() {
    return nodes.map(n => {
        const def = nodeDefinitions.find(d => d.id === n.defId);
        const nodeObj = { id: n.id, type: n.defId, inputs: {}, outputs: {} };
        if (def) {
            def.inputs.forEach(inputDef => {
                const portId = inputDef.id;
                const conn = connections.find(c => c.toNode === n.id && c.toSocket === portId);
                if (conn) {
                    nodeObj.inputs[portId] = { type: 'link', nodeId: conn.fromNode, socket: conn.fromSocket };
                } else {
                    const savedVal = n.inputValues && n.inputValues[portId];
                    const val = savedVal !== undefined ? savedVal : inputDef.defaultValue;
                    nodeObj.inputs[portId] = { type: 'value', value: val };
                }
            });
            def.outputs.forEach(outputDef => {
                const portId = outputDef.id;
                const conns = connections.filter(c => c.fromNode === n.id && c.fromSocket === portId);
                nodeObj.outputs[portId] = conns.map(c => ({ nodeId: c.toNode, socket: c.toSocket }));
            });
        }
        return nodeObj;
    });
}

async function saveToMinecraft() {
    const btn = document.getElementById('btn-save');
    btn.classList.add('loading');
    
    const data = {
        execution: getParsedNodes(),
        ui: {
            nodes: nodes.map(n => ({
                id: n.id,
                defId: n.defId,
                x: n.x,
                y: n.y,
                inputValues: n.inputValues || {}
            })),
            connections: connections.map(c => ({
                fromNode: c.fromNode,
                fromSocket: c.fromSocket,
                toNode: c.toNode,
                toSocket: c.toSocket
            }))
        }
    };

    try {
        const response = await fetch('/api/save', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        const result = await response.json();
        if (result.status === 'ok') {
            showToast('Saved successfully!');
        } else {
            showToast('Save failed: ' + result.message, true);
        }
    } catch (e) {
        showToast('Failed to save. Is the mod running?', true);
    } finally {
        btn.classList.remove('loading');
    }
}

async function loadFromMinecraft() {
    try {
        const response = await fetch('/api/load');
        const data = await response.json();
        
        // Update Info Panel Path
        if (data.metadata && data.metadata.savePath) {
            document.getElementById('info-path').textContent = data.metadata.savePath;
        }

        // Update Info Panel Player
        if (data.metadata && data.metadata.playerName) {
            document.getElementById('info-player').textContent = data.metadata.playerName;
            document.getElementById('info-player').title = `UUID: ${data.metadata.playerUuid}`;
        }

        if (data.ui) {
            nodes.forEach(n => unmountNode(n));
            nodes = [];
            connections.forEach(c => c.path.remove());
            connections = [];
            
            data.ui.nodes.forEach(n => {
                const def = nodeDefinitions.find(d => d.id === n.defId);
                if (!def) return;
                const id = n.id;
                const numId = parseInt(id.replace('node_', ''));
                if (!isNaN(numId)) nodeIdCounter = Math.max(nodeIdCounter, numId);
                const nodeData = { id, defId: n.defId, x: n.x, y: n.y, inputs: def.inputs, outputs: def.outputs, inputValues: n.inputValues || {} };
                nodes.push(nodeData);
                mountNode(nodeData);
            });
            
            data.ui.connections.forEach(c => {
                const sourceNode = document.getElementById(c.fromNode);
                const targetNode = document.getElementById(c.toNode);
                if (sourceNode && targetNode) {
                    const sourceEl = sourceNode.querySelector(`.socket[data-socket="${c.fromSocket}"][data-type="output"]`);
                    const targetEl = targetNode.querySelector(`.socket[data-socket="${c.toSocket}"][data-type="input"]`);
                    if (sourceEl && targetEl) {
                        createConnection(sourceEl, targetEl, getComputedStyle(sourceEl).borderColor);
                    }
                }
            });
            renderMinimap();
            updateTransform();
        }
    } catch (e) {
        console.error('Failed to load blueprint:', e);
    }
}

window.addEventListener('load', loadFromMinecraft);

function renderMinimap() {
    // Update Info Panel
    document.getElementById('info-nodes').textContent = nodes.length;
    document.getElementById('info-conns').textContent = connections.length;

    const w = miniCanvas.width = minimapEl.offsetWidth;
    const h = miniCanvas.height = minimapEl.offsetHeight;
    miniCtx.clearRect(0, 0, w, h);
    if (nodes.length === 0) return;
    let minX = Infinity, minY = Infinity, maxX = -Infinity, maxY = -Infinity;
    nodes.forEach(n => {
        minX = Math.min(minX, n.x);
        minY = Math.min(minY, n.y);
        maxX = Math.max(maxX, n.x + 150);
        maxY = Math.max(maxY, n.y + 100);
    });
    const vpX = -panX / zoomLevel;
    const vpY = -panY / zoomLevel;
    const vpW = window.innerWidth / zoomLevel;
    const vpH = window.innerHeight / zoomLevel;
    minX = Math.min(minX, vpX);
    minY = Math.min(minY, vpY);
    maxX = Math.max(maxX, vpX + vpW);
    maxY = Math.max(maxY, vpY + vpH);
    const padding = 500;
    minX -= padding; minY -= padding;
    maxX += padding; maxY += padding;
    const worldW = maxX - minX;
    const worldH = maxY - minY;
    const scaleX = w / worldW;
    const scaleY = h / worldH;
    const scale = Math.min(scaleX, scaleY);
    const offsetX = (w - worldW * scale) / 2;
    const offsetY = (h - worldH * scale) / 2;
    miniCtx.fillStyle = 'rgba(200, 200, 200, 0.5)';
    nodes.forEach(n => {
        const nx = offsetX + (n.x - minX) * scale;
        const ny = offsetY + (n.y - minY) * scale;
        miniCtx.fillRect(nx, ny, 150 * scale, 100 * scale);
    });
    const vX = offsetX + (vpX - minX) * scale;
    const vY = offsetY + (vpY - minY) * scale;
    const vW = vpW * scale;
    const vH = vpH * scale;
    miniViewport.style.left = vX + 'px';
    miniViewport.style.top = vY + 'px';
    miniViewport.style.width = vW + 'px';
    miniViewport.style.height = vH + 'px';
    minimapState = { scale, minX, minY, offsetX, offsetY };
}

let minimapState = { scale: 0.1, minX: 0, minY: 0, offsetX: 0, offsetY: 0 };

miniViewport.addEventListener('mousedown', (e) => {
    isDraggingMinimap = true;
    e.stopPropagation();
    e.preventDefault();
});

minimapEl.addEventListener('mousedown', (e) => {
    if (e.target !== miniViewport) {
        moveViewportFromMinimap(e.clientX, e.clientY);
        isDraggingMinimap = true;
    }
});

document.addEventListener('mousemove', (e) => {
    if (isDraggingMinimap) {
        moveViewportFromMinimap(e.clientX, e.clientY);
        e.preventDefault();
    }
});

document.addEventListener('mouseup', () => {
    isDraggingMinimap = false;
});

function moveViewportFromMinimap(clientX, clientY) {
    const rect = minimapEl.getBoundingClientRect();
    const mx = clientX - rect.left;
    const my = clientY - rect.top;
    const { scale, minX, minY, offsetX, offsetY } = minimapState;
    const vpCenterX = minX + (mx - offsetX) / scale;
    const vpCenterY = minY + (my - offsetY) / scale;
    const vpW = window.innerWidth / zoomLevel;
    const vpH = window.innerHeight / zoomLevel;
    const targetVpX = vpCenterX - vpW / 2;
    const targetVpY = vpCenterY - vpH / 2;
    panX = -targetVpX * zoomLevel;
    panY = -targetVpY * zoomLevel;
    updateTransform();
}

function resetView() {
    panX = 0;
    panY = 0;
    zoomLevel = 1.0;
    updateTransform();
}

renderMinimap();
window.addEventListener('resize', renderMinimap);

function toggleInfoPanel() {
    const panel = document.getElementById('info-panel');
    panel.classList.toggle('collapsed');
}
