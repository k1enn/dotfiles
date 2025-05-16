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
exports.getPubspecLock = exports.getPubspec = void 0;
const yaml = require("js-yaml");
const get_pubspec_path_1 = require("./get-pubspec-path");
const vscode_1 = require("vscode");
function getPubspec() {
    return __awaiter(this, void 0, void 0, function* () {
        const pubspecPath = get_pubspec_path_1.getPubspecPath();
        return getYAMLFileContent(pubspecPath);
    });
}
exports.getPubspec = getPubspec;
function getPubspecLock() {
    return __awaiter(this, void 0, void 0, function* () {
        const pubspecLockPath = get_pubspec_path_1.getPubspecLockPath();
        return getYAMLFileContent(pubspecLockPath);
    });
}
exports.getPubspecLock = getPubspecLock;
function getYAMLFileContent(path) {
    return __awaiter(this, void 0, void 0, function* () {
        if (path) {
            try {
                let content = yield vscode_1.workspace.fs.readFile(vscode_1.Uri.file(path));
                return yaml.load(content.toString());
            }
            catch (_) { }
        }
    });
}
//# sourceMappingURL=get-pubspec.js.map