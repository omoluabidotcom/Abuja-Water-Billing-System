package com.hackhaton.fctwaterbilling.service;

import com.hackhaton.fctwaterbilling.entity.CustomerAccount;
import com.hackhaton.fctwaterbilling.enums.AccountStatus;
import com.hackhaton.fctwaterbilling.repository.CustomerAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CustomerAccountService {

    private static final Pattern ACC_NUM = Pattern.compile("(?i)ACC-?(\\d+)");

    private final CustomerAccountRepository customerAccountRepository;

    public List<CustomerAccount> listAll() {
        return customerAccountRepository.findAllByOrderByIdAsc();
    }

    public long countAll() {
        return customerAccountRepository.count();
    }

    public List<CustomerAccount> listFiltered(String searchText, AccountStatus statusFilter) {
        List<CustomerAccount> base = statusFilter == null
                ? customerAccountRepository.findAllByOrderByIdAsc()
                : customerAccountRepository.findByAccountStatusOrderByIdAsc(statusFilter);

        if (searchText == null || searchText.isBlank()) {
            return base;
        }

        String q = searchText.trim();
        Long idFromAcc = parseAccountId(q);
        String lower = q.toLowerCase(Locale.ROOT);

        return base.stream()
                .filter(c -> matchesSearch(c, lower, idFromAcc))
                .toList();
    }

    private static Long parseAccountId(String q) {
        Matcher m = ACC_NUM.matcher(q.trim());
        if (m.find()) {
            try {
                return Long.parseLong(m.group(1));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static boolean matchesSearch(CustomerAccount c, String lower, Long idFromAcc) {
        if (idFromAcc != null && idFromAcc.equals(c.getId())) {
            return true;
        }
        String fullName = (c.getFirstName() + " " + c.getLastName()).toLowerCase(Locale.ROOT);
        return Stream.of(
                        fullName,
                        nullToEmpty(c.getFirstName()),
                        nullToEmpty(c.getLastName()),
                        nullToEmpty(c.getEmail()),
                        nullToEmpty(c.getPhoneNumber()),
                        nullToEmpty(c.getServiceAddress()),
                        nullToEmpty(c.getBillingAddress()),
                        "acc-" + c.getId(),
                        String.valueOf(c.getId()))
                .map(s -> s.toLowerCase(Locale.ROOT))
                .anyMatch(s -> s.contains(lower));
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    @Transactional
    public CustomerAccount saveNew(CustomerAccount account) {
        return customerAccountRepository.save(account);
    }
}
