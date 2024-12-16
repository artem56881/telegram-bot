package com.github.ArtemAndrew.PriceMonitoringBot.entity;

public enum UserState {
    DEFAULT("Дефолтное состояние"),
    AWAITING_PRODUCT_LINK_FOR_CHECK ("Бот ждёт ссылку, чтобы посмотреть цену"),
    AWAITING_PRODUCT_LINK ("Ждёт ссылку для добавления товара"),
    AWAITING_PRODUCT_LINK_FOR_REMOVAL ("Ждёт ссылку для удаления товара");

    private final String UserState;


    UserState(String UserState) {
        this.UserState = UserState;
    }

    public String getUserState(){
        return UserState;
    }



}
