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
exports.installBlocTools = exports.BLOC_TOOLS_VERSION = void 0;
const _1 = require(".");
exports.BLOC_TOOLS_VERSION = "0.1.0-dev.11";
exports.installBlocTools = () => __awaiter(void 0, void 0, void 0, function* () {
    const canInstall = yield _1.isDartInstalled();
    if (!canInstall)
        return false;
    try {
        yield _1.exec(`dart pub global activate bloc_tools ${exports.BLOC_TOOLS_VERSION}`);
        return true;
    }
    catch (_) {
        return false;
    }
});
//# sourceMappingURL=install-bloc-tools.js.map