package com.thematrix.labmanagement.dashboard.service;

import java.util.Map;

public interface DashboardService {
    Map<String, Object> getStats();
    Map<String, Object> getEquipmentStatus();
    Map<String, Object> getWeeklyUsage();
}
