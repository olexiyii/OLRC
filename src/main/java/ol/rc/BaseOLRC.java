package ol.rc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * The base calss for almost all project classes to provide general creation for:
 * Logger
 */
public class BaseOLRC {
    protected static final Logger loggerStatic = LoggerFactory.getLogger(BaseOLRC.class);
    protected Logger logger;
    private static final boolean canLogginErrors=true;

    public BaseOLRC(Class clazz) {
        setClass(clazz);
    }

    protected BaseOLRC() {
    }

    protected void setClass(Class clazz){
        logger = LoggerFactory.getLogger(clazz);
    }
    public static void logError(Throwable tr) {
        if (!canLogginErrors){
            return;
        }
        BaseOLRC.loggerStatic.error("==============================");
        BaseOLRC.loggerStatic.error(tr.getMessage());
        BaseOLRC.loggerStatic.error(Arrays.toString(tr.getStackTrace()));
    }


    public static void logInfo(String info) {
        BaseOLRC.loggerStatic.info(info);
    }

    public void error(Throwable tr) {
        if (!canLogginErrors){
            return;
        }
        logger.error(tr.getMessage());
        Arrays.stream(tr.getStackTrace()).forEach(System.out::println);
    }

    static class CustomException extends Throwable {
        final String message;

        CustomException(Throwable tr) {
            message = tr.getMessage();
        }
        CustomException(String str) {
            message = str;
        }

        public String toString() {
            return ("Custom Exception Occurred: " + message);
        }
    }
}
