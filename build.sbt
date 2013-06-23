/* In this tutorial you will learn how to add the sprouch library to your project and use it to create,
   read, update and delete documents in CouchDB (or BigCouch or Cloudant). The source code is available at
   <a href="https://github.com/KimStebel/sprouch-tutorial">github.com/KimStebel/sprouch-tutorial</a>.</p>
   <p>This tutorial is written for Scala 2.9, but you can also use 2.10. Here is the build.sbt file:*/
name := "sprouch-tutorial"

scalaVersion := "2.10.2"

/* Sprouch is available from its own repository on Github, so you need to add it to your resolvers. */
resolvers += "sprouch repo" at
             "http://kimstebel.github.com/sprouch/repository"

resolvers += "spray repo" at "http://repo.spray.io"

/* Now sbt will be able to resolve our dependency on sprouch. */
libraryDependencies += "sprouch" %% "sprouch" % "0.5.11"
/* If you want to use 2.10, just replace 2.9.2 with 2.10. */

