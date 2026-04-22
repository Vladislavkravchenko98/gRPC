package com.example.grpcuser.service;

import com.example.grpcuser.domain.CarEntity;
import com.example.grpcuser.domain.UserEntity;
import com.example.grpcuser.dto.AddCarsCommand;
import com.example.grpcuser.dto.CreateUserCommand;
import com.example.grpcuser.exception.DuplicateCarVinException;
import com.example.grpcuser.exception.UserAlreadyExistsException;
import com.example.grpcuser.exception.UserNotFoundException;
import com.example.grpcuser.mapper.UserMapper;
import com.example.grpcuser.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public UserEntity createUser(CreateUserCommand command) {
        if (userRepository.existsByExternalId(command.externalId())) {
            throw new UserAlreadyExistsException("User with externalId already exists: " + command.externalId());
        }

        if (userRepository.existsByEmail(command.email().toLowerCase())) {
            throw new UserAlreadyExistsException("User with email already exists: " + command.email());
        }

        UserEntity user = userMapper.toEntity(command);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public UserEntity addCars(AddCarsCommand command) {
        UUID userId = command.userId();
        UserEntity user = userRepository.findWithCarsById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        Set<String> existingVins = user.getCars().stream()
                .map(CarEntity::getVin)
                .collect(Collectors.toSet());

        List<CarEntity> carsToAdd = command.cars().stream()
                .map(userMapper::toEntity)
                .toList();

        for (CarEntity car : carsToAdd) {
            if (!existingVins.add(car.getVin())) {
                throw new DuplicateCarVinException("Duplicate VIN for user " + userId + ": " + car.getVin());
            }
        }

        user.addCars(carsToAdd);
        return userRepository.save(user);
    }
}
