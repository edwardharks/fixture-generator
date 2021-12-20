package com.edwardharker.fixturegenerator

import com.squareup.kotlinpoet.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class FixtureGenerator {

    fun generateFrom(testDataClass: KClass<*>): FileSpec {
        val type = testDataClass.asTypeName()
        val className = type.simpleName

        // Make this a CodeBlock
        val constructor = buildString {
            appendLine("${type.canonicalName}(")
            for (member in testDataClass.members) {
                if (member is KProperty) {
                    val memberValue = formatValue(member)
                    appendLine("    ${member.name} = $memberValue,")
                }
            }
            appendLine(")")
        }

        val fixtureTypeName = "${className}Fixtures"
        return FileSpec.builder("", fixtureTypeName)
            .addType(
                TypeSpec.objectBuilder(fixtureTypeName)
                    .addFunction(
                        FunSpec.builder(buildFactoryMethodName(type))
                            .addCode(CodeBlock.of("return $constructor"))
                            .returns(testDataClass)
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun buildFactoryMethodName(type: ClassName) =
        type.simpleName.replaceFirstChar { it.lowercaseChar() }

    private fun formatValue(value: KProperty<*>): String {
        val type = value.returnType.asTypeName() as ClassName
        val nullable = type.isNullable
        val typeName = type.toString()
        val isFixture = isFixture(type)
        return when {
            nullable -> "null"
            typeName == "kotlin.Int" -> "0"
            typeName == "kotlin.Boolean" -> "false"
            typeName == "kotlin.String" -> "\"\""
            isFixture -> buildFactoryFunctionCall(type)
            else -> throw IllegalArgumentException("Unknown property type: $type. Classes must have @Fixture")
        }
    }

    private fun buildFactoryFunctionCall(type: ClassName) =
        "${type.packageName}.${type.simpleName}Fixtures.${buildFactoryMethodName(type)}()"

    private fun isFixture(className: ClassName): Boolean {
        val clazz = try {
            Class.forName(className.reflectionName()).kotlin
        } catch (ignored: ClassNotFoundException) {
            return false
        }

        return clazz.annotations.contains(Fixture())
    }
}