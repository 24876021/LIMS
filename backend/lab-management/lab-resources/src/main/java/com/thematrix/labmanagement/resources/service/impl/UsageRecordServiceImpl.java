package com.thematrix.labmanagement.resources.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thematrix.labmanagement.resources.entity.UsageRecord;
import com.thematrix.labmanagement.resources.mapper.UsageRecordMapper;
import com.thematrix.labmanagement.resources.service.UsageRecordService;
import org.springframework.stereotype.Service;

@Service
public class UsageRecordServiceImpl extends ServiceImpl<UsageRecordMapper, UsageRecord> implements UsageRecordService {}
