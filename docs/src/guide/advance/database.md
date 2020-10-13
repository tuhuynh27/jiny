# Data Persistence

This page explains how to integrate Jiny with databases such as MySQL/MSSQL or MongoDB

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

## Hibernate ORM

First, install the MySQL and Hibernate dependencies:

`build.gradle`

```groovy
compile group: 'mysql', name: 'mysql-connector-java', version: '5.1.49'
compile group: 'org.hibernate', name: 'hibernate-core', version: '5.3.6.Final'
```

Create Hibernate Factory:

```java
@Setter
public class HibernateFactory {
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            val configuration = new Configuration();
            configuration.setProperty("hibernate.current_session_context_class", "thread");
            configuration.setProperty("connection.driver_class", "com.mysql.jdbc.Driver");
            configuration.setProperty("hibernate.connection.url", "jdbc:mysql://localhost:3306/hibernate");
            configuration.setProperty("hibernate.connection.username", "root");
            configuration.setProperty("hibernate.connection.password", "example");
            configuration.setProperty("dialect", "org.hibernate.dialect.MySQLDialect");
            configuration.setProperty("hibernate.hbm2ddl.auto", "update");
            configuration.setProperty("show_sql", "true");
            configuration.setProperty("hibernate.connection.pool_size", "10");
            configuration.addAnnotatedClass(Tiger.class);
            val builder = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties());
            sessionFactory = configuration.buildSessionFactory(builder.build());
        }

        return sessionFactory;
    }
}
```

Create an entity and use persistence annotations:

```java
@Data
@Entity
public class Tiger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name;
    String owner;
}
```

Then, create base repositories, example CrudRepository:

```java
@RequiredArgsConstructor
public abstract class CrudRepository<T> {
    private final SessionFactory sessionFactory;
    private final Class<T> entityClass;

    public T save(T entity) {
        val session = sessionFactory.getCurrentSession();
        val tx = session.beginTransaction();
        session.save(entity);
        tx.commit();
        return entity;
    }

    public T find(long id) {
        val session = sessionFactory.getCurrentSession();
        val tx = session.beginTransaction();
        try {
            return session.find(entityClass, id);
        } finally {
            tx.commit();
        }
    }

    public void update(T entity) {
        val session = sessionFactory.getCurrentSession();
        val tx = session.beginTransaction();
        sessionFactory.getCurrentSession().update(entity);
        tx.commit();
    }

    public void delete(T entity) {
        val session = sessionFactory.getCurrentSession();
        val tx = session.beginTransaction();
        session.delete(entity);
        tx.commit();
    }

    public List<T> list() {
        val session = sessionFactory.getCurrentSession();
        val tx = session.beginTransaction();
        try {
            CriteriaQuery<T> query = session.getCriteriaBuilder().createQuery(entityClass);
            query.select(query.from(entityClass));
            return session.createQuery(query).getResultList();
        } finally {
            tx.commit();
        }
    }
}
```

Define an entity's repository:

```java
public class TigerRepository extends CrudRepository<Tiger> {}
```

And finally use it in handlers:

```java
public class TigerHandler {
    private final Gson gson = AppFactory.getGson();
    private final TigerRepository tigerRepository = RepositoryFactory.getTigerRepository();

    public HttpResponse getTigers(Context ctx) {
        return HttpResponse.of(tigerRepository.list());
    }

    public HttpResponse getTiger(Context ctx) {
        val id = Integer.parseInt(ctx.pathParam("id"));
        return HttpResponse.of(tigerRepository.find(id));
    }

    public HttpResponse addTiger(Context ctx) {
        val body = ctx.getBody();
        val newTiger = gson.fromJson(body, Tiger.class);
        val added = tigerRepository.save(newTiger);
        return HttpResponse.of(added);
    }
}
```

:::tip See full example
[Hibernate CRUD Example](https://github.com/huynhminhtufu/jiny/tree/master/examples/crud-http-server/src/main/java/com/jinyframework/examples/crud/repositories)
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
