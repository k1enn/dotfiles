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
exports.deactivate = exports.activate = void 0;
const path = require("path");
const vscode_1 = require("vscode");
const code_actions_1 = require("./code-actions");
const commands_1 = require("./commands");
const utils_1 = require("./utils");
const DART_MODE = { language: "dart", scheme: "file" };
const node_1 = require("vscode-languageclient/node");
let client;
function initializeLanguageClient(context) {
    // The server is implemented in node
    const serverModule = context.asAbsolutePath(path.join("server", "out", "server.js"));
    // If the extension is launched in debug mode then the debug server options are used
    // Otherwise the run options are used
    const serverOptions = {
        run: { module: serverModule, transport: node_1.TransportKind.ipc },
        debug: {
            module: serverModule,
            transport: node_1.TransportKind.ipc,
        },
    };
    // Options to control the language client
    const clientOptions = {
        // Register the server for plain text documents
        documentSelector: [{ scheme: "file", language: "plaintext" }],
        synchronize: {
            // Notify the server about file changes to '.clientrc files contained in the workspace
            fileEvents: vscode_1.workspace.createFileSystemWatcher("**/.clientrc"),
        },
    };
    // Create the language client and start the client.
    client = new node_1.LanguageClient("languageServerExample", "Language Server Example", serverOptions, clientOptions);
    // Start the client. This will also launch the server
    client.start();
}
function activate(_context) {
    initializeLanguageClient(_context);
    if (vscode_1.workspace.getConfiguration("bloc").get("checkForUpdates")) {
        utils_1.analyzeDependencies();
    }
    utils_1.setShowContextMenu();
    _context.subscriptions.push(vscode_1.window.onDidChangeActiveTextEditor((_) => utils_1.setShowContextMenu()), vscode_1.workspace.onDidChangeWorkspaceFolders((_) => utils_1.setShowContextMenu()), vscode_1.workspace.onDidChangeTextDocument(function (event) {
        return __awaiter(this, void 0, void 0, function* () {
            if (event.document.uri.fsPath.endsWith("pubspec.yaml")) {
                utils_1.setShowContextMenu(event.document.uri);
            }
        });
    }), vscode_1.commands.registerCommand("extension.new-bloc", commands_1.newBloc), vscode_1.commands.registerCommand("extension.new-cubit", commands_1.newCubit), vscode_1.commands.registerCommand("extension.convert-multibloclistener", commands_1.convertToMultiBlocListener), vscode_1.commands.registerCommand("extension.convert-multiblocprovider", commands_1.convertToMultiBlocProvider), vscode_1.commands.registerCommand("extension.convert-multirepositoryprovider", commands_1.convertToMultiRepositoryProvider), vscode_1.commands.registerCommand("extension.wrap-blocbuilder", commands_1.wrapWithBlocBuilder), vscode_1.commands.registerCommand("extension.wrap-blocselector", commands_1.wrapWithBlocSelector), vscode_1.commands.registerCommand("extension.wrap-bloclistener", commands_1.wrapWithBlocListener), vscode_1.commands.registerCommand("extension.wrap-blocconsumer", commands_1.wrapWithBlocConsumer), vscode_1.commands.registerCommand("extension.wrap-blocprovider", commands_1.wrapWithBlocProvider), vscode_1.commands.registerCommand("extension.wrap-repositoryprovider", commands_1.wrapWithRepositoryProvider), vscode_1.languages.registerCodeActionsProvider(DART_MODE, new code_actions_1.BlocCodeActionProvider()));
}
exports.activate = activate;
function deactivate() {
    if (!client) {
        return undefined;
    }
    return client.stop();
}
exports.deactivate = deactivate;
//# sourceMappingURL=extension.js.map