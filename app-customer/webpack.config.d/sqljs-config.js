config.resolve.fallback = {
    fs: false,
    path: "path-browserify",
    os: "os-browserify/browser",//false,
    crypto: false,
};

const CopyWebpackPlugin = require('copy-webpack-plugin');
config.plugins.push(
    new CopyWebpackPlugin({
        patterns: [
            '../../node_modules/sql.js/dist/sql-wasm.wasm'
        ]
    })
);