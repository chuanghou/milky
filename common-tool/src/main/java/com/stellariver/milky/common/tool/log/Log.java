package com.stellariver.milky.common.tool.log;


import com.stellariver.milky.common.base.ErrorCode;
import com.stellariver.milky.common.tool.common.Prefix;

@FunctionalInterface
public interface Log {

    void log();

    static Log of(Log log) {
        return log;
    }

    default Log withLogTag(LogTagValue logTagValue) {
        return () -> {
            String oldTag = MDCTool.putAndGet(MDCTag.logTag, logTagValue);
            try {
                this.log();
            } finally {
                MDCTool.removeAndRestore(MDCTag.logTag, oldTag);
            }
        };
    }

    default Log withInfo(MDCTag mdcTag, String value) {
        return () -> {
            String oldValue = MDCTool.putAndGet(mdcTag, value);
            try {
                this.log();
            } finally {
                MDCTool.removeAndRestore(mdcTag, oldValue);
            }
        };
    }

    default void log(LogTagValue logTagValue) {
        String oldTag = MDCTool.putAndGet(MDCTag.logTag, logTagValue);
        try {
            this.log();
        } finally {
            MDCTool.removeAndRestore(MDCTag.logTag, oldTag);
        }
    }

    default void log(ErrorCode errorCode) {
        String oldTag = MDCTool.putAndGet(MDCTag.logTag, LogTagValue.of(Prefix.ERROR_CODE, errorCode.getCode()));
        try {
            this.log();
        } finally {
            MDCTool.removeAndRestore(MDCTag.logTag, oldTag);
        }
    }
}
