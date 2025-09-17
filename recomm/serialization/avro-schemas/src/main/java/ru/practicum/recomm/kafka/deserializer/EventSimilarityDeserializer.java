package ru.practicum.recomm.kafka.deserializer;

import ru.practicum.recommendations.avro.EventSimilarityAvro;

/**
 * Десериализатор для объектов EventSimilarityAvro из Kafka.
 * <p>
 * Использует схему Avro, связанную с классом EventSimilarityAvro, для десериализации байтовых данных.
 */
public class EventSimilarityDeserializer extends BaseAvroDeserializer<EventSimilarityAvro> {

    /**
     * Конструктор, инициализирующий десериализатор схемой Avro, связанной с классом EventSimilarityAvro.
     * <p>
     * Схема получается через статический метод getClassSchema(), который доступен в классах,
     * сгенерированных из Avro-файлов. Это гарантирует точное соответствие между данными и ожидаемой структурой.
     */
    public EventSimilarityDeserializer() {
        super(EventSimilarityAvro.getClassSchema());
    }
}