package com.stellariver.milky.demo;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

public class LogUtil {

    static public void setLogLevel(Class<?> clazz, Level level) {
        LoggerContext logContext = (LoggerContext) LogManager.getContext(false);
        Configuration configuration = logContext.getConfiguration();
        LoggerConfig loggerConfig = configuration.getLoggerConfig(clazz.getName());
        loggerConfig.setLevel(level);
        logContext.updateLoggers();
    }

}
