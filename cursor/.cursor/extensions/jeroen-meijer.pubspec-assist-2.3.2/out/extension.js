"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.deactivate = exports.activate = void 0;
const vscode = require("vscode");
const functions = require("./functions");
function activate(context) {
    const commands = {
        addDependencyCommand: vscode.commands.registerCommand("pubspec-assist.addDependency", (_) => functions.addDependency("dependencies")),
        addDevDependencyCommand: vscode.commands.registerCommand("pubspec-assist.addDevDependency", (_) => functions.addDependency("dev_dependencies")),
        sortAllDependenciesCommand: vscode.commands.registerCommand("pubspec-assist.sortAllDependencies", (_) => functions.sortAllDependencies()),
    };
    Object.values(commands).forEach((command) => context.subscriptions.push(command));
}
exports.activate = activate;
// this method is called when your extension is deactivated
function deactivate() { }
exports.deactivate = deactivate;
//# sourceMappingURL=extension.js.map