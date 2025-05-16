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
exports.getDartVersion = void 0;
const exec_1 = require("./exec");
const semver = require("semver");
exports.getDartVersion = () => __awaiter(void 0, void 0, void 0, function* () {
    var _a, _b;
    try {
        const result = yield exec_1.exec("dart --version");
        // Dart SDK version: 3.7.2 (stable) (Tue Mar 11 04:27:50 2025 -0700) on "macos_arm64"
        const output = result.trim();
        // Parse "major.minor.patch"
        const regexp = new RegExp(/\d+\.\d+\.\d+/);
        const versionString = (_b = (_a = output.match(regexp)) === null || _a === void 0 ? void 0 : _a[0]) !== null && _b !== void 0 ? _b : null;
        return semver.parse(versionString);
    }
    catch (_) {
        return null;
    }
});
//# sourceMappingURL=get-dart-version.js.map