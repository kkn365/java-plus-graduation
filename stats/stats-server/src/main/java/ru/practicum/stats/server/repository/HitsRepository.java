package ru.practicum.stats.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.stats.dto.HitsStatDTO;
import ru.practicum.stats.server.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Репозиторий для работы с сущностью Hit.
 * <p>
 * Предоставляет методы для получения статистики по просмотрам (hit) за определённый период.
 */
public interface HitsRepository extends JpaRepository<Hit, Long> {

    /**
     * Получает общую статистику по всем URI в указанном временном диапазоне.
     *
     * @param start начальная дата диапазона
     * @param end   конечная дата диапазона
     * @return список DTO со статистикой (app, uri, количество просмотров)
     */
    @Query("""
           SELECT new ru.practicum.stats.dto.HitsStatDTO(h.app, h.uri, COUNT(h))
           FROM Hit h
           WHERE h.timestamp BETWEEN :start AND :end
           GROUP BY h.app, h.uri
           ORDER BY COUNT(h) DESC
           """)
    List<HitsStatDTO> findAllStats(@Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);

    /**
     * Получает статистику с учётом уникальных IP-адресов в указанном временном диапазоне.
     *
     * @param start начальная дата диапазона
     * @param end   конечная дата диапазона
     * @return список DTO со статистикой (app, uri, количество уникальных IP)
     */
    @Query("""
           SELECT new ru.practicum.stats.dto.HitsStatDTO(h.app, h.uri, COUNT(DISTINCT h.ip))
           FROM Hit h
           WHERE h.timestamp BETWEEN :start AND :end
           GROUP BY h.app, h.uri
           ORDER BY COUNT(DISTINCT h.ip) DESC
           """)
    List<HitsStatDTO> findUniqueIpStats(@Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);

    /**
     * Получает общую статистику по указанным URI в указанном временном диапазоне.
     *
     * @param start начальная дата диапазона
     * @param end   конечная дата диапазона
     * @param uris  список URI для фильтрации
     * @return список DTO со статистикой (app, uri, количество просмотров)
     */
    @Query("""
           SELECT new ru.practicum.stats.dto.HitsStatDTO(h.app, h.uri, COUNT(h))
           FROM Hit h
           WHERE h.timestamp BETWEEN :start AND :end AND h.uri IN :uris
           GROUP BY h.app, h.uri
           ORDER BY COUNT(h) DESC
           """)
    List<HitsStatDTO> findAllStatsForUris(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end,
                                          @Param("uris") List<String> uris);

    /**
     * Получает статистику с учётом уникальных IP-адресов по указанным URI в указанном временном диапазоне.
     *
     * @param start начальная дата диапазона
     * @param end   конечная дата диапазона
     * @param uris  список URI для фильтрации
     * @return список DTO со статистикой (app, uri, количество уникальных IP)
     */
    @Query("""
           SELECT new ru.practicum.stats.dto.HitsStatDTO(h.app, h.uri, COUNT(DISTINCT h.ip))
           FROM Hit h
           WHERE h.timestamp BETWEEN :start AND :end AND h.uri IN :uris
           GROUP BY h.app, h.uri
           ORDER BY COUNT(DISTINCT h.ip) DESC
           """)
    List<HitsStatDTO> findUniqueIpStatsForUris(@Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end,
                                               @Param("uris") List<String> uris);
}