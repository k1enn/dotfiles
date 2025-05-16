"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.getPubspecLockPath = exports.getPubspecPath = void 0;
const vscode_1 = require("vscode");
const path = require("path");
const PUBSPEC_FILE_NAME = "pubspec.yaml";
const PUBSPEC_LOCK_FILE_NAME = "pubspec.lock";
function getPubspecPath() {
    return getWorkspacePath(PUBSPEC_FILE_NAME);
}
exports.getPubspecPath = getPubspecPath;
function getPubspecLockPath() {
    return getWorkspacePath(PUBSPEC_LOCK_FILE_NAME);
}
exports.getPubspecLockPath = getPubspecLockPath;
function getWorkspacePath(fileName) {
    if (vscode_1.workspace.workspaceFolders && vscode_1.workspace.workspaceFolders.length > 0) {
        return path.join(`${vscode_1.workspace.workspaceFolders[0].uri.path}`, fileName);
    }
}
//# sourceMappingURL=get-pubspec-path.js.map