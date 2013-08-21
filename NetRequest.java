import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.Serializable;

public class NetRequest implements Serializable {
	public Map<String, String> fields = new HashMap<String, String>();

	public NetRequest() {
		
	}

	public void addField(String field, String value) {
		this.addField(field, value, false);
	}

	public void addField(String field, String value, boolean overwrite) {
		if (!overwrite && fields.containsKey(field)) return;
		else fields.put(field, value);
	}

	public String getField(String field) {
		return fields.containsKey(field) ? fields.get(field) : null;
	}

	public int getType() {
		switch (getField("type")) {
			default: return 0; 
			case "token-request": return 1;
			case "data-request": return 2;
			case "execute-command": return 3;
			case "broadcast-message": return 4;
			case "shutdown-request": return 5;
		}
	}

	public TokenPair getToken() {
		return new TokenPair(getField("access-key"), getField("access-secret"));
	}

	public void setToken(TokenPair token) {
		this.addField("access-key", token.key);
		this.addField("access-secret", token.secret);
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		int i = 0;

		buf.append("{");

		for (String field : fields.keySet()) {
			buf.append("{" + field + ": " + fields.get(field) + "}");
			if (i++ != fields.size() - 1) buf.append(" ");
		}

		buf.append("}");

		return buf.toString();
	}
}