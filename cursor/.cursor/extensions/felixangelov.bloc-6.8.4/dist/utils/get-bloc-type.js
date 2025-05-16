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
exports.getBlocType = void 0;
const has_dependency_1 = require("./has-dependency");
const get_template_setting_1 = require("./get-template-setting");
const equatable = "equatable";
const freezed_annotation = "freezed_annotation";
function getBlocType(type) {
    return __awaiter(this, void 0, void 0, function* () {
        const setting = get_template_setting_1.getTemplateSetting(type);
        switch (setting) {
            case 2 /* Freezed */:
                return 2 /* Freezed */;
            case 1 /* Equatable */:
                return 1 /* Equatable */;
            case 3 /* Simple */:
                return 0 /* Simple */;
            case 0 /* Auto */:
            default:
                return getDefaultDependency();
        }
    });
}
exports.getBlocType = getBlocType;
function getDefaultDependency() {
    return __awaiter(this, void 0, void 0, function* () {
        if (yield has_dependency_1.hasDependency(freezed_annotation)) {
            return 2 /* Freezed */;
        }
        else if (yield has_dependency_1.hasDependency(equatable)) {
            return 1 /* Equatable */;
        }
        else {
            return 0 /* Simple */;
        }
    });
}
//# sourceMappingURL=get-bloc-type.js.map