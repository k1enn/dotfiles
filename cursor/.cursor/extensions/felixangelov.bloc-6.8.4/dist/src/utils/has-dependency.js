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
exports.hasDependency = void 0;
const _ = require("lodash");
const _1 = require(".");
function hasDependency(dependency) {
    return __awaiter(this, void 0, void 0, function* () {
        const pubspec = yield _1.getPubspec();
        const dependencies = _.get(pubspec, "dependencies", {});
        return _.has(dependencies, dependency);
    });
}
exports.hasDependency = hasDependency;
//# sourceMappingURL=has-dependency.js.map