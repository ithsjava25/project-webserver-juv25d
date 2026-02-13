// app.js - JavaScript for Java HTTP Server
// This file tests that JavaScript files are being served correctly

console.log('✅ JavaScript loaded successfully!');

// Log some server info when page loads
document.addEventListener("DOMContentLoaded", () => {
    console.log('🚀 Server: Java HTTP Server');
    console.log('👥 Team: juv25d');
    console.log('📍 Current path:', window.location.pathname);
    console.log('✨ Static file serving is working!');

    route(window.location.pathname);
});

window.addEventListener("popstate", () => {
    navigate(window.location.pathname);
});


const routes = {
    "/index.html": () => {},
    "/readme.html": initReadme,
};

function route(path) {
    const cleanPath = path.startsWith("/") ? path : "/" + path;
    const handler = routes[cleanPath];
    if (handler) handler();
}


const nav = document.querySelector(".nav-menu");

function navigate(href) {
    nav.classList.add("disable-anchors");
    const main = document.getElementById("main-content");

    main.classList.add("fade-out");

    setTimeout(() => {
        fetch(href)
            .then(res => {
                if (!res.ok) throw new Error(`HTTP ${res.status}`);
                return  res.text();
            })
            .then(html => {
                const doc = new DOMParser().parseFromString(html, "text/html");
                const newMain = doc.querySelector("main");

                if (!newMain) throw new Error("No <main> found in " + href);

                main.innerHTML = newMain.innerHTML;
                history.pushState(null, "", href);
                route(href);

                main.classList.remove("fade-out");

                setTimeout(() => {
                    nav.classList.remove("disable-anchors");
                }, 150);
            })
            .catch(err => {
                console.error("Navigation failed:", err);
                main.classList.remove("fade-out");
                nav.classList.remove("disable-anchors");
            });
    }, 200);
}


document.addEventListener("click", (e) => {
    const link = e.target.closest("a");
    if (!link) return;

    const href = link.getAttribute("href");
    if (!href || !href.endsWith(".html")) return;

    e.preventDefault();
    navigate(href);
});



function initReadme() {
    const container = document.getElementById("readme_content");
    if (!container) return;

    fetch("/README.md")
        .then(res => {
            if (!res.ok) throw new Error("Failed to load README.md");
            return res.text();
        })
        .then(md => {
            container.innerHTML = DOMPurify.sanitize(marked.parse(md));
        })
        .catch(err => console.error("Failed to load README.md", err));
}


