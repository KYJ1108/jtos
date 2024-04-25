package com.korea.jtos.User;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPwRequestDto {
    private String userName; // 사용자 이름을 저장

    @NotEmpty(message = "이메일은 필수입니다.")
    private String email;
}
