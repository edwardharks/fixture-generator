# Fixture generator
A work in progress Kotlin annotation processor to generate test fixtures.
```Kotlin
@Fixture
data class User(val name: String, val favouriteColour: String)
```
Annotating a class with `@Fixture` will generate a fixture with default values:
```Kotlin
@Test
fun `user fixture`() {
  val user = UserFixtures.user()
  assertThat(user.name).isEqualTo("")
}
```


## License

    Copyright 2022 Edward Harker.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
