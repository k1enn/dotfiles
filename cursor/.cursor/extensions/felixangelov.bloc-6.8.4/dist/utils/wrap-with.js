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
exports.wrapWith = void 0;
const vscode_1 = require("vscode");
const utils_1 = require("../utils");
const interpolatedVarRegExp = /[$]/g;
const escapedCharacterRegExp = /[\\]/g;
exports.wrapWith = (snippet) => __awaiter(void 0, void 0, void 0, function* () {
    let editor = vscode_1.window.activeTextEditor;
    if (!editor)
        return;
    const selection = utils_1.getSelectedText(editor);
    const widget = editor.document
        .getText(selection)
        .replace(escapedCharacterRegExp, "\\\\")
        .replace(interpolatedVarRegExp, "\\$");
    editor.insertSnippet(new vscode_1.SnippetString(snippet(widget)), selection);
    yield vscode_1.commands.executeCommand("editor.action.formatDocument");
});
//# sourceMappingURL=wrap-with.js.map