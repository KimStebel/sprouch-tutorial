/* In this tutorial I will show you how to add the sprouch library to your project and use it to create,
   read, update and delete documents in CouchDB (or BigCouch or Cloudant). The source code is available at
   <a href="https://github.com/KimStebel/sprouch-tutorial">github.com/KimStebel/sprouch-tutorial</a> in the 2.10 branch.</p>
   <p>Let's create a new sbt project. */
name := "sprouch-tutorial"

scalaVersion := "2.10.0-RC5"

/* Sprouch is available from its own repository on github, so we need to add that to our resolvers. */
resolvers += "sprouch repo" at
             "http://kimstebel.github.com/sprouch/repository"

/* Now sbt will be able to resolve our dependency on sprouch. */
libraryDependencies += "sprouch" % "sprouch_2.10" % "0.5.6"

