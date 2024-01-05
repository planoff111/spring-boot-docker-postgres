package com.kaluzny.demo.web;

import com.kaluzny.demo.domain.Automobile;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface JMSPublisher {

    ResponseEntity<Automobile> pushMessage(Automobile automobile);

    ResponseEntity<List<Automobile>> pushMessageColor(@RequestBody String color);
}
