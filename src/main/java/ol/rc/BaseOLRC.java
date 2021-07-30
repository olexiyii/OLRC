package ol.rc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * The base calss for almost all project classes to provide general creation for:
 * Logger
 */
public class BaseOLRC {
    protected static Logger loggerStatic = LoggerFactory.getLogger(BaseOLRC.class);
    protected Logger logger;

    public BaseOLRC(Class clazz) {
        logger = LoggerFactory.getLogger(clazz);
    }


    protected BaseOLRC() {
    }

    public static void logError(Throwable tr) {
        BaseOLRC.loggerStatic.error(tr.getMessage());
        BaseOLRC.loggerStatic.error(Arrays.toString(tr.getStackTrace()));
    }

    public static void logInfo(String info) {
        BaseOLRC.loggerStatic.info(info);
    }

    public void error(Throwable tr) {
        logger.error(tr.getMessage());
        Arrays.stream(tr.getStackTrace()).forEach(System.out::println);
    }

}
