package ru.skypro.homework.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.webjars.NotFoundException;
import ru.skypro.homework.dto.CommentDTO;
import ru.skypro.homework.dto.CommentsList;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.model.Ads;
import ru.skypro.homework.model.Comments;
import ru.skypro.homework.model.Users;
import ru.skypro.homework.repository.AdsRepository;
import ru.skypro.homework.repository.CommentsRepository;
import ru.skypro.homework.service.CommentService;
import ru.skypro.homework.service.UserAuthServise;
import ru.skypro.homework.service.mapped.CommentsMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для управления комментариями.
 * <p>
 * Содержит бизнес-логику работы с комментариями к объявлениям.
 * Реализует проверку прав доступа, валидацию данных и обеспечение целостности связей.
 * </p>
 *
 * <p><b>Архитектурные особенности:</b></p>
 * <ul>
 *   <li><b>Транзакционность:</b> все методы выполняются в транзакциях</li>
 *   <li><b>Безопасность:</b> проверка прав доступа (автор или администратор)</li>
 *   <li><b>Целостность:</b> проверка принадлежности комментария к объявлению</li>
 *   <li><b>Валидация:</b> проверка входных данных перед обработкой</li>
 * </ul>
 *
 * <p><b>Зависимости:</b></p>
 * <ul>
 *   <li>{@link CommentsRepository} - доступ к данным комментариев</li>
 *   <li>{@link AdsRepository} - доступ к данным объявлений</li>
 *   <li>{@link UserAuthServise} - определение текущего пользователя и проверка прав</li>
 *   <li>{@link CommentsMapper} - преобразование между сущностями и DTO</li>
 * </ul>
 *
 * @see CommentService
 * @see ru.skypro.homework.controller.CommentController
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final CommentsRepository commentsRepository;
    private final UserAuthServise userAuthServise;
    private final CommentsMapper commentsMapper;
    private final AdsRepository adsRepository;

    /**
     * Получает все комментарии для указанного объявления.
     *
     * @param id идентификатор объявления
     * @return список комментариев с пагинацией
     */
    @Override
    public CommentsList getCommentsByAds(Long id) {
        userAuthServise.getCurrentUser();

        // Получаем комментарии
        Optional<List<Comments>> commentsOpt = commentsRepository.findByAd_pk(id);
        List<Comments> comments = commentsOpt.orElse(Collections.EMPTY_LIST);

        // Преобразуем в DTO
        List<CommentDTO> dto = comments.stream()
                .map(commentsMapper::toDto)
                .collect(Collectors.toList());

        // Создаем результат
        CommentsList commentsList = new CommentsList();
        commentsList.setCount(comments.size());
        commentsList.setResults(dto);
        return commentsList;
    }

    /**
     * Создает новый комментарий к объявлению.
     *
     * <p><b>Процесс:</b></p>
     * <ol>
     *   <li>Проверка текста комментария</li>
     *   <li>Поиск объявления по ID</li>
     *   <li>Создание сущности через маппер</li>
     *   <li>Сохранение в базу данных</li>
     *   <li>Возврат созданного комментария в формате DTO</li>
     * </ol>
     *
     * @param adId идентификатор объявления
     * @param text текст комментария
     * @return DTO созданного комментария
     * @throws IllegalArgumentException если текст комментария пустой
     * @throws NotFoundException        если объявление не найдено
     */
    @Override
    public CommentDTO createComment(Long adId, String text) {
        // Получаем текущего пользователя
        Users author = userAuthServise.getCurrentUser();
        // Проверяем текст комментария
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment of ads does not exist");
        }
        // Находим объявление
        Ads ads = adsRepository.findById(adId).orElseThrow(() -> new NotFoundException("Ads not found"));

        // Создаем комментарий
        Comments comments = commentsMapper.toComments(text, author, ads);

        Comments savedComment = commentsRepository.save(comments);
        // Возвращаем DTO
        return commentsMapper.toDto(savedComment);
    }

    /**
     * Удаляет комментарий.
     * <ol>
     *   <li>Проверка прав доступа (автор или администратор)</li>
     *   <li>Поиск объявления и комментария</li>
     *   <li>Проверка принадлежности комментария к объявлению (отсутствует!)</li>
     *   <li>Удаление комментария</li>
     * </ol>
     *
     * @param adId      идентификатор объявления (для проверки принадлежности)
     * @param commentId идентификатор комментария для удаления
     * @throws AccessDeniedException если нет прав на удаление
     * @throws NotFoundException     если объявление или комментарий не найдены
     */
    @Override
    public void deleteComment(Long adId, Long commentId) {
        Users author = userAuthServise.getCurrentUser();
        // Проверяем права доступа
        boolean isAdmin = author.getRole().equals(Role.ADMIN);
        boolean isAuthor = userAuthServise.getCommentsAuthorByPk(commentId).equals(author.getId());

        if (!isAdmin && !isAuthor) {
            throw new AccessDeniedException("Only ADMIN or comment author can edit comments");
        }
        adsRepository.findById(adId).orElseThrow(() -> new NotFoundException("Ads not found"));
        // Находим комментарий
        Comments comments = commentsRepository.findByPk(commentId).orElseThrow(() -> new NotFoundException("Comments not found"));
        if (!adId.equals(comments.getAd().getPk())) {
            throw new IllegalArgumentException("Comment does not belong to this ad");
        }
        commentsRepository.delete(comments);
    }

    /**
     * Обновляет текст комментария.
     * <p><b>Процесс:</b></p>
     * <ol>
     *   <li>Проверка текста комментария</li>
     *   <li>Проверка прав доступа (автор или администратор)</li>
     *   <li>Поиск объявления и комментария</li>
     *   <li>Проверка принадлежности комментария к объявлению</li>
     *   <li>Обновление текста и сохранение</li>
     *   <li>Возврат обновленного комментария в формате DTO</li>
     * </ol>
     *
     * @param adId      идентификатор объявления
     * @param commentId идентификатор комментария
     * @param text      новый текст комментария
     * @return DTO обновленного комментария
     * @throws IllegalArgumentException если текст пустой или комментарий не принадлежит объявлению
     * @throws AccessDeniedException    если нет прав на обновление
     * @throws NotFoundException        если объявление или комментарий не найдены
     */
    @Override
    public CommentDTO updateComment(Long adId, Long commentId, String text) {
        Users author = userAuthServise.getCurrentUser();
        // Проверяем права доступа
        boolean isAdmin = author.getRole().equals(Role.ADMIN);
        boolean isAuthor = userAuthServise.getCommentsAuthorByPk(commentId).equals(author.getId());
        // Проверяем текст
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment of ads does not exist");
        }
        // Проверяем права
        if (!isAdmin && !isAuthor) {
            throw new AccessDeniedException("Only ADMIN or comment author can edit comments");
        }
        // Находим объявление и комментарий
        Ads ads = adsRepository.findById(adId).orElseThrow(() -> new NotFoundException("Ads not found"));
        Comments comments = commentsRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Comments not found"));
        //проверяем принадлежность комментария к объявлению
        if (comments.getAd() == null || !adId.equals(comments.getAd().getPk())) {
            throw new IllegalArgumentException("Comment does not belong to this ad");
        }
        // Обновляем текст
        comments.setText(text);
        Comments savedComment = commentsRepository.save(comments);
        return commentsMapper.toDto(savedComment);
    }
}
