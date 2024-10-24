package de.tum.in.www1.modulemanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

@SpringBootApplication
@Modulithic(systemName = "ModuleManagement")
public class ModuleManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModuleManagementApplication.class, args);
    }

}
