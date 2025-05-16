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
exports.reportDiagnostics = void 0;
const child_process_1 = require("child_process");
const util_1 = require("util");
const vscode_1 = require("vscode");
exports.reportDiagnostics = (root, diagnostics) => {
    var spawned = false;
    const completer = new Completer();
    vscode_1.window.withProgress({
        location: vscode_1.ProgressLocation.Window,
        title: "bloc analyzer",
    }, (_) => __awaiter(void 0, void 0, void 0, function* () {
        try {
            yield completer.promise;
            vscode_1.window.setStatusBarMessage("✓ bloc analyzer");
        }
        catch (err) {
            vscode_1.window.showErrorMessage(`${err}`);
        }
    }));
    const lint = child_process_1.spawn("dart", [
        "/Users/felix/Development/playgrounds/dart_playground/bin/bloc.dart",
        root,
        "--watch",
    ]);
    const decoder = new util_1.TextDecoder();
    lint.stdout.on("data", (data) => __awaiter(void 0, void 0, void 0, function* () {
        if (!spawned) {
            spawned = true;
            completer.complete();
        }
        const decoded = decoder.decode(data);
        const diagnosticsJson = JSON.parse(decoded);
        for (let file in diagnosticsJson) {
            const results = [];
            const document = yield vscode_1.workspace.openTextDocument(file);
            const diagnosticsForFile = diagnosticsJson[file];
            for (let diagnostic of diagnosticsForFile) {
                const range = toRange(document, diagnostic.offset, diagnostic.length);
                results.push(new vscode_1.Diagnostic(range, diagnostic.message, toSeverity(diagnostic.severity)));
            }
            diagnostics.set(document.uri, results);
        }
    }));
};
function toRange(document, offset, length) {
    return new vscode_1.Range(document.positionAt(offset), document.positionAt(offset + length));
}
function toSeverity(value) {
    if (value == 1) {
        return vscode_1.DiagnosticSeverity.Warning;
    }
    if (value == 2) {
        return vscode_1.DiagnosticSeverity.Information;
    }
    if (value == 3) {
        return vscode_1.DiagnosticSeverity.Hint;
    }
    return vscode_1.DiagnosticSeverity.Error;
}
class Completer {
    constructor() {
        this.promise = new Promise((resolve, reject) => {
            this.complete = resolve;
            this.reject = reject;
        });
    }
}
//# sourceMappingURL=diagnostics.js.map