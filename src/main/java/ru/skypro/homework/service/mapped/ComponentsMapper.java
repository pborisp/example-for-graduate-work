package ru.skypro.homework.service.mapped;

import org.springframework.stereotype.Component;
import ru.skypro.homework.dto.CommentDTO;
import ru.skypro.homework.model.Comments;

import java.time.ZoneOffset;

@Component
public class ComponentsMapper {
    //преобразование CommentDTO в Comments - при регистрации
    public Comments toComments(CommentDTO dto) {
        Comments comments = new Comments();
        comments.setText(dto.getText());
        return comments;
    }

    //преобразование Comments в CommentDTO
    public CommentDTO getCommentDTO(Comments comments) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setText(comments.getText());
        commentDTO.setPk(comments.getPk());
        if (comments.getCreatedAt() != null) {
            commentDTO.setCreatedAt(comments.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli());
        }
        if (comments.getAuthor() != null) {
            commentDTO.setAuthorFirstName(comments.getAuthor().getFirstName());
            commentDTO.setAuthorImage(comments.getAuthor().getImage());
            commentDTO.setAuthor(comments.getAuthor().getId());
        }
        return commentDTO;
    }

    //преобразование текста CommentDTO в Comments
    public void updateComment(CommentDTO dto, Comments comments) {
        if (dto.getText() != null && dto.getText().isBlank()) {
            comments.setText(dto.getText());
        }
    }

}
