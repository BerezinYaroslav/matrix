package ru.cinimex.kafkaLesson.kafka;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@Getter
public class KafkaConsumer {
    private String payload;

    @KafkaListener(topics = "${spring.kafka.consumer.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, String> payload) {
        log.info("Received message from kafka topic: {}", payload.topic());
        log.info("Key: {}", payload.key());
        log.info("Headers: {}", payload.headers());
        log.info("Partition: {}", payload.partition());
        log.info("Content: {}", payload.value());

        this.payload = payload.value();
    }
}
