package com.hackhaton.fctwaterbilling.repository;

import com.hackhaton.fctwaterbilling.entity.CustomerAccount;
import com.hackhaton.fctwaterbilling.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerAccountRepository extends JpaRepository<CustomerAccount, Long> {

    List<CustomerAccount> findAllByOrderByIdAsc();

    List<CustomerAccount> findByAccountStatusOrderByIdAsc(AccountStatus accountStatus);

    @Query("SELECT c FROM CustomerAccount c WHERE c.isestimated = true AND c.accountStatus = :status ORDER BY c.id ASC")
    List<CustomerAccount> findEstimatedByAccountStatus(@Param("status") AccountStatus status);
}
