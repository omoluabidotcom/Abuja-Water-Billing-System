package com.hackhaton.fctwaterbilling.repository;

import com.hackhaton.fctwaterbilling.entity.CustomerAccount;
import com.hackhaton.fctwaterbilling.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerAccountRepository extends JpaRepository<CustomerAccount, Long> {

    List<CustomerAccount> findAllByOrderByIdAsc();

    List<CustomerAccount> findByAccountStatusOrderByIdAsc(AccountStatus accountStatus);
}
