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
exports.convertToMultiRepositoryProvider = exports.convertToMultiBlocListener = exports.convertToMultiBlocProvider = void 0;
const utils_1 = require("../utils");
const multiBlocProviderSnippet = (widget, child) => {
    return `MultiBlocProvider(
    providers: [
        ${widget},
        BlocProvider(
            create: (context) => \${1:Subject}\${2|Bloc,Cubit|}(),
        ),
    ],
    ${child}
)`;
};
const multiBlocListenerSnippet = (widget, child) => {
    return `MultiBlocListener(
    listeners: [
        ${widget},
        BlocListener<\${1:Subject}\${2|Bloc,Cubit|}, \$1State>(
            listener: (context, state) {
                \${4:// TODO: implement listener}
            },
        ),
    ],
    ${child}
)`;
};
const multiRepositoryProviderSnippet = (widget, child) => {
    return `MultiRepositoryProvider(
    providers: [
        ${widget},
        RepositoryProvider(
            create: (context) => \${1:Subject}Repository(),
        ),
    ],
    ${child}
)`;
};
exports.convertToMultiBlocProvider = () => __awaiter(void 0, void 0, void 0, function* () { return utils_1.convertTo(multiBlocProviderSnippet); });
exports.convertToMultiBlocListener = () => __awaiter(void 0, void 0, void 0, function* () { return utils_1.convertTo(multiBlocListenerSnippet); });
exports.convertToMultiRepositoryProvider = () => __awaiter(void 0, void 0, void 0, function* () { return utils_1.convertTo(multiRepositoryProviderSnippet); });
//# sourceMappingURL=convert-to.command.js.map