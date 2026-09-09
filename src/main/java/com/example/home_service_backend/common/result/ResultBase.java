package com.example.home_service_backend.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultBase implements Result{
    private String code = ResultFactory.SYSTEM_ERROR_CODE;
    private String message;
    private Map<String, Object> sysLog = new HashMap<>();
}
