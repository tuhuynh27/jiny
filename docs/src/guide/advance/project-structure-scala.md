# Project Structure (Scala)

## Handler object

```scala
object MouseHandler {
  private val gson = AppFactory.getGson
  private val collection: MongoCollection[Mouse] = AppFactory.getMongoDatabase
    .getCollection("mouse")
    
  val getMouses: Handler = _ => {
    val mouses = collection.find()
    HttpResponse.of(mouses)
  }
  
  val addMouse: Handler = ctx => {
    val body = ctx.getBody
    val newMouse: Mouse = gson.fromJson(body, classOf[Mouse])
    collection.insertOne(Mouse.apply(newMouse.name, newMouse.owner))
    HttpResponse.of(newMouse)
  }
}
```

## Router object

```scala
object MouseRouter {
  def getRouter: HttpRouter = {
    val router = new HttpRouter()
    router.get("/", MouseHandler.getMouses)
    router.post("/", MouseHandler.addMouse)
    router
  }
}
```

## Main object

```scala
object ServerBootstrap extends Runnable {
  private val server = HttpServer.port(1234)
  server.setupResponseTransformer(s => AppFactory.getGson.toJson(s))

  server.get("/", _ => HttpResponse.of("Hello Scala"))
  server.use("/cat", CatRouter.getRouter)
  server.use("/mouse", MouseRouter.getRouter)

  override def run(): Unit = server.start()
}
```
