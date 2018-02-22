package sk.accerek.hamlet.world.storage;

public class WorldLoaderException extends Exception {
    public WorldLoaderException() {
        super();
    }

    public WorldLoaderException(String message) {
        super(message);
    }

    public WorldLoaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorldLoaderException(Throwable cause) {
        super(cause);
    }

    protected WorldLoaderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
