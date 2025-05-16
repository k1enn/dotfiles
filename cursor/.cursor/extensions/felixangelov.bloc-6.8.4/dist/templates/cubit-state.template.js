"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.getCubitStateTemplate = void 0;
const changeCase = require("change-case");
function getCubitStateTemplate(cubitName, type, useSealedClasses) {
    switch (type) {
        case 2 /* Freezed */:
            return getFreezedCubitStateTemplate(cubitName);
        case 1 /* Equatable */:
            return getEquatableCubitStateTemplate(cubitName, useSealedClasses);
        default:
            return getDefaultCubitStateTemplate(cubitName, useSealedClasses);
    }
}
exports.getCubitStateTemplate = getCubitStateTemplate;
function getEquatableCubitStateTemplate(cubitName, useSealedClasses) {
    const classPrefix = useSealedClasses ? "sealed" : "abstract";
    const subclassPrefix = useSealedClasses ? "final " : "";
    const pascalCaseCubitName = changeCase.pascalCase(cubitName);
    const snakeCaseCubitName = changeCase.snakeCase(cubitName);
    return `part of '${snakeCaseCubitName}_cubit.dart';

${classPrefix} class ${pascalCaseCubitName}State extends Equatable {
  const ${pascalCaseCubitName}State();

  @override
  List<Object> get props => [];
}

${subclassPrefix}class ${pascalCaseCubitName}Initial extends ${pascalCaseCubitName}State {}
`;
}
function getDefaultCubitStateTemplate(cubitName, useSealedClasses) {
    const classPrefix = useSealedClasses ? "sealed" : "abstract";
    const subclassPrefix = useSealedClasses ? "final " : "";
    const pascalCaseCubitName = changeCase.pascalCase(cubitName);
    const snakeCaseCubitName = changeCase.snakeCase(cubitName);
    return `part of '${snakeCaseCubitName}_cubit.dart';

@immutable
${classPrefix} class ${pascalCaseCubitName}State {}

${subclassPrefix}class ${pascalCaseCubitName}Initial extends ${pascalCaseCubitName}State {}
`;
}
function getFreezedCubitStateTemplate(cubitName) {
    const pascalCaseCubitName = changeCase.pascalCase(cubitName);
    const snakeCaseCubitName = changeCase.snakeCase(cubitName);
    return `part of '${snakeCaseCubitName}_cubit.dart';

@freezed
class ${pascalCaseCubitName}State with _\$${pascalCaseCubitName}State {
  const factory ${pascalCaseCubitName}State.initial() = _Initial;
}
`;
}
//# sourceMappingURL=cubit-state.template.js.map