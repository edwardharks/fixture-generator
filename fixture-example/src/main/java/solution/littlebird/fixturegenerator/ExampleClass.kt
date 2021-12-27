package solution.littlebird.fixturegenerator

import com.edwardharker.fixturegenerator.Fixture

@Fixture
data class ExampleClass(
    val nullable: String?,
    val string: String,
    val int: Int,
    val float: Float,
    val double: Double,
    val short: Short,
    val long: Long,
    val byte: Byte,
    val boolean: Boolean,
    val fixture: AnotherExampleClass
)

@Fixture
data class AnotherExampleClass(
    val anotherExample: String
)

// TODO

/* Use emptyList etc */
//@Fixture
//data class CollectionTypes(
//    val list: List<String>,
//    val map: Map<String, String>,
//    val set: Set<String>,
//)

/* Don't generate fixtures for private constructors */
//@Fixture
//class PrivateConstructor private constructor(val foo: String)

/* Inner class Fixture should be child of outer class fixture to avoid conflicts */
//@Fixture
//class OuterClass(
//    val inner: InnerClass
//) {
//    @Fixture
//    class InnerClass(foo: String)
//}

/* Support types that cannot be annotated */
//@Fixture
//data class LibraryTypes(
//    val libraryType: Date
//)

/* How do we know which constructor to generate the fixture for?
   Easy solution is just to use the primary constructor */
//@Fixture
//class MultipleConstructors(
//    val one: Int,
//    val two: Int,
//) {
//    constructor() : this(1, 2)
//}
