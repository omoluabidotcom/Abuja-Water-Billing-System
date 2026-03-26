package com.hackhaton.fctwaterbilling.service;

import com.hackhaton.fctwaterbilling.entity.Tariff;
import com.hackhaton.fctwaterbilling.enums.HouseType;
import com.hackhaton.fctwaterbilling.enums.TariffTier;
import com.hackhaton.fctwaterbilling.repository.TariffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TariffService {

    private final TariffRepository tariffRepository;

    @Transactional(readOnly = true)
    public List<Tariff> listAll() {
        return tariffRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return tariffRepository.count();
    }

    @Transactional(readOnly = true)
    public Optional<Tariff> findById(Long id) {
        return tariffRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Tariff> listFiltered(String searchText, HouseType houseType, TariffTier tierFilter) {
        List<Tariff> base = tariffRepository.findAllByOrderByIdAsc();

        return base.stream()
                .filter(t -> houseType == null || t.getHouseType() == houseType)
                .filter(t -> tierFilter == null || t.getTariffTier() == tierFilter)
                .filter(t -> matchesSearch(t, searchText))
                .toList();
    }

    private static boolean matchesSearch(Tariff t, String searchText) {
        if (searchText == null || searchText.isBlank()) {
            return true;
        }
        String lower = searchText.trim().toLowerCase(Locale.ROOT);
        String house = t.getHouseType() != null ? t.getHouseType().name().toLowerCase(Locale.ROOT) : "";
        String tier = t.getTariffTier() != null ? t.getTariffTier().name().toLowerCase(Locale.ROOT) : "";
        return Stream.of(
                        nullToEmpty(t.getName()),
                        nullToEmpty(t.getDescription()),
                        house,
                        tier,
                        t.getRatePerUnit() != null ? t.getRatePerUnit().toPlainString() : "",
                        t.getFixedTariff() != null ? t.getFixedTariff().toPlainString() : "",
                        String.valueOf(t.getId()))
                .map(s -> s.toLowerCase(Locale.ROOT))
                .anyMatch(s -> s.contains(lower));
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    @Transactional
    public Tariff create(Tariff tariff) {
        tariff.setId(null);
        return tariffRepository.save(tariff);
    }

    @Transactional
    public Tariff update(Long id, Tariff incoming) {
        Tariff existing = tariffRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tariff not found: " + id));
        existing.setName(incoming.getName());
        existing.setDescription(incoming.getDescription());
        existing.setHouseType(incoming.getHouseType());
        existing.setRatePerUnit(incoming.getRatePerUnit());
        existing.setActive(incoming.isActive());
        existing.setTariffTier(incoming.getTariffTier());
        existing.setFixedTariff(incoming.getFixedTariff() != null ? incoming.getFixedTariff() : BigDecimal.ZERO);
        return tariffRepository.save(existing);
    }
}
