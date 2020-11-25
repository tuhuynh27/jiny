package ;

import com.jinyframework.*;
import static com.jinyframework.core.AbstractRequestBinder.HttpResponse.of;

import lombok.val;

public class Main {
    public static void main(String[] args) {
        val server = HttpServer.port(1234);
        server.get("/hello", ctx -> of("Hello World!"));
        server.start();
    }
}
