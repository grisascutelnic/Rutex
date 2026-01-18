package com.scutelnic.rutex.service;

import com.scutelnic.rutex.entity.Statistics;
import com.scutelnic.rutex.repository.StatisticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class StatisticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);
    
    private final StatisticsRepository statisticsRepository;
    
    public StatisticsService(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }
    
    /**
     * Increment a statistic value
     */
    @Transactional
    public void incrementStat(String statKey) {
        try {
            if (statisticsRepository == null) {
                logger.error("StatisticsRepository is null!");
                return;
            }
            
            // Get current value and increment
            Long currentValue = getStat(statKey);
            Long newValue = currentValue + 1;
            setStat(statKey, newValue);
            
        } catch (Exception e) {
            logger.error("Error incrementing stat: " + statKey, e);
            // Final fallback: try to create the stat if it doesn't exist
            createOrUpdateStat(statKey, 1L);
        }
    }
    
    /**
     * Set a statistic value
     */
    @Transactional
    public void setStat(String statKey, Long value) {
        try {
            createOrUpdateStat(statKey, value);
        } catch (Exception e) {
            logger.error("Error setting stat: " + statKey, e);
        }
    }
    
    /**
     * Get a statistic value
     */
    public Long getStat(String statKey) {
        try {
            Optional<Statistics> stat = statisticsRepository.findByStatKey(statKey);
            return stat.map(Statistics::getStatValue).orElse(0L);
        } catch (Exception e) {
            logger.error("Error getting stat: " + statKey, e);
            return 0L;
        }
    }
    
    /**
     * Create or update a statistic
     */
    @Transactional
    public void createOrUpdateStat(String statKey, Long value) {
        try {
            Optional<Statistics> existingStat = statisticsRepository.findByStatKey(statKey);
            if (existingStat.isPresent()) {
                Statistics stat = existingStat.get();
                stat.setStatValue(value);
                statisticsRepository.save(stat);
            } else {
                Statistics newStat = new Statistics(statKey, value);
                statisticsRepository.save(newStat);
            }
        } catch (Exception e) {
            logger.error("Error creating/updating stat: " + statKey, e);
        }
    }
    
    /**
     * Get all search statistics
     */
    public Map<String, Object> getSearchStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Local search statistics
        stats.put("totalLocalSearches", getStat("total_local_searches"));
        stats.put("todayLocalSearches", getStat("today_local_searches"));
        stats.put("weekLocalSearches", getStat("week_local_searches"));
        stats.put("monthLocalSearches", getStat("month_local_searches"));
        
        // Google API search statistics
        stats.put("totalGoogleApiSearches", getStat("total_google_api_searches"));
        stats.put("todayGoogleApiSearches", getStat("today_google_api_searches"));
        stats.put("weekGoogleApiSearches", getStat("week_google_api_searches"));
        stats.put("monthGoogleApiSearches", getStat("month_google_api_searches"));
        
        // Combined statistics
        long totalSearches = getStat("total_local_searches") + getStat("total_google_api_searches");
        stats.put("totalSearches", totalSearches);
        
        // Calculate percentages
        if (totalSearches > 0) {
            double localPercentage = (double) getStat("total_local_searches") / totalSearches * 100;
            double googlePercentage = (double) getStat("total_google_api_searches") / totalSearches * 100;
            stats.put("localSearchPercentage", Math.round(localPercentage * 100.0) / 100.0);
            stats.put("googleApiSearchPercentage", Math.round(googlePercentage * 100.0) / 100.0);
        } else {
            stats.put("localSearchPercentage", 0.0);
            stats.put("googleApiSearchPercentage", 0.0);
        }
        
        return stats;
    }
    
    /**
     * Get Google Places API statistics
     */
    public Map<String, Object> getGooglePlacesApiStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalApiCalls", getStat("total_google_places_api_calls"));
        stats.put("todayApiCalls", getStat("today_google_places_api_calls"));
        stats.put("weekApiCalls", getStat("week_google_places_api_calls"));
        stats.put("monthApiCalls", getStat("month_google_places_api_calls"));
        
        return stats;
    }
    
    /**
     * Reset daily counters if needed
     */
    @Transactional
    public void resetDailyCountersIfNeeded() {
        long currentTime = System.currentTimeMillis();
        long lastResetTime = getStat("last_reset_time");
        
        if (currentTime - lastResetTime > 24 * 60 * 60 * 1000) { // 24 hours
            setStat("today_local_searches", 0L);
            setStat("today_google_api_searches", 0L);
            setStat("today_google_places_api_calls", 0L);
            setStat("week_local_searches", 0L);
            setStat("week_google_api_searches", 0L);
            setStat("week_google_places_api_calls", 0L);
            setStat("month_local_searches", 0L);
            setStat("month_google_api_searches", 0L);
            setStat("month_google_places_api_calls", 0L);
            setStat("last_reset_time", currentTime);
            
            logger.info("Daily counters reset at: " + currentTime);
        }
    }
    
    /**
     * Reset all counters (for testing purposes)
     */
    @Transactional
    public void resetAllCounters() {
        setStat("total_local_searches", 0L);
        setStat("total_google_api_searches", 0L);
        setStat("today_local_searches", 0L);
        setStat("today_google_api_searches", 0L);
        setStat("week_local_searches", 0L);
        setStat("week_google_api_searches", 0L);
        setStat("month_local_searches", 0L);
        setStat("month_google_api_searches", 0L);
        setStat("total_google_places_api_calls", 0L);
        setStat("today_google_places_api_calls", 0L);
        setStat("week_google_places_api_calls", 0L);
        setStat("month_google_places_api_calls", 0L);
        setStat("last_reset_time", System.currentTimeMillis());
        
        logger.info("All statistics counters reset");
    }
}
