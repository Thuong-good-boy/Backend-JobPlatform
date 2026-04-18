package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.dtos.*;
import com.jobplatform.job_recruitment_system.dtos.Response.CompanyDashboardResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.CompanyProfileResponse;
import com.jobplatform.job_recruitment_system.dtos.request.CompanyOnboardingRequest;
import com.jobplatform.job_recruitment_system.dtos.request.UpDateProfileCompanyRequest;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.mapper.CompanyMapper;
import com.jobplatform.job_recruitment_system.models.*;
import com.jobplatform.job_recruitment_system.repositories.ApplicationRepository;
import com.jobplatform.job_recruitment_system.repositories.CompanyRepository;
import com.jobplatform.job_recruitment_system.repositories.JobRepository;
import com.jobplatform.job_recruitment_system.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Lưu ý import đúng cái này
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {

     private final UserRepository userRepository;
     private final CompanyRepository companyRepository;
     private final FileUploadService fileUploadService;
     private final AiOcrService aiOcrService;
     private final JobRepository jobRepository;
     private final ApplicationRepository applicationRepository;
     private  final UserService userService;
     private final CompanyMapper companyMapper;

    @Transactional
    public void processOnboarding( CompanyOnboardingRequest request) throws Exception {
        Long userId = userService.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_008));
        Company company = companyRepository.findById(userId).orElse(new Company());
        if (company.getUser() == null) {
            company.setUser(user);
        }
        companyMapper.upDateCompany( request,company);

        if (request.getLogo() != null && !request.getLogo().isEmpty()) {
            String logoUrl = fileUploadService.uploadFile(request.getLogo());
            company.setLogoUrl(logoUrl);
        }

        if (request.getLicenseImage() != null && !request.getLicenseImage().isEmpty()) {
            String tempLicenseUrl = null;
            try {
                tempLicenseUrl = fileUploadService.uploadFile(request.getLicenseImage());

                OcrResult ocrResult = aiOcrService.extractCompanyInfo(request.getLicenseImage());

                if (ocrResult != null && ocrResult.getTaxCode() != null && ocrResult.getCompanyName() != null) {

                    String inputName = normalizeString(request.getCompanyName());
                    String aiName = normalizeString(ocrResult.getCompanyName());

                    String inputTax = request.getTaxCode().trim();
                    String aiTax = ocrResult.getTaxCode().trim();

                    boolean isTaxMatch = inputTax.equals(aiTax);
                    boolean isNameMatch = aiName.contains(inputName) || inputName.contains(aiName);

                    if (isTaxMatch && isNameMatch) {
                        company.setVerified(true);
                    } else {
                        throw  new AppException(ErrorCode.GPKD_002);
                    }
                }
            } catch (Exception e) {
                    throw  new AppException(ErrorCode.GPKD_003);
            } finally {
                if (tempLicenseUrl != null) {
                    fileUploadService.deleteImage(tempLicenseUrl);
                    company.setLicenseImageUrl(null);
                }
            }
        }

        companyRepository.save(company);
    }

    private final String normalizeString(String input) {
        if (input == null) return "";
        return input.toLowerCase()
                .replaceAll("công ty", "")
                .replaceAll("cổ phần", "")
                .replaceAll("cp", "")
                .replaceAll("tnhh", "")
                .replaceAll("[^a-z0-9]", "")
                .trim();
    }
    public CompanyDashboardResponse getCompanyDashboard() {
        Long userId = userService.getCurrentUserId();
        Company company = companyRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.COM_001));

        long totalJobs = jobRepository.countByCompanyUserId(userId);
        long activeJobs = jobRepository.countByCompanyUserIdAndStatus(userId, JobStatus.OPEN);
        long totalApplications = applicationRepository.countByJob_Company_UserId(userId);

        List<Application> recentApps = applicationRepository.findTop7ByJob_Company_UserIdOrderByAppliedAtDesc(userId);

        CompanyDashboardResponse Response = companyMapper.todto(company);
        Response.setTotalJobs(totalJobs);
        Response.setTotalApplications(totalApplications);
        Response.setActiveJobs(activeJobs);

        return Response;
    }
    private final List<RecentApplicationDTO> mapToRecentAppDTOList(List<Application> applications) {
        return applications.stream().map(app -> new RecentApplicationDTO(
                app.getId(),
                app.getFullname(),
                app.getJob().getTitle(),
                app.getStatus().name(),
                app.getAppliedAt()
        )).collect(Collectors.toList());
    }
    @Transactional
    public void updateProfileText(UpDateProfileCompanyRequest data) {
        Long userId = userService.getCurrentUserId();
        Company company = companyRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.COM_001));
         companyMapper.updateCompanyByProfileRequest(data,company);
        companyRepository.save(company);
    }

    @Transactional
    public void verifyLicense( MultipartFile licenseImage) throws Exception {
        Long userId = userService.getCurrentUserId();
        Company company = companyRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.COM_001));

        if (company.isVerified()) {
            throw new AppException(ErrorCode.COM_002);
        }

        String tempLicenseUrl = null;
        try {
            tempLicenseUrl = fileUploadService.uploadFile(licenseImage);
            OcrResult ocrResult = aiOcrService.extractCompanyInfo(licenseImage);

            if (ocrResult == null || ocrResult.getTaxCode() == null || ocrResult.getCompanyName() == null) {
                throw new AppException(ErrorCode.COM_003);
            }

            String inputName = normalizeString(company.getCompanyName());
            String aiName = normalizeString(ocrResult.getCompanyName());

            String currentTax = company.getTaxCode() != null ? company.getTaxCode().trim() : "";
            String aiTax = ocrResult.getTaxCode().trim();

            boolean isTaxMatch = currentTax.equals(aiTax);
            boolean isNameMatch = aiName.contains(inputName) || inputName.contains(aiName);

            if (isTaxMatch && isNameMatch) {
                company.setVerified(true);
                companyRepository.save(company);

            } else {
                throw new AppException(ErrorCode.COM_004);
            }

        } finally {
            if (tempLicenseUrl != null) {
                fileUploadService.deleteImage(tempLicenseUrl);
            }
        }
    }
    public CompanyProfileResponse getCompanyProfile() {
        Long userId = userService.getCurrentUserId();
        Company company = companyRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.COM_001));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_008));

        CompanyProfileResponse profile = companyMapper.toProfileResponse(company);


        return profile;
    }
    public List<TopCompanyResponseDTO> getTopCompanies() {
        return companyRepository.findTopCompaniesByJobCount();
    }

    public List<TopCompanyResponseDTO> searchCompanies(String keyword) {
        return companyRepository.searchCompaniesWithJobCount(keyword);
    }

}