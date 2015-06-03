package hyn.com.lib;

/**
 * Created by hanyanan on 2015/6/2.
 * The interface to identity the finger print.
 */
public interface Fingerprint {
    public static final Fingerprint DEFAULT_FINGERPRINT = new Fingerprint(){
        @Override
        public String fingerprint() {
            return toString();
        }
    };

    /**
     * Return the unique string to identify the fingerprint!
     */
    public String fingerprint();

    public static class Builder {
        private static long id = 0;

        public Builder() {

        }

        public synchronized Fingerprint build() {
            return new Fingerprint() {
                private final long id = Builder.id++;

                @Override
                public String fingerprint() {
                    return String.valueOf(this.id);
                }
            };
        }
    }
}
