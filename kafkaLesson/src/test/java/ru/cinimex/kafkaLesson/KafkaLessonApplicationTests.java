package ru.cinimex.kafkaLesson;

import static org.junit.Assert.assertEquals;

import ru.cinimex.kafkaLesson.kafka.KafkaConsumer;
import ru.cinimex.kafkaLesson.kafka.KafkaProducer;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@DirtiesContext
class KafkaLessonApplicationTests {
    @Autowired
    private KafkaProducer producer;

    @Autowired
    private KafkaConsumer consumer;

    @Test
    @SneakyThrows
    void whenSendingKafkaMessage_thenMessageReceived() {
        String data = "TEST";

        producer.send(data);

        Thread.sleep(1000);

        String response = consumer.getPayload();

        assertEquals(response, data);
    }
}
