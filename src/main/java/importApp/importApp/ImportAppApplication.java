package importApp.importApp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("importApp.importApp.Mapper")
public class ImportAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImportAppApplication.class, args);
	}

}
