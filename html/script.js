const state = {
    currentScreen: 'dashboard',
    vitals: { hr: 74, o2: 98, temp: 36.6, motion: 'Walking' },
    location: { lat: 13.0827, lng: 80.2707 },
    logs: [
        { type: 'NEO Analysis', score: 12, outcome: 'Stable' },
        { type: 'Manual SOS', score: 95, outcome: 'Cancelled' }
    ]
};

const contentArea = document.getElementById('content');
const navItems = document.querySelectorAll('.nav-item');

// --- Screen Templates ---

const screens = {
    dashboard: () => `
        <div class="header">
            <h1>SafeHer</h1>
            <p>IoT Protection Hub Active</p>
        </div>
        <div class="card status-banner">
            <div class="status-dot"></div>
            <div>
                <div style="font-weight: 700; color: var(--safety-green); font-size: 14px;">STATUS: SECURE</div>
                <div style="font-size: 11px; color: var(--text-secondary);">Neural Orchestration monitoring vitals</div>
            </div>
        </div>
        <div class="grid">
            <div class="card stat-card">
                <span class="material-icons-outlined icon" style="color: var(--emergency-red)">favorite</span>
                <div class="value">${state.vitals.hr} BPM</div>
                <div class="label">Pulse Rate</div>
            </div>
            <div class="card stat-card">
                <span class="material-icons-outlined icon" style="color: var(--electric-violet)">air</span>
                <div class="value">${state.vitals.o2}% O₂</div>
                <div class="label">Blood Oxygen</div>
            </div>
            <div class="card stat-card">
                <span class="material-icons-outlined icon" style="color: var(--warning-amber)">thermostat</span>
                <div class="value">${state.vitals.temp}°C</div>
                <div class="label">Body Temp</div>
            </div>
            <div class="card stat-card">
                <span class="material-icons-outlined icon" style="color: var(--safety-green)">directions_run</span>
                <div class="value">${state.vitals.motion}</div>
                <div class="label">Movement</div>
            </div>
        </div>
        <div class="sos-btn-area">
            <div style="font-size: 11px; font-weight: 700; opacity: 0.5; margin-bottom: 20px;">EMERGENCY OVERRIDE</div>
            <div class="sos-outer">
                <div class="sos-inner">
                    <h2>SOS</h2>
                    <span>HOLD 2S</span>
                </div>
            </div>
            <p style="font-size: 11px; color: var(--text-secondary); margin-top: 20px;">Long press in case of immediate danger</p>
        </div>
    `,
    biometrics: () => `
        <div class="header">
            <h1>ECG & Bio Analytics</h1>
        </div>
        <div class="card" style="height: 200px; display: flex; align-items: center; justify-content: center; color: var(--electric-violet);">
            <div style="text-align: center;">
                <span class="material-icons-outlined" style="font-size: 48px;">show_chart</span>
                <div style="font-size: 12px; font-weight: 600; margin-top: 10px;">LIVE SENSOR WAVEFORM</div>
            </div>
        </div>
        <div class="card">
            <h3 style="margin-top:0; font-size: 14px;">Cloud Sync Activity</h3>
            <div style="font-family: monospace; font-size: 10px; color: var(--safety-green); background: rgba(0,0,0,0.2); padding: 10px; border-radius: 10px;">
                [${new Date().toLocaleTimeString()}] ✔ Biometrics Updated<br>
                [${new Date().toLocaleTimeString()}] ✔ GPS Stream Active<br>
                [${new Date().toLocaleTimeString()}] ✔ Firebase Synced (HTTP 200)
            </div>
        </div>
    `,
    location: () => `
        <div class="header">
            <h1>Live GPS Tracker</h1>
        </div>
        <div id="map-container">
            <div id="map"></div>
        </div>
        <div class="grid">
            <div class="card">
                <div class="label">LATITUDE</div>
                <div class="value" style="font-size: 16px; margin-top: 5px;">${state.location.lat.toFixed(5)}</div>
            </div>
            <div class="card">
                <div class="label">LONGITUDE</div>
                <div class="value" style="font-size: 16px; margin-top: 5px;">${state.location.lng.toFixed(5)}</div>
            </div>
        </div>
        <button class="btn-primary">SHARE LIVE LOCATION</button>
    `,
    guardians: () => `
        <div class="header" style="display: flex; justify-content: space-between; align-items: center;">
            <h1>Trusted Circles</h1>
            <span class="material-icons-outlined" style="color: var(--electric-violet); background: rgba(124,77,255,0.1); padding: 8px; border-radius: 50%;">add</span>
        </div>
        <div class="card" style="display: flex; justify-content: space-between; align-items: center;">
            <div>
                <div style="font-weight: 700;">Mom (Primary)</div>
                <div style="font-size: 12px; color: var(--text-secondary);">+91 9876543210</div>
                <div style="font-size: 10px; color: var(--safety-green); font-weight: 700; margin-top: 5px;">● ACTIVE</div>
            </div>
            <span class="material-icons-outlined" style="color: var(--emergency-red); opacity: 0.5;">delete</span>
        </div>
        <div class="card" style="display: flex; justify-content: space-between; align-items: center;">
            <div>
                <div style="font-weight: 700;">Dad</div>
                <div style="font-size: 12px; color: var(--text-secondary);">+91 9765432109</div>
                <div style="font-size: 10px; color: var(--safety-green); font-weight: 700; margin-top: 5px;">● ACTIVE</div>
            </div>
            <span class="material-icons-outlined" style="color: var(--emergency-red); opacity: 0.5;">delete</span>
        </div>
    `,
    evidence: () => `
        <div class="header">
            <h1>Log Book & Evidence</h1>
        </div>
        <div class="card" style="display: flex; align-items: center; gap: 15px;">
            <span class="material-icons-outlined" style="color: var(--electric-violet)">library_music</span>
            <div style="flex: 1;">
                <div style="font-weight: 700; font-size: 14px;">Evidence_Audio_911.wav</div>
                <div style="font-size: 11px; color: var(--text-secondary);">0:45 | 1.2 MB</div>
            </div>
            <span class="material-icons-outlined">play_circle_outline</span>
        </div>
        <h3 style="font-size: 14px; margin: 20px 0 10px 0;">Threat History</h3>
        ${state.logs.map(log => `
            <div class="card" style="display: flex; justify-content: space-between; align-items: center; padding: 15px;">
                <div>
                    <div style="font-weight: 700; font-size: 13px;">${log.type}</div>
                    <div style="font-size: 11px; color: var(--text-secondary);">Score: ${log.score}/100</div>
                </div>
                <div style="font-size: 11px; font-weight: 800; color: ${log.outcome === 'Cancelled' ? 'var(--safety-green)' : 'var(--warning-amber)'}">${log.outcome.toUpperCase()}</div>
            </div>
        `).join('')}
    `
};

// --- Logic ---

function navigate(screenId) {
    state.currentScreen = screenId;

    // Update UI
    contentArea.innerHTML = screens[screenId]();

    // Update Nav
    navItems.forEach(btn => {
        if(btn.dataset.screen === screenId) btn.classList.add('active');
        else btn.classList.remove('active');
    });

    // Special handling for map
    if(screenId === 'location') {
        initWebMap();
    }
}

function initWebMap() {
    setTimeout(() => {
        const map = L.map('map', { zoomControl: false, attributionControl: false }).setView([state.location.lat, state.location.lng], 15);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);
        L.marker([state.location.lat, state.location.lng]).addTo(map);
    }, 100);
}

// Initial Navigation
navItems.forEach(btn => {
    btn.addEventListener('click', () => navigate(btn.dataset.screen));
});

navigate('dashboard');

// Simulate real-time updates
setInterval(() => {
    state.vitals.hr = 70 + Math.floor(Math.random() * 10);
    if(state.currentScreen === 'dashboard') {
        navigate('dashboard');
    }
}, 3000);
