package sprouch.tutorial

import spray.json.JsValue
import spray.json.JsonFormat
import spray.json.JsString

object Schemaless extends App {
  import sprouch._
  import sprouch.JsonProtocol._
  case class Dummy(js:JsValue)
  case class PartialDoc(dontcare: Dummy, important: String)
  implicit object DummyFormat extends JsonFormat[Dummy] {
    override def read(js:JsValue):Dummy = {
      Dummy(js)
    }
    
    override def write(d:Dummy):JsValue = {
      d.js
    }
  }
  implicit val productFormat = jsonFormat2(PartialDoc)
  
  import akka.actor.ActorSystem
  val actorSystem = ActorSystem("myActorSystem")
  val config = Config(actorSystem)
  val couch = Couch(config)
  val future = for {
    db <- couch.getDb("test") recoverWith { case _ =>
      couch.createDb("test")
    }
    par = PartialDoc(Dummy(JsString("")), "important")
    parDoc <- db.createDoc(par)
    gotten <- db.getDoc[PartialDoc](parDoc.id)
    _ <- db.deleteDoc(gotten)
  } yield {
    println(gotten)
    
  }
  import akka.util.Duration
  import akka.dispatch.Await
  val duration = Duration("10 seconds")
  Await.result(future, duration)
  actorSystem.shutdown()
}
