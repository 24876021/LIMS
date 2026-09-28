package com.thematrix.labmanagement.resources.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thematrix.labmanagement.resources.entity.PurchaseRecord;
import com.thematrix.labmanagement.resources.mapper.PurchaseRecordMapper;
import com.thematrix.labmanagement.resources.service.PurchaseRecordService;
import org.springframework.stereotype.Service;

@Service
public class PurchaseRecordServiceImpl extends ServiceImpl<PurchaseRecordMapper, PurchaseRecord> implements PurchaseRecordService {}
