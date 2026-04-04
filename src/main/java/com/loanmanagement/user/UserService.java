package com.loanmanagement.user;

import com.loanmanagement.exception.BadRequestException;
import com.loanmanagement.exception.ResourceNotFoundException;
import com.loanmanagement.user.dto.ChangePasswordRequestDTO;
import com.loanmanagement.user.dto.UpdateUserRequestDTO;
import com.loanmanagement.user.dto.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.loanmanagement.user.dto.UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        User user = findUserById(id);
        return userMapper.userToUserResponseDTO(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::userToUserResponseDTO);
    }

    @Transactional
    public UserResponseDTO updateUser(Long id, UpdateUserRequestDTO request) {
        User user = findUserById(id);

        if (request.getName() != null) user.setName(request.getName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getRole() != null) user.setRole(request.getRole());
        if (request.getActive() != null) user.setActive(request.getActive());

        return userMapper.userToUserResponseDTO(userRepository.save(user));
    }

    @Transactional
    public void changePassword(User currentUser, ChangePasswordRequestDTO request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }
        if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPassword())) {
            throw new BadRequestException("New password must be different from the current password");
        }

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);
        log.info("Password changed for user: {}", currentUser.getEmail());
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id " + id);
        }
        userRepository.deleteById(id);
        log.info("User deleted: id={}", id);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public UserResponseDTO mapToResponse(User user) {
        return userMapper.userToUserResponseDTO(user);
    }
}