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
exports.tryStartLanguageServer = exports.client = void 0;
const semver = require("semver");
const vscode_1 = require("vscode");
const node_1 = require("vscode-languageclient/node");
const utils_1 = require("../utils");
let client;
exports.client = client;
const DART_FILE = { language: "dart", scheme: "file" };
const ANALYSIS_OPTIONS_FILE = {
    pattern: "**/analysis_options.yaml",
    scheme: "file",
};
const DART_SDK_CONSTRAINT = "^3.7.0";
function startLanguageServer() {
    return __awaiter(this, void 0, void 0, function* () {
        const serverOptions = {
            command: "bloc",
            args: ["language-server"],
            options: {
                env: process.env,
                shell: true,
            },
            transport: node_1.TransportKind.stdio,
        };
        const clientOptions = {
            revealOutputChannelOn: node_1.RevealOutputChannelOn.Info,
            documentSelector: [DART_FILE, ANALYSIS_OPTIONS_FILE],
        };
        exports.client = client = new node_1.LanguageClient("blocAnalysisLSP", "Bloc Analysis Server", serverOptions, clientOptions);
        return client.start();
    });
}
function startLanguageServerWithProgress() {
    return __awaiter(this, void 0, void 0, function* () {
        vscode_1.window.withProgress({
            location: vscode_1.ProgressLocation.Window,
            title: "Bloc Analysis Server",
        }, () => __awaiter(this, void 0, void 0, function* () {
            try {
                yield startLanguageServer();
                vscode_1.window.setStatusBarMessage("✓ Bloc Analysis Server", 3000);
            }
            catch (err) {
                vscode_1.window.showErrorMessage(`${err}`);
            }
        }));
    });
}
function tryStartLanguageServer() {
    return __awaiter(this, void 0, void 0, function* () {
        const blocToolsVersion = yield utils_1.getBlocToolsVersion();
        if (blocToolsVersion == utils_1.BLOC_TOOLS_VERSION) {
            return startLanguageServerWithProgress();
        }
        const dartVersion = yield utils_1.getDartVersion();
        if (!dartVersion) {
            vscode_1.window.showWarningMessage("The Bloc Language Server requires Dart to be installed");
            return;
        }
        if (!semver.satisfies(dartVersion, DART_SDK_CONSTRAINT)) {
            vscode_1.window.showWarningMessage(`The Bloc Language Server requires a newer version of Dart (${DART_SDK_CONSTRAINT})`);
            return;
        }
        const areBlocToolsInstalled = blocToolsVersion != null;
        var didActivate = false;
        yield vscode_1.window.withProgress({
            location: vscode_1.ProgressLocation.Notification,
            title: areBlocToolsInstalled
                ? "Upgrading the Bloc Language Server"
                : "Installing the Bloc Language Server",
        }, () => __awaiter(this, void 0, void 0, function* () {
            try {
                didActivate = yield utils_1.installBlocTools();
                vscode_1.window.setStatusBarMessage(areBlocToolsInstalled
                    ? "Bloc Language Server upgraded"
                    : "Bloc Language Server installed", 3000);
            }
            catch (err) {
                vscode_1.window.showErrorMessage(`${err}`);
            }
        }));
        if (!didActivate) {
            vscode_1.window.showErrorMessage(areBlocToolsInstalled
                ? "Failed to upgrade the Bloc Language Server"
                : "Failed to install the Bloc Language Server");
            return;
        }
        return startLanguageServerWithProgress();
    });
}
exports.tryStartLanguageServer = tryStartLanguageServer;
//# sourceMappingURL=language-server.js.map