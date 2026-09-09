package com.example.home_service_backend.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultFile extends ResultBase{
    private String originalName;
    private String serverName;

}
