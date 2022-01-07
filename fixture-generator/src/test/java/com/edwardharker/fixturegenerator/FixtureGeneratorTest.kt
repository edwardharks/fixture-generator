package com.edwardharker.fixturegenerator

import com.edwardharker.fixturegenerator.ksp.FakeKSDeclaration
import com.edwardharker.fixturegenerator.ksp.FakeKSFunctionDeclaration
import com.edwardharker.fixturegenerator.ksp.FakeKSPLogger
import com.google.devtools.ksp.symbol.Modifier.PRIVATE
import com.google.devtools.ksp.symbol.Modifier.PUBLIC
import org.junit.Test

internal class FixtureGeneratorTest {

    private val fixtureGenerator = FixtureGenerator(FakeKSPLogger())

    @Test(expected = IllegalArgumentException::class)
    fun `throws when constructor is private`() {
        val constructorDeclaration = FakeKSFunctionDeclaration(
            modifiers = setOf(PRIVATE),
        )

        fixtureGenerator.generateFrom(constructorDeclaration)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws when class is private`() {
        val constructorDeclaration = FakeKSFunctionDeclaration(
            parentDeclaration = FakeKSDeclaration(
                modifiers = setOf(PRIVATE),
            ),
            modifiers = setOf(PUBLIC)
        )

        fixtureGenerator.generateFrom(constructorDeclaration)
    }
}
