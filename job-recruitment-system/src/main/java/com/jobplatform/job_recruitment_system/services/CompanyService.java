package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.dtos.Response.*;
import com.jobplatform.job_recruitment_system.dtos.request.CompanyOnboardingRequest;
import com.jobplatform.job_recruitment_system.dtos.request.UpDateProfileCompanyRequest;
import com.jobplatform.job_recruitment_system.dtos.request.VietQrResponse;
import com.jobplatform.job_recruitment_system.enums.JobStatus;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.mapper.CompanyMapper;
import com.jobplatform.job_recruitment_system.models.*;
import com.jobplatform.job_recruitment_system.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Lưu ý import đúng cái này
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;

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
    private  final ObjectMapper objectMapper = new ObjectMapper();
    private  final CandidateRepository candidateRepository;
    private  final UserSubscriptionRepository userSubscriptionRepository;
    @Transactional
    public void processOnboarding(CompanyOnboardingRequest request) throws Exception {
        Long userId = userService.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_008));

        Long companyId = companyRepository.getCompanyId(userId);
        Company company = (companyId != null)
                ? companyRepository.findById(companyId).orElse(new Company())
                : new Company();

        if (company.getUser() == null) {
            company.setUser(user);
        }

        companyMapper.upDateCompany(request, company);
        company.setRemainingBoosts(0);

        if (request.getLicenseImage() == null || request.getLicenseImage().isEmpty()) {
            throw new AppException(ErrorCode.GPKD_001);
        }

        try {
            OcrResultReponse ocrResultReponse = aiOcrService.extractCompanyInfo(request.getLicenseImage());
            if (ocrResultReponse == null || ocrResultReponse.getTaxCode() == null) {
                throw new AppException(ErrorCode.GPKD_002);
            }

            String taxCode = ocrResultReponse.getTaxCode().trim();

            if (companyRepository.existsTCode(taxCode) && !taxCode.equals(company.getTaxCode())) {
                throw new AppException(ErrorCode.COM_006);
            }

            RestTemplate restTemplate = new RestTemplate();
            String vietQrUrl = "https://api.vietqr.io/v2/business/" + taxCode;
            VietQrResponse qrResponse = null;

            int maxRetries = 3;
            for (int i = 0; i < maxRetries; i++) {
                try {
                    qrResponse = restTemplate.getForObject(vietQrUrl, VietQrResponse.class);
                    break;
                } catch (HttpClientErrorException.TooManyRequests e) {
                    if (i == maxRetries - 1) {
                        System.err.println("VietQR quá tải (429) sau 3 lần thử.");
                        throw new AppException(ErrorCode.GPKD_004);
                    }
                    Thread.sleep(2000);
                }
            }

            if (qrResponse == null || !"00".equals(qrResponse.getCode()) || qrResponse.getData() == null) {
                throw new AppException(ErrorCode.GPKD_004);
            }

            VietQrDataRequest officialData = qrResponse.getData();
            company.setCompanyName(officialData.getName());
            company.setTaxCode(officialData.getId());
            company.setAddress(officialData.getAddress());
            company.setVerified(true);

            String licenseUrl = fileUploadService.uploadFile(request.getLicenseImage());
            company.setLicenseImageUrl(licenseUrl);

            String websiteUrl = ocrResultReponse.getWebsite();
            String finalLogoUrl = "";
            if (websiteUrl != null && !websiteUrl.trim().isEmpty() && !websiteUrl.equalsIgnoreCase("null")) {
                String domain = websiteUrl.replaceAll("^https?://", "")
                        .replaceFirst("^www\\.", "")
                        .split("/")[0];

                finalLogoUrl = "https://www.google.com/s2/favicons?domain=" + domain + "&sz=128";
            } else {
                // Fallback UI Avatar
                String encodedName = officialData.getName().replace(" ", "+");
                finalLogoUrl = "https://ui-avatars.com/api/?name=" + encodedName + "&background=random&color=fff&size=128";
            }

            company.setLogoUrl(finalLogoUrl);
            user.setAvatarUrl(finalLogoUrl);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            System.out.println("Lỗi hệ thống khi Onboarding: " + e.getMessage());
            throw new AppException(ErrorCode.GPKD_003);
        }
        userRepository.save(user);
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
        Company company = companyRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AppException(ErrorCode.COM_001));

        long totalJobs = jobRepository.countByCompany_User_Id(userId);
        long activeJobs = jobRepository.countByCompany_User_IdAndStatus(userId, JobStatus.OPEN);
        long totalApplications = applicationRepository.countByJob_Company_User_Id(userId);

        List<Application> recentApps = applicationRepository.findTop7ByJob_Company_User_IdOrderByAppliedAtDesc(userId);

        CompanyDashboardResponse Response = companyMapper.todto(company);
        Response.setTotalJobs(totalJobs);
        Response.setTotalApplications(totalApplications);
        Response.setActiveJobs(activeJobs);

        return Response;
    }

    @Transactional
    public void updateProfileText(UpDateProfileCompanyRequest data) {
        Long userId = userService.getCurrentUserId();
        Company company = companyRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AppException(ErrorCode.COM_001));
         companyMapper.updateCompanyByProfileRequest(data,company);
        companyRepository.save(company);
    }

    @Transactional
    public void verifyLicense( MultipartFile licenseImage) throws Exception {
        Long userId = userService.getCurrentUserId();
        Company company = companyRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AppException(ErrorCode.COM_001));

        if (company.isVerified()) {
            throw new AppException(ErrorCode.COM_002);
        }

        String tempLicenseUrl = null;
        try {
            tempLicenseUrl = fileUploadService.uploadFile(licenseImage);
            OcrResultReponse ocrResultReponse = aiOcrService.extractCompanyInfo(licenseImage);

            if (ocrResultReponse == null || ocrResultReponse.getTaxCode() == null ) {
                throw new AppException(ErrorCode.COM_003);
            }
            String currentTax = company.getTaxCode() != null ? company.getTaxCode().trim() : "";
            String aiTax = ocrResultReponse.getTaxCode().trim();
            System.out.println();
            boolean isTaxMatch = currentTax.equals(aiTax);

            if (isTaxMatch ) {
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
        Company company = companyRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AppException(ErrorCode.COM_001));
        CompanyProfileResponse profile = companyMapper.toProfileResponse(company);
        profile.setProEnd(userSubscriptionRepository.getProEnd(userId));
        return profile;
    }
    public List<TopCompanyResponse> getTopCompanies() {
        return companyRepository.findTopCompaniesByJobCount();
    }

    public Slice<TopCompanyResponse> searchCompanies(String keyword, int page, int  size) {
        Pageable  pageable = PageRequest.of(page,size);
        return companyRepository.searchCompaniesWithJobCount(pageable,keyword);
    }
    @Transactional
    public  void postLogo(MultipartFile logo){
        Long userId = userService.getCurrentUserId();
        Company company = companyRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AppException(ErrorCode.COM_001));
        try {
            String logoUrl = fileUploadService.uploadFile(logo);
            deleteAvatarCu(company.getLogoUrl());
            company.setLogoUrl(logoUrl);
            companyRepository.save(company);
        }catch (Exception e){
            e.printStackTrace();
        }


    }
    @Async
    public  void deleteAvatarCu(String urlAvatar){
        fileUploadService.deleteFile(urlAvatar);

    }
    public  Company getCompanyById(Long companyId){
        return  companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(ErrorCode.COM_001));
    }
    public Page<Candidate> searchCandidate(String keyword, String location, Integer minExp,List<String> skills, int page){
        String skillsJsonString = null;
        if(skills != null && !skills.isEmpty()){
            ObjectNode rootNode = objectMapper.createObjectNode();
            ArrayNode skillsArray = rootNode.putArray("skills");
            skills.forEach(skillsArray:: add);
            skillsJsonString = rootNode.toString();
        }
        Pageable pageable = PageRequest.of(page,10);
        Page<Candidate> result = candidateRepository.searchCandidates(location, minExp, keyword, skillsJsonString, pageable);
        System.out.println("total_pages : "+ result.getTotalPages());
        return  result;
    }

}