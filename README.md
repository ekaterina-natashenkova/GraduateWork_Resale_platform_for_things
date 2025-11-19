# Проект платформы по перепродаже вещей

## Описание проекта
Дипломная работа по разработке проекта платформы по перепродаже вещей - написание бэкенд-части сайта на Java на базе имеющейся фронтенд-части сайта.

### Бэкенд-часть проекта реализует следующий функционал:
- Авторизация и аутентификация пользователей.  
- Распределение ролей между пользователями: пользователь и администратор.  
- CRUD-операции для объявлений и комментариев: администратор может удалять или редактировать все объявления и комментарии, а пользователи — только свои.  
- Возможность для пользователей оставлять комментарии под каждым объявлением.  
- Показ и сохранение картинок объявлений, а также аватарок пользователей.  
---

## Описание окружения
- JDK 17
- Maven
- Spring Boot 3.5.7
- Spring Security
- Swagger
- Postman
- PostgreSQL
- JUnit 5
- Docker

***
## Шаги развертывания

### Установка зависимостей
- springdoc-openapi-ui 1.6.14  
- spring-boot-starter-validation  
- spring-boot-starter-web  
- spring-boot-starter-data-jpa  
- spring-boot-starter-security  
- spring-boot-starter-test  
- spring-security-test  
- spring-dotenv 2.5.4  
- liquibase-core  
- mapstruct  
- postgresql  
- h2  

### Установка плагинов
- spring-boot-maven-plugin  
- maven-compiler-plugin  
- mapstruct-processor  
- lombok  
- lombok-mapstruct-binding  

### Настройка конфигураций (application.properties)
#### Конфигурация сервера и приложения
```
server.port=8080  
server.servlet.context-path=/
spring.application.name=ads
cors.allowed-origins=http://192.168.99.100:3000,http://192.168.1.186:3000,http://localhost:3000
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```
#### Подключение БД системы PostgreSQL 
```
spring.datasource.url=jdbc:postgresql://localhost:5432/Resale_platform
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.maximum-pool-size=10
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```
#### Подключение liquibase
```
spring.liquibase.change-log=classpath:liquibase/changelog-master.yaml
spring.liquibase.enabled=true
```
#### Настройка Hibernate
```
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.jdbc.batch_size=10
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.open-in-view=false
```
#### Логирование и отладка
```
logging.level.org.springframework.security=DEBUG
logging.level.ru.skypro.homework=DEBUG
spring.mvc.throw-exception-if-no-handler-found=true
spring.web.resources.add-mappings=false
spring.config.import=optional:file:.env[.properties]
```
#### Переменные окружения
Создайте файл .env в корне проекта для хранения чувствительных данных:  
```DB_PASSWORD=your_postgres_password_here```

### Настройка конфигураций для пакета test (application.properties)
#### Подключение БД системы H2 (In-memory H2 database for testing)
```
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
```
#### Настройка Hibernate для тестирования
```
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.defer-datasource-initialization=true
```
#### Конфигурация логирования для тестов
```
logging.level.org.springframework=WARN
logging.level.org.hibernate=WARN
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```
#### Специфичные настройки для тестовой среды
```
spring.test.database.replace=ANY
app.images.path=uploads/images
app.images.auto-create-dir=true
```
#### Отключение Liquibase в тестовом режиме
```spring.liquibase.enabled=false```

***
## Запуск приложения
### Предварительные требования перед запуском
- Установлена Java 17 или выше
- Запущен PostgreSQL на localhost:5432
- Создана база данных Resale_platform
- Настроена переменная окружения DB_PASSWORD
- Порт 8080 свободен - приложение будет доступно по адресу: http://localhost:8080

### Сборка проекта
```mvn clean package```
(Maven -> Ads application -> Lifecycle -> clean -> package)

### Запуск приложения
```java -jar target/ads-0.0.1-SNAPSHOT.jar```

***
## Документация API
После запуска приложения документация доступна по адресам:  
Swagger UI: http://localhost:8080/swagger-ui.html  
OpenAPI: http://localhost:8080/v3/api-docs

***
## Основные функции API
#### Аутентификация:
•	POST /login - вход  
•	POST /register - регистрация  
#### Пользователи:
•	GET /users/me - получить данные пользователя  
•	PATCH /users/me - обновить данные пользователя  
•	POST /users/set_password - смена пароля  
•	PATCH /users/me/image - обновить аватар  
#### Объявления:
•	GET /ads - все объявления  
•	POST /ads - создать объявление  
•	GET /ads/me - все объявления текущего пользователя  
•	GET /ads/{id} - Получение информации об объявлении по ID  
•	PATCH /ads/{id} - обновить объявление по ID  
•	DELETE /ads/{id} - удалить объявление по ID  
•	PATCH /ads/{id}/image - обновить изображение объявления  по ID  
#### Комментарии:
•	GET /ads/{id}/comments - комментарии к объявлению  
•	POST /ads/{id}/comments - добавить комментарий  
•	PATCH /ads/{adId}/comments/{commentId} - обновить комментарий  
•	DELETE /ads/{adId}/comments/{commentId} - удалить комментарий  
#### Изображения:
•	GET /images/ads/{adId}/image - получение изображения объявления  
•	GET /images/users/{userId}/avatar - получение аватара пользователя  

---
## Автор
Наташенкова Екатерина (ekaterina-natashenkova)

***
Последнее обновление: ноябрь 2025
