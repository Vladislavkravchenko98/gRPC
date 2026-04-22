package com.example.grpcuser.mapper;

import com.example.grpcuser.domain.CarEntity;
import com.example.grpcuser.domain.UserEntity;
import com.example.grpcuser.dto.CarInputCommand;
import com.example.grpcuser.dto.CreateUserCommand;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserEntity toEntity(CreateUserCommand command) {
        UserEntity user = new UserEntity();
        user.setExternalId(command.externalId());
        user.setFirstName(command.firstName());
        user.setLastName(command.lastName());
        user.setEmail(command.email().toLowerCase());
        return user;
    }

    public CarEntity toEntity(CarInputCommand command) {
        CarEntity car = new CarEntity();
        car.setVin(command.vin());
        car.setBrand(command.brand());
        car.setModel(command.model());
        car.setProductionYear(command.productionYear());
        return car;
    }
}
