# Work with Databases

This page explains how to integrate Jiny with a database such as MySQL/MSSQL or MongoDB

## SQL Databases

First, install SQL driver dependency:

`build.gradle`

```groovy
// Can be SQLite or H2 ...
compile group: 'mysql', name: 'mysql-connector-java', version: '5.1.49'
```

Next, create an SQL initial (get connection) methods:

```java
public final class MySQLUtils {
    public static Connection init() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance(); // Can be SQLite or H2 ...
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            log.error(e.getMessage(), e);
        }
        try {
            // Can be SQLite or H2 ...
            return DriverManager.getConnection("jdbc:mysql://localhost/test?user=root&password=example");
        } catch (SQLException t) {
            log.error(t.getMessage(), t);
        }

        return null;
    }
}
```

Then use that connection to do queries:

```java
public HttpResponse getDogs(Context ctx) throws SQLException {
    @Cleanup val stmt = conn.prepareStatement("SELECT * FROM dog");
    @Cleanup val rs = stmt.executeQuery();
    val dogs = new ArrayList<Dog>();
    while (rs.next()) {
        val id = rs.getInt("id");
        val name = (String) rs.getObject("name");
        val owner = (String) rs.getObject("owner");
        val dog = new Dog(id, name, owner);
        dogs.add(dog);
    }
    return HttpResponse.of(dogs);
}
```

:::tip See full example
[JDBC CRUD Example](https://github.com/huynhminhtufu/jiny/blob/master/examples/crud-http-server/src/main/java/com/jinyframework/examples/crud/handlers/DogHandler.java)
:::

## Mongo Database

First, install the MongoDB driver dependency:

`build.gradle`

```grovy
compile group: 'org.mongodb', name: 'mongodb-driver-sync', version: '4.1.0-beta2'
```

Next, create a MongoDB initial (get connection) methods:

```java
public final class MongoDBUtils {
    public static MongoClient init() {
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        // Use MongoDB POJO Mapping feature, the driver will convert POJO to MongoDB query result internally
        MongoClientSettings settings = MongoClientSettings.builder()
                .codecRegistry(pojoCodecRegistry)
                .build();

        return MongoClients.create(settings);
    }
}
```

Then use that connection to do queries:

```java
public HttpResponse getCats(Context ctx) {
    val catArr = new ArrayList<Cat>();
    catCollection.find().forEach(catArr::add);
    return HttpResponse.of(catArr.toArray());
}
```

:::tip See full example
[MongoDB CRUD Example](https://github.com/huynhminhtufu/jiny/blob/master/examples/crud-http-server/src/main/java/com/jinyframework/examples/crud/handlers/CatHandler.java)
:::
