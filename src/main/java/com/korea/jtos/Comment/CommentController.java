package com.korea.jtos.Comment;

import com.korea.jtos.Answer.Answer;
import com.korea.jtos.Answer.AnswerForm;
import com.korea.jtos.Answer.AnswerService;
import com.korea.jtos.Question.Question;
import com.korea.jtos.Question.QuestionForm;
import com.korea.jtos.Question.QuestionService;
import com.korea.jtos.User.SiteUser;
import com.korea.jtos.User.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RequestMapping("/comment")
@RequiredArgsConstructor
@Controller
public class CommentController {


    private final QuestionService questionService;
    private final AnswerService answerService;
    private final UserService userService;
    private final CommentService commentService;


    @PreAuthorize("isAuthenticated()")
    @PostMapping("/createQuestion/{id}")
    public String createQuestionComment(@PathVariable("id") Integer id,
                                        @ModelAttribute("commentForm") @Valid CommentForm commentForm,
                                        BindingResult bindingResult,
                                        Principal principal, Model model) {
        if (bindingResult.hasErrors()) {
            Question question = questionService.getQuestion(id);
            model.addAttribute("question", question);
            model.addAttribute("commentForm", commentForm);
            model.addAttribute("org.springframework.validation.BindingResult.commentForm", bindingResult);
            return "question_detail";
        }
        SiteUser siteuser = userService.getUser(principal.getName());
        Comment comment = commentService.createQuestionComment(questionService.getQuestion(id), commentForm.getContent(), siteuser);
        return String.format("redirect:/question/detail/%s#comment_%s", comment.getQuestion().getId(), comment.getId());
    }
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/createAnswer/{id}")
    public String createAnswerComment(@PathVariable("id") Integer id,
                                      @ModelAttribute("commentForm") @Valid CommentForm commentForm,
                                      BindingResult bindingResult,
                                      Principal principal, Model model) {
        if (bindingResult.hasErrors()) {
            Answer answer = answerService.getAnswer(id);
            model.addAttribute("answer", answer);
            model.addAttribute("commentForm", commentForm);
            model.addAttribute("org.springframework.validation.BindingResult.commentForm", bindingResult);
            return "question_detail";
        }
        SiteUser siteuser = userService.getUser(principal.getName());
        Comment comment = commentService.createAnswerComment(answerService.getAnswer(id), commentForm.getContent(), siteuser);
        return String.format("redirect:/question/detail/%s#comment_%s",comment.getAnswer().getQuestion().getId(),comment.getAnswer());
    }
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modifyQC/{id}")
    public String modifyQuestionComment(AnswerForm answerForm,
                                        @PathVariable("id") Integer id, Principal principal) {
        Comment comment= this.commentService.getQuestionComment(id);
        if (!comment.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
        }
        answerForm.setContent(comment.getContent());
        return "answer_form";
    }
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modifyQC/{id}")
    public String modifyQuestionComment(@Valid AnswerForm answerForm,
                                        BindingResult bindingResult,
                                        Principal principal, @PathVariable("id") Integer id) {
        if (bindingResult.hasErrors()) {
            return "answer_form";
        }
        Comment comment = this.commentService.getQuestionComment(id);
        if (!comment.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        this.commentService.modify(comment,answerForm.getContent());
        return String.format("redirect:/question/detail/%s#answer_%s",comment.getQuestion().getId(),comment.getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modifyAC/{id}")
    public String modifyAnswerComment(AnswerForm answerForm,
                                      @PathVariable("id") Integer id, Principal principal) {
        Comment comment= this.commentService.getAnswerComment(id);
        if (!comment.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
        }
        answerForm.setContent(comment.getContent());
        return "answer_form";
    }
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modifyAC/{id}")
    public String modifyAnswerComment(@Valid AnswerForm answerForm,
                                      BindingResult bindingResult,
                                      Principal principal, @PathVariable("id") Integer id) {
        if (bindingResult.hasErrors()) {
            return "answer_form";
        }
        Comment comment = this.commentService.getAnswerComment(id);
        if (!comment.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        this.commentService.modify(comment,answerForm.getContent());
        return String.format("redirect:/question/detail/%s#answer_%s",comment.getAnswer().getQuestion().getId(),comment.getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/deleteQC/{id}")
    public String deleteQuestionComment(@PathVariable("id") Integer id, Principal principal){
        Comment comment = this.commentService.getQuestionComment(id);
        if(!comment.getAuthor().getUsername().equals(principal.getName())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"삭제 권한이 없습니다.");
        }
        this.commentService.delete(comment);
        return String.format("redirect:/question/detail/%s#comment_%s",comment.getQuestion().getId(),comment.getId());
    }
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/deleteAC/{id}")
    public String deleteAnswerComment(@PathVariable("id") Integer id, Principal principal){
        Comment comment = this.commentService.getAnswerComment(id);
        if(!comment.getAuthor().getUsername().equals(principal.getName())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"삭제 권한이 없습니다.");
        }
        this.commentService.delete(comment);
        return String.format("redirect:/question/detail/%s#comment_%s",comment.getAnswer().getQuestion().getId(),comment.getId());
    }
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/voteQC/{id}")
    public String voteQuestionComment(Principal principal, @PathVariable("id") Integer id){
        Comment comment = this.commentService.getQuestionComment(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.commentService.vote(comment,siteUser);
        return String.format("redirect:/question/detail/%s",comment.getQuestion().getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/voteAC/{id}")
    public String voteAnswerComment(Principal principal, @PathVariable("id") Integer id){
        Comment comment = this.commentService.getAnswerComment(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.commentService.vote(comment,siteUser);
        return String.format("redirect:/question/detail/%s",comment.getAnswer().getQuestion().getId());
    }

}