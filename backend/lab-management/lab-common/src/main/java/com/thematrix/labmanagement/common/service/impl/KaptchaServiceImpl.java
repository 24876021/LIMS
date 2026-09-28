package com.thematrix.labmanagement.common.service.impl;

import cn.hutool.core.lang.UUID;
import com.google.code.kaptcha.Producer;
import com.thematrix.labmanagement.common.constant.Const;
import com.thematrix.labmanagement.common.service.KaptchaService;
import com.thematrix.labmanagement.common.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class KaptchaServiceImpl implements KaptchaService {

    @Autowired
    private Producer producer;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public Map<String, String> generateCaptcha() throws Exception {
        String key = UUID.randomUUID().toString();
        String code = producer.createText();

        BufferedImage image = producer.createImage(code);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", outputStream);

        byte[] imageBytes = outputStream.toByteArray();
        String base64Img = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageBytes);

        redisUtil.hset(Const.CAPTCHA_KEY, key, code, 120);

        Map<String, String> result = new HashMap<>();
        result.put("userKey", key);
        result.put("captcherImg", base64Img);
        return result;
    }
}
