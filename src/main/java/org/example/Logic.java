package org.example;


import java.util.Objects;

public class Logic {
    public String reverse(String message){
        StringBuilder reveres = new StringBuilder();
        char[] reversedMessageArray = message.toCharArray();

        for (int i = reversedMessageArray.length - 1; i >= 0; i--) {
            reveres.append(reversedMessageArray[i]);
        }
        System.out.println(reveres);
        return reveres.toString();
    }
    public String processMessage(String inputMessage){
        if (Objects.equals(inputMessage, "/option1")){
            return ":D";
        }
        if (Objects.equals(inputMessage, "/option2")){
            return ":DDDD";
        }

        if (Objects.equals(inputMessage, "/start")){
            return "Hello";
        }
        return reverse(inputMessage);

    }

}
