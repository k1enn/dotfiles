"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const vscode = require("vscode");
const core_1 = require("./core");
const completion_1 = require("./completion");
const constants_1 = require("./core/constants");
const language = 'dart';
function widgetsFromAbbr() {
    let editor = vscode.window.activeTextEditor;
    if (!editor) {
        vscode.window.showInformationMessage('No editor');
        return;
    }
    vscode.window.showInputBox({
        prompt: 'Enter Abbreviation', placeHolder: 'Container>Center'
    }).then(abbr => {
        if (editor) {
            let expandText = abbr || '';
            if (core_1.validate(expandText)) {
                editor.insertSnippet(new vscode.SnippetString(core_1.expand(expandText)));
            }
            else {
                vscode.window.showWarningMessage('Invalid syntax');
            }
        }
    });
}
function widgetsFromSelection() {
    vscode.window.showInformationMessage('This feature is not available now');
}
function activate(context) {
    registerCompletionProviders(context);
    context.subscriptions.push(vscode.commands.registerCommand('extension.fromAbbr', () => {
        widgetsFromAbbr();
    }));
    context.subscriptions.push(vscode.commands.registerCommand('extension.fromSelection', () => {
        widgetsFromSelection();
    }));
}
exports.activate = activate;
function registerCompletionProviders(context) {
    let completionProvider = new completion_1.CompletionProvider();
    const provider = vscode.languages.registerCompletionItemProvider([{ language, scheme: 'file' }, { language, scheme: 'untitled' }], completionProvider, ...constants_1.triggers);
    context.subscriptions.push(provider);
}
function deactivate() { }
exports.deactivate = deactivate;
//# sourceMappingURL=extension.js.map