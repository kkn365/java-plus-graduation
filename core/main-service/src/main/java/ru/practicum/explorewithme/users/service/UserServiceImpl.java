package ru.practicum.explorewithme.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.exception.DataAlreadyExistException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.users.dto.NewUserRequest;
import ru.practicum.explorewithme.users.dto.UserDto;
import ru.practicum.explorewithme.users.mapper.UserMapper;
import ru.practicum.explorewithme.users.model.User;
import ru.practicum.explorewithme.users.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto createUser(NewUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DataAlreadyExistException("User with email " + request.getEmail() + " already exists.");
        }
        User user = userMapper.toUser(request);

        return userMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto getById(Long userId) {
        User user = getUser(userId);

        return userMapper.toUserDto(user);
    }

    @Override
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {

        if (ids != null && !ids.isEmpty()) {
            return userRepository.findAllById(ids).stream()
                    .map(userMapper::toUserDto)
                    .collect(Collectors.toList());
        }
        return userRepository.findAll(PageRequest.of(from / size, size)).stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}