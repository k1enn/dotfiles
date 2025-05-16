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
exports.wrapWithRepositoryProvider = exports.wrapWithBlocProvider = exports.wrapWithBlocConsumer = exports.wrapWithBlocListener = exports.wrapWithBlocSelector = exports.wrapWithBlocBuilder = void 0;
const utils_1 = require("../utils");
const blocBuilderSnippet = (widget) => {
    return `BlocBuilder<\${1:Subject}\${2|Bloc,Cubit|}, $1State>(
  builder: (context, state) {
    return ${widget};
  },
)`;
};
const blocSelectorSnippet = (widget) => {
    return `BlocSelector<\${1:Subject}\${2|Bloc,Cubit|}, $1State, \${3:SelectedState}>(
  selector: (state) {
    return \${4:state};
  },
  builder: (context, state) {
    return ${widget};
  },
)`;
};
const blocListenerSnippet = (widget) => {
    return `BlocListener<\${1:Subject}\${2|Bloc,Cubit|}, $1State>(
  listener: (context, state) {
    \${3:// TODO: implement listener}
  },
  child: ${widget},
)`;
};
const blocProviderSnippet = (widget) => {
    return `BlocProvider(
  create: (context) => \${1:Subject}\${2|Bloc,Cubit|}(),
  child: ${widget},
)`;
};
const blocConsumerSnippet = (widget) => {
    return `BlocConsumer<\${1:Subject}\${2|Bloc,Cubit|}, $1State>(
  listener: (context, state) {
    \${3:// TODO: implement listener}
  },
  builder: (context, state) {
    return ${widget};
  },
)`;
};
const repositoryProviderSnippet = (widget) => {
    return `RepositoryProvider(
  create: (context) => \${1:Subject}Repository(),
    child: ${widget},
)`;
};
exports.wrapWithBlocBuilder = () => __awaiter(void 0, void 0, void 0, function* () { return utils_1.wrapWith(blocBuilderSnippet); });
exports.wrapWithBlocSelector = () => __awaiter(void 0, void 0, void 0, function* () { return utils_1.wrapWith(blocSelectorSnippet); });
exports.wrapWithBlocListener = () => __awaiter(void 0, void 0, void 0, function* () { return utils_1.wrapWith(blocListenerSnippet); });
exports.wrapWithBlocConsumer = () => __awaiter(void 0, void 0, void 0, function* () { return utils_1.wrapWith(blocConsumerSnippet); });
exports.wrapWithBlocProvider = () => __awaiter(void 0, void 0, void 0, function* () { return utils_1.wrapWith(blocProviderSnippet); });
exports.wrapWithRepositoryProvider = () => __awaiter(void 0, void 0, void 0, function* () { return utils_1.wrapWith(repositoryProviderSnippet); });
//# sourceMappingURL=wrap-with.command.js.map