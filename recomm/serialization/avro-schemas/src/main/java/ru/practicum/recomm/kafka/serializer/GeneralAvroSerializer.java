package ru.practicum.recomm.kafka.serializer;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Общий сериализатор для объектов Avro в Kafka.
 * <p>
 * Реализует интерфейс {@link Serializer}, позволяя преобразовывать объекты, унаследованные от {@link SpecificRecordBase},
 * в байтовый массив формата Avro. Используется при отправке сообщений в Kafka-топик.
 */
public class GeneralAvroSerializer implements Serializer<SpecificRecordBase> {

    /**
     * Фабрика кодировщиков, используемая для создания экземпляров {@link BinaryEncoder}.
     * <p>
     * Создаётся один раз при инициализации класса и используется для всех операций сериализации.
     */
    private final EncoderFactory encoderFactory = EncoderFactory.get();

    /**
     * Сериализует переданный объект в байтовый массив в формате Avro.
     * <p>
     * Если переданный объект равен null, возвращается пустой массив байтов.
     *
     * @param topic имя топика Kafka, в который будет отправлено сообщение (используется для логирования)
     * @param data  объект, который нужно сериализовать; должен быть наследником {@link SpecificRecordBase}
     * @return байтовое представление объекта в формате Avro
     * @throws SerializationException если произошла ошибка ввода-вывода во время сериализации
     */
    @Override
    public byte[] serialize(String topic, SpecificRecordBase data) {
        if (data == null) {
            return new byte[0]; // Возвращаем пустой массив вместо null
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = encoderFactory.binaryEncoder(out, null);
            DatumWriter<SpecificRecordBase> writer = new SpecificDatumWriter<>(data.getSchema());
            writer.write(data, encoder);
            encoder.flush();
            return out.toByteArray();
        } catch (IOException ex) {
            throw new SerializationException("Ошибка сериализации данных для топика [" + topic + "]", ex);
        }
    }
}