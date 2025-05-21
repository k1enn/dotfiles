"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.deactivate = exports.activate = void 0;
const vscode_1 = require("vscode");
const documentDecorationManager_1 = require("./documentDecorationManager");
function activate(context) {
    const isNativeBracketPairColorizationEnabled = !!vscode_1.workspace.getConfiguration().get("editor.bracketPairColorization.enabled");
    const configuration = vscode_1.workspace.getConfiguration("bracket-pair-colorizer-2", undefined);
    let noticeKey = "depreciation-notice";
    var showNotice = configuration.get(noticeKey);
    if (showNotice) {
        const items = [{ title: "Learn more" }];
        items.push({ title: "Migrate to native colorization" });
        items.push({ title: "Don't show again" });
        vscode_1.window.showInformationMessage("Bracket Pair Colorizer is no longer being maintained.", ...items).then(e => {
            if ((e === null || e === void 0 ? void 0 : e.title) == "Learn more") {
                vscode_1.env.openExternal(vscode_1.Uri.parse('https://github.com/CoenraadS/Bracket-Pair-Colorizer-2#readme'));
            }
            if ((e === null || e === void 0 ? void 0 : e.title) == "Migrate to native colorization") {
                if (!isNativeBracketPairColorizationEnabled) {
                    vscode_1.workspace
                        .getConfiguration()
                        .update("editor.bracketPairColorization.enabled", true, vscode_1.ConfigurationTarget.Global);
                    vscode_1.workspace
                        .getConfiguration()
                        .update("editor.guides.bracketPairs", "active", vscode_1.ConfigurationTarget.Global);
                    // Disable extension, only required if `isNativeBracketPairColorizationEnabled` is not true.
                    for (const sub of context.subscriptions) {
                        sub.dispose();
                    }
                    context.subscriptions.length = 0;
                    documentDecorationManager.Dispose();
                }
                vscode_1.commands.executeCommand("workbench.extensions.uninstallExtension", "CoenraadS.bracket-pair-colorizer-2");
            }
            if ((e === null || e === void 0 ? void 0 : e.title) == "Don't show again") {
                configuration.update(noticeKey, false, true);
            }
        });
    }
    if (isNativeBracketPairColorizationEnabled) {
        // don't do bracket pair colorization if native colorization is already enabled.
        return;
    }
    let documentDecorationManager = new documentDecorationManager_1.default();
    context.subscriptions.push(vscode_1.extensions.onDidChange(() => restart()), vscode_1.commands.registerCommand("bracket-pair-colorizer-2.expandBracketSelection", () => {
        const editor = vscode_1.window.activeTextEditor;
        if (!editor) {
            return;
        }
        documentDecorationManager.expandBracketSelection(editor);
    }), vscode_1.commands.registerCommand("bracket-pair-colorizer-2.undoBracketSelection", () => {
        const editor = vscode_1.window.activeTextEditor;
        if (!editor) {
            return;
        }
        documentDecorationManager.undoBracketSelection(editor);
    }), vscode_1.workspace.onDidChangeConfiguration((event) => {
        if (event.affectsConfiguration("bracket-pair-colorizer-2") ||
            event.affectsConfiguration("editor.lineHeight") ||
            event.affectsConfiguration("editor.fontSize")) {
            restart();
        }
    }), vscode_1.window.onDidChangeVisibleTextEditors(() => {
        documentDecorationManager.updateAllDocuments();
    }), vscode_1.workspace.onDidChangeTextDocument((event) => {
        if (event.contentChanges.length > 0) {
            documentDecorationManager.onDidChangeTextDocument(event);
        }
    }), vscode_1.workspace.onDidCloseTextDocument((event) => {
        documentDecorationManager.onDidCloseTextDocument(event);
    }), vscode_1.workspace.onDidOpenTextDocument((event) => {
        documentDecorationManager.onDidOpenTextDocument(event);
    }), vscode_1.window.onDidChangeTextEditorSelection((event) => {
        documentDecorationManager.onDidChangeSelection(event);
    }));
    documentDecorationManager.updateAllDocuments();
    function restart() {
        documentDecorationManager.Dispose();
        documentDecorationManager = new documentDecorationManager_1.default();
        documentDecorationManager.updateAllDocuments();
    }
}
exports.activate = activate;
// tslint:disable-next-line:no-empty
function deactivate() {
}
exports.deactivate = deactivate;
//# sourceMappingURL=extension.js.map