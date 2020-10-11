# Dependency Injection

Wiring everything together is a tedious part of application development. There are several approaches to connect data, service, and presentation classes to one another. The dependency injection pattern leads to code that's modular and testable

The easiest & most expressive way (without annotations :smirk:) to implement a "DI-like" system in Jiny is to use `Factory Classes`.

A factory class decouples the client and implementing class. A simple factory uses static methods to get and set mock implementations for interfaces. A factory is implemented with some boilerplate code:

```java
import lombok.*;

@Setter // Auto generate setSqlConnection static method
public class AppFactory {
    // Connection is an interface which any SQL Database Driver
    // such as MySQL/MSSQL/H2/SQLite... can implement it
    private static Connection sqlConnection;

    public static Connection getSQLConnection() {
        if (sqlConnection == null) {
            // Set the default implementation
            sqlConnection = MySQL.init();
        }

        return sqlConnection;
    }
}
```

In our client code, we just replace the new calls with factory lookups:

```java
public class DogHandler {
    // "Inject"
    private final Connection conn = AppFactory.getSQLConnection();

    public HttpResponse getDogs(Context ctx) throws SQLException {
        // Use the dependency
        @Cleanup val stmt = conn.prepareStatement("SELECT * FROM dog");
        // Query and return ...
    }
    // ...
```

The factory makes it possible to write a proper unit test:

```java
class DogCRUDTest {
    final Connection mockDBConn = new MockDB().getConn();
    
    @BeforeAll void setupMockDB() {
        // Set custom implementation
        AppFactory.setSqlConnection(mockDBConn);
    }
    
    // Get dogs will use mockDB instead of MySQL
    void testSuccessGetDogs() {
        val ctx = getVirtualContext();
        assertEquals(result, new DogHandler().getDogs(ctx));
    // ...
```

You can also use a DI framework like [Google's Guice](https://github.com/google/guice/) or [Dagger](https://github.com/google/dagger).
