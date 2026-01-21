rootProject.name = "packetbooks"

sequenceOf("abstract", "1-21-6").forEach {
  include("paper-$it")
}

