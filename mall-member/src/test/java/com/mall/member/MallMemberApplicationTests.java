package com.mall.member;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class MallMemberApplicationTests {

    @Test
    void contextLoads() {

        String md5Hex = DigestUtils.md5Hex("123456");

        String s = Md5Crypt.md5Crypt("123456".getBytes());

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encoded = passwordEncoder.encode("123456");
        boolean matches = passwordEncoder.matches("123456", "$2a$10$3gUQvdg9sWsHC9FDwAAFWOAsdPVdiB92M38WO4XkPVWdD7MJrgEa2");
        System.out.println(matches);
        System.out.println(encoded);
        System.out.println(md5Hex);
        System.out.println(s);
    }

}
