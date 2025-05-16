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
exports.addDependenciesToYamlString = exports.addDependency = exports.InsertionMethod = void 0;
const fs = require("fs");
const vscode = require("vscode");
const messaging_1 = require("../helper/messaging");
const pubApi_1 = require("../model/pubApi");
const getValue_1 = require("../helper/getValue");
const getSettings_1 = require("../helper/getSettings");
const YAML = require("yaml");
const types_1 = require("yaml/types");
const sortDependencies_1 = require("../helper/sortDependencies");
const getFileContext_1 = require("../helper/getFileContext");
const getPubspecText_1 = require("../helper/getPubspecText");
const formatIfOpened_1 = require("../helper/formatIfOpened");
var InsertionMethod;
(function (InsertionMethod) {
    InsertionMethod["ADD"] = "Added";
    InsertionMethod["REPLACE"] = "Replaced";
})(InsertionMethod = exports.InsertionMethod || (exports.InsertionMethod = {}));
function addDependency(dependencyType) {
    return __awaiter(this, void 0, void 0, function* () {
        const api = new pubApi_1.PubAPI();
        const context = Object.assign(Object.assign({}, (0, getFileContext_1.getFileContext)()), { settings: (0, getSettings_1.getSettings)(), dependencyType: dependencyType });
        if (!context.openInEditor && !fs.existsSync(context.path)) {
            (0, messaging_1.showError)(new Error("Pubspec file not found in workspace root. " +
                "Open the pubspec file you would like to edit and try again."));
            return;
        }
        const packageQueries = yield getPackageNames(context);
        if (packageQueries.length === 0) {
            return;
        }
        const packagesToAdd = [];
        for (const query of packageQueries) {
            const searchingMessage = setMessage({
                message: `Looking for package '${query}'...`,
            });
            const res = yield (0, getValue_1.getValue)(() => api.smartSearchPackage(query));
            if (!res) {
                continue;
            }
            const searchResult = res.result;
            searchingMessage.dispose();
            if (searchResult.packages.length === 0) {
                (0, messaging_1.showInfo)(`Package with name '${packageQueries}' not found.`);
                continue;
            }
            const chosenPackageString = searchResult.packages.length === 1
                ? searchResult.packages[0]
                : yield selectFrom(query, searchResult.packages);
            if (!chosenPackageString) {
                continue;
            }
            if (chosenPackageString.startsWith("dart:")) {
                (0, messaging_1.showInfo)('You don\'t need to add a "dart:" package as a dependency; ' +
                    "they're preinstalled and can be imported directly.");
                continue;
            }
            const gettingPackageMessage = setMessage({
                message: `Getting info for package '${chosenPackageString}'...`,
            });
            const chosenPackageResponse = yield (0, getValue_1.getValue)(() => api.getPackage(chosenPackageString));
            gettingPackageMessage.dispose();
            if (!chosenPackageResponse) {
                continue;
            }
            packagesToAdd.push(chosenPackageResponse.result);
        }
        if (packagesToAdd.length === 0) {
            return;
        }
        try {
            (0, formatIfOpened_1.formatIfOpened)(context);
            const pubspecString = (0, getPubspecText_1.getPubspecText)(context);
            const pubspecParserResult = addDependenciesToYamlString({
                context,
                pubspecString,
                newPackages: packagesToAdd,
            });
            if (!pubspecParserResult.success) {
                (0, messaging_1.showError)(Error(pubspecParserResult.error));
                return;
            }
            let newPubspecString = pubspecParserResult.result;
            if (context.settings.sortDependencies) {
                newPubspecString = (0, sortDependencies_1.sortDependencies)({
                    pubspecString: newPubspecString,
                    useLegacySorting: context.settings.useLegacySorting,
                });
            }
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
            const infoText = packagesToAdd.length === 1
                ? `Added/updated '${packagesToAdd[0].name}' (version ${packagesToAdd[0].latestVersion})${!context.settings.sortDependencies ? "" : " and sorted file"}.`
                : `Added/updated ${packagesToAdd.length} packages and sorted file.`;
            (0, messaging_1.showInfo)(infoText);
        }
        catch (error) {
            (0, messaging_1.handleCriticalError)(error);
        }
    });
}
exports.addDependency = addDependency;
function addDependenciesToYamlString({ context, pubspecString, newPackages, }) {
    const options = {
        schema: "core",
    };
    const pubspecDoc = YAML.parseDocument(pubspecString, options);
    for (const newPackage of newPackages) {
        const versionString = `${context.settings.useCaretSyntax ? "^" : ""}${newPackage.latestVersion}`;
        const dependencyPath = pubspecDoc.get(context.dependencyType, true);
        const dependencyPathIsEmpty = dependencyPath === null || dependencyPath === undefined;
        const dependencyPathIsYAMLMap = dependencyPath instanceof types_1.YAMLMap;
        if ((dependencyPathIsEmpty || !dependencyPathIsYAMLMap) &&
            !pubspecDoc.contents) {
            pubspecDoc.contents = new types_1.YAMLMap();
        }
        const existingDependencies = dependencyPathIsEmpty
            ? {}
            : dependencyPathIsYAMLMap
                ? dependencyPath.toJSON()
                : dependencyPath;
        pubspecDoc.set(context.dependencyType, Object.assign(Object.assign({}, existingDependencies), { [newPackage.name]: versionString }));
    }
    const result = pubspecDoc.toString();
    return { success: true, result };
}
exports.addDependenciesToYamlString = addDependenciesToYamlString;
function getPackageNames(context) {
    return __awaiter(this, void 0, void 0, function* () {
        const rawResult = yield vscode.window.showInputBox({
            prompt: "Enter package names, separated by commas.",
            placeHolder: context.dependencyType === "dependencies"
                ? "Package names (cloud_firestore, get_it, ...)"
                : "Package names (build_runner, freezed, ...)",
        });
        if (!rawResult) {
            return [];
        }
        return rawResult
            .split(",")
            .map((s) => s.trim().toLowerCase())
            .filter((s) => s.length > 0);
    });
}
function setMessage({ message, labelIcon = "sync~spin", }) {
    return vscode.window.setStatusBarMessage(`$(${labelIcon}) ${message}`);
}
function selectFrom(original, items) {
    return vscode.window.showQuickPick(items, {
        canPickMany: false,
        matchOnDescription: true,
        placeHolder: `Search results for "${original}"`,
    });
}
//# sourceMappingURL=addDependency.js.map