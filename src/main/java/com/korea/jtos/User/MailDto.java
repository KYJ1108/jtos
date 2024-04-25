package com.korea.jtos.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MailDto {
    private String address; // 수신자 이메일 주소
    private String title;   // 이메일 제목
    private String message; // 이메일 내용

}
