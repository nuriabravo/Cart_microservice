# Cart_Workshop

## Shopping Cart Microservice

Este repositorio contiene el código fuente de un microservicio de carrito de compras para una tienda online, desarrollado con Java Spring Boot. El microservicio maneja todas las operaciones relacionadas con el carrito de compras, como agregar, eliminar y actualizar productos en el carrito, así como calcular el total del carrito teniendo en cuenta unos descuentos por cantidad o región.

## Tabla de Contenidos

- [Descripción](#descripción)
- [Características](#características)
- [Arquitectura](#arquitectura)
- [Tecnologías Utilizadas](#tecnologías-utilizadas)
- [Requisitos Previos](#requisitos-previos)
- [Instalación](#instalación)
- [API Endpoints](#api-endpoints)

## Descripción

El microservicio de carrito de compras permite a los usuarios gestionar sus carritos de compras en una tienda online. Proporciona endpoints RESTful para agregar productos al carrito, eliminar productos, actualizar cantidades y obtener el contenido actual del carrito.

## Características

- Agregar productos al carrito
- Eliminar productos del carrito
- Actualizar la cantidad de productos en el carrito
- Obtener el contenido del carrito
- Calcular el total del carrito
- Persistencia de datos en una base de datos relacional

## Arquitectura

Este microservicio sigue una arquitectura de microservicios basada en Spring Boot. Utiliza una base de datos relacional para almacenar los datos del carrito de compras y proporciona una API RESTful para interactuar con el carrito.

## Tecnologías Utilizadas

- Java 11
- Spring Boot
- Spring Data JPA
- H2 Database (para pruebas y desarrollo)
- MySQL (para producción)
- Maven

## Requisitos Previos

- JDK 11 o superior
- Maven 3.6.0 o superior
- MySQL (para entorno de producción)

## Instalación

1. Clona este repositorio:
    ```bash
    git clone https://github.com/nurvoz/Cart_microservice.git
    ```
2. Navega al directorio del proyecto:
    ```bash
    cd Cart_microservice
    ```
3. Compila y ejecuta el microservicio:
    ```bash
    mvn clean install
    mvn spring-boot:run
    ```

## API Endpoints

- **POST /carts/{userId}**: Crea un nuevo carrito para un usuario.
- **GET /carts/{id}**: Obtiene el carrito actual de un usuario.
- **DELETE /carts/{id}**: Elimina el carrito de un usuario.
- **POST /carts/products**: Agrega un nuevo producto al carrito.
- **PATCH /carts/products**: Actualiza la cantidad de un producto en el carrito.
- **DELETE /carts/products/{id}**: Elimina un producto del carrito.

