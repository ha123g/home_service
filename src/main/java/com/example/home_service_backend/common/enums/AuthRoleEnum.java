package com.example.home_service_backend.common.enums;

import com.fasterxml.jackson.databind.deser.std.EnumDeserializer;
import lombok.Getter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.EnumSerializer;


@Getter
@JsonDeserialize(using = EnumDeserializer.class)
@JsonSerialize(using = EnumSerializer.class)
public enum AuthRoleEnum {
    USERNAME_SUPER_ADMIN("username_super_admin",1,"超级管理员"),
    USERNAME_SEC_ADMIN("username_sec_admin",2,"安全管理员"),
    USERNAME_AUD_ADMIN("username_aud_admin",3,"审计管理员"),
    USERNAME_SYS_ADMIN("username_sys_admin",4,"系统管理员"),
    USERNAME_NORMAL_USER("username_normal_user",5,"普通用户"),
    USERNAME_PLA_USER("username_pla_user",6,"平台用户");

    private String value;
    private Integer roleCode;
    private String description;

    AuthRoleEnum(String value, Integer roleCode, String description) {
        this.value = value;
        this.roleCode = roleCode;
        this.description = description;
    }
}
