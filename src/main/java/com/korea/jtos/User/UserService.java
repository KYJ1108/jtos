package com.korea.jtos.User;

import com.korea.jtos.Answer.Answer;
import com.korea.jtos.Answer.AnswerService;
import com.korea.jtos.Comment.Comment;
import com.korea.jtos.Comment.CommentService;
import com.korea.jtos.DataNotFoundException;
import com.korea.jtos.Question.Question;
import com.korea.jtos.Question.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final CommentService commentService;

    public void create(String username, String email, String password) {
        SiteUser user = new SiteUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        this.userRepository.save(user);
    }

    public SiteUser getUser(String username) {
        Optional<SiteUser> siteUser = this.userRepository.findByUsername(username);
        if (siteUser.isPresent()) {
            return siteUser.get();
        } else {
            throw new DataNotFoundException("siteuser not found");
        }
    }

    public Map<String, String> findId(UserFindRequestDto requestDto) throws CustomException {
        Optional<SiteUser> user = userRepository.findByUsername(requestDto.getUserName());
        if (!user.isPresent()) {
            throw new CustomException(ErrorCode.ID_NOT_FOUND_ERROR);
        }
        Map<String, String> result = new HashMap<>();
        result.put("userName", String.valueOf(user.get().getId()));
        return result;
    }

    public void changePassword(String userId, String oldPassword, String newPassword) throws CustomException {
        Optional<SiteUser> userOptional = userRepository.findByUsername(userId);
        if (userOptional.isPresent()) {
            SiteUser user = userOptional.get();
            if (passwordEncoder.matches(oldPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                // 비밀번호 변경 후에 이메일로 알림을 보내는 등의 추가 로직이 필요할 수 있습니다.
            } else {
                throw new CustomException(ErrorCode.INVALID_PASSWORD);
            }
        } else {
            throw new DataNotFoundException("User not found");
        }
    }

    // 사용자가 작성한 질문 가져오기
//    public List<Question> getQuestion(Long id) {
//        return Collections.singletonList(questionService.getQuestion(Math.toIntExact(id)));
//    }
//    // 사용자가 작성한 답변 가져오기
//    public List<Answer> getAnswer(Long id) {
//        return Collections.singletonList(answerService.getAnswer(Math.toIntExact(id)));
//    }
//    // 사용자가 작성한 댓글 가져오기
//    public List<Comment> getComment(Long id) {
//        return Collections.singletonList(commentService.getComment(Math.toIntExact(id)));
//    }

    public List<Question> getQuestionsByUserId(Long userId) {
        return questionService.getQuestionsByUserId(userId);
    }

    public List<Answer> getAnswersByUserId(Long userId) {
        return answerService.getAnswersByUserId(userId);
    }

    public List<Comment> getCommentsByUserId(Long userId) {
        return commentService.getCommentsByUserId(userId);
    }

    public void saveImageFile(String fileName){
        // 이미지 파일 저장
    }

    public void save(SiteUser siteUser){
        this.userRepository.save(siteUser);
    }
}