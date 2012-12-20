/* In this tutorial I will show you how to add the sprouch library to your project and use it to create,
   read, update and delete documents in CouchDB (or BigCouch or Cloudant). The source code is available at
   <a href="https://github.com/KimStebel/sprouch-tutorial">github.com/KimStebel/sprouch-tutorial</a>.</p>
   <p>Let's create a new sbt project. We're using Scala 2.9 for the tutorial, but you can also use 2.10. */
name := "sprouch-tutorial"

scalaVersion := "2.9.2"

/* Sprouch is available from its own repository on github, so we need to add that to our resolvers. */
resolvers += "sprouch repo" at
             "http://kimstebel.github.com/sprouch/repository"

/* Now sbt will be able to resolve our dependency on sprouch. */
libraryDependencies += "sprouch" % "sprouch_2.9.2" % "0.5.2"
/* If you want to use 2.10, just replace 2.9.2 with 2.10.0-RC3. */

