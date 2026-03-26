package com.hackhaton.fctwaterbilling.repository;

import com.hackhaton.fctwaterbilling.entity.Tariff;
import com.hackhaton.fctwaterbilling.enums.HouseType;
import com.hackhaton.fctwaterbilling.enums.TariffTier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TariffRepository extends JpaRepository<Tariff, Long> {

    List<Tariff> findAllByOrderByIdAsc();

    Optional<Tariff> findFirstByHouseTypeAndTariffTierAndIsActiveTrueOrderByIdAsc(
            HouseType houseType, TariffTier tariffTier);
}
