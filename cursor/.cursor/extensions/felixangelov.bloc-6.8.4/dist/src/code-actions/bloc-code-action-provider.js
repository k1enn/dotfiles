"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.BlocCodeActionProvider = void 0;
const vscode_1 = require("vscode");
const utils_1 = require("../utils");
const blocListenerRegExp = new RegExp("^BlocListener(\\<.*\\>)*\\(.*\\)", "ms");
const blocProviderRegExp = new RegExp("^BlocProvider(\\<.*\\>)*(\\.value)*\\(.*\\)", "ms");
const repositoryProviderRegExp = new RegExp("^RepositoryProvider(\\<.*\\>)*(\\.value)*\\(.*\\)", "ms");
class BlocCodeActionProvider {
    provideCodeActions() {
        const editor = vscode_1.window.activeTextEditor;
        if (!editor)
            return [];
        const selectedText = editor.document.getText(utils_1.getSelectedText(editor));
        if (selectedText === "")
            return [];
        const isBlocListener = blocListenerRegExp.test(selectedText);
        const isBlocProvider = blocProviderRegExp.test(selectedText);
        const isRepositoryProvider = repositoryProviderRegExp.test(selectedText);
        return [
            ...(isBlocListener
                ? [
                    {
                        command: "extension.convert-multibloclistener",
                        title: "Convert to MultiBlocListener",
                    },
                ]
                : []),
            ...(isBlocProvider
                ? [
                    {
                        command: "extension.convert-multiblocprovider",
                        title: "Convert to MultiBlocProvider",
                    },
                ]
                : []),
            ...(isRepositoryProvider
                ? [
                    {
                        command: "extension.convert-multirepositoryprovider",
                        title: "Convert to MultiRepositoryProvider",
                    },
                ]
                : []),
            {
                command: "extension.wrap-blocbuilder",
                title: "Wrap with BlocBuilder",
            },
            {
                command: "extension.wrap-blocselector",
                title: "Wrap with BlocSelector",
            },
            {
                command: "extension.wrap-bloclistener",
                title: "Wrap with BlocListener",
            },
            {
                command: "extension.wrap-blocconsumer",
                title: "Wrap with BlocConsumer",
            },
            {
                command: "extension.wrap-blocprovider",
                title: "Wrap with BlocProvider",
            },
            {
                command: "extension.wrap-repositoryprovider",
                title: "Wrap with RepositoryProvider",
            },
        ].map((c) => {
            let action = new vscode_1.CodeAction(c.title, vscode_1.CodeActionKind.Refactor);
            action.command = {
                command: c.command,
                title: c.title,
            };
            return action;
        });
    }
}
exports.BlocCodeActionProvider = BlocCodeActionProvider;
//# sourceMappingURL=bloc-code-action-provider.js.map