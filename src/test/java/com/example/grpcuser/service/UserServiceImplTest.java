package com.example.grpcuser.service;

import com.example.grpcuser.domain.UserEntity;
import com.example.grpcuser.dto.AddCarsCommand;
import com.example.grpcuser.dto.CarInputCommand;
import com.example.grpcuser.dto.CreateUserCommand;
import com.example.grpcuser.exception.DuplicateCarVinException;
import com.example.grpcuser.exception.UserAlreadyExistsException;
import com.example.grpcuser.exception.UserNotFoundException;
import com.example.grpcuser.mapper.UserMapper;
import com.example.grpcuser.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private CreateUserCommand createUserCommand;

    @BeforeEach
    void setUp() {
        createUserCommand = new CreateUserCommand("ext-001", "Jane", "Doe", "jane@corp.com");
    }

    @Test
    void createUser_ShouldThrow_WhenExternalIdAlreadyExists() {
        when(userRepository.existsByExternalId("ext-001")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(createUserCommand))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("externalId");
    }

    @Test
    void addCars_ShouldThrow_WhenUserNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findWithCarsById(id)).thenReturn(Optional.empty());

        AddCarsCommand command = new AddCarsCommand(id, List.of(new CarInputCommand("VIN1", "Tesla", "Model3", 2024)));

        assertThatThrownBy(() -> userService.addCars(command))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void addCars_ShouldThrow_WhenVinDuplicated() {
        UUID id = UUID.randomUUID();
        UserEntity user = new UserEntity();
        var existingCar = new com.example.grpcuser.domain.CarEntity();
        existingCar.setVin("VIN1");
        user.addCars(List.of(existingCar));

        when(userRepository.findWithCarsById(id)).thenReturn(Optional.of(user));
        when(userMapper.toEntity(any(CarInputCommand.class))).thenAnswer(invocation -> {
            CarInputCommand cmd = invocation.getArgument(0);
            var car = new com.example.grpcuser.domain.CarEntity();
            car.setVin(cmd.vin());
            car.setBrand(cmd.brand());
            car.setModel(cmd.model());
            car.setProductionYear(cmd.productionYear());
            return car;
        });

        AddCarsCommand command = new AddCarsCommand(id, List.of(new CarInputCommand("VIN1", "BMW", "X5", 2020)));

        assertThatThrownBy(() -> userService.addCars(command))
                .isInstanceOf(DuplicateCarVinException.class);
    }

    @Test
    void createUser_ShouldPersist_WhenDataValid() {
        UserEntity entity = new UserEntity();
        entity.setExternalId("ext-001");
        entity.setFirstName("Jane");
        entity.setLastName("Doe");
        entity.setEmail("jane@corp.com");

        when(userRepository.existsByExternalId("ext-001")).thenReturn(false);
        when(userRepository.existsByEmail("jane@corp.com")).thenReturn(false);
        when(userMapper.toEntity(createUserCommand)).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(entity);

        UserEntity saved = userService.createUser(createUserCommand);

        assertThat(saved.getEmail()).isEqualTo("jane@corp.com");
    }
}
