import sprouch._
import sprouch.dsl._
import sprouch.JsonProtocol._
import akka.actor.ActorSystem
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait Helpers {
  
  private val as = ActorSystem("mysystem")
  implicit val dispatcher = as.dispatcher
  
  def withDb[A](f:Future[Database] => Future[A]) = {
    val dbf = {
      val couch = Couch(Config(as))
      couch.createDb("test").recoverWith { case _ => couch.deleteDb("test"); couch.createDb("test") }
    }
    val r = f(dbf).andThen { case _ => {
      dbf.flatMap(db => db.delete)
    }}
    Await.result(r, Duration("10 seconds"))
    as.shutdown()
    
  
  }
  
}

case class ShopItem(
    name: String,
    price: BigDecimal,
    description:Option[String]
)
  
object ShopItem {
  implicit val jsonFormat = jsonFormat3(ShopItem.apply)
}
  