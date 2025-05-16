"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.getSettings = void 0;
const vscode = require("vscode");
const getSettings = () => {
    var _a, _b, _c, _d, _e;
    const getSettingByKey = (keyName) => vscode.workspace.getConfiguration().get(`pubspec-assist.${keyName}`);
    return {
        autoAddPackage: (_a = getSettingByKey("autoAddPackage")) !== null && _a !== void 0 ? _a : true,
        sortDependencies: (_b = getSettingByKey("sortDependencies")) !== null && _b !== void 0 ? _b : false,
        useLegacySorting: (_c = getSettingByKey("useLegacySorting")) !== null && _c !== void 0 ? _c : false,
        useCaretSyntax: (_d = getSettingByKey("useCaretSyntax")) !== null && _d !== void 0 ? _d : true,
        useLegacyParser: (_e = getSettingByKey("useLegacyParser")) !== null && _e !== void 0 ? _e : false,
    };
};
exports.getSettings = getSettings;
//# sourceMappingURL=getSettings.js.map