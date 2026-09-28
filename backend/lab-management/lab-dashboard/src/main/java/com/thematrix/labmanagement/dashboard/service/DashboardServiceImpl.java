package com.thematrix.labmanagement.dashboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thematrix.labmanagement.equipment.entity.Equipment;
import com.thematrix.labmanagement.equipment.entity.EquipmentUsageRecord;
import com.thematrix.labmanagement.equipment.mapper.EquipmentMapper;
import com.thematrix.labmanagement.equipment.mapper.EquipmentUsageRecordMapper;
import com.thematrix.labmanagement.personnel.entity.Personnel;
import com.thematrix.labmanagement.personnel.mapper.PersonnelMapper;
import com.thematrix.labmanagement.reservation.entity.Reservation;
import com.thematrix.labmanagement.reservation.mapper.ReservationMapper;
import com.thematrix.labmanagement.resources.entity.Resource;
import com.thematrix.labmanagement.resources.mapper.ResourceMapper;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired(required = false)
    private EquipmentMapper equipmentMapper;

    @Autowired(required = false)
    private EquipmentUsageRecordMapper equipmentUsageRecordMapper;

    @Autowired(required = false)
    private PersonnelMapper personnelMapper;

    @Autowired(required = false)
    private ReservationMapper reservationMapper;

    @Autowired(required = false)
    private ResourceMapper resourceMapper;

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        try { stats.put("equipment", equipmentMapper != null ? equipmentMapper.selectCount(null) : 0); } catch (Exception e) { stats.put("equipment", 0); }
        try { stats.put("personnel", personnelMapper != null ? personnelMapper.selectCount(null) : 0); } catch (Exception e) { stats.put("personnel", 0); }
        try { stats.put("resources", resourceMapper != null ? resourceMapper.selectCount(null) : 0); } catch (Exception e) { stats.put("resources", 0); }
        try {
            if (reservationMapper != null) {
                QueryWrapper<Reservation> qw = new QueryWrapper<>();
                qw.eq("status", "pending");
                stats.put("reservations", reservationMapper.selectCount(qw));
            } else { stats.put("reservations", 0); }
        } catch (Exception e) { stats.put("reservations", 0); }
        return stats;
    }

    @Override
    public Map<String, Object> getEquipmentStatus() {
        Map<String, Object> status = new HashMap<>();
        try {
            if (equipmentMapper != null) {
                status.put("normal", equipmentMapper.selectCount(new QueryWrapper<Equipment>().eq("status", "normal")));
                status.put("borrowed", equipmentMapper.selectCount(new QueryWrapper<Equipment>().eq("status", "borrowed")));
                status.put("maintenance", equipmentMapper.selectCount(new QueryWrapper<Equipment>().eq("status", "maintenance")));
            } else {
                status.put("normal", 0); status.put("borrowed", 0); status.put("maintenance", 0);
            }
        } catch (Exception e) { status.put("normal", 0); status.put("borrowed", 0); status.put("maintenance", 0); }
        return status;
    }

    @Override
    public Map<String, Object> getWeeklyUsage() {
        String[] labels = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        int[] data = new int[7];
        try {
            if (equipmentUsageRecordMapper != null) {
                LocalDate today = LocalDate.now();
                LocalDate monday = today.with(DayOfWeek.MONDAY);
                QueryWrapper<EquipmentUsageRecord> qw = new QueryWrapper<>();
                qw.ge("borrow_time", monday.atStartOfDay()).lt("borrow_time", today.plusDays(1).atStartOfDay());
                List<EquipmentUsageRecord> records = equipmentUsageRecordMapper.selectList(qw);
                for (EquipmentUsageRecord r : records) {
                    if (r.getBorrowTime() != null) {
                        int idx = r.getBorrowTime().getDayOfWeek().getValue() - 1;
                        if (idx >= 0 && idx < 7) data[idx]++;
                    }
                }
            }
        } catch (Exception e) { /* ignore */ }
        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("data", data);
        return result;
    }

}
