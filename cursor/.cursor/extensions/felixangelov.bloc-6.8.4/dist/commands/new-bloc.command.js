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
exports.newBloc = void 0;
const _ = require("lodash");
const changeCase = require("change-case");
const mkdirp = require("mkdirp");
const vscode_1 = require("vscode");
const fs_1 = require("fs");
const templates_1 = require("../templates");
const utils_1 = require("../utils");
exports.newBloc = (uri) => __awaiter(void 0, void 0, void 0, function* () {
    const blocName = yield promptForBlocName();
    if (_.isNil(blocName) || blocName.trim() === "") {
        vscode_1.window.showErrorMessage("The bloc name must not be empty");
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
    const blocType = yield utils_1.getBlocType(0 /* Bloc */);
    const pascalCaseBlocName = changeCase.pascalCase(blocName);
    try {
        yield generateBlocCode(blocName, targetDirectory, blocType);
        vscode_1.window.showInformationMessage(`Successfully Generated ${pascalCaseBlocName} Bloc`);
    }
    catch (error) {
        vscode_1.window.showErrorMessage(`Error:
        ${error instanceof Error ? error.message : JSON.stringify(error)}`);
    }
});
function promptForBlocName() {
    const blocNamePromptOptions = {
        prompt: "Bloc Name",
        placeHolder: "counter",
    };
    return vscode_1.window.showInputBox(blocNamePromptOptions);
}
function promptForTargetDirectory() {
    return __awaiter(this, void 0, void 0, function* () {
        const options = {
            canSelectMany: false,
            openLabel: "Select a folder to create the bloc in",
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
function generateBlocCode(blocName, targetDirectory, type) {
    return __awaiter(this, void 0, void 0, function* () {
        const shouldCreateDirectory = vscode_1.workspace
            .getConfiguration("bloc")
            .get("newBlocTemplate.createDirectory");
        const blocDirectoryPath = shouldCreateDirectory
            ? `${targetDirectory}/bloc`
            : targetDirectory;
        if (!fs_1.existsSync(blocDirectoryPath)) {
            yield createDirectory(blocDirectoryPath);
        }
        const useSealedClasses = vscode_1.workspace
            .getConfiguration("bloc")
            .get("newBlocTemplate.useSealedClasses", true);
        yield Promise.all([
            createBlocEventTemplate(blocName, blocDirectoryPath, type, useSealedClasses),
            createBlocStateTemplate(blocName, blocDirectoryPath, type, useSealedClasses),
            createBlocTemplate(blocName, blocDirectoryPath, type),
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
function createBlocEventTemplate(blocName, targetDirectory, type, useSealedClasses) {
    const snakeCaseBlocName = changeCase.snakeCase(blocName);
    const targetPath = `${targetDirectory}/${snakeCaseBlocName}_event.dart`;
    if (fs_1.existsSync(targetPath)) {
        throw Error(`${snakeCaseBlocName}_event.dart already exists`);
    }
    return new Promise((resolve, reject) => __awaiter(this, void 0, void 0, function* () {
        fs_1.writeFile(targetPath, templates_1.getBlocEventTemplate(blocName, type, useSealedClasses), "utf8", (error) => {
            if (error) {
                reject(error);
                return;
            }
            resolve();
        });
    }));
}
function createBlocStateTemplate(blocName, targetDirectory, type, useSealedClasses) {
    const snakeCaseBlocName = changeCase.snakeCase(blocName);
    const targetPath = `${targetDirectory}/${snakeCaseBlocName}_state.dart`;
    if (fs_1.existsSync(targetPath)) {
        throw Error(`${snakeCaseBlocName}_state.dart already exists`);
    }
    return new Promise((resolve, reject) => __awaiter(this, void 0, void 0, function* () {
        fs_1.writeFile(targetPath, templates_1.getBlocStateTemplate(blocName, type, useSealedClasses), "utf8", (error) => {
            if (error) {
                reject(error);
                return;
            }
            resolve();
        });
    }));
}
function createBlocTemplate(blocName, targetDirectory, type) {
    const snakeCaseBlocName = changeCase.snakeCase(blocName);
    const targetPath = `${targetDirectory}/${snakeCaseBlocName}_bloc.dart`;
    if (fs_1.existsSync(targetPath)) {
        throw Error(`${snakeCaseBlocName}_bloc.dart already exists`);
    }
    return new Promise((resolve, reject) => __awaiter(this, void 0, void 0, function* () {
        fs_1.writeFile(targetPath, templates_1.getBlocTemplate(blocName, type), "utf8", (error) => {
            if (error) {
                reject(error);
                return;
            }
            resolve();
        });
    }));
}
//# sourceMappingURL=new-bloc.command.js.map