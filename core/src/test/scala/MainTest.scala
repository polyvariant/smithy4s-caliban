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
