"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.getFileContext = void 0;
const vscode = require("vscode");
function getFileContext() {
    const pubspecIsOpen = pubspecFileIsOpen();
    const pubspecPath = pubspecIsOpen
        ? vscode.window.activeTextEditor.document.uri.fsPath
        : `${vscode.workspace.rootPath}/pubspec.yaml`;
    return {
        openInEditor: pubspecIsOpen,
        path: pubspecPath,
    };
}
exports.getFileContext = getFileContext;
function pubspecFileIsOpen() {
    return ((vscode.window.activeTextEditor &&
        (vscode.window.activeTextEditor.document.fileName.endsWith("pubspec.yaml") ||
            vscode.window.activeTextEditor.document.fileName.endsWith("pubspec.yml"))) ||
        false);
}
//# sourceMappingURL=getFileContext.js.map