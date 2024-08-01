package com.beyond.lanst.member.controller;

import com.beyond.lanst.common.auth.JwtTokenProvider;
import com.beyond.lanst.common.dto.CommonErrorDto;
import com.beyond.lanst.common.dto.CommonResDto;
import com.beyond.lanst.member.domain.Member;
import com.beyond.lanst.member.dto.MemberLoginDto;
import com.beyond.lanst.member.dto.MemberRefreshDto;
import com.beyond.lanst.member.dto.MemberSaveReqDto;
import com.beyond.lanst.member.dto.MemberUpReqDto;
import com.beyond.lanst.member.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    @Qualifier("4")
    private final RedisTemplate<String, Object> redisTemplate;
    @Autowired
    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider, RedisTemplate<String, Object> redisTemplate) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/member/create")
    public ResponseEntity<?> memberCreate(@Valid @RequestBody MemberSaveReqDto dto){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "회원가입성공", memberService.memberCreate(dto).getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody MemberLoginDto dto){
        Member member = memberService.login(dto);
        String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());
        String refreshToken = jwtTokenProvider.createRtoken(member.getEmail(), member.getRole().toString());
        redisTemplate.opsForValue().set(member.getEmail(), refreshToken, 240, TimeUnit.HOURS);
        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", member.getId());
        loginInfo.put("token", jwtToken);
        loginInfo.put("refreshToken", refreshToken);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "로그인성공", loginInfo);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @GetMapping("/member/myinfo")
    public ResponseEntity<?> memberMyinfo(){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "회원정보조회성공", memberService.memberMyinfo());
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN)")
    @GetMapping("/member/list")
    public ResponseEntity<?> memberList(Pageable pageable){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "회원목록조회성공", memberService.memberList(pageable));
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @PatchMapping("/member/update")
    public ResponseEntity<?> memberUpdate(@RequestBody MemberUpReqDto dto){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "회원정보수정성공", memberService.memberUpdate(dto).getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @DeleteMapping("/member/delete")
    public ResponseEntity<?> memberDelete(){
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "회원삭제성공", memberService.memberDelete().getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    @PostMapping("/refresh-token")
    public ResponseEntity<?> generateNewAccessToken(@RequestBody MemberRefreshDto dto){
        String rt = dto.getRefreshToken();
        Claims claims = null;
        try{
            claims = Jwts.parser().setSigningKey(secretKeyRt).parseClaimsJws(rt).getBody();
        }catch (Exception e){
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.UNAUTHORIZED, "invalid refresh token"), HttpStatus.UNAUTHORIZED);
        }
        String email = claims.getSubject();
        String role = claims.get("role").toString();
        Object object = redisTemplate.opsForValue().get(email);
        if(object==null || !object.toString().equals(rt)){
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.UNAUTHORIZED, "invalid refresh token"), HttpStatus.UNAUTHORIZED);
        }
        String newAt = jwtTokenProvider.createToken(email, role);
        Map<String, Object> info = new HashMap<>();
        info.put("token",newAt);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "At is renewed", info);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }



}
