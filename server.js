const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 5000;
const rootDir = __dirname;

const server = http.createServer((req, res) => {
    // Routage
    if (req.url === '/' || req.url === '/index.html') {
        serveFile(res, path.join(rootDir, 'docs/index.html'), 'text/html');
    } else if (req.url === '/simulator') {
        serveFile(res, path.join(rootDir, 'app-simulator.html'), 'text/html');
    } else if (req.url === '/download-apk') {
        const apkPath = path.join(rootDir, 'app/build/outputs/apk/debug/app-debug.apk');
        if (fs.existsSync(apkPath)) {
            res.setHeader('Content-Type', 'application/vnd.android.package-archive');
            res.setHeader('Content-Disposition', 'attachment; filename="GenshinWishTracker.apk"');
            fs.createReadStream(apkPath).pipe(res);
        } else {
            res.writeHead(404, { 'Content-Type': 'text/html' });
            res.end('<h1>❌ APK non généré</h1><p>Lancer ./build-apk.sh pour compiler</p>');
        }
    } else if (req.url === '/api/status') {
        const apkPath = path.join(rootDir, 'app/build/outputs/apk/debug/app-debug.apk');
        const status = {
            apkGenerated: fs.existsSync(apkPath),
            projectReady: true,
            buildCommand: './build-apk.sh'
        };
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify(status, null, 2));
    } else {
        res.writeHead(404, { 'Content-Type': 'text/html' });
        res.end('<h1>404 Not Found</h1>');
    }
});

function serveFile(res, filePath, contentType) {
    fs.readFile(filePath, (err, data) => {
        if (err) {
            res.writeHead(500, { 'Content-Type': 'text/html' });
            res.end('<h1>❌ Erreur serveur</h1>');
            return;
        }
        res.writeHead(200, { 'Content-Type': contentType });
        res.end(data);
    });
}

server.listen(PORT, '0.0.0.0', () => {
    console.log('\n🚀 Genshin Wish Tracker - Web Server');
    console.log('====================================');
    console.log(`\n📍 Serveur démarré sur http://0.0.0.0:${PORT}`);
    console.log(`\n🌐 Accès local : http://localhost:${PORT}`);
    console.log(`\n📋 Routes disponibles :`);
    console.log(`   / ou /index.html - Documentation`);
    console.log(`   /simulator - Simulateur web interactif`);
    console.log(`   /download-apk - Télécharger l'APK compilé`);
    console.log(`   /api/status - État du build\n`);
});
