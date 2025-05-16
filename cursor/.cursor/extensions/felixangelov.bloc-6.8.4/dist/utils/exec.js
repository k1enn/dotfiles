"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.exec = void 0;
const cp = require("child_process");
exports.exec = (cmd, options) => new Promise((resolve, reject) => {
    cp.exec(cmd, { cwd: options === null || options === void 0 ? void 0 : options.cwd }, (err, output) => {
        if (err) {
            return reject(err);
        }
        return resolve(output);
    });
});
//# sourceMappingURL=exec.js.map