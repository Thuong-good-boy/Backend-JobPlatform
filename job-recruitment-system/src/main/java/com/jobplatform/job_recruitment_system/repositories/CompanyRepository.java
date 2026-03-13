package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.models.Company;
import com.jobplatform.job_recruitment_system.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> {

    // Kiểm tra xem Mã số thuế đã tồn tại chưa (để tránh trùng lặp)
    boolean existsByTaxCode(String taxCode);

    Optional<Company> findByUserId(Integer userId);

    Optional<Company> findByUser(User user);


}