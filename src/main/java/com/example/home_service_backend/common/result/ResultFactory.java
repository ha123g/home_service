package com.example.home_service_backend.common.result;

public class ResultFactory {
    public static final String SUCCESS_CODE = "0000";
    public static final String SYSTEM_ERROR_CODE = "400";
    public static final String UNAUTHORIZED_CODE = "401";

    public static final String RES_TYPE_BASE = "base";
    public static final String RES_TYPE_PAGE = "page";
    public static final String RES_TYPE_DATA = "data";
    public static final String RES_TYPE_FILE = "file";

    public static Result buildResult(String type){
        Result result = null;
        if (RES_TYPE_BASE.equals(type)){
            result = new ResultBase();
        } else if (RES_TYPE_PAGE.equals(type)){
            result = new ResultPage();
        } else if (RES_TYPE_DATA.equals(type)){
            result = new ResultData();
        } else if (RES_TYPE_FILE.equals(type)){
            result = new ResultFile();
        }
        return result;
    }

    public static ResultBase buildResultBase(){return (ResultBase) buildResult(RES_TYPE_BASE);}
    public static ResultPage buildResultPage(){return (ResultPage) buildResult(RES_TYPE_PAGE);}
    public static ResultData buildResultData(){return (ResultData) buildResult(RES_TYPE_DATA);}
    public static ResultFile buildResultFile(){return (ResultFile) buildResult(RES_TYPE_FILE);}
    public static ResultBase buildSuccessBase(){
        ResultBase result = new ResultBase();
        result.setCode(SUCCESS_CODE);
        result.setMessage("success");
        return result;
    }

    public static <T> ResultData<T> buildSuccessData(T data){
        ResultData<T> result = new ResultData<>();
        result.setCode(SUCCESS_CODE);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T, R> ResultPage<T, R> buildSuccessPage(T data, int total, R footer){
        ResultPage<T, R> result = new ResultPage<>();
        result.setCode(SUCCESS_CODE);
        result.setMessage("success");
        result.setData(data);
        result.setTotal(total);
        result.setFooter(footer);
        return result;
    }

    public static ResultBase buildError(String code, String message){
        ResultBase result = new ResultBase();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

}
