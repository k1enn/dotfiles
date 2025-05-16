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
exports.sortAllDependencies = void 0;
const fs = require("fs");
const vscode = require("vscode");
const messaging_1 = require("../helper/messaging");
const getSettings_1 = require("../helper/getSettings");
const sortDependencies_1 = require("../helper/sortDependencies");
const getFileContext_1 = require("../helper/getFileContext");
const getPubspecText_1 = require("../helper/getPubspecText");
const formatIfOpened_1 = require("../helper/formatIfOpened");
function sortAllDependencies() {
    return __awaiter(this, void 0, void 0, function* () {
        const context = Object.assign(Object.assign({}, (0, getFileContext_1.getFileContext)()), { settings: (0, getSettings_1.getSettings)() });
        if (!context.openInEditor && !fs.existsSync(context.path)) {
            (0, messaging_1.showError)(new Error("Pubspec file not found in workspace root. " +
                "Open the pubspec file you would like to sort and try again."));
            return;
        }
        try {
            const pubspecString = (0, getPubspecText_1.getPubspecText)(context);
            const newPubspecString = (0, sortDependencies_1.sortDependencies)({
                pubspecString: pubspecString,
                useLegacySorting: context.settings.useLegacySorting,
            });
            if (context.openInEditor) {
                const originalLines = pubspecString.split("\n");
                vscode.window.activeTextEditor.edit((editBuilder) => {
                    editBuilder.replace(new vscode.Range(new vscode.Position(0, 0), new vscode.Position(originalLines.length - 1, originalLines[originalLines.length - 1].length)), newPubspecString);
                });
            }
            else {
                fs.writeFileSync(context.path, newPubspecString, "utf-8");
            }
            (0, formatIfOpened_1.formatIfOpened)(context);
        }
        catch (error) {
            (0, messaging_1.handleCriticalError)(error);
        }
    });
}
exports.sortAllDependencies = sortAllDependencies;
//# sourceMappingURL=sortAllDependencies.js.map