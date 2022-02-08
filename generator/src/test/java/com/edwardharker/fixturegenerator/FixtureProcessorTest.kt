package com.edwardharker.fixturegenerator

import com.google.common.truth.StringSubject
import com.google.common.truth.Truth.assertThat
import com.tschuchort.compiletesting.*
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class FixtureProcessorTest {
    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun `generates file`() {
        val compilation = prepareCompilation(
            kotlin(
                "Example.kt",
                """
package test
import com.edwardharker.fixturegenerator.Fixture
@Fixture
class ExampleClass()
                """.trimIndent()
            )
        )

        val generatedFileText = compilation.compileAndGetFileText()

        assertThat(generatedFileText).isEqualToKotlin(
            """
package test

public object ExampleClassFixtures {
  public fun exampleClass(): ExampleClass = test.ExampleClass(
  )
}

        """.trimIndent()
        )
    }

    @Test
    fun `nullable types are null`() {
        val compilation = prepareCompilation(
            kotlin(
                "Example.kt",
                """
package test
import com.edwardharker.fixturegenerator.Fixture
@Fixture
class ExampleClass(val nullable: String?)
                """.trimIndent()
            )
        )

        val generatedFileText = compilation.compileAndGetFileText()

        assertThat(generatedFileText).isEqualToKotlin(
            """
package test

public object ExampleClassFixtures {
  public fun exampleClass(): ExampleClass = test.ExampleClass(
      nullable = null,
  )
}

        """.trimIndent()
        )
    }

    @Test
    fun `primitive types are default`() {
        val compilation = prepareCompilation(
            kotlin(
                "Example.kt",
                """
package test
import com.edwardharker.fixturegenerator.Fixture
@Fixture
class ExampleClass(
    val string: String,
    val char: Char,
    val int: Int,
    val float: Float,
    val double: Double,
    val short: Short,
    val long: Long,
    val byte: Byte,
    val boolean: Boolean,
)
                """.trimIndent()
            )
        )

        val generatedFileText = compilation.compileAndGetFileText()

        assertThat(generatedFileText).isEqualToKotlin(
            """
package test

public object ExampleClassFixtures {
  public fun exampleClass(): ExampleClass = test.ExampleClass(
      string = "",
      char = '\u0000',
      int = 0,
      float = 0.0F,
      double = 0.0,
      short = 0,
      long = 0L,
      byte = 0,
      boolean = false,
  )
}

        """.trimIndent()
        )
    }

    @Test
    fun `collection types are empty`() {
        val compilation = prepareCompilation(
            kotlin(
                "Example.kt",
                """
package test
import com.edwardharker.fixturegenerator.Fixture
@Fixture
class ExampleClass(
    val list: List<String>,
    val map: Map<String, String>,
    val set: Set<String>,
)
                """.trimIndent()
            )
        )

        val generatedFileText = compilation.compileAndGetFileText()

        assertThat(generatedFileText).isEqualToKotlin(
            """
package test

public object ExampleClassFixtures {
  public fun exampleClass(): ExampleClass = test.ExampleClass(
      list = emptyList(),
      map = emptyMap(),
      set = emptySet(),
  )
}

        """.trimIndent()
        )
    }

    @Test
    fun `array types are empty`() {
        val compilation = prepareCompilation(
            kotlin(
                "Example.kt",
                """
package test
import com.edwardharker.fixturegenerator.Fixture
@Fixture
class ExampleClass(
    val array: Array<String>,
    val intArray: IntArray,
    val charArray: CharArray,
    val floatArray: FloatArray,
    val doubleArray: DoubleArray,
    val shortArray: ShortArray,
    val longArray: LongArray,
    val byteArray: ByteArray,
    val booleanArray: BooleanArray,
)
                """.trimIndent()
            )
        )

        val generatedFileText = compilation.compileAndGetFileText()

        assertThat(generatedFileText).isEqualToKotlin(
            """
package test

public object ExampleClassFixtures {
  public fun exampleClass(): ExampleClass = test.ExampleClass(
      array = emptyArray(),
      intArray = intArrayOf(),
      charArray = charArrayOf(),
      floatArray = floatArrayOf(),
      doubleArray = doubleArrayOf(),
      shortArray = shortArrayOf(),
      longArray = longArrayOf(),
      byteArray = byteArrayOf(),
      booleanArray = booleanArrayOf(),
  )
}

        """.trimIndent()
        )
    }

    @Test
    fun `picks first enum value`() {
        val compilation = prepareCompilation(
            kotlin(
                "Example.kt",
                """
package test
import com.edwardharker.fixturegenerator.Fixture
@Fixture
class ExampleClass(val enum: ExampleEnum)
enum class ExampleEnum {
    FIRST, SECOND
}
                """.trimIndent()
            )
        )

        val generatedFileText = compilation.compileAndGetFileText()

        assertThat(generatedFileText).isEqualToKotlin(
            """
package test

public object ExampleClassFixtures {
  public fun exampleClass(): ExampleClass = test.ExampleClass(
      enum = test.ExampleEnum.FIRST,
  )
}

        """.trimIndent()
        )
    }

    @Test
    fun `throws error when enum has no values`() {
        val compilation = prepareCompilation(
            kotlin(
                "Example.kt",
                """
package test
import com.edwardharker.fixturegenerator.Fixture
@Fixture
class ExampleClass(val enum: ExampleEnum)
enum class ExampleEnum
                """.trimIndent()
            )
        )

        val result = compilation.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("Enum has no values")
    }

    @Language("kotlin")
    private fun KotlinCompilation.compileAndGetFileText(): String {
        val result = compile()
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        return File(kspSourcesDir, "kotlin/test/ExampleClassFixtures.kt").readText()
    }

    private fun StringSubject.isEqualToKotlin(@Language("kotlin") expected: String) = isEqualTo(expected)

    private fun prepareCompilation(vararg sourceFiles: SourceFile): KotlinCompilation {
        return KotlinCompilation()
            .apply {
                workingDir = temporaryFolder.root
                inheritClassPath = true
                symbolProcessorProviders = listOf(FixtureProcessorProvider())
                sources = sourceFiles.asList()
                verbose = false
                kspIncremental = true
            }
    }
}