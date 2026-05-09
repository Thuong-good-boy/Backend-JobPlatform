package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.models.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSubscriptionRepository  extends JpaRepository<UserSubscription, Long> {
    @Query(value = """
            SELECT EXISTS (
       SELECT 1
       FROM user_subscriptions u
       JOIN packages p ON u.package_id = p.id
       WHERE u.user_id = :userId
         AND p.type IN ('COMPANY_PRO', 'CANDIDATE_PRO')
         AND u.status = 'ACTIVE'
         AND u.end_date >= CURRENT_TIMESTAMP
   );""",nativeQuery = true)
    boolean userispro(@Param("userId") Long userId);

    @Query("""
    SELECT us FROM UserSubscription us
    WHERE us.user.id = :userId
      AND us.status = SubscriptionStatus.ACTIVE
""")
    UserSubscription findActiveSubscription(@Param("userId") Long userId);

    @Query("select sum(p.price) from UserSubscription u join Package p on u.jobPackage.id= p.id ")
    Long getTotalDoanhThu();
}
