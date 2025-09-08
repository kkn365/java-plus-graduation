package ru.practicum.ewm.stats.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.dto.HitsStatDTO;
import ru.practicum.ewm.stats.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface HitsRepository extends JpaRepository<Hit, Long> {

    @Query("SELECT new ru.practicum.dto.HitsStatDTO(h.app, h.uri, COUNT(h)) " +
           "FROM Hit h " +
           "WHERE h.timestamp BETWEEN :start AND :end " +
           "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h) DESC")
    List<HitsStatDTO> findAllStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT new ru.practicum.dto.HitsStatDTO(h.app, h.uri, COUNT(DISTINCT h.ip)) " +
           "FROM Hit h " +
           "WHERE h.timestamp BETWEEN :start AND :end " +
           "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<HitsStatDTO> findUniqueIpStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT new ru.practicum.dto.HitsStatDTO(h.app, h.uri, COUNT(h)) " +
           "FROM Hit h " +
           "WHERE h.timestamp BETWEEN :start AND :end AND h.uri IN :uris " +
           "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h) DESC")
    List<HitsStatDTO> findAllStatsForUris(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("uris") List<String> uris);

    @Query("SELECT new ru.practicum.dto.HitsStatDTO(h.app, h.uri, COUNT(DISTINCT h.ip)) " +
           "FROM Hit h " +
           "WHERE h.timestamp BETWEEN :start AND :end AND h.uri IN :uris " +
           "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<HitsStatDTO> findUniqueIpStatsForUris(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("uris") List<String> uris);
}