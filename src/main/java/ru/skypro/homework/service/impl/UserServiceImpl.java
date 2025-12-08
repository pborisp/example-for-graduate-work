package ru.skypro.homework.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.PasswordDTO;
import ru.skypro.homework.dto.UserForUpdateDTO;
import ru.skypro.homework.dto.UsersDTO;
import ru.skypro.homework.model.Users;
import ru.skypro.homework.repository.UsersRepository;
import ru.skypro.homework.service.ImageService;
import ru.skypro.homework.service.UserService;
import ru.skypro.homework.service.mapped.UsersMapper;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UsersRepository usersRepository;
    private final UsersMapper usersMapper;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;

    @Override
    public UsersDTO getUsers() {
        Users user = getCurrentUser();
        UsersDTO dto = new UsersDTO();
        dto = usersMapper.toUsersDTO(user);
        return dto;
    }

    @Override
    public UserForUpdateDTO updateUser(UserForUpdateDTO userForUpdateDTO) {
        Users user = getCurrentUser();
        usersMapper.updateUser(userForUpdateDTO, user);
        usersRepository.save(user);
        return userForUpdateDTO;
    }

    @Override
    @Transactional
    public void updateUserImage(MultipartFile image) throws IOException {
        Users user = getCurrentUser();
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image image is empty");
        }
        String path = imageService.saveImage(image);
        user.setImage(path);
        usersRepository.save(user);
        //сбрасываем принудительно кэш для текущего пользователя
        usersRepository.flush();
    }

    @Override
    public void setPassword(PasswordDTO passwordDTO) {
        Users user = getCurrentUser();
        // Проверяем соответсвие введенного пароля существующему - если не соответствует - выбрасываем исключение
        if (!passwordEncoder.matches(passwordDTO.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Wrong password");
        }
        // Шифруем и устанавливаем новый пароль
        String newEncodedPassword = passwordEncoder.encode(passwordDTO.getNewPassword());
        user.setPassword(newEncodedPassword);
        usersRepository.save(user);
        updatePasswordInSpringSecurity(user.getUsername(), user.getPassword());
    }

    public Users getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        String username = authentication.getName();
        if (username == null || username.equals("anonymousUser")) {
            throw new RuntimeException("User is not authenticated");
        }
        Optional<Users> users = usersRepository.findByUsername(authentication.getName());
        return users.orElseThrow(() -> new RuntimeException("User not found"));
    }
    private void updatePasswordInSpringSecurity(String username, String newPassword) {
        // В реальном приложении здесь может быть логика обновления пароля
        // в UserDetailsManager, но JdbcUserDetailsManager автоматически обновляет пароль
        // при изменении через userDetailsManager.updatePassword()
    }
}
