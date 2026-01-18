package com.scutelnic.rutex.config;

import com.scutelnic.rutex.service.SiteVisitorService;
import com.scutelnic.rutex.service.SecurityMonitoringService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class BanInterceptor implements HandlerInterceptor {
    
    @Autowired
    private SiteVisitorService siteVisitorService;
    
    @Autowired
    private SecurityMonitoringService securityMonitoringService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ipAddress = getClientIpAddress(request);
        
        // Monitor request for security threats
        securityMonitoringService.monitorRequest(request);
        
        // Check if IP is banned
        if (siteVisitorService.isIpBanned(ipAddress)) {
            // Return 403 Forbidden for banned IPs
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Acces Restricționat - Rutex</title>
                    <style>
                        body { 
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
                            background-color: #f8f9fa;
                            margin: 0;
                            padding: 20px;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            min-height: 100vh;
                        }
                        .error-container {
                            background-color: #ffffff;
                            padding: 40px;
                            border-radius: 8px;
                            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                            text-align: center;
                            max-width: 500px;
                        }
                        .error-icon {
                            font-size: 64px;
                            color: #dc3545;
                            margin-bottom: 20px;
                        }
                        h1 {
                            color: #2c3e50;
                            margin-bottom: 15px;
                        }
                        p {
                            color: #6c757d;
                            line-height: 1.6;
                            margin-bottom: 20px;
                        }
                        .contact-info {
                            background-color: #f8f9fa;
                            padding: 15px;
                            border-radius: 6px;
                            margin-top: 20px;
                        }
                    </style>
                </head>
                <body>
                    <div class="error-container">
                        <div class="error-icon">🚫</div>
                        <h1>Acces Restricționat</h1>
                        <p>IP-ul dvs. a fost blocat din cauza activității suspecte sau încălcării termenilor de utilizare.</p>
                        <p>Dacă considerați că aceasta este o eroare, vă rugăm să ne contactați.</p>
                        <div class="contact-info">
                            <strong>Email:</strong> contact@rutex.md<br>
                            <strong>Subiect:</strong> Cerere deblocare IP
                        </div>
                    </div>
                </body>
                </html>
                """);
            return false; // Stop the request
        }
        
        return true; // Allow the request to continue
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
