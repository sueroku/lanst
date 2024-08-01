package com.beyond.lanst.member.service;

import com.beyond.lanst.member.domain.Member;
import com.beyond.lanst.member.dto.MemberLoginDto;
import com.beyond.lanst.member.dto.MemberResDto;
import com.beyond.lanst.member.dto.MemberSaveReqDto;
import com.beyond.lanst.member.dto.MemberUpReqDto;
import com.beyond.lanst.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
    @Autowired
    PasswordEncoder passwordEncoder;

    @Transactional
    public Member memberCreate(MemberSaveReqDto dto){
        if(memberRepository.findByEmailAndDelYn(dto.getEmail(), "N").isPresent()){
            throw new IllegalArgumentException("존재하는 회원입니다.");
        }
        return memberRepository.save(dto.toEntity(passwordEncoder.encode(dto.getPassword())));
    }

    @Transactional
    public Member login(MemberLoginDto dto){
        Member member = memberRepository.findByEmailAndDelYn(dto.getEmail(), "N").orElseThrow(()->new EntityNotFoundException("없는 회원입니다."));
        if(!passwordEncoder.matches(dto.getPassword(), member.getPassword())){
            throw new IllegalArgumentException("비밀번호 불일치");
        }
        return member;
    }

    @Transactional
    public Member memberUpdate(MemberUpReqDto dto){
        Member member = memberRepository.findByEmailAndDelYn(SecurityContextHolder.getContext().getAuthentication().getName(), "N").orElseThrow(()->new EntityNotFoundException("없는 회원입니다."));
        member.updateEntity(dto);
        return member;
    }

    public MemberResDto memberMyinfo(){
        Member member = memberRepository.findByEmailAndDelYn(SecurityContextHolder.getContext().getAuthentication().getName(), "N").orElseThrow(()->new EntityNotFoundException("없는 회원입니다."));
        return member.fromEntity();
    }

    public Page<MemberResDto> memberList(Pageable pageable){
        Page<Member> members = memberRepository.findByDelYn(pageable, "N");
        return members.map(a->a.fromEntity());
    }

    @Transactional
    public Member memberDelete(){
        Member member = memberRepository.findByEmailAndDelYn(SecurityContextHolder.getContext().getAuthentication().getName(), "N").orElseThrow(()->new EntityNotFoundException("없는 회원입니다."));
        member.updateDelYn("Y");
        return member;
    }


}
