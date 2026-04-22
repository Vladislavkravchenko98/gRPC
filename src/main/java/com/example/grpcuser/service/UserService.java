package com.example.grpcuser.service;

import com.example.grpcuser.domain.UserEntity;
import com.example.grpcuser.dto.AddCarsCommand;
import com.example.grpcuser.dto.CreateUserCommand;

public interface UserService {

    UserEntity createUser(CreateUserCommand command);

    UserEntity addCars(AddCarsCommand command);
}
