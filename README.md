📚 Полная документация проекта Homework Platform
🎯 О проекте
Homework Platform - это полнофункциональное REST API приложение для управления объявлениями и пользователями, разработанное на Spring Boot. Приложение предоставляет возможности регистрации, аутентификации, создания/редактирования объявлений и комментирования.

🏗️ Архитектура проекта
Стек технологий
Технология	Версия	Назначение
Java	11	Основной язык разработки
Spring Boot	2.7.15	Фреймворк для создания приложения
PostgreSQL	12+	Основная база данных
H2 Database	2.1.214	In-memory БД для тестирования
Spring Security	5.7.x	Аутентификация и авторизация
Spring Data JPA	2.7.x	Доступ к данным
Liquibase	4.20.x	Управление миграциями БД
SpringDoc OpenAPI	1.6.14	Документация API (Swagger)
Lombok	1.18.x	Уменьшение boilerplate кода
Maven	3.6+	Сборка проекта
Структура проекта
text
ru.skypro.homework/
├── 📁 config/                  # Конфигурационные классы
│   ├── WebSecurityConfig.java  # Конфигурация безопасности
│   ├── MvcConfig.java          # Конфигурация MVC
│   └── OpenApiConfig.java      # Конфигурация OpenAPI
│
├── 📁 controller/              # REST контроллеры
│   ├── AuthController.java     # Аутентификация и регистрация
│   ├── UserController.java     # Управление пользователями
│   ├── AdsController.java      # Управление объявлениями
│   └── CommentController.java  # Управление комментариями
│
├── 📁 service/                 # Бизнес-логика
│   ├── 📁 impl/               # Реализации сервисов
│   ├── 📁 mapped/             # Мапперы (DTO ↔ Entity)
│   ├── AuthService.java       # Интерфейс аутентификации
│   ├── UserService.java       # Интерфейс пользователей
│   ├── AdsService.java        # Интерфейс объявлений
│   ├── CommentService.java    # Интерфейс комментариев
│   ├── ImageService.java      # Работа с изображениями
│   └── UserAuthServise.java   # Вспомогательный сервис безопасности
│
├── 📁 repository/              # Доступ к данным (JPA)
│   ├── UsersRepository.java
│   ├── AdsRepository.java
│   └── CommentsRepository.java
│
├── 📁 model/                   # Сущности базы данных
│   ├── Users.java             # Пользователь (реализует UserDetails)
│   ├── Ads.java               # Объявление
│   └── Comments.java          # Комментарий
│
├── 📁 dto/                     # Data Transfer Objects
│   ├── UsersDTO.java          # DTO пользователя
│   ├── RegisterDTO.java       # DTO регистрации
│   ├── LoginDTO.java          # DTO входа
│   ├── AdDTO.java             # DTO объявления (краткий)
│   ├── FullAd.java            # DTO объявления (полный)
│   └── CommentDTO.java        # DTO комментария
│
└── 📁 filter/                  # HTTP фильтры
└── BasicAuthCorsFilter.java # Фильтр для CORS с Basic Auth
🚀 Быстрый старт
Предварительные требования
Java 11+ (рекомендуется OpenJDK 11)

Maven 3.6+

PostgreSQL 12+ (или Docker)

Git

Установка и запуск
bash
# 1. Клонировать репозиторий
git clone <repository-url>
cd homework-platform

# 2. Настроить базу данных
# Создайте базу данных PostgreSQL:
createdb market
# или используйте Docker:
docker run --name postgres -e POSTGRES_PASSWORD=test -e POSTGRES_DB=market -p 5432:5432 -d postgres:15

# 3. Настроить приложение
# Отредактируйте application.properties:
# spring.datasource.url=jdbc:postgresql://localhost:5432/market
# spring.datasource.username=postgres
# spring.datasource.password=test

# 4. Собрать и запустить
mvn clean package
java -jar target/ads-0.0.1-SNAPSHOT.jar

# Или запустить напрямую:
mvn spring-boot:run
Docker запуск
bash
# Сборка и запуск всего стека через docker-compose
docker-compose up --build

# Только база данных
docker-compose up postgres
🔐 Безопасность
Аутентификация
Тип: HTTP Basic Authentication

Хеширование паролей: BCrypt

Сессии: Stateless (без сессий)

Роли пользователей
USER: Обычный пользователь (может создавать объявления и комментарии)

ADMIN: Администратор (полный доступ ко всем ресурсам)

Защищенные эндпоинты
Все эндпоинты кроме /login, /register, /ads (GET), /ads/{id} (GET) требуют аутентификации

Каждый пользователь может редактировать/удалять только свои ресурсы

Администратор имеет доступ ко всем операциям

