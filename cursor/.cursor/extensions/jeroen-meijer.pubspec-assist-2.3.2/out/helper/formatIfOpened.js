"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.formatIfOpened = void 0;
const vscode = require("vscode");
function formatIfOpened(context) {
    if (context.openInEditor) {
        vscode.commands.executeCommand("editor.action.formatDocument");
    }
}
exports.formatIfOpened = formatIfOpened;
//# sourceMappingURL=formatIfOpened.js.map