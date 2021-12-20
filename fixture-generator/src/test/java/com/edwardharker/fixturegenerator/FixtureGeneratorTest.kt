package com.edwardharker.fixturegenerator

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FixtureGeneratorTest {
    @Test
    fun `generates fixture`() {
        val fixtureGenerator = FixtureGenerator()

        val fixtureSpec = fixtureGenerator.generateFrom(TestClass::class)

        val actual = StringBuilder()
        fixtureSpec.writeTo(actual)

        println(actual.toString())
        assertThat(actual.toString()).isEqualTo(EXPECTED)
    }

    @Fixture
    data class TestClass(
        val nullable: String?,
        val string: String,
        val int: Int,
        val boolean: Boolean,
        val fixture: AnotherTestClass
    )

    @Fixture
    class AnotherTestClass

    private companion object {
        private val EXPECTED = """
import com.edwardharker.fixturegenerator.FixtureGeneratorTest

public object TestClassFixtures {
  public fun testClass(): FixtureGeneratorTest.TestClass =
      com.edwardharker.fixturegenerator.FixtureGeneratorTest.TestClass(
      boolean = false,
      fixture = com.edwardharker.fixturegenerator.AnotherTestClassFixtures.anotherTestClass(),
      int = 0,
      nullable = null,
      string = "",
  )
}

""".trimIndent()
    }
}
