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
exports.getValue = void 0;
const pubError_1 = require("../model/pubError");
const messaging_1 = require("./messaging");
const getValue = (f) => __awaiter(void 0, void 0, void 0, function* () {
    let value;
    let tryAgain = true;
    while (tryAgain) {
        try {
            value = f();
            tryAgain = false;
        }
        catch (error) {
            if (error instanceof pubError_1.PubApiNotRespondingError) {
                tryAgain = yield (0, messaging_1.showRetryableError)(error);
            }
            else {
                (0, messaging_1.handleCriticalError)(error);
                tryAgain = false;
            }
        }
    }
    return value;
});
exports.getValue = getValue;
//# sourceMappingURL=getValue.js.map