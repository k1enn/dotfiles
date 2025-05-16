"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.getPubspecText = void 0;
const fs = require("fs");
const vscode = require("vscode");
function getPubspecText(context) {
    return context.openInEditor
        ? vscode.window.activeTextEditor.document.getText()
        : fs.readFileSync(context.path, "utf8");
}
exports.getPubspecText = getPubspecText;
//# sourceMappingURL=getPubspecText.js.map