📡 API Документация
Основные эндпоинты
Метод	Путь	Описание	Доступ
POST	/login	Аутентификация пользователя	Публичный
POST	/register	Регистрация нового пользователя	Публичный
GET	/users/me	Получить профиль текущего пользователя	Аутентифицированный
PATCH	/users/me	Обновить профиль пользователя	Аутентифицированный
POST	/users/set_password	Сменить пароль	Аутентифицированный
PATCH	/users/me/image	Обновить аватар	Аутентифицированный
GET	/ads	Получить все объявления	Публичный
POST	/ads	Создать новое объявление	Аутентифицированный
GET	/ads/{id}	Получить объявление по ID	Публичный
DELETE	/ads/{id}	Удалить объявление	Автор или Админ
PATCH	/ads/{id}	Обновить объявление	Автор или Админ
POST	/ads/{id}/image	Обновить изображение объявления	Автор или Админ
GET	/ads/me	Получить мои объявления	Аутентифицированный
GET	/ads/{id}/comments	Получить комментарии объявления	Публичный
POST	/ads/{id}/comments	Добавить комментарий	Аутентифицированный
DELETE	/ads/{adId}/comments/{commentId}	Удалить комментарий	Автор или Админ
PATCH	/ads/{adId}/comments/{commentId}	Обновить комментарий	Автор или Админ
Документация Swagger
После запуска приложения доступна интерактивная документация:

Swagger UI: http://localhost:8080/swagger-ui.html

OpenAPI Spec: http://localhost:8080/v3/api-docs

🗄️ База данных
Схема данных
sql
-- Основные таблицы
CREATE TABLE users (
id BIGSERIAL PRIMARY KEY,
username VARCHAR(32) UNIQUE NOT NULL,
password VARCHAR(64) NOT NULL,
first_name VARCHAR(16) NOT NULL,
last_name VARCHAR(16) NOT NULL,
phone VARCHAR(18),
role VARCHAR(20) NOT NULL DEFAULT 'USER',
image_url VARCHAR(500),
enabled BOOLEAN DEFAULT true
);

CREATE TABLE ads (
pk BIGSERIAL PRIMARY KEY,
image_url VARCHAR(500),
description VARCHAR(64) NOT NULL,
price INTEGER NOT NULL,
title VARCHAR(32) NOT NULL,
author_id BIGINT NOT NULL REFERENCES users(id)
);

CREATE TABLE comments (
pk BIGSERIAL PRIMARY KEY,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
text TEXT,
users_id BIGINT NOT NULL REFERENCES users(id),
ad_id BIGINT NOT NULL REFERENCES ads(id)
);

-- Индексы для производительности
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_ads_author ON ads(author_id);
CREATE INDEX idx_comments_ad ON comments(ad_id);
CREATE INDEX idx_comments_created ON comments(created_at DESC);
Миграции
Используется Liquibase для управления миграциями

Файлы миграций: src/main/resources/liquibase/

Автоматическое применение при запуске приложения

История миграций: таблицы DATABASECHANGELOG, DATABASECHANGELOGLOCK

🧪 Тестирование
Запуск тестов
bash
# Все тесты
mvn test

# С покрытием кода
mvn jacoco:report

# Интеграционные тесты
mvn verify -P integration
Профили
dev: Разработка (логирование DEBUG, H2 база)

test: Тестирование (интеграционные тесты)

prod: Продакшен (логирование WARN, PostgreSQL)

bash
# Запуск с профилем разработки
mvn spring-boot:run -Dspring.profiles.active=dev

# Запуск с профилем продакшена
java -jar app.jar --spring.profiles.active=prod
📁 Структура конфигурации
Основные конфигурационные файлы
text
src/main/resources/
├── application.properties          # Основная конфигурация
├── application-dev.properties      # Конфигурация для разработки
├── application-prod.properties     # Конфигурация для продакшена
├── banner.txt                     # ASCII баннер при запуске
└── liquibase/
├── changelog-master.yml       # Главный файл миграций
└── scripts/                   # SQL скрипты миграций
Ключевые настройки
properties
# Основные настройки
server.port=8080
spring.application.name=homework-platform

# База данных
spring.datasource.url=jdbc:postgresql://localhost:5432/market
spring.datasource.username=postgres
spring.datasource.password=test

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Загрузка файлов
file.upload.dir=./uploads
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# CORS настройки (для разработки)
security.cors.allowed-origins=http://localhost:3000
security.cors.allow-credentials=true
🛠️ Разработка
Стиль кода
Именование:

Классы: PascalCase (UserController)

Методы: camelCase (getUsers)

Переменные: camelCase (userName)

Константы: UPPER_SNAKE_CASE (MAX_FILE_SIZE)

Документация:

Все публичные классы и методы должны иметь JavaDoc

Сложная бизнес-логика документируется подробно

Использовать аннотации Swagger для API

Логирование:

Использовать SLF4J с Lombok @Slf4j

Уровни: ERROR (ошибки), WARN (предупреждения), INFO (информация), DEBUG (отладка)

Рабочий процесс
bash
# 1. Клонировать и настроить проект
git clone <url>
cd homework-platform

# 2. Создать ветку для задачи
git checkout -b feature/new-endpoint

# 3. Установить зависимости
mvn clean install

# 4. Запустить локально для разработки
mvn spring-boot:run -Dspring.profiles.active=dev

# 5. Протестировать изменения
mvn test

# 6. Запустить проверку качества кода
mvn checkstyle:check
mvn pmd:check
mvn spotbugs:check

