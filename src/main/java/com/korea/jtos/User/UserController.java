package com.korea.jtos.User;

import com.korea.jtos.Answer.Answer;
import com.korea.jtos.Comment.Comment;
import com.korea.jtos.Question.Question;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final SendEmailService sendEmailService;
    private final PasswordEncoder passwordEncoder;
    private static final String UPLOAD_DIR = "./uploads"; // 이미지 업로드 디렉토리

    @GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm) {
        return "signup_form";
    }

    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "signup_form";
        }

        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect",
                    "2개의 패스워드가 일치하지 않습니다.");
            return "signup_form";
        }
        try {
            userService.create(userCreateForm.getUsername(),userCreateForm.getEmail(),userCreateForm.getPassword1());
        } catch (DataIntegrityViolationException e){
            e.printStackTrace();
            bindingResult.reject("signupFailed","이미 등록된 사용자입니다.");
            return "signup_form";
        }catch (Exception e){
            e.printStackTrace();
            bindingResult.reject("signupFailed",e.getMessage());
            return "signup_form";
        }

        return "redirect:/";
    }

    @GetMapping("/login")
    public String login(){
        return "login_form";
    }

    @GetMapping("/findPw")
    public String findPw(UserPwRequestDto userPwRequestDto) {

        return "findPassword_form";
    }

    @PostMapping(value = "/findPw")
    public String pwFind(@Valid UserPwRequestDto userPwRequestDto, BindingResult bindingResult, Model model) {
        if(bindingResult.hasErrors()){
            model.addAttribute("error","아이디와 이메일을 확인해주세요.");
            return "findPassword_form";
        }

        String tempPassword = this.sendEmailService.getTempPassword();
        SiteUser siteUser = this.userService.getUser(userPwRequestDto.getUserName());
        siteUser.setPassword(passwordEncoder.encode(tempPassword));
        this.userService.save(siteUser);
        this.sendEmailService.mailSend(siteUser,"임시비밀번호 발송","임시 비밀번호는" +tempPassword+" 입니다.");

        return "redirect:/question/list"; // 비밀번호 재설정이 성공했음을 알리는 페이지로 이동
    }

    @GetMapping("/modifyPassword")
    public String modifyPw(){
        return "modifyPassword_form";
    }

    @PostMapping("/modifyPassword")
    public String changePassword(Model model, Principal principal,
                                 @RequestParam("currentPassword")String currentPassword,
                                 @RequestParam("newPassword")String newPassword,
                                 @RequestParam("confirmPassword")String confirmPassword){
        if (!newPassword.equals(confirmPassword)){
//             비밀번호가 일치하지 않음.
            model.addAttribute("error", "새로운 비밀번호와 새로운 비밀번호 확인이 일치하지 않습니다.");
            return "modifyPassword_form";
        }
        try {
            // 현재 사용자의 아이디를 얻어옴
            String userName = principal.getName();
            // 사용자의 비밀번호 변경을 userService를 통해 처리
            userService.changePassword(userName, currentPassword, newPassword);
            return "redirect:/question/list";
        } catch (CustomException e){
            // 비밀번호 변경 중에 예외가 발생한 경우 에러 메시지를 받아와서 비밀번호 변경 페이지로
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            return "modifyPassword_form";
        }
    }


    @GetMapping("/profile")
    public String userProfile(Principal principal, Model model) {
        // 현재 로그인한 사용자의 아이디를 가져옴
        String userId = principal.getName();

        // userService를 사용하여 사용자 정보 가져옴
        SiteUser user = userService.getUser(userId);
        model.addAttribute("user", user);

        // 사용자가 작성한 질문, 답변, 댓글 등의 정보를 가져옴.
        List<Question> questions = userService.getQuestionsByUserId(user.getId());
        List<Answer> answers = userService.getAnswersByUserId(user.getId());
        List<Comment> comments = userService.getCommentsByUserId(user.getId());

        model.addAttribute("userQuestion", questions);
        model.addAttribute("userAnswer", answers);
        model.addAttribute("userComment", comments);
        return "profile_form";
    }

    @PostMapping("/image/upload")
    public String uploadProfileImage(@RequestParam("imageFile")MultipartFile file) throws IOException {
        // 파일명에서 공백 제거
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        // 업로드 디렉토리 생성
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)){
            Files.createDirectories(uploadPath);
        }
        
        // 파일 저장
        Path filePath = uploadPath.relativize(Path.of(fileName));
        Files.copy(file.getInputStream(), filePath);

        // 파일 경로를 db에 저장
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/user/profile/image/")
                .path(fileName)
                .toUriString();
        userService.saveImageFile(fileName);
        return "profile_form";
    }

}