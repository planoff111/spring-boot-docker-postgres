package com.kaluzny.demo.listener;

import com.kaluzny.demo.domain.Automobile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ColoredAutomobileConsumer {

    @JmsListener(destination = "AutoTopic-Color", containerFactory = "automobileJmsContFactory")
    public void getAutomobileColorListener1(List<Automobile> automobileList) {
        log.info("\u001B[34m" + "Automobile Consumer 1: " + automobileList + "\u001B[0m");

    }

    @JmsListener(destination = "AutoTopic-Color", containerFactory = "automobileJmsContFactory")
    public void getAutomobileColorListener2(List<Automobile> automobileList) {
        log.info("\u001B[34m" + "Automobile Consumer 2: " + automobileList + "\u001B[0m");
    }

    @JmsListener(destination = "AutoTopic-Color", containerFactory = "automobileJmsContFactory")
    public void getAutomobileColorListener3(List<Automobile> automobileList) {
        log.info("\u001B[34m" + "Automobile Consumer 3: " + automobileList + "\u001B[0m");
    }
}
