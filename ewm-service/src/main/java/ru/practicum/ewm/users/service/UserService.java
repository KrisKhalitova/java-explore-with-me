package ru.practicum.ewm.users.service;

import ru.practicum.ewm.users.dto.NewUserRequest;
import ru.practicum.ewm.users.dto.UserDto;

import java.util.Collection;
import java.util.List;

public interface UserService {

    UserDto addNewUser(NewUserRequest newUserRequest);

    Collection<UserDto> getUsers(List<Long> ids, int from, int size);

    void deleteUser(Long userId);
}
