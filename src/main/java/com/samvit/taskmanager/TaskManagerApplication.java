package com.samvit.taskmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// This ONE annotation does three things:
// 1. @Configuration — this class can define beans
// 2. @EnableAutoConfiguration — auto-configure based on dependencies
// 3. @ComponentScan — scan this package + all sub-packages for beans
@SpringBootApplication
public class TaskManagerApplication {

    public static void main(String[] args) {
        // This single line starts EVERYTHING:
        // - Creates the ApplicationContext (IoC container)
        // - Scans for all @Service, @Repository, @Controller classes
        // - Creates and wires all beans
        // - Starts embedded Tomcat on port 8080
        SpringApplication.run(TaskManagerApplication.class, args);
    }
}
