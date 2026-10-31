# LEGGA — Microservicio de Legajos

Microservicio encargado de administrar los legajos y sus categorías dentro de LEGGA. Expone operaciones para crear y consultar legajos, además de gestionar las categorías asociadas.

## Tecnologías

- Java 17
- Spring Boot
- Maven
- PostgreSQL
- Docker

## Arquitectura

`Frontend Angular → MS-Legajos → PostgreSQL`

MS-Legajos trabaja junto a MS-Documentos, que consulta este servicio para validar el estado y la existencia de los legajos.

## Ejecución local

Desde el directorio que contiene la configuración Docker Compose:

```bash
docker compose up --build
```

## Información del servicio

| Elemento | Valor |
|---|---|
| Puerto | 8081 |
| Swagger UI | `/swagger-ui/index.html` |
| Microservicio relacionado | MS-Documentos |
| Persistencia | PostgreSQL |

## Endpoints principales

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/legajos` | Consultar legajos |
| POST | `/api/legajos` | Crear un legajo |
| GET | `/api/legajos/{id}` | Consultar un legajo por ID |
| GET | `/api/categorias` | Consultar categorías |
| POST | `/api/categorias` | Crear una categoría |

## Avances

- Implementación inicial de la gestión de legajos.
- Implementación inicial de la gestión de categorías.
- Integración con MS-Documentos para la validación de legajos.
- Respaldo inicial del código en GitHub.

## Documentación

Los informes técnicos y documentos del proyecto se organizan en la carpeta `/docs`.

## Configuración

La aplicación utiliza variables de entorno para configurar la conexión a PostgreSQL.

No se deben almacenar credenciales directamente en el repositorio.