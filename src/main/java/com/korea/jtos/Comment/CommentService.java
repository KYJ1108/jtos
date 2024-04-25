package com.korea.jtos.Comment;

import com.korea.jtos.Answer.Answer;
import com.korea.jtos.DataNotFoundException;
import com.korea.jtos.Question.Question;
import com.korea.jtos.User.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;

    // 질문에 대한 댓글 생성
    public Comment createQuestionComment(Question question, String content, SiteUser author) {
        // 새로운 댓글 엔티티 생성 및 저장
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setCreateDate(LocalDateTime.now());
        comment.setQuestion(question); // 댓글이 속한 질문 설정
        comment.setAuthor(author); // 댓글 작성자 정보 설정
        this.commentRepository.save(comment);
        return comment;
    }

    // 댓글 ID를 이용하여 질문 댓글 반환
    public Comment getQuestionComment(Integer id) {
        // 댓글 ID로 댓글을 찾아 존재할 경우 반환, 그렇지 않으면 예외 발생
        Optional<Comment> comment = this.commentRepository.findById(id);
        if (comment.isPresent()) {
            return comment.get();
        } else {
            throw new DataNotFoundException("question not found");
        }
    }

    // 답변에 대한 댓글 생성
    public Comment createAnswerComment(Answer answer, String content, SiteUser author) {
        // 새로운 댓글 엔티티 생성 및 저장
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setCreateDate(LocalDateTime.now());
        comment.setAnswer(answer); // 댓글이 속한 답변 설정
        comment.setAuthor(author); // 댓글 작성자 정보 설정
        this.commentRepository.save(comment);
        return comment;
    }

    // 댓글 ID를 이용하여 답변 댓글 반환
    public Comment getAnswerComment(Integer id) {
        // 댓글 ID로 댓글을 찾아 존재할 경우 반환, 그렇지 않으면 예외 발생
        Optional<Comment> comment = this.commentRepository.findById(id);
        if (comment.isPresent()) {
            return comment.get();
        } else {
            throw new DataNotFoundException("answer not found");
        }
    }

    // 댓글 ID를 이용하여 댓글 반환 메서드
    public Comment getComment(Integer id){
        // 댓글 ID로 댓글을 찾아 존재할 경우 반환, 그렇지 않으면 예외 발생
        Optional<Comment> comment = this.commentRepository.findById(id);
        if (comment.isPresent()) {
            return comment.get();
        } else {
            throw new DataNotFoundException("answer not found");
        }
    }

    // 댓글 삭제
    public void delete(Comment comment) {
        this.commentRepository.delete(comment);
    }

    // 댓글 수정
    public void modify(Comment comment,String content){
        comment.setContent(content);
        comment.setModifyDate(LocalDateTime.now());
        this.commentRepository.save(comment);
    }
    // 댓글 추천
    public void vote(Comment comment,SiteUser siteUser){
        comment.getVoter().add(siteUser);
        this.commentRepository.save(comment);
    }

    // CommentService.java
    public List<Comment> getCommentsByUserId(Long userId) {
        return commentRepository.findByAuthorId(userId);
    }
}
