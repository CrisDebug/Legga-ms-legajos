LEGGA — Microservicio de Legajos

1. Descripción

Microservicio backend encargado de administrar los legajos y sus categorías dentro de la plataforma LEGGA. Centraliza la información de los legajos, la configuración de categorías, los requisitos documentales y los campos utilizados para formularios dinámicos.

2. Tecnologías

Java 17

Spring Boot

Maven

PostgreSQL

Docker

3. Arquitectura

Frontend Angular
       |
       v
    BFF LEGGA
       |
       v
   MS-Legajos ------> PostgreSQL
       ^
       |
 MS-Documentos

MS-Legajos trabaja junto a MS-Documentos, proporcionando información para validar la existencia y el contexto de los legajos.

4. Funcionalidades principales

Gestión de legajos

Creación y consulta de legajos.

Consulta de legajos mediante identificador.

Administración de información y estado de los legajos.

Gestión de categorías

Creación y consulta de categorías.

Administración de información y estado.

Asociación de categorías con instituciones.

Requisitos documentales

Permite configurar los documentos requeridos para cada categoría.

Nombre y código del requisito.

Descripción.

Indicador de obligatoriedad.

Orden de presentación.

Estado activo o inactivo.

Formularios dinámicos

Permite configurar los campos asociados a cada categoría para adaptar la información solicitada según el tipo de legajo.

Código y etiqueta del campo.

Tipo de dato.

Indicador de obligatoriedad.

Orden de presentación.

Opciones configurables.

Estado activo o inactivo.

5. Información del servicio

Elemento

Valor

Microservicio

MS-Legajos

Puerto

8081

Swagger UI

/swagger-ui/index.html

Persistencia

PostgreSQL

Microservicio relacionado

MS-Documentos

6. Endpoints principales

Método

Endpoint

Descripción

GET

/api/legajos

Consultar legajos

POST

/api/legajos

Crear un legajo

GET

/api/legajos/{id}

Consultar un legajo por ID

GET

/api/categorias

Consultar categorías

POST

/api/categorias

Crear una categoría

Los endpoints adicionales de requisitos documentales y campos dinámicos se pueden consultar en Swagger UI.

7. Ejecución local

Desde el directorio que contiene la configuración Docker Compose:

docker compose up --build

También se puede ejecutar mediante Maven:

mvn spring-boot:run

8. Configuración

La aplicación utiliza variables de entorno para configurar la conexión a PostgreSQL y los parámetros necesarios para su ejecución.

No se deben almacenar credenciales directamente en el repositorio.

9. Avances implementados

Gestión de legajos.

Gestión de categorías.

Asociación de categorías con instituciones.

Administración de requisitos documentales por categoría.

Configuración de campos para formularios dinámicos.

Integración con MS-Documentos para la validación de legajos.

10. Documentación y evidencias

Los informes técnicos y documentos del proyecto se organizan en la carpeta docs.

## evidencia

- [Hito técnico: Creación de legajo y persistencia Outbox](docs/hitos-tecnicos/legajo%20create%20outbox/README.md)