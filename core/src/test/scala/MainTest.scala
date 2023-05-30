/*
 * Copyright 2023 Polyvariant
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import weaver._

object MainTest extends FunSuite {

  def showPlatform =
    (if (Platform.isJS)
       "\u001b[36m" +
         "JS"
     else if (Platform.isNative)
       "\u001b[32m" +
         "Native"
     else
       "\u001b[31m" +
         "JVM") + "\u001b[0m"

  def showScala =
    (if (Platform.isScala3)
       "\u001b[32m" +
         "Scala 3"
     else
       "\u001b[31m" +
         "Scala 2") + "\u001b[0m"

  test(
    s"main is tested ($showPlatform, $showScala)"
  ) {
    assert(Main.v == 1)
  }
}
