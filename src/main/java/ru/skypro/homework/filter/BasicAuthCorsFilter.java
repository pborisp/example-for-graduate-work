package ru.skypro.homework.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Фильтр для добавления CORS заголовков при использовании Basic аутентификации.
 * <p>
 * Этот фильтр решает проблему, когда браузеры блокируют кросс-доменные запросы
 * с учетными данными (credentials), если не установлен заголовок
 * {@code Access-Control-Allow-Credentials: true}.
 * </p>
 *
 * <p><b>Проблема, которую решает фильтр:</b></p>
 * <pre>
 * Без этого фильтра:                С этим фильтром:
 * ┌─────────────┐                  ┌─────────────┐
 * │   Browser   │                  │   Browser   │
 * │ (localhost:3000) │             │ (localhost:3000) │
 * └──────┬──────┘                  └──────┬──────┘
 *        │ (fetch with credentials)        │ (fetch with credentials)
 *        ▼                                 ▼
 * ┌─────────────┐                  ┌─────────────┐
 * │   Server    │                  │   Server    │
 * │ (localhost:8080) │             │ (localhost:8080) │
 * └─────────────┘                  └─────────────┘
 *        │                                 │
 *        ❌ No CORS headers                ✅ Access-Control-Allow-Credentials: true
 *        │                                 │
 *        ▼                                 ▼
 * ❌ CORS error:                          ✅ Request succeeds
 * "Credentials not allowed"
 * </pre>
 *
 * <p><b>Когда требуется:</b></p>
 * <ul>
 *   <li>Frontend и backend на разных доменах/портах</li>
 *   <li>Используется HTTP Basic аутентификация</li>
 *   <li>Запросы отправляются с {@code credentials: 'include'}</li>
 *   <li>Браузеры Chrome/Firefox/Safari</li>
 * </ul>
 *
 * <p><b>Пример запроса из JavaScript:</b></p>
 * <pre>
 * fetch('http://localhost:8080/api/data', {
 *   method: 'GET',
 *   credentials: 'include',  // Отправка cookies/авторизации
 *   headers: {
 *     'Authorization': 'Basic ' + btoa('user:pass')
 *   }
 * })
 * </pre>
 *
 * <p><b>Альтернативы:</b></p>
 * <ul>
 *   <li>Использовать {@code @CrossOrigin} в контроллерах</li>
 *   <li>Настроить CORS в {@link org.springframework.web.cors.CorsConfiguration}</li>
 *   <li>Использовать Spring Security CORS конфигурацию</li>
 * </ul>
 *
 * <p><b>Важно:</b> Этот фильтр должен выполняться ДО Spring Security фильтров.</p>
 *
 * @see org.springframework.web.cors
 * @see org.springframework.web.filter.OncePerRequestFilter
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS">MDN CORS Documentation</a>
 */
@Component
public class BasicAuthCorsFilter extends OncePerRequestFilter {

    /**
     * Основной метод фильтрации, вызываемый для каждого HTTP запроса.
     * <p>
     * Добавляет заголовок {@code Access-Control-Allow-Credentials: true}
     * ко всем ответам, позволяя браузерам отправлять учетные данные
     * при кросс-доменных запросах.
     * </p>
     *
     * <p><b>Порядок выполнения фильтров:</b></p>
     * <ol>
     *   <li>BasicAuthCorsFilter (этот фильтр) - добавляет CORS заголовки</li>
     *   <li>Spring Security фильтры - проверяют аутентификацию</li>
     *   <li>DispatcherServlet - обрабатывает запрос</li>
     *   <li>Контроллеры - возвращают ответ</li>
     * </ol>
     *
     * <p><b>Добавляемые заголовки:</b></p>
     * <table>
     *   <tr><th>Заголовок</th><th>Значение</th><th>Назначение</th></tr>
     *   <tr><td>Access-Control-Allow-Credentials</td><td>true</td>
     *       <td>Разрешает отправку cookies/авторизации</td></tr>
     * </table>
     *
     * <p><b>Безопасность:</b> Убедитесь, что {@code Access-Control-Allow-Origin}
     * не установлен в {@code *} при использовании credentials.
     * Должен быть указан конкретный origin.</p>
     *
     * @param httpServletRequest  HTTP запрос
     * @param httpServletResponse HTTP ответ
     * @param filterChain         цепочка фильтров для продолжения обработки
     * @throws ServletException если произошла ошибка Servlet
     * @throws IOException      если произошла ошибка ввода/вывода
     * @see HttpServletResponse#addHeader(String, String)
     * @see FilterChain#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Критически важный заголовок для CORS с credentials
        httpServletResponse.addHeader("Access-Control-Allow-Credentials", "true");

        // Продолжаем обработку запроса следующими фильтрами
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
    /**
     * Опционально: можно добавить дополнительные CORS заголовки для полной совместимости.
     * <p>
     * Рекомендуемый минимальный набор заголовков:
     * </p>
     * <pre>
     * // Разрешенные origins (должны быть конкретные, не '*')
     * response.addHeader("Access-Control-Allow-Origin", "http://localhost:3000");
     *
     * // Разрешенные методы
     * response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
     *
     * // Разрешенные заголовки
     * response.addHeader("Access-Control-Allow-Headers",
     *     "Authorization, Content-Type, X-Requested-With");
     *
     * // Время кэширования preflight запросов
     * response.addHeader("Access-Control-Max-Age", "3600");
     * </pre>
     */

    /**
     * Для отладки можно добавить логирование.
     * <p>
     * <b>Пример:</b>
     * </p>
     * <pre>
     * private static final Logger logger = LoggerFactory.getLogger(BasicAuthCorsFilter.class);
     *
     * protected void doFilterInternal(...) {
     *     String origin = request.getHeader("Origin");
     *     logger.debug("CORS request from origin: {}", origin);
     *
     *     if (origin != null && allowedOrigins.contains(origin)) {
     *         response.addHeader("Access-Control-Allow-Origin", origin);
     *         response.addHeader("Access-Control-Allow-Credentials", "true");
     *     }
     *
     *     filterChain.doFilter(request, response);
     * }
     * </pre>
     */
}
