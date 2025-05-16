"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.getFreezedCubitTemplate = exports.getCubitTemplate = void 0;
const changeCase = require("change-case");
function getCubitTemplate(cubitName, type) {
    switch (type) {
        case 2 /* Freezed */:
            return getFreezedCubitTemplate(cubitName);
        case 1 /* Equatable */:
            return getEquatableCubitTemplate(cubitName);
        default:
            return getDefaultCubitTemplate(cubitName);
    }
}
exports.getCubitTemplate = getCubitTemplate;
function getEquatableCubitTemplate(cubitName) {
    const pascalCaseCubitName = changeCase.pascalCase(cubitName);
    const snakeCaseCubitName = changeCase.snakeCase(cubitName);
    const cubitState = `${pascalCaseCubitName}State`;
    return `import 'package:bloc/bloc.dart';
import 'package:equatable/equatable.dart';

part '${snakeCaseCubitName}_state.dart';

class ${pascalCaseCubitName}Cubit extends Cubit<${cubitState}> {
  ${pascalCaseCubitName}Cubit() : super(${pascalCaseCubitName}Initial());
}
`;
}
function getDefaultCubitTemplate(cubitName) {
    const pascalCaseCubitName = changeCase.pascalCase(cubitName);
    const snakeCaseCubitName = changeCase.snakeCase(cubitName);
    const cubitState = `${pascalCaseCubitName}State`;
    return `import 'package:bloc/bloc.dart';
import 'package:meta/meta.dart';

part '${snakeCaseCubitName}_state.dart';

class ${pascalCaseCubitName}Cubit extends Cubit<${cubitState}> {
  ${pascalCaseCubitName}Cubit() : super(${pascalCaseCubitName}Initial());
}
`;
}
function getFreezedCubitTemplate(cubitName) {
    const pascalCaseCubitName = changeCase.pascalCase(cubitName);
    const snakeCaseCubitName = changeCase.snakeCase(cubitName);
    const cubitState = `${pascalCaseCubitName}State`;
    return `import 'package:bloc/bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

part '${snakeCaseCubitName}_state.dart';
part '${snakeCaseCubitName}_cubit.freezed.dart';

class ${pascalCaseCubitName}Cubit extends Cubit<${cubitState}> {
  ${pascalCaseCubitName}Cubit() : super(${pascalCaseCubitName}State.initial());
}
`;
}
exports.getFreezedCubitTemplate = getFreezedCubitTemplate;
//# sourceMappingURL=cubit.template.js.map