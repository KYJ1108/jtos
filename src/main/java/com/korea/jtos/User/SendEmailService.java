package com.korea.jtos.User;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SendEmailService {

    private final JavaMailSender mailSender;
    private static final String FROM_ADDRESS = "kimyeji546408@gmail.com";
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public void createMailAndChargePassword(UserPwRequestDto userPwRequestDto) throws CustomException {
        String str = getTempPassword();
        MailDto dto = new MailDto();
        dto.setAddress(userPwRequestDto.getEmail());
        dto.setTitle(userPwRequestDto.getUserName() + "님의 임시비밀번호 안내 이메일 입니다.");
        dto.setMessage("안녕하세요. 임시비밀번호 안내 관련 메일입니다. \n" + userPwRequestDto.getUserName() + "님의 임시비밀번호는 [ " + str + "] 입니다.");
        updatePassword(str, userPwRequestDto);
    }

    public void updatePassword(String str, UserPwRequestDto requestDto) {
        String pw = passwordEncoder.encode(str);
        Long userId = Long.valueOf(requestDto.getEmail());
        // 비밀 번호 수정할 회원을 꺼내옴

        // 그놈을 비밀번호 고쳐

        // 그다음에 save

//        userRepository.updatePw(pw, userId);
    }

    public String getTempPassword() {
        char[] charSet = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
                'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        StringBuilder strBuilder = new StringBuilder();
        int idx;
        for (int i = 0; i < 10; i++) {
            idx = (int) (charSet.length * Math.random());
            strBuilder.append(charSet[idx]);
        }
        return strBuilder.toString();
    }

    public void mailSend(SiteUser siteUser,String subjuect,String text) {
        System.out.println("이메일 전송 완료");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(siteUser.getEmail());
        message.setSubject(subjuect);
        message.setText(text);
        mailSender.send(message);
    }
}
