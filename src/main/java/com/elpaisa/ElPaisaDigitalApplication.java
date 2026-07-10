package com.elpaisa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicacion "El Paisa Digital".
 * Arquitectura: MVC (Model-View-Controller) sobre Spring Boot + Thymeleaf,
 * con capa DAO explicita para el acceso a datos y capa Service para la logica de negocio.
 */
@SpringBootApplication
public class ElPaisaDigitalApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElPaisaDigitalApplication.class, args);
    }
}
