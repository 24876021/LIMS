package com.thematrix.labmanagement.common.handler.password;

import com.thematrix.labmanagement.common.constant.RsaProperties;
import com.thematrix.labmanagement.common.utils.RSAUtils;
import lombok.NoArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码编译器
 */
@NoArgsConstructor
public class PasswordEncoder extends BCryptPasswordEncoder {

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        String pwd = rawPassword.toString();
        try {
            pwd = RSAUtils.decryptByPrivateKey(RsaProperties.privateKey, pwd);
        } catch (Exception e) {
            throw new BadCredentialsException(e.getMessage());
        }
        if (encodedPassword != null && encodedPassword.length() != 0) {
            return BCrypt.checkpw(pwd, encodedPassword);
        } else {
            return false;
        }
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return super.encode(rawPassword);
    }
}
