package com.scutelnic.rutex.repository;

import com.scutelnic.rutex.entity.RouteSeoPageEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RouteSeoPageEventRepository extends JpaRepository<RouteSeoPageEvent, Long> {

    interface RouteSeoEventAggregate {
        String getRouteSlug();
        String getLanguage();
        String getEventType();
        Long getTotalEvents();
        Long getUniqueVisitors();
        LocalDateTime getLastEventAt();
    }

    interface RouteViewAggregate {
        String getRouteSlug();
        Long getTotalViews();
    }

    @Query("""
            SELECT e.routeSlug AS routeSlug, COUNT(e.id) AS totalViews
            FROM RouteSeoPageEvent e
            WHERE e.eventType = 'view'
            GROUP BY e.routeSlug
            """)
    List<RouteViewAggregate> aggregateViewsByRouteSlug();

    @Modifying
    @Query("UPDATE RouteSeoPageEvent e SET e.routeSlug = :newSlug WHERE e.routeSlug = :oldSlug")
    int moveEventsToSlug(String oldSlug, String newSlug);

    interface RouteSeoReferrerAggregate {
        String getRouteSlug();
        String getLanguage();
        String getReferrer();
        Long getTotalEvents();
    }

    @Query("""
            SELECT e.routeSlug AS routeSlug,
                   e.language AS language,
                   e.eventType AS eventType,
                   COUNT(e.id) AS totalEvents,
                   COUNT(DISTINCT e.visitorKey) AS uniqueVisitors,
                   MAX(e.createdAt) AS lastEventAt
            FROM RouteSeoPageEvent e
            GROUP BY e.routeSlug, e.language, e.eventType
            """)
    List<RouteSeoEventAggregate> aggregateAll();

    @Query("""
            SELECT e.routeSlug AS routeSlug,
                   e.language AS language,
                   e.eventType AS eventType,
                   COUNT(e.id) AS totalEvents,
                   COUNT(DISTINCT e.visitorKey) AS uniqueVisitors,
                   MAX(e.createdAt) AS lastEventAt
            FROM RouteSeoPageEvent e
            WHERE e.createdAt >= :since
            GROUP BY e.routeSlug, e.language, e.eventType
            """)
    List<RouteSeoEventAggregate> aggregateSince(LocalDateTime since);

    @Query("""
            SELECT e.routeSlug AS routeSlug,
                   e.language AS language,
                   e.referrer AS referrer,
                   COUNT(e.id) AS totalEvents
            FROM RouteSeoPageEvent e
            WHERE e.eventType = 'view'
            GROUP BY e.routeSlug, e.language, e.referrer
            """)
    List<RouteSeoReferrerAggregate> aggregateReferrers();
}
