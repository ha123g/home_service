package com.example.home_service_backend.common.enums;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.EnumDeserializer;
import com.fasterxml.jackson.databind.ser.std.EnumSerializer;
import lombok.Getter;


@Getter
@JsonDeserialize(using = EnumDeserializer.class)
@JsonSerialize(using = EnumSerializer.class)
public enum AuthUserStateEnum {
    USER_STATE_NORMAL("normal",1,"正常"),
    USER_STATE_LOCKED("locked",2,"锁定"),
    USER_STATE_LOGOUT("logout",3,"注销");

    private String value;
    private Integer stateCode;
    private String description;

    AuthUserStateEnum(String value, Integer stateCode, String description){
        this.value = value;
        this.stateCode = stateCode;
        this.description = description;
    }
}
