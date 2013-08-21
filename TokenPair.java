import java.io.Serializable;

public class TokenPair implements Serializable {
    public final String key;
    public final String secret;

    public TokenPair(String key, String secret) {
        //if (key == null) throw new IllegalArgumentException("'key' must be non-null");
        //if (key.contains("|")) throw new IllegalArgumentException("'key' must not contain a \"|\" character: \"" + key + "\"");
        //if (secret == null) throw new IllegalArgumentException("'secret' must be non-null");

        this.key = key;
        this.secret = secret;
    }

    @Override
    public int hashCode() {
        return key.hashCode() ^ (secret.hashCode() << 1);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TokenPair && equals((TokenPair) o);
    }

    public boolean equals(TokenPair o) {
        return key.equals(o.key) && secret.equals(o.secret);
    }

    @Override
    public String toString() {
        return "{key=\"" + key + "\", secret=\"" + secret + "\"}";
    }
}