# 7. Создать Pull Request
Проверка качества кода
bash
# Проверка стиля кода
mvn checkstyle:check

# Статический анализ кода
mvn pmd:check
mvn spotbugs:check

# Покрытие тестами
mvn jacoco:check

# Проверка зависимостей
mvn dependency:check
mvn versions:display-dependency-updates
🚀 Деплоймент
Подготовка к продакшену
Безопасность:

Заменить хардкодные пароли на переменные окружения

Отключить Swagger в продакшене

Настроить HTTPS

Ограничить CORS origins

Производительность:

Настроить connection pool (HikariCP)

Включить кэширование

Настроить GZIP сжатие

Конфигурировать thread pool

Мониторинг:

Включить Spring Boot Actuator

Настроить метрики Prometheus

Настроить логирование в файл

Контейнеризация
dockerfile
# Dockerfile
FROM openjdk:11-jre-slim
WORKDIR /app
COPY target/*.jar app.jar
RUN useradd -m -u 1000 appuser && chown -R appuser:appuser /app
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
yaml
# docker-compose.prod.yml
version: '3.8'
services:
app:
build: .
ports:
- "8080:8080"
environment:
- SPRING_PROFILES_ACTIVE=prod
- DB_URL=jdbc:postgresql://postgres:5432/market
- DB_USERNAME=${DB_USERNAME}
- DB_PASSWORD=${DB_PASSWORD}
depends_on:
- postgres
restart: unless-stopped

postgres:
image: postgres:15-alpine
environment:
POSTGRES_DB: market
POSTGRES_USER: ${DB_USERNAME}
POSTGRES_PASSWORD: ${DB_PASSWORD}
volumes:
- postgres-data:/var/lib/postgresql/data
restart: unless-stopped

volumes:
postgres-data:
📊 Мониторинг и логирование
Spring Boot Actuator
properties
# Включение actuator endpoints
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=when_authorized
Доступные эндпоинты:

/actuator/health - Health check

/actuator/info - Информация о приложении

/actuator/metrics - Метрики приложения

/actuator/prometheus - Метрики в формате Prometheus

Логирование
properties
# Настройки логирования
logging.level.ru.skypro.homework=DEBUG
logging.level.org.springframework.web=INFO
logging.level.org.hibernate.SQL=DEBUG
logging.file.name=logs/application.log
logging.file.max-size=10MB
logging.file.max-history=30
🐛 Отладка и устранение неполадок
Частые проблемы
Проблемы с подключением к БД:

bash
# Проверить подключение к PostgreSQL
psql -h localhost -p 5432 -U postgres -d market

# Проверить настройки в application.properties
Проблемы с загрузкой файлов:

bash
# Проверить права доступа к директории uploads
ls -la ./uploads
chmod 755 ./uploads
Проблемы с CORS:

Проверить настройки в WebSecurityConfig

Проверить заголовки в браузере DevTools

Инструменты отладки
bash
# Запуск с debug портом
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"

# Анализ дампа памяти
jmap -heap <pid>
jstack <pid>

# Мониторинг производительности
jstat -gc <pid> 1000
📈 Дальнейшее развитие
Планируемые улучшения
Функциональность:

Поиск объявлений с фильтрами

Избранные объявления

Уведомления по email

Пагинация и сортировка

Социальные функции (лайки, рейтинги)

Технические улучшения:

Кэширование через Redis

Асинхронная обработка

WebSocket для real-time уведомлений

Микросервисная архитектура

GraphQL API

Безопасность:

JWT аутентификация

OAuth2 интеграция

Двухфакторная аутентификация

Rate limiting

Audit logging

Рекомендации по масштабированию
Вертикальное масштабирование:

Увеличение памяти JVM

Настройка пулов соединений

Оптимизация запросов к БД

Горизонтальное масштабирование:

Сессии в Redis

Балансировка нагрузки

Репликация базы данных

Оптимизация:

CDN для статических файлов

Сжатие ответов

Минимизация запросов к БД

🤝 Вклад в проект
Форкните репозиторий

Создайте ветку для своей фичи (git checkout -b feature/amazing-feature)

Закоммитьте изменения (git commit -m 'Add amazing feature')

Запушьте в ветку (git push origin feature/amazing-feature)

Откройте Pull Request

Guidelines для контрибьютеров
Следуйте существующему стилю кода

Добавляйте тесты для новой функциональности

Обновляйте документацию

Используйте осмысленные названия коммитов

Один PR = одна функциональность

📄 Лицензия
Этот проект лицензирован под MIT License - смотрите файл LICENSE для деталей.

📞 Поддержка
Полезные ссылки
Документация: /swagger-ui.html

Health check: /actuator/health

Метрики: /actuator/metrics

Логи: logs/application.log

Контакты
Для вопросов и предложений:

Создайте Issue в репозитории

Напишите на email: [ваш-email@example.com]

Присоединитесь к нашему Slack/Telegram каналу

Примечание: Этот проект предназначен для учебных целей. Для использования в production требуется дополнительная настройка безопасности и производительности.

Последнее обновление: 12.12.2025

