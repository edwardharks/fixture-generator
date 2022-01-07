package com.edwardharker.fixturegenerator.ksp

import com.google.devtools.ksp.symbol.*

class FakeKSFunctionDeclaration(
    override val parentDeclaration: KSDeclaration? = null,
    override val modifiers: Set<Modifier> = emptySet(),
) : KSFunctionDeclaration {
    override val annotations: Sequence<KSAnnotation>
        get() = TODO("Not yet implemented")
    override val containingFile: KSFile?
        get() = TODO("Not yet implemented")
    override val declarations: Sequence<KSDeclaration>
        get() = TODO("Not yet implemented")
    override val docString: String?
        get() = TODO("Not yet implemented")
    override val extensionReceiver: KSTypeReference?
        get() = TODO("Not yet implemented")
    override val functionKind: FunctionKind
        get() = TODO("Not yet implemented")
    override val isAbstract: Boolean
        get() = TODO("Not yet implemented")
    override val isActual: Boolean
        get() = TODO("Not yet implemented")
    override val isExpect: Boolean
        get() = TODO("Not yet implemented")
    override val location: Location
        get() = TODO("Not yet implemented")
    override val origin: Origin
        get() = TODO("Not yet implemented")
    override val packageName: KSName
        get() = TODO("Not yet implemented")
    override val parameters: List<KSValueParameter>
        get() = TODO("Not yet implemented")
    override val parent: KSNode?
        get() = TODO("Not yet implemented")
    override val qualifiedName: KSName?
        get() = TODO("Not yet implemented")
    override val returnType: KSTypeReference?
        get() = TODO("Not yet implemented")
    override val simpleName: KSName
        get() = TODO("Not yet implemented")
    override val typeParameters: List<KSTypeParameter>
        get() = TODO("Not yet implemented")

    override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D): R {
        TODO("Not yet implemented")
    }

    override fun asMemberOf(containing: KSType): KSFunction {
        TODO("Not yet implemented")
    }

    override fun findActuals(): Sequence<KSDeclaration> {
        TODO("Not yet implemented")
    }

    override fun findExpects(): Sequence<KSDeclaration> {
        TODO("Not yet implemented")
    }

    override fun findOverridee(): KSDeclaration? {
        TODO("Not yet implemented")
    }
}