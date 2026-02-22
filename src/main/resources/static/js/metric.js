async function loadHealth() {
    try {
        const res = await fetch("/metric");
        if (!res.ok) {
            throw new Error(`HTTP ${res.status}`)
        };
        const data = await res.json();

        document.getElementById("local-time").textContent = data.localTime;
        document.getElementById("utc-time").textContent = data.utcTime;
        document.getElementById("health-version").textContent = data.buildVersion;
        document.getElementById("health-commit").textContent = data.gitCommit;
        document.getElementById("health-response").textContent =
            data.responseTimeUs + " µs";

        const usedMb = data.memory
            ? (data.memory.usedBytes / 1024 / 1024).toFixed(1)
            : "N/A";
        const maxMb = data.memory
            ? (data.memory.maxBytes / 1024 / 1024).toFixed(1)
            : "N/A";

        document.getElementById("health-memory").textContent =
            `${usedMb} / ${maxMb} MB`;
        document.getElementById("health-status").textContent = "UP";

    } catch (e) {
        document.getElementById("health-status").textContent = "DOWN";
    }
}

// Update every second
setInterval(loadHealth, 1_000);
loadHealth();
