package ru.practicum.recomm.kafka.deserializer;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Deserializer;

/**
 * Базовый десериализатор для Avro-объектов из Kafka.
 * <p>
 * Реализует интерфейс Deserializer и предоставляет универсальную логику десериализации
 * байтовых данных в объекты, реализующие интерфейс {@link SpecificRecordBase}.
 */
@Slf4j
public class BaseAvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {
    private final DecoderFactory decoderFactory;
    private final SpecificDatumReader<T> reader;

    /**
     * Конструктор с указанием схемы Avro.
     *
     * @param schema схема Avro для десериализации
     * @throws IllegalArgumentException если схема равна null
     */
    public BaseAvroDeserializer(Schema schema) {
        this(DecoderFactory.get(), schema);
    }

    /**
     * Конструктор с указанием фабрики декодера и схемы Avro.
     *
     * @param decoderFactory фабрика для создания BinaryDecoder
     * @param schema         схема Avro для десериализации
     * @throws IllegalArgumentException если decoderFactory или schema равны null
     */
    public BaseAvroDeserializer(DecoderFactory decoderFactory, Schema schema) {
        if (decoderFactory == null) {
            throw new IllegalArgumentException("DecoderFactory не может быть null");
        }
        if (schema == null) {
            throw new IllegalArgumentException("Схема Avro не может быть null");
        }
        this.decoderFactory = decoderFactory;
        this.reader = new SpecificDatumReader<>(schema);
    }

    /**
     * Десериализует байтовые данные в объект типа T.
     *
     * @param topic  имя топика Kafka
     * @param data   байтовые данные для десериализации
     * @return       десериализованный объект типа T или null, если данные пустые
     * @throws RuntimeException если произошла ошибка десериализации
     */
    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }

        try {
            BinaryDecoder decoder = decoderFactory.binaryDecoder(data, null);
            return reader.read(null, decoder);
        } catch (Exception e) {
            log.error("Ошибка десериализации данных из топика [{}]. Причина: {}", topic, e.getMessage(), e);
            throw new RuntimeException("Не удалось десериализовать данные из топика [" + topic + "]", e);
        }
    }
}