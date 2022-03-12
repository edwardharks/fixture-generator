package solution.littlebird.fixturegenerator

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ExampleTest {

    @Test
    fun `generates primitive fixtures`() {
        val expected = PrimitiveExamples(
            nullable = null,
            string = "",
            char = '\u0000',
            int = 0,
            short = 0,
            long = 0L,
            double = 0.0,
            float = 0.0F,
            byte = 0,
            boolean = false,
        )

        val actual = PrimitiveExamplesFixtures.primitiveExamples()

        assertThat(expected).isEqualTo(actual)
    }

    @Test
    fun `generates fixture fixtures`() {
        val expected = FixtureExamples(
            fixture = FixtureExampleClass(
                example = "",
            )
        )

        val actual = FixtureExamplesFixtures.fixtureExamples()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `generates collection fixtures`() {
        val expected = CollectionExamples(
            list = emptyList(),
            map = emptyMap(),
            set = emptySet()
        )

        val actual = CollectionExamplesFixtures.collectionExamples()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `generates array fixtures`() {
        val expected = ArrayExamples(
            array = emptyArray(),
            intArray = intArrayOf(),
            floatArray = floatArrayOf(),
            doubleArray = doubleArrayOf(),
            shortArray = shortArrayOf(),
            longArray = longArrayOf(),
            byteArray = byteArrayOf(),
            booleanArray = booleanArrayOf(),
            charArray = charArrayOf(),
        )

        val actual = ArrayExamplesFixtures.arrayExamples()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `generates enum fixtures`() {
        val expected = EnumExample.MONDAY

        val actual = EnumExampleFixtures.enumExample()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `generates object fixtures`() {
        val expected = ExampleObject

        val actual = ExampleObjectFixtures.exampleObject()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `generated nested class fixtures`() {
        val expectedOuter = ExampleOuterClass("")
        val expectedInner = ExampleOuterClass.ExampleInnerClass("")
        val expectedAnotherInner = ExampleOuterClass.ExampleInnerClass.AnotherExampleInnerClass("")

        val actualOuter = ExampleOuterClassFixtures.exampleOuterClass()
        val actualInner = ExampleOuterClassFixtures.ExampleInnerClassFixtures.exampleInnerClass()
        val actualAnotherInner =
            ExampleOuterClassFixtures.ExampleInnerClassFixtures.AnotherExampleInnerClassFixtures.anotherExampleInnerClass()

        assertThat(actualOuter).isEqualTo(expectedOuter)
        assertThat(actualInner).isEqualTo(expectedInner)
        assertThat(expectedAnotherInner).isEqualTo(actualAnotherInner)
    }

    @Test
    fun `generated nested class fixtures when not all classes are annotated`() {
        val expected = ExampleOuterClassNotAnnotated.ExampleInnerClassNotAnnotated.ExampleInnerClass("")

        val actual =
            ExampleOuterClassNotAnnotatedFixtures.ExampleInnerClassNotAnnotatedFixtures.ExampleInnerClassFixtures.exampleInnerClass()

        assertThat(actual).isEqualTo(expected)
    }
}
