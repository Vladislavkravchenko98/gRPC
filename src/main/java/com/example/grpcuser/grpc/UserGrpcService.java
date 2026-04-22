package com.example.grpcuser.grpc;

import com.example.grpcuser.domain.UserEntity;
import com.example.grpcuser.dto.AddCarsCommand;
import com.example.grpcuser.dto.CarInputCommand;
import com.example.grpcuser.dto.CreateUserCommand;
import com.example.grpcuser.exception.ApiErrorCode;
import com.example.grpcuser.exception.BusinessException;
import com.example.grpcuser.proto.AddCarsToUserRequest;
import com.example.grpcuser.proto.AddCarsToUserResponse;
import com.example.grpcuser.proto.Car;
import com.example.grpcuser.proto.CreateUserRequest;
import com.example.grpcuser.proto.CreateUserResponse;
import com.example.grpcuser.proto.UserServiceGrpc;
import com.example.grpcuser.service.UserService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@GrpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private final UserService userService;
    private final Validator validator;

    public UserGrpcService(UserService userService, Validator validator) {
        this.userService = userService;
        this.validator = validator;
    }

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<CreateUserResponse> responseObserver) {
        try {
            CreateUserCommand command = new CreateUserCommand(
                    request.getExternalId(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getEmail()
            );
            validate(command);

            UserEntity user = userService.createUser(command);
            responseObserver.onNext(toCreateResponse(user));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(mapException(e));
        }
    }

    @Override
    public void addCarsToUser(AddCarsToUserRequest request, StreamObserver<AddCarsToUserResponse> responseObserver) {
        try {
            AddCarsCommand command = new AddCarsCommand(
                    UUID.fromString(request.getUserId()),
                    request.getCarsList().stream()
                            .map(car -> new CarInputCommand(car.getVin(), car.getBrand(), car.getModel(), car.getProductionYear()))
                            .toList()
            );
            validate(command);

            UserEntity user = userService.addCars(command);
            responseObserver.onNext(toAddCarsResponse(user));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid UUID format").asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(mapException(e));
        }
    }

    private void validate(Object command) {
        Set<ConstraintViolation<Object>> violations = validator.validate(command);
        if (!violations.isEmpty()) {
            String details = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(Collectors.joining(", "));
            throw Status.INVALID_ARGUMENT
                    .withDescription(ApiErrorCode.VALIDATION_ERROR + ": " + details)
                    .asRuntimeException();
        }
    }

    private RuntimeException mapException(Exception e) {
        if (e instanceof BusinessException businessException) {
            Status status = switch (businessException.getErrorCode()) {
                case USER_NOT_FOUND -> Status.NOT_FOUND;
                case USER_ALREADY_EXISTS, DUPLICATE_CAR_VIN, VALIDATION_ERROR -> Status.FAILED_PRECONDITION;
            };
            return status.withDescription(businessException.getErrorCode() + ": " + e.getMessage()).asRuntimeException();
        }
        if (e instanceof io.grpc.StatusRuntimeException statusRuntimeException) {
            return statusRuntimeException;
        }
        return Status.INTERNAL.withDescription("INTERNAL_ERROR").withCause(e).asRuntimeException();
    }

    private CreateUserResponse toCreateResponse(UserEntity user) {
        return CreateUserResponse.newBuilder()
                .setUserId(user.getId().toString())
                .setExternalId(user.getExternalId())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setEmail(user.getEmail())
                .addAllCars(toCars(user))
                .build();
    }

    private AddCarsToUserResponse toAddCarsResponse(UserEntity user) {
        return AddCarsToUserResponse.newBuilder()
                .setUserId(user.getId().toString())
                .addAllCars(toCars(user))
                .build();
    }

    private List<Car> toCars(UserEntity user) {
        return user.getCars().stream()
                .map(car -> Car.newBuilder()
                        .setVin(car.getVin())
                        .setBrand(car.getBrand())
                        .setModel(car.getModel())
                        .setProductionYear(car.getProductionYear())
                        .build())
                .toList();
    }
}
