package com.example.grpcuser.grpc;

import com.example.grpcuser.proto.AddCarsToUserRequest;
import com.example.grpcuser.proto.AddCarsToUserResponse;
import com.example.grpcuser.proto.CarInput;
import com.example.grpcuser.proto.CreateUserRequest;
import com.example.grpcuser.proto.CreateUserResponse;
import com.example.grpcuser.proto.UserServiceGrpc;
import com.example.grpcuser.repository.UserRepository;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class UserGrpcServiceIntegrationTest {

    private static ManagedChannel channel;
    private static UserServiceGrpc.UserServiceBlockingStub stub;

    @Autowired
    private UserRepository userRepository;

    @BeforeAll
    static void initClient() {
        channel = ManagedChannelBuilder.forAddress("localhost", 9091)
                .usePlaintext()
                .build();
        stub = UserServiceGrpc.newBlockingStub(channel);
    }

    @AfterAll
    static void shutdown() {
        if (channel != null) {
            channel.shutdownNow();
        }
    }

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateUserAndAddCars() {
        CreateUserResponse created = stub.createUser(CreateUserRequest.newBuilder()
                .setExternalId("crm-777")
                .setFirstName("Ivan")
                .setLastName("Petrov")
                .setEmail("ivan.petrov@company.com")
                .build());

        assertThat(created.getUserId()).isNotBlank();
        assertThat(created.getCarsCount()).isZero();

        AddCarsToUserResponse carsResponse = stub.addCarsToUser(AddCarsToUserRequest.newBuilder()
                .setUserId(created.getUserId())
                .addCars(CarInput.newBuilder().setVin("VIN-111").setBrand("Audi").setModel("A6").setProductionYear(2021).build())
                .addCars(CarInput.newBuilder().setVin("VIN-222").setBrand("Toyota").setModel("Camry").setProductionYear(2020).build())
                .build());

        assertThat(carsResponse.getCarsCount()).isEqualTo(2);
        assertThat(carsResponse.getCarsList()).extracting("vin").containsExactlyInAnyOrder("VIN-111", "VIN-222");
    }

    @Test
    void shouldReturnErrorOnInvalidEmail() {
        assertThatThrownBy(() -> stub.createUser(CreateUserRequest.newBuilder()
                .setExternalId("crm-888")
                .setFirstName("Bad")
                .setLastName("Email")
                .setEmail("not-an-email")
                .build()))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("VALIDATION_ERROR");
    }
}
