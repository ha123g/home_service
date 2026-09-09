package com.example.home_service_backend.common.result;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultPage<T, R> extends ResultBase{
    private int total;// 总条数
    private T data;// 当前页数据
    private R footer;// 额外信息：分页信息，总条数等
}
