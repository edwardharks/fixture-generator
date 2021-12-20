package solution.littlebird.fixturegenerator

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ExampleTest {

    @Test
    fun `generates fixtures`() {
        val expected = ExampleClass(
            nullable = null,
            string = "",
            int = 0,
            short = 0,
            long = 0L,
            double = 0.0,
            float = 0.0F,
            byte = 0,
            boolean = false,
            fixture = AnotherExampleClass(
                anotherExample = ""
            ),
        )

        val actual = ExampleClassFixtures.exampleClass()

        assertThat(expected).isEqualTo(actual)
    }
}