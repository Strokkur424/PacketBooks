plugins {
  id("io.papermc.paperweight.userdev")
}

val userdevExtension: UserdevExtension = extensions.create("userdev", UserdevExtension::class.java)

dependencies {
  paperweight.paperDevBundle(userdevExtension.version)
}

abstract class UserdevExtension @Inject constructor(objects: ObjectFactory) {
  val version: Property<String> = objects.property(String::class.java)
}
