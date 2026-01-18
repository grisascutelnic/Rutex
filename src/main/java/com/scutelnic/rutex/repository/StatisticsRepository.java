package com.scutelnic.rutex.repository;

import com.scutelnic.rutex.entity.Statistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface StatisticsRepository extends JpaRepository<Statistics, Long> {
    
    Optional<Statistics> findByStatKey(String statKey);
    
    @Modifying
    @Transactional
    @Query("UPDATE Statistics s SET s.statValue = s.statValue + :increment WHERE s.statKey = :statKey")
    int incrementStatValue(@Param("statKey") String statKey, @Param("increment") Long increment);
    
    @Modifying
    @Transactional
    @Query("UPDATE Statistics s SET s.statValue = :value WHERE s.statKey = :statKey")
    int setStatValue(@Param("statKey") String statKey, @Param("value") Long value);
}
