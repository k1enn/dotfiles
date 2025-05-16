"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.convertTo = void 0;
const vscode_1 = require("vscode");
const utils_1 = require("../utils");
const childRegExp = new RegExp("[^S\r\n]*child: .*,s*", "ms");
exports.convertTo = (snippet) => __awaiter(void 0, void 0, void 0, function* () {
    let editor = vscode_1.window.activeTextEditor;
    if (!editor)
        return;
    const selection = utils_1.getSelectedText(editor);
    const rawWidget = editor.document.getText(selection).replace("$", "//$");
    const match = rawWidget.match(childRegExp);
    if (!match || !match.length)
        return;
    const child = match[0];
    if (!child)
        return;
    const widget = rawWidget.replace(childRegExp, "");
    editor.insertSnippet(new vscode_1.SnippetString(snippet(widget, child)), selection);
    yield vscode_1.commands.executeCommand("editor.action.formatDocument");
});
//# sourceMappingURL=convert-to.js.map