package ru.practicum.recomm.client;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.recommendations.messages.UserActionProto;
import ru.practicum.recommendations.services.UserActionControllerGrpc;

/**
 * Клиент для взаимодействия с gRPC-сервисом Collector.
 * <p>
 * Используется для отправки информации о пользовательских действиях в сервис сбора данных.
 */
@Component
public class CollectorClient {

    /**
     * Стаб gRPC-клиента для работы с сервисом {@link UserActionControllerGrpc}.
     * <p>
     * Адрес сервиса указывается через параметр конфигурации "collector".
     */
    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub collector;

    /**
     * Метод отправки нового пользовательского действия в сервис сбора.
     *
     * @param action объект, содержащий информацию о действии пользователя
     *               (идентификатор пользователя, идентификатор мероприятия, тип действия, временная метка)
     */
    public void newUserAction(UserActionProto action) {
        collector.collectUserAction(action);
    }
}