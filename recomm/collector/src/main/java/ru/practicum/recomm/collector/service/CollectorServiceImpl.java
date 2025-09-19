package ru.practicum.recomm.collector.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.recomm.collector.kafka.config.KafkaTopics;
import ru.practicum.recommendations.avro.ActionTypeAvro;
import ru.practicum.recommendations.avro.UserActionAvro;
import ru.practicum.recommendations.messages.ActionTypeProto;
import ru.practicum.recommendations.messages.UserActionProto;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реализация сервиса CollectorService для обработки пользовательских действий.
 * <p>
 * Отвечает за преобразование данных из protobuf-формата в Avro и отправку их в Kafka-топик stats.user-actions.v1
 * для дальнейшей обработки.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CollectorServiceImpl implements CollectorService {

    private final KafkaTemplate<String, UserActionAvro> kafkaTemplate;
    private final KafkaTopics kafkaTopics;

    /**
     * Мапа для сопоставления protobuf-перечисления ActionTypeProto с Avro-перечислением ActionTypeAvro.
     * Инициализируется один раз при загрузке класса.
     */
    private static final Map<ActionTypeProto, ActionTypeAvro> ACTION_TYPE_MAP = new ConcurrentHashMap<>();

    static {
        ACTION_TYPE_MAP.put(ActionTypeProto.ACTION_VIEW, ActionTypeAvro.VIEW);
        ACTION_TYPE_MAP.put(ActionTypeProto.ACTION_LIKE, ActionTypeAvro.LIKE);
        ACTION_TYPE_MAP.put(ActionTypeProto.ACTION_REGISTER, ActionTypeAvro.REGISTER);
    }

    /**
     * Метод обработки нового пользовательского действия.
     * <p>
     * Преобразует данные из protobuf-объекта {@link UserActionProto} в Avro-объект {@link UserActionAvro},
     * устанавливает временные метки и отправляет событие в Kafka.
     *
     * @param actionProto объект с данными о пользовательском действии в protobuf-формате
     */
    @Override
    public void newUserAction(UserActionProto actionProto) {
        log.debug("Обработка нового пользовательского действия: {}", actionProto);

        try {
            UserActionAvro actionAvro = convertToAvro(actionProto);
            sendToKafka(actionAvro);
            log.info("Событие успешно отправлено в Kafka: {}", actionAvro);
        } catch (Exception e) {
            log.error("Ошибка отправки события в Kafka для пользователя {} и события {}",
                    actionProto.getUserId(), actionProto.getEventId(), e);
            throw new RuntimeException("Ошибка отправки события в Kafka", e);
        }
    }

    /**
     * Отправка события в Kafka с использованием KafkaTemplate.
     * <p>
     *
     * @param actionAvro Avro-объект, который нужно отправить
     */
    private void sendToKafka(UserActionAvro actionAvro) {
        String topic = kafkaTopics.getUserActions();
        log.trace("Отправка события в топик: {}", topic);
        kafkaTemplate.send(topic, actionAvro);
    }

    /**
     * Преобразует protobuf-объект в Avro-объект.
     * <p>
     * Выполняет проверку входных данных и преобразование временных меток.
     *
     * @param actionProto protobuf-объект
     * @return Avro-объект
     * @throws IllegalArgumentException если данные некорректны
     */
    private UserActionAvro convertToAvro(UserActionProto actionProto) {
        log.trace("Начало преобразования protobuf-данных: {}", actionProto);

        Assert.notNull(actionProto.getTimestamp(), "Временная метка не может быть null");

        // Проверка корректности значений seconds и nanos
        if (actionProto.getTimestamp().getSeconds() < 0 || actionProto.getTimestamp().getNanos() < 0) {
            throw new IllegalArgumentException("Временные метки не могут быть отрицательными");
        }

        UserActionAvro actionAvro = UserActionAvro.newBuilder()
                .setEventId(actionProto.getEventId())
                .setUserId(actionProto.getUserId())
                .setActionType(getAvroType(actionProto.getActionType()))
                .setTimestamp(Instant.ofEpochSecond(
                        actionProto.getTimestamp().getSeconds(),
                        actionProto.getTimestamp().getNanos()))
                .build();

        log.debug("Результат преобразования для пользователя {}: {}", actionProto.getUserId(), actionAvro);
        return actionAvro;
    }

    /**
     * Вспомогательный метод для преобразования типа действия из protobuf-перечисления в Avro-перечисление.
     *
     * @param proto тип действия в protobuf-формате
     * @return соответствующий тип действия в Avro-формате
     * @throws IllegalArgumentException если передан неизвестный тип действия
     */
    private ActionTypeAvro getAvroType(ActionTypeProto proto) {
        log.trace("Преобразование типа действия: {}", proto);

        ActionTypeAvro type = ACTION_TYPE_MAP.get(proto);
        if (type == null) {
            log.warn("Неизвестное действие: {}", proto);
            throw new IllegalArgumentException("Неизвестное действие: " + proto);
        }

        log.debug("Тип действия {} преобразован в {}", proto, type);
        return type;
    }
}