"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.getBlocStateTemplate = void 0;
const changeCase = require("change-case");
function getBlocStateTemplate(blocName, type, useSealedClasses) {
    switch (type) {
        case 2 /* Freezed */:
            return getFreezedBlocStateTemplate(blocName);
        case 1 /* Equatable */:
            return getEquatableBlocStateTemplate(blocName, useSealedClasses);
        default:
            return getDefaultBlocStateTemplate(blocName, useSealedClasses);
    }
}
exports.getBlocStateTemplate = getBlocStateTemplate;
function getEquatableBlocStateTemplate(blocName, useSealedClasses) {
    const classPrefix = useSealedClasses ? "sealed" : "abstract";
    const subclassPrefix = useSealedClasses ? "final " : "";
    const pascalCaseBlocName = changeCase.pascalCase(blocName);
    const snakeCaseBlocName = changeCase.snakeCase(blocName);
    return `part of '${snakeCaseBlocName}_bloc.dart';

${classPrefix} class ${pascalCaseBlocName}State extends Equatable {
  const ${pascalCaseBlocName}State();
  
  @override
  List<Object> get props => [];
}

${subclassPrefix}class ${pascalCaseBlocName}Initial extends ${pascalCaseBlocName}State {}
`;
}
function getDefaultBlocStateTemplate(blocName, useSealedClasses) {
    const classPrefix = useSealedClasses ? "sealed" : "abstract";
    const subclassPrefix = useSealedClasses ? "final " : "";
    const pascalCaseBlocName = changeCase.pascalCase(blocName);
    const snakeCaseBlocName = changeCase.snakeCase(blocName);
    return `part of '${snakeCaseBlocName}_bloc.dart';

@immutable
${classPrefix} class ${pascalCaseBlocName}State {}

${subclassPrefix}class ${pascalCaseBlocName}Initial extends ${pascalCaseBlocName}State {}
`;
}
function getFreezedBlocStateTemplate(blocName) {
    const pascalCaseBlocName = changeCase.pascalCase(blocName) + "State";
    const snakeCaseBlocName = changeCase.snakeCase(blocName);
    return `part of '${snakeCaseBlocName}_bloc.dart';

@freezed
class ${pascalCaseBlocName} with _\$${pascalCaseBlocName} {
  const factory ${pascalCaseBlocName}.initial() = _Initial;
}
`;
}
//# sourceMappingURL=bloc-state.template.js.map