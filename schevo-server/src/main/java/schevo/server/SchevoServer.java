package schevo.server;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 
 * @author Tome (tomecode.com)
 *
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
@SpringBootApplication
public class SchevoServer {

	public static final void main(String args[]) {
		//

		System.setProperty(Config.CONFIG_SPACE_LOCAL_DIR, System.getProperty("user.dir") + File.separator + "testSpace");
		System.setProperty("server.port", "9999");
		SpringApplication.run(SchevoServer.class, args);
	}

	@ExceptionHandler(Exception.class)
	public final String fooException(HttpServletRequest request, Throwable e) {
		e.printStackTrace();
		return e.getMessage();
	}

}
