package com.korea.jtos.Question;

import java.security.Principal;
import java.util.List;

import com.korea.jtos.Answer.Answer;
import com.korea.jtos.Answer.AnswerForm;
import com.korea.jtos.Answer.AnswerService;
import com.korea.jtos.Category.Category;
import com.korea.jtos.Category.CategoryService;
import com.korea.jtos.Comment.Comment;
import com.korea.jtos.Comment.CommentForm;
import com.korea.jtos.User.SiteUser;
import com.korea.jtos.User.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequestMapping("/question")
@RequiredArgsConstructor
@Controller
public class QuestionController {

    private final QuestionService questionService;
    private final AnswerService answerService;
    private final UserService userService;
    private final CategoryService categoryService;

    @GetMapping("/list")
    public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "kw", defaultValue = "") String kw) {
        Page<Question> paging = this.questionService.getList(page, kw);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        return "question_list";
    }

    @GetMapping("/list/{categoryId}")
    public String listByCategory(Model model,
                                 @PathVariable(name = "categoryId") int categoryId,
                                 @RequestParam(name = "page", defaultValue = "0") int page) {
        Page<Question> paging = this.questionService.getListByCategory(categoryId, page);
        model.addAttribute("paging", paging);
        model.addAttribute("categoryId", categoryId);
        return "question_list";
    }

    @GetMapping(value = "/detail/{id}")
    public String detail(Model model, @PathVariable("id") Integer id,
                         @ModelAttribute("answerForm") AnswerForm answerForm, BindingResult answerBindingResult,
                         @ModelAttribute("commentForm") CommentForm commentForm, BindingResult commentBindingResult,
                         @RequestParam(value = "page", defaultValue = "0") int page) {
        Question question = this.questionService.getQuestion(id);
        Page<Answer> paging = this.answerService.getListByQuestionId(id, page);

        model.addAttribute("paging", paging);
        model.addAttribute("question", question);
        model.addAttribute("answerForm", answerForm);
        model.addAttribute("commentForm", commentForm);

        // IF 문을 안 넣어줘도 상관 없음
        // 바인딩 결과가 모델에 포함되지 않은 경우 추가
        if (!model.containsAttribute("org.springframework.validation.BindingResult.answerForm")) {
            model.addAttribute("org.springframework.validation.BindingResult.answerForm", answerBindingResult);
        }
        if (!model.containsAttribute("org.springframework.validation.BindingResult.commentForm")) {
            model.addAttribute("org.springframework.validation.BindingResult.commentForm", commentBindingResult);
        }
        return "question_detail";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String questionCreate(QuestionForm questionForm,Model model) {
        List<Category> categoryList = categoryService.getAllCategories(); // 카테고리 서비스를 통해 카테고리 목록을 가져옴
        model.addAttribute("categoryList", categoryList); // 모델에 카테고리 목록 추가
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String questionCreate(@Valid QuestionForm questionForm, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "question_form";
        }
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.questionService.create(questionForm.getSubject(), questionForm.getContent(), siteUser, questionForm.getCategory());
        return "redirect:/question/list";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String questionModify(QuestionForm questionForm,
                                 @PathVariable("id") Integer id, Principal principal,Model model) {
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
        }
        questionForm.setSubject(question.getSubject());
        questionForm.setContent(question.getContent());
        List<Category> categoryList = categoryService.getAllCategories(); // 카테고리 서비스를 통해 카테고리 목록을 가져옴
        model.addAttribute("categoryList", categoryList); // 모델에 카테고리 목록 추가
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String questionModify(@Valid QuestionForm questionForm
            ,BindingResult bindingResult,
                                 Principal principal, @PathVariable("id") Integer id) {
        if (bindingResult.hasErrors()) {
            return "question_form";
        }
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent(),questionForm.getCategory());
        return String.format("redirect:/question/detail/%s", id);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String questionDelete(Principal principal, @PathVariable("id") Integer id) {
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제 권한이 없습니다.");
        }
        this.questionService.delete(question);
        return "redirect:/";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String questionVote(Principal principal, @PathVariable("id") Integer id) {
        Question question = this.questionService.getQuestion(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.questionService.vote(question, siteUser);
        return String.format("redirect:/question/detail/%s", id);
    }

    @GetMapping(value = "/voter/{id}")
    public String sortByPopularity(Model model, @PathVariable("id") Integer id,
                                   @RequestParam(value = "page", defaultValue = "0") int page) {
        Question question = this.questionService.getQuestion(id);
        Page<Answer> paging = this.answerService.getListByQuestionIdOrderByRecommendation(id, page);

        model.addAttribute("question", question);
        model.addAttribute("paging", paging);
        model.addAttribute("answerForm", new AnswerForm());
        model.addAttribute("commentForm", new CommentForm());

        return "question_detail";
    }

}