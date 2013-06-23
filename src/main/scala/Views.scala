/*
 * In this tutorial you will learn how to query CouchDB views with Sprouch.
 *  I will assume that you already know how views work and how to write Javascript views. I will also assume
 *  that you have read the basic Sprouch tutorial and know how to connect to CouchDB with Sprouch. You will need
 *  the usual imports to get started.
 */
object ViewsTutorial extends App with Helpers {
  import sprouch._, dsl._, JsonProtocol._
  import scala.concurrent.{Await, Future}, scala.concurrent.duration.Duration
  /*First you will need some test data to run your views on. The following creates a sequence of ShopItems
   *  with prices between 100 and 1000.*/
  val items = for (i <- 1 to 10)
    yield ShopItem("Item " + i, BigDecimal(i * 100), None)
  /*
   * Since creating a database was already covered, I'll assume that you have some function that passes
   * a <tt>Future[Database]</tt> to its argument. You make the argument implicit to be able to use it with the DSL syntax.
   */
  withDb(implicit dbf => for {
    /*First you create the <tt>ShopItem</tt> documents in the database.*/
    docs <- items.create
    /*Then you write the views. The first one just returns the price of the item as the key.*/
    mapView = MapReduce(
        map = """
          function(doc) {
            emit(doc.price);
          }
        """
    )
    /*The second view computes the sum of all prices.*/
    reduceView = MapReduce(
        map = """
          function(doc) {
            emit(doc.name, doc.price);
          }
        """,
        reduce = """
          function(keys, values) {
            return sum(values);
          }
        """
    )
    /*You can create the views in the database with the <tt>createViews</tt> method.*/
    viewsDoc <- NewDocument(
        "my views",
        Views(Map("price" -> mapView, "sum" -> reduceView))
    ).createViews
    /*Now you can query the map view. This query will get us all items whose price is between 350 and 550.
     * The second type parameter is <tt>Null</tt>, since the view does not emit values.*/
    prices <- queryView[Double, Null]("my views", "price",
        flags = ViewQueryFlag(reduce = false, include_docs = true),
        startKey = Some(350),
        endKey = Some(550))
    _ = {
      println("Items between 350 and 550:")
      prices.rows.foreach(r => {
        println(r.docAs[ShopItem].get.name + ": " + r.key)
      })
    }
    /*Querying the reduce view is even simpler. The first type parameter is <tt>Null</tt>, since the key of this
     * reduce view will be <tt>null</tt> as it is not grouped.*/
    sum <- queryView[Null, Double]("my views", "sum")
    _ = println("Sum of prices of all items: " + sum.values.head)
    
  } yield ())
}
/*That's it! The output will be
 * <p><pre>
Items between 350 and 550:
Item 4: 400.0
Item 5: 500.0
Sum of prices of all items: 5500.0

 * </pre></p>
 * There are many more options for querying views. Sprouch covers CouchDb's complete view API. You can find more
 * information about views in the CouchDb documentation. If you have a question about Sprouch, please leave a comment.*/