// app.js - JavaScript for Java HTTP Server
// This file tests that JavaScript files are being served correctly

console.log('✅ JavaScript loaded successfully!');

// Log some server info when page loads
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 Server: Java HTTP Server');
    console.log('👥 Team: juv25d');
    console.log('📍 Current path:', window.location.pathname);
    console.log('✨ Static file serving is working!');
});
