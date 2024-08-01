package com.beyond.lanst.member.dto;

import com.beyond.lanst.member.domain.Member;
import com.beyond.lanst.member.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.swing.plaf.PanelUI;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberSaveReqDto {
    private String nickname;
    @NotEmpty(message="이메일미입력")
    private String email;
    @NotEmpty(message="비밀번호미입력")
    @Size(min=8, message="비밀번호 최소 길이는 8")
    private String password;
    private Role role = Role.USER;

    public Member toEntity(String password){
        return new Member().builder()
                .nickname(this.nickname).email(this.email).password(password)
                .role(this.role)
                .build();
    }
}
