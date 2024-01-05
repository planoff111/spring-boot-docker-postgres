package com.kaluzny.demo.service;

import com.kaluzny.demo.domain.Automobile;
import com.kaluzny.demo.domain.AutomobileRepository;
import com.kaluzny.demo.exception.AutoWasDeletedException;
import jakarta.annotation.PostConstruct;
import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.Session;
import jakarta.jms.Topic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import com.kaluzny.demo.exception.ThereIsNoSuchAutoException;

import java.time.Instant;
import java.util.*;

import static com.kaluzny.demo.web.AutomobileRestController.getTiming;

@Service
@Slf4j
public class AutomobileService {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private AutomobileRepository repository;


    public List<Automobile> findByColorAndSendMessageAndReturn(String color) {
        try {
            log.info("Executing findByColorAndSendMessageAndReturn with color: {}", color);

            List<Automobile> automobilesByColor = repository.findByColor(color);


            Topic autoTopic = jmsTemplate.getConnectionFactory().createConnection()
                    .createSession()
                    .createTopic("AutoTopic-Color");

            log.info("Sending Automobiles to JMS: {}", automobilesByColor);
            jmsTemplate.convertAndSend(autoTopic, automobilesByColor);

            log.info("Returning Automobiles: {}", automobilesByColor);

            return automobilesByColor;

        } catch (JMSException e) {
            log.error("An error occurred while processing JMS operations", e);
            throw new RuntimeException("Failed to send message", e);
        }
    }



    public Automobile saveAndSendMessage(Automobile automobile) {
        try (Connection connection = jmsTemplate.getConnectionFactory().createConnection();

             Session session = connection.createSession()) {

            Topic autoTopic = session.createTopic("AutoTopic");

            Automobile savedAutomobile = repository.save(automobile);

            log.info("\u001B[32m" + "Sending Automobile with id: " + savedAutomobile.getId() + "\u001B[0m");

            jmsTemplate.convertAndSend(autoTopic, savedAutomobile);

            return savedAutomobile;
        } catch (JMSException e) {
            log.error("An error occurred while processing JMS operations", e);
            throw new RuntimeException("Failed to save and send message", e);
        }
    }

    public List<String> listAllAutomobileNames() {
        log.info("getAllAutomobilesByName() - start");

        List<Automobile> collection = repository.findAll();
        List<String> collectionName = Optional.of(collection)
                .map(list -> list.stream()
                        .map(Automobile::getName)
                        .sorted()
                        .toList())
                .orElse(Collections.emptyList());

        log.info("getAllAutomobilesByName() - end");
        return collectionName;
    }

    public Collection<Automobile> findByColorStartsWith(String colorStartsWith, int page, int size) {
        log.info("findByColorStartsWith() - start: color = {}", colorStartsWith);

        Collection<Automobile> collection = repository.findByColorStartsWith(colorStartsWith, PageRequest.of(page, size, Sort.by("color")));

        log.info("findByColorStartsWith() - end: collection = {}", collection);
        return collection;
    }


    public Collection<Automobile> findByNameAndColor(String name, String color) {
        log.info("findByNameAndColor() - start: name = {}, color = {}", name, color);

        Collection<Automobile> collection = repository.findByNameAndColor(name, color);

        log.info("findByNameAndColor() - end: collection = {}", collection);
        return collection;
    }

    public Collection<Automobile> findByColor(String color) {
        Instant start = Instant.now();
        log.info("findAutomobileByColor() - start: time = {}", start);
        log.info("findAutomobileByColor() - start: color = {}", color);

        Collection<Automobile> collection = repository.findByColor(color);
        Instant end = Instant.now();

        log.info("findAutomobileByColor() - end: milliseconds = {}", getTiming(start, end));
        log.info("findAutomobileByColor() - end: collection = {}", collection);
        return collection;
    }

    public String removeAll() {
        log.info("removeAllAutomobiles() - start");
        repository.deleteAll();
        log.info("removeAllAutomobiles() - end");
        return "All automobiles removed successfully";
    }

    @CacheEvict(value = "automobile", key = "#id")
    public String removeAutomobileById(Long id) {
        log.info("removeAutomobileById() - start: id = {}", id);

        Automobile deletedAutomobile = repository.findById(id)
                .orElseThrow(ThereIsNoSuchAutoException::new);

        deletedAutomobile.setDeleted(Boolean.TRUE);
        repository.save(deletedAutomobile);

        log.info("removeAutomobileById() - end: id = {}", id);
        return "Deleted";
    }

    public Automobile refreshAutomobile(Long id, Automobile updatedAutomobile) {
        log.info("refreshAutomobile() - start: id = {}, automobile = {}", id, updatedAutomobile);

        Automobile updatedEntity = repository.findById(id)
                .map(entity -> {
                    entity.checkColor(updatedAutomobile);
                    entity.setName(updatedAutomobile.getName());
                    entity.setColor(updatedAutomobile.getColor());
                    entity.setUpdateDate(updatedAutomobile.getUpdateDate());
                    if (entity.getDeleted()) {
                        throw new AutoWasDeletedException();
                    }
                    return repository.save(entity);
                })
                .orElseThrow(ThereIsNoSuchAutoException::new);

        log.info("refreshAutomobile() - end: updatedAutomobile = {}", updatedEntity);
        return updatedEntity;
    }

    public Collection<Automobile> findByName(String name) {
        log.info("findAutomobileByName() - start: name = {}", name);
        Collection<Automobile> collection = repository.findByName(name);
        log.info("findAutomobileByName() - end: collection = {}", collection);
        return collection;
    }

    public Automobile getAutomobileById(Long id) {
        log.info("getAutomobileById() - start: id = {}", id);
        Automobile receivedAutomobile = repository.findById(id)
                .orElseThrow(ThereIsNoSuchAutoException::new);

        if (receivedAutomobile.getDeleted()) {
            throw new AutoWasDeletedException();
        }

        log.info("getAutomobileById() - end: Automobile = {}", receivedAutomobile.getId());
        return receivedAutomobile;
    }

    public Collection<Automobile> getAllAutomobiles() {
        log.info("getAllAutomobiles() - start");
        Collection<Automobile> collection = repository.findAll();
        log.info("getAllAutomobiles() - end");
        return collection;
    }

    public Automobile saveAutomobile(Automobile automobile) {
        log.info("saveAutomobile() - start: automobile = {}", automobile);
        Automobile savedAutomobile = repository.save(automobile);
        log.info("saveAutomobile() - end: savedAutomobile = {}", savedAutomobile.getId());
        return savedAutomobile;
    }
}