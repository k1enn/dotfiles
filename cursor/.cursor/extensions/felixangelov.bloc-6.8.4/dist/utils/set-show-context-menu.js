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
exports.setShowContextMenu = void 0;
const yaml = require("js-yaml");
const _ = require("lodash");
const vscode_1 = require("vscode");
function setShowContextMenu(pubspec) {
    return __awaiter(this, void 0, void 0, function* () {
        function pubspecIncludesBloc(pubspec) {
            return __awaiter(this, void 0, void 0, function* () {
                try {
                    const content = yield vscode_1.workspace.fs.readFile(pubspec);
                    const yamlContent = yaml.load(content.toString());
                    const dependencies = _.get(yamlContent, "dependencies", {});
                    return [
                        "angular_bloc",
                        "bloc",
                        "flutter_bloc",
                        "hydrated_bloc",
                        "replay_bloc",
                    ].some((d) => dependencies.hasOwnProperty(d));
                }
                catch (_) { }
                return false;
            });
        }
        function workspaceIncludesBloc() {
            return __awaiter(this, void 0, void 0, function* () {
                try {
                    const pubspecs = yield vscode_1.workspace.findFiles("**/**/pubspec.yaml");
                    for (const pubspec of pubspecs) {
                        if (yield pubspecIncludesBloc(pubspec)) {
                            return true;
                        }
                    }
                }
                catch (_) { }
                return false;
            });
        }
        vscode_1.commands.executeCommand("setContext", "bloc.showContextMenu", pubspec
            ? yield pubspecIncludesBloc(pubspec)
            : yield workspaceIncludesBloc());
    });
}
exports.setShowContextMenu = setShowContextMenu;
//# sourceMappingURL=set-show-context-menu.js.map