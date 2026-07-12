package com.Hstep.Hstep.global.exception;



import com.Hstep.Hstep.global.response.code.BaseResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BaseException extends RuntimeException{
    private BaseResponseCode baseResponseCode;
}
