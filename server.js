const http = require('http');
const fs = require('fs');
const path = require('path');

const PUBLIC_DIR = path.join(__dirname, 'public');
const PORT = 3000;

const server = http.createServer((req, res) => {
    let safeUrl = req.url.split('?')[0];
    let filePath = path.join(PUBLIC_DIR, safeUrl === '/' ? 'index.html' : safeUrl);

    if (!filePath.startsWith(PUBLIC_DIR)) {
        res.writeHead(403);
        res.end('Forbidden');
        return;
    }

    fs.stat(filePath, (err, stats) => {
        if (err || !stats.isFile()) {
            filePath = path.join(PUBLIC_DIR, 'index.html');
        }

        const ext = path.extname(filePath).toLowerCase();
        const contentTypes = {
            '.html': 'text/html; charset=utf-8',
            '.apk': 'application/vnd.android.package-archive',
            '.zip': 'application/zip',
            '.png': 'image/png',
            '.jpg': 'image/jpeg',
            '.json': 'application/json',
            '.css': 'text/css',
            '.js': 'application/javascript'
        };

        const contentType = contentTypes[ext] || 'application/octet-stream';

        fs.stat(filePath, (err2, stat) => {
            if (err2 || !stat.isFile()) {
                res.writeHead(404);
                res.end('Not Found');
                return;
            }

            res.writeHead(200, {
                'Content-Type': contentType,
                'Content-Length': stat.size,
                'Access-Control-Allow-Origin': '*'
            });

            if (req.method === 'HEAD') {
                res.end();
                return;
            }

            fs.createReadStream(filePath).pipe(res);
        });
    });
});

server.listen(PORT, '0.0.0.0', () => {
    console.log(`James Chat Web & APK server listening on port ${PORT}`);
});

process.on('SIGTERM', () => process.exit(0));
process.on('SIGINT', () => process.exit(0));
