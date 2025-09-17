package ru.practicum.recomm.kafka.deserializer;

import ru.practicum.recommendations.avro.UserActionAvro;

/**
 * Десериализатор для объектов UserActionAvro из Kafka.
 * <p>
 * Использует схему Avro, связанную с классом UserActionAvro, для десериализации байтовых данных.
 */
public class UserActionDeserializer extends BaseAvroDeserializer<UserActionAvro> {

    /**
     * Конструктор, инициализирующий десериализатор схемой Avro, связанной с классом UserActionAvro.
     * <p>
     * Схема получается через статический метод {@code getClassSchema()}, который доступен в классах,
     * сгенерированных из Avro-файлов. Это гарантирует точное соответствие между данными и ожидаемой структурой.
     */
    public UserActionDeserializer() {
        super(UserActionAvro.getClassSchema());
    }
}