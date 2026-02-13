fetch("/README.md")
    .then(response => response.text())
    .then(markdown => {
        const container = document.getElementById("readme_content");
        container.innerHTML = marked.parse(markdown);
    })
    .catch(error => {
        console.error("Kunde inte ladda README.md", error);
    });
