package org.psc.playground.logic;

import org.springframework.stereotype.Component;

@Component
public class MiscLogic {

    public String getException() throws Exception {
        throw new Exception("an exception");
    }
}
