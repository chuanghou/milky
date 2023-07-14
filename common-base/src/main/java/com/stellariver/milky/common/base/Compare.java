package com.stellariver.milky.common.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Date;

@Getter
@AllArgsConstructor
public enum Compare {

    BIGGER("大于", ">") {
        @Override
        public boolean compare(Date date, Date compareValue) {
            return date.after(compareValue);
        }
    },
    BIGGER_OR_EQUAL("大于等于", ">=") {
        @Override
        public boolean compare(Date date, Date compareValue) {
            return !date.before(compareValue);
        }
    },
    EQUAL("等于", "==") {
        @Override
        public boolean compare(Date date, Date compareValue) {
            return date.equals(compareValue);
        }
    },
    NOT_EQUAL("不等于", "!=") {
        @Override
        public boolean compare(Date date, Date compareValue) {
            return !date.equals(compareValue);
        }
    },
    SMALLER("小于", "<") {
        @Override
        public boolean compare(Date date, Date compareValue) {
            return date.before(compareValue);
        }
    },
    SMALLER_OR_EQUAL("小于等于", "<") {
        @Override
        public boolean compare(Date date, Date compareValue) {
            return !date.after(compareValue);
        }
    },

    NOT_CHECK("不检查", null) {
        @Override
        public boolean compare(Date date, Date compareValue) {
            throw new SysEx(ErrorEnumsBase.CONFIG_ERROR);
        }
    };

    final String desc;
    final String symbol;

    public abstract boolean compare(Date date, Date compareValue);

}
