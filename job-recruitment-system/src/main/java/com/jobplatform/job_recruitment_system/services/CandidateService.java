package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.dtos.Response.CandidateProfileResponse;
import com.jobplatform.job_recruitment_system.dtos.request.CandidateProfileRequest;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.mapper.CandidateMapper;
import com.jobplatform.job_recruitment_system.models.Candidate;
import com.jobplatform.job_recruitment_system.models.User;
import com.jobplatform.job_recruitment_system.repositories.CandidateRepository;
import com.jobplatform.job_recruitment_system.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CandidateService {


    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;
    private final CandidateMapper candidateMapper;
    private final UserService userService;

    @Transactional
    public Candidate saveOrUpdateProfile(Long userId, Candidate profileData) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElse(new Candidate());

        if (candidate.getUser() == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));
            candidate.setUser(user);
        }

        candidateMapper.upDidateCandidate(profileData,candidate);

        return candidateRepository.save(candidate);
    }
    @Transactional
    public void updateProfile( CandidateProfileRequest dto) {
        Long userId = userService.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_012));

        user.setFullName(dto.getFullName());
        userRepository.save(user);

        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElse(new Candidate());
        candidateMapper.upCadidateByDTO(dto,candidate);
        candidateRepository.save(candidate);
    }

    public CandidateProfileResponse getMyProfile(){
        Long userid= userService.getCurrentUserId();
        User user = userService.getUserId(userid).orElseThrow(() ->new AppException(ErrorCode.AUTH_008));
        Candidate candidate = candidateRepository.findByUserId(userid).orElseThrow(()-> new AppException(ErrorCode.USER_012));
        return  candidateMapper.toDTO(user,candidate);

    }
    public Candidate getProfile(Long userId) {
        return candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Chưa có profile ứng viên"));
    }

}
