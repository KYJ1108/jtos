package com.korea.jtos.Question;


import com.korea.jtos.Answer.Answer;
import com.korea.jtos.Category.Category;
import com.korea.jtos.DataNotFoundException;
import com.korea.jtos.User.SiteUser;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class QuestionService {

    private final QuestionRepository questionRepository;

    
    // 검색을 위한 Specification 구성
    private Specification<Question> search(String kw) {
        return new Specification<>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Predicate toPredicate(Root<Question> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
                query.distinct(true);  // 중복을 제거
                // 관련된 엔티티에서 검색하기 위한 조인 설정
                Join<Question, SiteUser> u1 = q.join("author", JoinType.LEFT);
                Join<Question, Answer> a = q.join("answerList", JoinType.LEFT);
                Join<Answer, SiteUser> u2 = a.join("author", JoinType.LEFT);
                // 검색 조건 설정
                return cb.or(cb.like(q.get("subject"), "%" + kw + "%"), // 제목
                        cb.like(q.get("content"), "%" + kw + "%"),      // 내용
                        cb.like(u1.get("username"), "%" + kw + "%"),    // 질문 작성자
                        cb.like(a.get("content"), "%" + kw + "%"),      // 답변 내용
                        cb.like(u2.get("username"), "%" + kw + "%"));   // 답변 작성자
            }
        };
    }

    // 키워드를 이용한 페이지네이션된 질문 목록 반환
    public Page<Question> getList(int page,String kw) {
        List<Sort.Order> sorts = new ArrayList<>();
        // CreateDate 말고 ID로 정렬해야 정렬이 제대로 가능
        // TEST 데이터를 넣을 때 생성 시간이 같게 동시에 들어가서 순서가 제멋대로 변경이 됨
        sorts.add(Sort.Order.desc("ID")); // ID를 내림차순으로 정렬
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        // 키워드로 질문을 검색하고 페이지네이션된 결과 반환
        return this.questionRepository.findAllByKeyword(kw, pageable);
    }

    // ID를 이용하여 단일 질문 반환
    public Question getQuestion(Integer id) {
        // ID로 질문을 찾아 존재할 경우 반환, 그렇지 않으면 예외 발생
        Optional<Question> question = this.questionRepository.findById(id);
        if (question.isPresent()) {
            return question.get();
        } else {
            throw new DataNotFoundException("question not found");
        }
    }

    // 카테고리별 페이지네이션된 질문 목록 반환 메서드
    public Page<Question> getListByCategory(int categoryId, int page) {
        // 생성 날짜를 기준으로 내림차순으로 정렬된 페이지 요청
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createDate"));
        // 카테고리 ID로 질문을 검색하고 페이지네이션된 결과 반환
        return questionRepository.findByCategoryIdOrderByCreateDateDesc(categoryId, pageable);
    }


    // 새로운 질문 생성 메서드
    public void create(String subject, String content, SiteUser author,Category category) {
        // 새로운 질문 엔티티 생성 및 저장
        Question q = new Question();
        q.setSubject(subject);
        q.setContent(content);
        q.setCreateDate(LocalDateTime.now());
        q.setAuthor(author);
        q.setCategory(category);
        this.questionRepository.save(q);
    }

    // 기존 질문 수정
    public void modify(Question question, String subject, String content, Category category) {
        // 질문 속성 수정 및 변경 사항 저장
        question.setSubject(subject);
        question.setContent(content);
        question.setModifyDate(LocalDateTime.now());
        question.setCategory(category);
        this.questionRepository.save(question);
    }

    // 질문 삭제
    public void delete(Question question) {
        this.questionRepository.delete(question);
    }
    
    // 질문 추천
    public void vote(Question question, SiteUser siteUser) {
        // 질문에 사용자 추가 및 변경 사항 저장
        question.getVoter().add(siteUser);
        this.questionRepository.save(question);
    }
    public List<Question> getQuestionsByUserId(Long userId) {
        return questionRepository.findByAuthorId(userId);
    }
}