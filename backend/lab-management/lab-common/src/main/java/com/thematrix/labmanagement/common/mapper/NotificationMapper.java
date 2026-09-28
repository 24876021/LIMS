package com.thematrix.labmanagement.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thematrix.labmanagement.common.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    @Select("SELECT n.*, u.name AS sender_name FROM notification n LEFT JOIN sys_user u ON n.sender_id = u.user_id WHERE n.receiver_id = #{userId} ORDER BY n.create_time DESC")
    List<Notification> getByReceiverId(Long userId);

    @Select("SELECT COUNT(*) FROM notification WHERE receiver_id = #{userId} AND is_read = 0")
    Long countUnread(Long userId);
}
