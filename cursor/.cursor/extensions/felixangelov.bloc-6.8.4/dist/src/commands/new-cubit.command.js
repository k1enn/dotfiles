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
exports.newCubit = void 0;
const _ = require("lodash");
const changeCase = require("change-case");
const mkdirp = require("mkdirp");
const vscode_1 = require("vscode");
const fs_1 = require("fs");
const templates_1 = require("../templates");
const utils_1 = require("../utils");
exports.newCubit = (uri) => __awaiter(void 0, void 0, void 0, function* () {
    const cubitName = yield promptForCubitName();
    if (_.isNil(cubitName) || cubitName.trim() === "") {
        vscode_1.window.showErrorMessage("The cubit name must not be empty");
        return;
    }
    let targetDirectory;
    if (_.isNil(_.get(uri, "fsPath")) || !fs_1.lstatSync(uri.fsPath).isDirectory()) {
        targetDirectory = yield promptForTargetDirectory();
        if (_.isNil(targetDirectory)) {
            vscode_1.window.showErrorMessage("Please select a valid directory");
            return;
        }
    }
    else {
        targetDirectory = uri.fsPath;
    }
    const blocType = yield utils_1.getBlocType(1 /* Cubit */);
    const pascalCaseCubitName = changeCase.pascalCase(cubitName);
    try {
        yield generateCubitCode(cubitName, targetDirectory, blocType);
        vscode_1.window.showInformationMessage(`Successfully Generated ${pascalCaseCubitName} Cubit`);
    }
    catch (error) {
        vscode_1.window.showErrorMessage(`Error:
        ${error instanceof Error ? error.message : JSON.stringify(error)}`);
    }
});
function promptForCubitName() {
    const cubitNamePromptOptions = {
        prompt: "Cubit Name",
        placeHolder: "counter",
    };
    return vscode_1.window.showInputBox(cubitNamePromptOptions);
}
function promptForTargetDirectory() {
    return __awaiter(this, void 0, void 0, function* () {
        const options = {
            canSelectMany: false,
            openLabel: "Select a folder to create the cubit in",
            canSelectFolders: true,
        };
        return vscode_1.window.showOpenDialog(options).then((uri) => {
            if (_.isNil(uri) || _.isEmpty(uri)) {
                return undefined;
            }
            return uri[0].fsPath;
        });
    });
}
function generateCubitCode(cubitName, targetDirectory, type) {
    return __awaiter(this, void 0, void 0, function* () {
        const shouldCreateDirectory = vscode_1.workspace
            .getConfiguration("bloc")
            .get("newCubitTemplate.createDirectory");
        const cubitDirectoryPath = shouldCreateDirectory
            ? `${targetDirectory}/cubit`
            : targetDirectory;
        if (!fs_1.existsSync(cubitDirectoryPath)) {
            yield createDirectory(cubitDirectoryPath);
        }
        const useSealedClasses = vscode_1.workspace
            .getConfiguration("bloc")
            .get("newCubitTemplate.useSealedClasses", true);
        yield Promise.all([
            createCubitStateTemplate(cubitName, cubitDirectoryPath, type, useSealedClasses),
            createCubitTemplate(cubitName, cubitDirectoryPath, type),
        ]);
    });
}
function createDirectory(targetDirectory) {
    return new Promise((resolve, reject) => {
        mkdirp(targetDirectory, (error) => {
            if (error) {
                return reject(error);
            }
            resolve();
        });
    });
}
function createCubitStateTemplate(cubitName, targetDirectory, type, useSealedClasses) {
    const snakeCaseCubitName = changeCase.snakeCase(cubitName);
    const targetPath = `${targetDirectory}/${snakeCaseCubitName}_state.dart`;
    if (fs_1.existsSync(targetPath)) {
        throw Error(`${snakeCaseCubitName}_state.dart already exists`);
    }
    return new Promise((resolve, reject) => __awaiter(this, void 0, void 0, function* () {
        fs_1.writeFile(targetPath, templates_1.getCubitStateTemplate(cubitName, type, useSealedClasses), "utf8", (error) => {
            if (error) {
                reject(error);
                return;
            }
            resolve();
        });
    }));
}
function createCubitTemplate(cubitName, targetDirectory, type) {
    const snakeCaseCubitName = changeCase.snakeCase(cubitName);
    const targetPath = `${targetDirectory}/${snakeCaseCubitName}_cubit.dart`;
    if (fs_1.existsSync(targetPath)) {
        throw Error(`${snakeCaseCubitName}_cubit.dart already exists`);
    }
    return new Promise((resolve, reject) => __awaiter(this, void 0, void 0, function* () {
        fs_1.writeFile(targetPath, templates_1.getCubitTemplate(cubitName, type), "utf8", (error) => {
            if (error) {
                reject(error);
                return;
            }
            resolve();
        });
    }));
}
//# sourceMappingURL=new-cubit.command.js.map