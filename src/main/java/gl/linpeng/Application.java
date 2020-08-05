package gl.linpeng;

//import gl.linpeng.serverless.aengine.service.IAnalyzerService;
//import gl.linpeng.serverless.aengine.service.IHealthService;
//import gl.linpeng.serverless.aengine.service.impl.HealthAnalyzerServiceImpl;
//import gl.linpeng.serverless.aengine.service.impl.HealthServiceImpl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        // show slogan
        showSlogan();
    }

    private static void showSlogan() {
        System.out.println("   _____                      __     ___       __      _                ");
        System.out.println("  / ___/____ ___  ____ ______/ /_   /   | ____/ /   __(_)________  _____");
        System.out.println("  \\__ \\/ __ `__ \\/ __ `/ ___/ __/  / /| |/ __  / | / / / ___/ __ \\/ ___/");
        System.out.println(" ___/ / / / / / / /_/ / /  / /_   / ___ / /_/ /| |/ / (__  ) /_/ / /    ");
        System.out.println("/____/_/ /_/ /_/\\__,_/_/   \\__/  /_/  |_\\__,_/ |___/_/____/\\____/_/     ");
        System.out.println(" :: 呵护全家健康  小智与您同行 ::                               (v1.0.0)");
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
//    @Bean
//    ServletRegistrationBean h2servletRegistration() {
//        ServletRegistrationBean registrationBean = new ServletRegistrationBean(new WebServlet());
//        registrationBean.addUrlMappings("/h2-console/*");
//        return registrationBean;
//    }
}
