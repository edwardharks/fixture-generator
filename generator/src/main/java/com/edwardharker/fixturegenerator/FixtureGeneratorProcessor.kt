package com.edwardharker.fixturegenerator

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.kspDependencies
import com.squareup.kotlinpoet.ksp.writeTo
import java.util.*

class FixtureProcessor(
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {
    private val fixtureTypeGenerator = FixtureTypeGenerator()
    private val types = mutableMapOf<TypeName, FixtureType>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Fixture::class.qualifiedName!!)
        val ret = symbols.filter { !it.validate() }.toList()
        symbols
            .filter { it is KSClassDeclaration && it.validate() }
            .forEach { it.accept(FixtureVisitor(), Unit) }

        return ret
    }

    @OptIn(KotlinPoetKspPreview::class)
    override fun finish() {
        for ((typeName, fixtureType) in types) {
            val declaration = fixtureType.declaration
            val packageName = declaration.packageName.asString()
            val typeSpecBuilder = fixtureType.createTypeSpecBuilder(typeName)

            addChildTypesToTypeSpec(typeSpecBuilder, fixtureType.children)

            val typeSpec = typeSpecBuilder.build()
            val fileSpec = FileSpec.builder(packageName, typeSpec.name!!)
                .addType(typeSpec)
                .build()
            fileSpec.writeTo(
                codeGenerator,
                fileSpec.kspDependencies(aggregating = false)
            )
        }
    }

    private fun addChildTypesToTypeSpec(outerTypeSpecBuilder: TypeSpec.Builder, children: Map<TypeName, FixtureType>) {
        for ((typeName, fixtureType) in children) {
            val typeSpecBuilder = fixtureType.createTypeSpecBuilder(typeName)
            addChildTypesToTypeSpec(typeSpecBuilder, fixtureType.children)
            outerTypeSpecBuilder.addType(typeSpecBuilder.build())
        }
    }

    inner class FixtureVisitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            when (classDeclaration.classKind) {
                ClassKind.CLASS, ClassKind.ENUM_CLASS -> classDeclaration.primaryConstructor!!.accept(this, data)
                ClassKind.OBJECT -> {
                    val typeHierarchy = buildTypesHierarchy(classDeclaration)
                    val typeSpec = fixtureTypeGenerator.generateFromObject(classDeclaration)
                    addTypeSpecToFixtureType(types, typeHierarchy, typeSpec, classDeclaration)
                }
                ClassKind.INTERFACE -> throw IllegalArgumentException("Interfaces cannot be annotated with @Fixture")
                ClassKind.ENUM_ENTRY -> TODO()
                ClassKind.ANNOTATION_CLASS -> TODO()
            }
        }

        @OptIn(KotlinPoetKspPreview::class)
        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
            val typeHierarchy = buildTypesHierarchy(function)
            val typeSpec = fixtureTypeGenerator.generateFromConstructor(function)
            addTypeSpecToFixtureType(types, typeHierarchy, typeSpec, function)
        }
    }

    private fun addTypeSpecToFixtureType(
        types: MutableMap<String, FixtureType>,
        typeHierarchy: Deque<String>,
        typeSpec: TypeSpec,
        declaration: KSDeclaration
    ) {
        val type = typeHierarchy.pop()
        var fixtureType = types[type]
        if (fixtureType == null) {
            fixtureType = FixtureType(declaration)
            types[type] = fixtureType
        }
        if (typeHierarchy.isEmpty()) {
            fixtureType.typeSpec = typeSpec
        } else {
            addTypeSpecToFixtureType(fixtureType.children, typeHierarchy, typeSpec, declaration)
        }
    }

    private fun buildTypesHierarchy(declaration: KSDeclaration): Deque<String> {
        val typeHierarchy = LinkedList<String>()
        if (declaration is KSClassDeclaration) {
            typeHierarchy.push(declaration.simpleName.asString())
        }
        var parent = declaration.parent
        while (parent != null && parent is KSDeclaration) {
            typeHierarchy.push(parent.simpleName.asString())
            parent = parent.parent
        }
        return typeHierarchy
    }

    private fun FixtureType.createTypeSpecBuilder(typeName: String): TypeSpec.Builder {
        return typeSpec?.toBuilder()
            ?: fixtureTypeGenerator.generateEmptyFixtureType(
                typeName = typeName,
                containingFile = declaration.containingFile!!,
                classVisibility = KModifier.PUBLIC,
            ).toBuilder()
    }
}

class FixtureProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return FixtureProcessor(
            environment.codeGenerator,
        )
    }
}

private typealias TypeName = String

private data class FixtureType(
    val declaration: KSDeclaration,
    var typeSpec: TypeSpec? = null,
    var children: MutableMap<TypeName, FixtureType> = mutableMapOf(),
)
