package ru.skypro.homework.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.RegisterDTO;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.model.Users;
import ru.skypro.homework.repository.UsersRepository;
import ru.skypro.homework.service.AuthService;
import ru.skypro.homework.service.mapped.UsersMapper;

@Service
public class AuthServiceImpl implements AuthService {
    private final UsersRepository usersRepository;
    private final UsersMapper usersMapper;
    private final PasswordEncoder encoder;


    public AuthServiceImpl(UsersRepository usersRepository, UsersMapper usersMapper, PasswordEncoder encoder) {
        this.usersRepository = usersRepository;
        this.usersMapper = usersMapper;
        this.encoder = encoder;
    }

    @Override
    public boolean login(String userName, String password) {
        Users user = usersRepository.findByUsername(userName).orElseThrow(null);
        if (user == null) {
            return false;
        }
        if (!encoder.matches(password, user.getPassword())) {
            return false;
        }
        return true;
    }

    @Override
    public boolean register(RegisterDTO register) {
        if (usersRepository.existsByUsername(register.getUsername())) {
            return false;
        }
        if (register.getRole() != Role.ADMIN ||  register.getRole() != Role.USER) {
            return false;
        }
        Users user = usersMapper.toUsers(register);
        user.setPassword(encoder.encode(register.getPassword()));
        user.setEnabled(true);
        user.setRole(register.getRole());
        usersRepository.save(user);
        return true;
    }
}