"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.updatePubspecDependency = void 0;
const fs = require("fs");
const vscode_1 = require("vscode");
const get_pubspec_path_1 = require("./get-pubspec-path");
function updatePubspecDependency(dependency) {
    if (vscode_1.workspace.workspaceFolders && vscode_1.workspace.workspaceFolders.length > 0) {
        const pubspecPath = get_pubspec_path_1.getPubspecPath();
        if (pubspecPath) {
            try {
                fs.writeFileSync(pubspecPath, fs
                    .readFileSync(pubspecPath, "utf8")
                    .replace(`${dependency.name}: ${dependency.currentVersion}`, `${dependency.name}: ${dependency.latestVersion}`));
            }
            catch (_) { }
        }
    }
}
exports.updatePubspecDependency = updatePubspecDependency;
//# sourceMappingURL=update-pubspec-dependency.js.map