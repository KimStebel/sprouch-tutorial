   /* Now to the actual code. First we need to import sprouch._ as well as sprouch.JsonProtocol._,
   which contains methods to convert Scala types from and to JSON.
 */
object Main extends App {
  import sprouch._
  import sprouch.dsl._
  import sprouch.JsonProtocol._
  /* Suppose we have a Scala class used to represent products in an online store.*/
  case class ShopItem(
      name: String,
      price: BigDecimal,
      description:Option[String])
  /* We would like to store it in CouchDB. Since CouchDB stores its data as JSON documents,
     we first need a way to convert products to and from JSON. Since we defined Sh opItem as a case class,
     there is an easy way to do this. We call jsonFormat3(Product) to get a JsonFormat[Product]. JsonFormat
     is a just trait with a read and a write method, so it is easy to implement one yourself if needed.
   */
  implicit val productFormat = jsonFormat3(ShopItem)
  /* Now we need to connect to CouchDB. First we create a Config object.
     The config object takes an ActorSystem and any configuration settings needed to connect to CouchDB.
     In this tutorial, we create our own actor system. In a real application you will probably already have one configured.
     If you don't specify any other parameters, sprouch asseumes CouchDB is running on localhost port 5894, does not
     require a username or password and uses plain HTTP rather than HTTPS.
   */
  import akka.actor.ActorSystem
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
  /* Then we create a new couch object with this config.
   */
  val couch = Couch(config)
  /* Now comes the interesting part. 
     We get a reference to the items database or create it, if it does not already exist. We make the val implicit
     so we don't have to pass it explicitly to ever method that creates/reads/updates documents in the database. */
  implicit val db = couch.getDb("items") recoverWith { case _ =>
    couch.createDb("items")
  }
  /* We create a new product to put into our database */
  val phone = ShopItem("Samsung S5", 500, Some("Shiny new smartphone"))
  /* Since sprouch is an asynchronous library, all its methods return futures.
     We use a for comprehension to chain these futures together.
   */
  val future = for {
    /* First we add the phone to the database. */
    phoneDoc <- phone.create
    /* The create method gives us a RevedDocument[ShopItem]. The RevedDocument contains an ID created by CouchDB (you can
       also pass your own ID to create), the document revision returned by CouchDB and of course the phone object.
       
       Let's say we want to reduce the phone's price. Since we're dealing with a
       case class, we can use its copy method to update the price and the description.
     */
    reduced = phone.copy(
        price = 400,
        description = Some("Shiny new smartphone. Now 20% off!")
    )
    /* Then we persist the changes in the database. */
    updatedPhoneDoc <- phoneDoc := reduced
    /* To read a document, there are several options. If you already have a document and just want to
       get the most recent revision, you can pass the old document to the get method.*/
    current <- get(phoneDoc)
     /* Or you can simply get the document by its ID.
       In that case you have to specify the type of the document explicitly. */
    byId <- get[ShopItem](phoneDoc.id)
    /* If we don't want to sell this phone anymore, we can delete it from the database. Note that we need to have the
       latest version of the document, because deletion will fail if the revision is not current.
     */
    _ <- current.delete
  /* We output the first and second version of the phone document. */
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
       Finally we block and wait for the result to be computed. Again, this is something you might not
       want to do in a real application. If you want your methods to block, you can just use sprouch's blocking API.
     */
  import akka.util.Duration
  import akka.dispatch.Await
  val duration = Duration("10 seconds")
  Await.result(future, duration)
  actorSystem.shutdown()
}
/* That's it, we're done! If you have any questions, please leave a comment. */
