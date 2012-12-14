package sprouch.tutorial

import sprouch._
import JsonProtocol._
import akka.actor.ActorSystem
import akka.util.Duration
import akka.dispatch.Await

object Main {
  case class Contact(name:String, email:Option[String], phone:Option[String])
  implicit val contactFormat = jsonFormat3(Contact)
  
  def main(args:Array[String]) {
    val actorSystem = ActorSystem("myActorSystem")
    val duration = Duration("10 seconds")
    val couch = Couch(Config(actorSystem))
    val future = for {
      db <- couch.createDb("testdb")
      doc <- db.createDoc(Contact("john", Some("john@acme.org"), None))
      updatedDoc <- db.updateDoc(doc.updateData(_.copy(phone = Some("12345"))))
      _ <- db.delete()
    } yield {
      println("first version: " + doc)
      println("second version: " + updatedDoc)
    }
    Await.result(future, duration)
    actorSystem.shutdown()
  }
}
