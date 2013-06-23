   /* Now to the actual code. First you need a few imports:
      <tt>sprouch</tt> contains the main classes Couch and Database.
      <tt>sprouch.dsl</tt> contains implicit conversions that allow 
      you to use a shorter, more convenient syntax.
      Finally, <tt>sprouch.JsonProtocol</tt> contains methods to convert Scala types from and to JSON.
 */
object Main extends App {
  import sprouch._
  import sprouch.dsl._
  import sprouch.JsonProtocol._
  /* Suppose you have a Scala class used to represent products in an online store.*/
  case class ShopItem(
      name: String,
      price: BigDecimal,
      description:Option[String])
  /* You would like to store it in CouchDB. Since CouchDB stores its data as JSON documents,
     you first need a way to convert products to and from JSON. Since you defined ShopItem as a case class,
     there is an easy way to do this. You call <tt>jsonFormat3(ShopItem)</tt> to get a <tt>JsonFormat[ShopItem]</tt>.
     JsonFormat is a just trait with a read and a write method, so you can easily implement it yourself, if needed.
   */
  implicit val productFormat = jsonFormat3(ShopItem)
  /* Now you need to connect to CouchDB. First you create a Config object.
     The config object takes an ActorSystem and any configuration settings needed to connect to CouchDB.
     In this tutorial, an actor system is created just to pass it to Sprouch. In a real application you will
     probably already have one configured.
     If you don't specify any other parameters, sprouch assumes CouchDB is running on localhost, port 5894, does not
     require a username or password and uses HTTP and not HTTPS.
   */
  import akka.actor.ActorSystem
  import scala.concurrent.ExecutionContext.Implicits.global
  val actorSystem = ActorSystem("myActorSystem")
  val config = Config(actorSystem)
  /* Here is a different configuration that would work with cloudant.
   */
  val config2 = Config(
      actorSystem,
      hostName="someone.cloudant.com",
      port=443,
      userPass=Some("someone" -> "password"),
      https=true
  )
  /* Then we create a new couch object with this config.*/
  val couch = Couch(config)
  /* Now comes the interesting part. 
     You get a reference to the items database or create it, if it does not already exist. You make the val implicit
     so you don't have to pass it to every method that creates/reads/updates documents in the database. */
  implicit val db = couch.getDb("items") recoverWith { case _ =>
    couch.createDb("items")
  }
  /* You create a new product to put into our database */
  val phone = ShopItem("Samsung S5", 500, Some("Shiny new smartphone"))
  /* Since Sprouch is an asynchronous library, all its methods return futures.
     You can use a for comprehension to chain these futures together.
   */
  val future = for {
    /* First you add the phone to the database. */
    phoneDoc <- phone.create
    /* The create method gives us a <tt>RevedDocument[ShopItem]</tt>. The RevedDocument contains an automatically
       generated ID (you canalso pass your own ID to <tt>create</tt>), the document revision returned by CouchDB
       and of course the phone object.
       
       Let's say you want to reduce the phone's price. Since you have a
       case class, you can use its copy method to update the price and the description.
     */
    reduced = phone.copy(
        price = 400,
        description = Some("Shiny new smartphone. Now 20% off!")
    )
    /* Then you persist the changes in the database. */
    updatedPhoneDoc <- phoneDoc := reduced
    /* To read a document, there are several options. If you already have a document and just want to
       get the most recent revision, you can pass the old document to the <tt>get</tt> method.*/
    latest <- get(phoneDoc)
     /* Or you can get the document by its ID.
       In this case you have to specify the type of the document. */
    byId <- get[ShopItem](phoneDoc.id)
    /* If you don't want to sell this phone anymore, you can delete it from the database. Note that you need to have the
       latest version of the document, because deletion will fail if the revision is not current.
     */
    _ <- latest.delete
  /* This outputs the first and second version of the phone document. */
  } yield {
    println("First version: " + phoneDoc)
    println("Second version: " + updatedPhoneDoc)
  }
  /* The output is</p>
       <pre>
First version: Document(
    id: 14ad3c4d-f82d-4aef-b084-c2838decc53a,
    rev: 1-3ef3ac1b402f0948d2a94a383708dcda,
    data: ShopItem(Samsung S5,500,Some(Shiny new smartphone)))
Second version: Document(
    id: 14ad3c4d-f82d-4aef-b084-c2838decc53a,
    rev: 2-9e8f214f19641582c22b09bf6fab8141,
    data: ShopItem(Samsung S5,400,Some(Shiny new smartphone.
                                       Now 20% off!)))
       </pre><p>
       Finally you wait for the result to be computed. Again, this is something you probably don't
       want to do in a real application. If you want your methods to block, you can use Sprouch's synchronous API.
     */
  import scala.concurrent.duration.Duration
  import scala.concurrent.Await
  val duration = Duration("10 seconds")
  Await.result(future, duration)
  actorSystem.shutdown()
}
/* That's it, you're done! If you have any questions, please leave a comment. */