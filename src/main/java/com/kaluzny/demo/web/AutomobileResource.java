package com.kaluzny.demo.web;

import com.kaluzny.demo.domain.Automobile;
import org.springframework.http.ResponseEntity;

import java.util.Collection;
import java.util.List;

public interface AutomobileResource {

    Automobile saveAutomobile(Automobile automobile);

    Collection<Automobile> getAllAutomobiles();

    Automobile getAutomobileById(Long id);

    Collection<Automobile> findAutomobileByName(String name);

    Automobile refreshAutomobile(Long id, Automobile automobile);

    ResponseEntity<String> removeAutomobileById(Long id);

    ResponseEntity<String> removeAllAutomobiles();

    ResponseEntity<Collection<Automobile>> findAutomobileByColor(String color);

    ResponseEntity<Collection<Automobile>> findAutomobileByNameAndColor(String name, String color);

    ResponseEntity<Collection<Automobile>> findAutomobileByColorStartsWith(String colorStartsWith, int page, int size);

    ResponseEntity<List<String>> getAllAutomobilesByName();
}
