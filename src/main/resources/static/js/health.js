async function loadHealth() {
    try {
        const res = await fetch("/health");
        const data = await res.json();

        document.getElementById("health-status").textContent = data.status;
        document.getElementById("local-time").textContent = data.localTime;
        document.getElementById("utc-time").textContent = data.utcTime;
        document.getElementById("health-version").textContent = data.buildVersion;
        document.getElementById("health-commit").textContent = data.gitCommit;
        document.getElementById("health-response").textContent =
            data.responseTimeMs + " µs";

        const usedMb = (data.memory.usedBytes / 1024 / 1024).toFixed(1);
        const maxMb = (data.memory.maxBytes / 1024 / 1024).toFixed(1);
        document.getElementById("health-memory").textContent =
            `${usedMb} / ${maxMb} MB`;

    } catch (e) {
        document.getElementById("health-status").textContent = "DOWN";
    }
}

// Update every second
setInterval(loadHealth, 1_000);
loadHealth();
