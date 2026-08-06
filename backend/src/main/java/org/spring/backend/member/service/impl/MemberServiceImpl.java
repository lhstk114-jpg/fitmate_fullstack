package org.spring.backend.member.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.spring.backend.member.enumtype.Role;
import org.spring.backend.file.enumtype.TableType;
import org.spring.backend.file.handler.FileHandler;
import org.spring.backend.member.dto.MemberDto;
import org.spring.backend.member.entity.MemberAddEntity;
import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.member.repository.MemberAddRepository;
import org.spring.backend.member.repository.MemberRepository;
import org.spring.backend.member.service.MemberService;
import org.spring.backend.shop.reservation.dto.ReservationDto;
import org.spring.backend.shop.reservation.entity.ReservationEntity;
import org.spring.backend.shop.reservation.repository.ReservationRepository;
import org.spring.backend.trainer.dto.TrainerDto;
import org.spring.backend.trainer.entity.TrainerEntity;
import org.spring.backend.trainer.repository.TrainerRepository;
import org.spring.backend.trainer.service.TrainerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final MemberAddRepository memberAddRepository;
    private final FileHandler fileHandler;
    private final RedisTemplate<String, String> redisTemplate;
    private final TrainerService trainerService;
    private final ReservationRepository reservationRepository;
    private final TrainerRepository trainerRepository;

    @Value("${img.path.member}")
    private String filePath;
    @Transactional
    @Override
    public void insertMember(MemberDto memberDto) {
        Optional<MemberEntity> optionalMemberEntity = memberRepository.findByUserEmail(memberDto.getUserEmail());
        if(optionalMemberEntity.isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        //저장과 동시에 저장용 데이터 생성
        MemberEntity memberEntity = MemberEntity.toInsertMemberEntity(memberDto,passwordEncoder.encode(memberDto.getUserPw()));
        //추가 멤버데이터 저장을 위해 더미데이터 생성
        MemberAddEntity memberAdd = MemberAddEntity.createDefault();
        memberAdd.setInterest(memberDto.getInterest());

        memberEntity.setMemberAddEntity(memberAdd);
        //추가 멤버데이터까지 새로 저장
        memberRepository.save(memberEntity);
    }

    @Override
    public void insertAdminMember(MemberDto memberDto) {
        Optional<MemberEntity> optionalMemberEntity = memberRepository.findByUserEmail(memberDto.getUserEmail());
        if(optionalMemberEntity.isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        //저장과 동시에 저장용 데이터 생성
        MemberEntity memberEntity = MemberEntity.toInsertMemberAdminEntity(memberDto,passwordEncoder.encode(memberDto.getUserPw()));
        //추가 멤버데이터 저장을 위해 더미데이터 생성
        MemberAddEntity memberAdd = MemberAddEntity.createDefault();

        memberEntity.setMemberAddEntity(memberAdd);
        //추가 멤버데이터까지 새로 저장
        memberRepository.save(memberEntity);
    }

    @Override
    public boolean emailCheck(String userEmail) {
        return memberRepository.existsByUserEmail(userEmail);
    }

    @Override
    public List<MemberDto> memberList() {
        return memberRepository.findAll().stream().map(MemberDto::toMemberDto)
                .collect(Collectors.toList());
    }
    @Override
    public Page<MemberDto> memberList(Pageable pageable, String subject, String search, Role role) {
        if(subject==null||subject.isBlank()||search==null||search.isBlank()){
            return memberRepository.findByRole(pageable, role).map(MemberDto::toMemberDto);
        }
        Page<MemberEntity> memberEntities = switch (subject) {
            case "userName" -> memberRepository.findByRoleAndUserNameContaining(pageable, search, role);
            case "userEmail" -> memberRepository.findByRoleAndUserEmailContaining(pageable, search, role);
            default -> memberRepository.findByRole(pageable,role);
        };
        //멤버리스트 검색필터링기능
        return memberEntities.map(MemberDto::toMemberDto);
    }

    @Override
    public Page<ReservationDto> memberListSummary(Pageable pageable, String subject, String search, Long trainerId) {
        System.out.println("트레이너아이디:"+trainerId);
        if(subject==null||subject.isBlank()||search==null||search.isBlank()){
            return reservationRepository.findByTrainerId(trainerId,pageable).map(ReservationDto::toReservationDto);
        }
        Page<ReservationEntity> reservationEntities = switch (subject) {
            case "userName" -> reservationRepository.findByTrainerIdAndUserName(trainerId,search,pageable);
            case "interest" -> reservationRepository.findByTrainerIdAndUserName(trainerId, search ,pageable);
            default -> reservationRepository.findByTrainerId(trainerId,pageable);
        };
        //멤버리스트 검색필터링기능
        return reservationEntities.map(ReservationDto::toReservationDto);
    }

    @Override
    public MemberDto memberDetail(Long id) {
        MemberEntity memberEntity = memberRepository.findById(id)
                .orElseThrow(()->new NoSuchElementException("회원아이디 없음"));
        return MemberDto.toMemberDto(memberEntity);
    }

    @Override
    public MemberDto memberSummary(Long id) {
        MemberEntity memberEntity = memberRepository.findById(id)
                .orElseThrow(()->new NoSuchElementException("회원아이디 없음"));
        return MemberDto.toMemberDtoSummary(memberEntity);
    }

    @Override
    public MemberDto trainerSummary(Long id) {
        TrainerEntity trainer = trainerRepository.findById(id)
                .orElseThrow(()->new NoSuchElementException("트레이너아이디 없음"));
        MemberEntity memberEntity = memberRepository.findById(trainer.getMember().getId())
                .orElseThrow(()->new NoSuchElementException("회원아이디 없음"));
        return MemberDto.toMemberDtoSummary(memberEntity);
    }

    @Override
    public MemberDto memberDetail(String userEmail) {
        MemberEntity memberEntity = memberRepository.findByUserEmail(userEmail)
                .orElseThrow(()->new NoSuchElementException("회원아이디 없음"));
        return MemberDto.toMemberDtoSummary(memberEntity);
    }

    @Transactional
    @Override
    public void memberUpdate(MemberDto memberDto) throws IOException {
        MemberEntity originMemberEntity = memberRepository.findById(memberDto.getId())
                .orElseThrow(()->new NoSuchElementException("회원아이디 없음"));

        if(!memberDto.getUserEmail().equals(originMemberEntity.getUserEmail())) {
            if (memberRepository.existsByUserEmail(memberDto.getUserEmail())) {
                throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
            }
        }
        originMemberEntity.setUserEmail(memberDto.getUserEmail());
        if(memberDto.getUserPw() !=null && !memberDto.getUserPw().trim().isEmpty()){
            originMemberEntity.setUserPw(passwordEncoder.encode(memberDto.getUserPw()));
        }
        originMemberEntity.setUserName(memberDto.getUserName());
        originMemberEntity.setUserAddress(memberDto.getUserAddress());
        originMemberEntity.setUserPhone(memberDto.getUserPhone());
        originMemberEntity.setSubscribe(memberDto.getSubscribe());
        //멤버 추가데이터에 저장할 데이터 세팅(데이터가 있을 경우에만)
        if(memberDto.hasAdditionalData()){
            //멤버 추가데이터 저장 로직
            MemberAddEntity originMemberAddEntity =
                    memberAddRepository.findById(originMemberEntity.getMemberAddEntity().getId())
                            .orElseThrow(()->new NoSuchElementException("회원아이디 없음"));
            originMemberAddEntity.setInterest(memberDto.getInterest());
            originMemberAddEntity.setHeight(memberDto.getHeight());
            originMemberAddEntity.setWeight(memberDto.getWeight());
            originMemberAddEntity.setGoalWeight(memberDto.getGoalWeight());
        }
        Role beforeRole = originMemberEntity.getRole();

        originMemberEntity.setRole(memberDto.getRole());
        
        if(beforeRole != Role.TRAINER
                && memberDto.getRole() == Role.TRAINER){
        
            trainerService.createTrainerByRoleChange(originMemberEntity);
        }

        if(memberDto.getMemberFile() == null){
            originMemberEntity.setProfilePhoto(memberDto.getProfilePhoto());
            memberRepository.save(originMemberEntity);
            return;
        }
        originMemberEntity.setProfilePhoto(1);
        MemberEntity saveMember = memberRepository.save(originMemberEntity);
        fileHandler.insertFile(filePath,TableType.MEMBER, saveMember.getId(), memberDto.getMemberFile());
    }
    @Transactional
    @Override
    public void memberDelete(Long id) throws IOException{
        MemberEntity memberEntity = memberRepository.findById(id)
                .orElseThrow(()->new NoSuchElementException("회원아이디 없음"));
        memberCommonDelete(memberEntity);
    }

    @Transactional
    @Override
    public void memberDelete(String userEmail) throws IOException{
        MemberEntity memberEntity = memberRepository.findByUserEmail(userEmail)
                .orElseThrow(()->new NoSuchElementException("회원정보 없음"));
        memberCommonDelete(memberEntity);
    }

    @Override
    public MemberDto memberInit(String userEmail) {
        MemberEntity memberEntity = memberRepository.findByUserEmail(userEmail)
        .orElseThrow(()->new NoSuchElementException("이메일이 존재하지 않습니다."));

        return MemberDto.toInitMemberDto(memberEntity);
    }

    //멤버 삭제시 공통으로 들어가는 작업 함수화
    void memberCommonDelete(MemberEntity memberEntity) throws IOException{
        //redis에 userEmail명으로 저장되어있는 refresh토큰이 있는지 확인 후 제거
        if (redisTemplate.hasKey(memberEntity.getUserEmail())) {
            redisTemplate.delete(memberEntity.getUserEmail());
        }
        //멤버 추가 엔티티가 존재하는지 확인 후 연결상태들 전부 제거
        if (memberEntity.getMemberAddEntity() != null) {
            MemberAddEntity addEntity = memberEntity.getMemberAddEntity();

            // 부모 객체에서 자식으로 가는 연결 고리를 null로 끊음
            memberEntity.setMemberAddEntity(null);

            // 자식(MemberAdd) 데이터를 먼저 직접 삭제
            memberAddRepository.delete(addEntity);

            //자식데이터 직접 삭제
            memberEntity.setMemberAddEntity(null);
        }
        //파일이 존재하면 삭제, 존재하지 않을땐 그대로 넘어감
        fileHandler.deleteFile(filePath, TableType.MEMBER,memberEntity.getId());
        //이후 회원탈퇴 진행
        memberRepository.delete(memberEntity);
    }

    
}
