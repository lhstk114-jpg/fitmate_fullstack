package org.spring.backend.chatbot.entity;

import jakarta.persistence.*;
import lombok.*;
import org.spring.backend.chatbot.enumtype.KeywordType;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "chat_tb")
public class ChatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id")
    private Long id;

    @Column
    private String resStr; //답변

    @Column(nullable = false, unique = true)
    private String search; //검색단어

    //키워드 타입 추가 (CATEGORY / ACTION)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KeywordType keywordType;

    //1:N
    @OneToMany(mappedBy = "chatEntity",
            cascade = CascadeType.REMOVE,fetch = FetchType.LAZY)
    private List<AnswerEntity> answerEntities;
}
