package com.edwardharker.fixturegenerator

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toTypeName

class FixtureGeneratorKsp(
    private val logger: KSPLogger
) {

    @OptIn(KotlinPoetKspPreview::class)
    fun generateFrom(functionDeclaration: KSFunctionDeclaration): FileSpec {
        val parentDeclaration = requireNotNull(functionDeclaration.parentDeclaration)

        // Make this a CodeBlock
        val constructor = buildString {
            appendLine("${parentDeclaration.qualifiedName!!.asString()}(")
            for (parameter in functionDeclaration.parameters) {
                val memberValue = formatValue(parameter)
                appendLine("    ${parameter.name?.asString()} = $memberValue,")
            }
            appendLine(")")
        }

        val fixtureTypeName = "${parentDeclaration.simpleName.getShortName()}Fixtures"
        return FileSpec.builder(parentDeclaration.packageName.asString(), fixtureTypeName)
            .addType(
                TypeSpec.objectBuilder(fixtureTypeName)
                    .addOriginatingKSFile(functionDeclaration.containingFile!!)
                    .addFunction(
                        FunSpec.builder(buildFactoryMethodName(parentDeclaration))
                            .addCode(CodeBlock.of("return $constructor"))
                            .returns(functionDeclaration.returnType!!.resolve().toTypeName())
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun formatValue(parameter: KSValueParameter): String {
        val type = parameter.type.resolve()
        val nullable = type.isMarkedNullable
        val typeName = type.declaration.qualifiedName?.asString()
        val isFixture = isFixture(type)
        return when {
            nullable -> "null"
            typeName == "kotlin.Int" -> "0"
            typeName == "kotlin.Float" -> "0.0F"
            typeName == "kotlin.Double" -> "0.0"
            typeName == "kotlin.Short" -> "0"
            typeName == "kotlin.Long" -> "0L"
            typeName == "kotlin.Byte" -> "0"
            typeName == "kotlin.Boolean" -> "false"
            typeName == "kotlin.String" -> "\"\""
            isFixture -> buildFactoryFunctionCall(type)
            else -> throw IllegalArgumentException("Unknown property type: $type. Classes must have @Fixture")
        }
    }

    private fun buildFactoryFunctionCall(type: KSType): String {
        val qualifiedName = type.declaration.qualifiedName
        return "${qualifiedName?.getQualifier()}.${qualifiedName?.getShortName()}Fixtures" +
                ".${buildFactoryMethodName(type.declaration)}()"
    }

    private fun buildFactoryMethodName(type: KSDeclaration) =
        type.simpleName.getShortName().replaceFirstChar { it.lowercaseChar() }

    private fun isFixture(type: KSType): Boolean {
        val annotations = type.declaration.annotations
        return annotations.any { annotation ->
            annotation.annotationType.resolve()
                .declaration.qualifiedName?.asString() == Fixture::class.qualifiedName
        }

    }
}