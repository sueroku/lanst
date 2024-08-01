package com.beyond.lanst.member.domain;

import com.beyond.lanst.common.domain.BaseEntity;
import com.beyond.lanst.member.dto.MemberResDto;
import com.beyond.lanst.member.dto.MemberUpReqDto;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nickname;
    @Column(nullable = false, unique = true)
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    public void updateEntity(MemberUpReqDto dto){
        this.nickname = dto.getNickname();
        this.password = dto.getPassword();
    }

    public MemberResDto fromEntity(){
        return MemberResDto.builder()
                .id(this.id)
                .nickname(this.nickname)
                .email(this.email)
                .build();
    }
}
