package com.mall.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @ClassName: User
 * @Description:
 * @Author: L
 * @Create: 2023-08-03 08:11
 * @Version: 1.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User implements Serializable {
    private Integer id;
    private String name;
    private Integer age;
    private String gender;
}
