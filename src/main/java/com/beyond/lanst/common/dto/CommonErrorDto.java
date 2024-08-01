package com.beyond.lanst.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
public class CommonErrorDto {
    private int error_code;
    private String error_message;

    public CommonErrorDto(HttpStatus httpStatus, String message){
        this.error_code = httpStatus.value();
        this.error_message = message;
    }
}
