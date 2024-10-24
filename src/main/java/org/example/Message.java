package org.example;

import java.util.List;

public record Message(String text, List<Button> buttonList) {
    public Message(String text){
        this(text, null);
    }
}
