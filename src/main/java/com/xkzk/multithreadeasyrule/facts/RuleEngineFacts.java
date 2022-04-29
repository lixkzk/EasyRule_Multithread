package com.xkzk.multithreadeasyrule.facts;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author : ligang1
 * @version : v1.0
 **/
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RuleEngineFacts {
    /**
     * 条件1
     */
    String condition1;

    /**
     * 条件2
     */
    String condition2;
}
