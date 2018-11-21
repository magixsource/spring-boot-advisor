package gl.linpeng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


//    @Bean
//    public IAnalyzerService analyzerService() {
//        return new HealthAnalyzerServiceImpl();
//    }
//
//    @Bean
//    public IHealthService healthService() {
//        return new HealthServiceImpl();
//    }
}
