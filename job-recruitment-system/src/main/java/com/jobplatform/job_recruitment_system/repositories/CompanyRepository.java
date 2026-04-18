package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.dtos.TopCompanyResponseDTO;
import com.jobplatform.job_recruitment_system.models.Company;
import com.jobplatform.job_recruitment_system.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    // Kiểm tra xem Mã số thuế đã tồn tại chưa (để tránh trùng lặp)
    boolean existsByTaxCode(String taxCode);

    Optional<Company> findByUserId(Long userId);

    Optional<Company> findByUser(User user);

    @Query("SELECT new com.jobplatform.job_recruitment_system.dtos.TopCompanyResponseDTO(" +
            "c.id, c.companyName, c.logoUrl, c.description, COUNT(j.id)) " +
            "FROM Company c " +
            "LEFT JOIN Job j ON c.id = j.company.id  AND j.status = 'OPEN'" +
            "GROUP BY c.id, c.companyName, c.logoUrl, c.description " +
            "ORDER BY COUNT(j.id) DESC")
    List<TopCompanyResponseDTO> findTopCompaniesByJobCount();

    // 2. Tìm kiếm công ty theo tên, vẫn đếm số job
    @Query("SELECT new com.jobplatform.job_recruitment_system.dtos.TopCompanyResponseDTO(" +
            "c.id, c.companyName, c.logoUrl, c.description, COUNT(j.id)) " +
            "FROM Company c " +
            "LEFT JOIN Job j ON c.id = j.company.id " +
            "WHERE LOWER(c.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "GROUP BY c.id, c.companyName, c.logoUrl, c.description " +
            "ORDER BY COUNT(j.id) DESC")
    List<TopCompanyResponseDTO> searchCompaniesWithJobCount(@Param("keyword") String keyword);
}