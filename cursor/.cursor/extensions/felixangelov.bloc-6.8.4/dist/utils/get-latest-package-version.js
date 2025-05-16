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
exports.getLatestPackageVersion = void 0;
const _ = require("lodash");
const node_fetch_1 = require("node-fetch");
function getLatestPackageVersion(name) {
    return __awaiter(this, void 0, void 0, function* () {
        try {
            const url = `https://pub.dev/api/packages/${name}`;
            const response = yield node_fetch_1.default(url);
            const body = yield response.json();
            return _.get(body, "latest.version", "");
        }
        catch (_) {
            return "";
        }
    });
}
exports.getLatestPackageVersion = getLatestPackageVersion;
//# sourceMappingURL=get-latest-package-version.js.map