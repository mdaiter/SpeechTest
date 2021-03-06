name := "testScala"

version := "1.0"

scalaVersion := "2.10.3"

connectInput in run := true

fork in run := true

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Apache Staging" at "https://repository.apache.org/content/groups/staging/"

libraryDependencies += "com.michaelpollmeier" % "gremlin-scala" % "2.4.1"

libraryDependencies += "com.thinkaurelius.titan" % "titan-core" % "0.4.2"

libraryDependencies += "com.tinkerpop" % "frames" % "2.4.0"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.2.3"

libraryDependencies += "com.datastax.cassandra" % "cassandra-driver-core" % "2.0.0-rc2"

libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.2.3"

resolvers += "spray" at "http://repo.spray.io/"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.2.3"

libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.2.3"

libraryDependencies += "io.argonaut" %% "argonaut" % "6.0.2"

libraryDependencies += "com.github.jodersky" %% "flow" % "1.1.0"

libraryDependencies += "io.spray" %% "spray-json" % "1.2.5"
