package gl.linpeng;

//import gl.linpeng.serverless.aengine.service.IAnalyzerService;
//import gl.linpeng.serverless.aengine.service.IHealthService;
//import gl.linpeng.serverless.aengine.service.impl.HealthAnalyzerServiceImpl;
//import gl.linpeng.serverless.aengine.service.impl.HealthServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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
