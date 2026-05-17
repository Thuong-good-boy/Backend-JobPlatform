package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.dtos.Response.TopCompanyResponse;
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

    Optional<Company> findByUser_Id(Long userId);

    @Query("select count(c)>0 from Company c where c.taxCode = :taxcode")
    boolean existsTCode(@Param("taxcode") String taxcode);

    Optional<Company> findByUser(User user);

    @Query("SELECT new com.jobplatform.job_recruitment_system.dtos.Response.TopCompanyResponse(" +
            "c.id, c.companyName, c.logoUrl, c.description, COUNT(j.id)) " +
            "FROM Company c " +
            "LEFT JOIN Job j ON j.company = c AND j.status = 'OPEN' " +
            "GROUP BY c.id, c.companyName, c.logoUrl, c.description " +
            "ORDER BY COUNT(j.id) DESC")
    List<TopCompanyResponse> findTopCompaniesByJobCount();

    @Query("SELECT new com.jobplatform.job_recruitment_system.dtos.Response.TopCompanyResponse(" +
            "c.id, c.companyName, c.logoUrl, c.description, COUNT(j.id)) " +
            "FROM Company c " +
            "LEFT JOIN Job j ON j.company = c " +
            "WHERE LOWER(c.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "GROUP BY c.id, c.companyName, c.logoUrl, c.description " +
            "ORDER BY COUNT(j.id) DESC")
    List<TopCompanyResponse> searchCompaniesWithJobCount(@Param("keyword") String keyword);

    @Query("SELECT COUNT(c) FROM Company c")
    Long totalCompany();

    @Query("select c.id from Company  c where c.user.id = :userId ")
    Long getCompanyId(@Param("userId") Long userId);

}