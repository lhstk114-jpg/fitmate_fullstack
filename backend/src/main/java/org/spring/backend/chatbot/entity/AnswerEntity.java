package org.spring.backend.chatbot.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "answer_tb")
public class AnswerEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "answer_id")
  private Long id;

//질문에 대해 답변할 단어
  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String content;

  //N:1
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "chat_id")
  private ChatEntity chatEntity;

}
