package com.project5.OneStop.collegeprediction.repository;

import com.project5.OneStop.collegeprediction.entity.CollegeCutoff;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CollegeCutoffRepository
        extends JpaRepository<CollegeCutoff, Long> {

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM CollegeCutoff c
        WHERE c.year = :year
        AND c.counsellingAuthority = :authority
    """)
    void deleteByYearAndCounsellingAuthority(
            @Param("year") Integer year,
            @Param("authority") String authority
    );

    List<CollegeCutoff> findByYearAndCounsellingAuthority(
            Integer year,
            String counsellingAuthority
    );
}