package importApp.entity;

public enum DeviceType {
    WEB("WEB"),
    MOBILE("MOBILE"),
    TABLET("TABLET"),
    DESKTOP("DESKTOP"),
    UNKNOWN("UNKNOWN");

    private final String value;

    DeviceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DeviceType fromUserAgent(String userAgent) {
        if (userAgent == null) {
            return UNKNOWN;
        }

        String ua = userAgent.toLowerCase();

        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return MOBILE;
        } else if (ua.contains("tablet") || ua.contains("ipad")) {
            return TABLET;
        } else if (ua.contains("chrome") || ua.contains("firefox") || ua.contains("safari") || ua.contains("edge")) {
            return WEB;
        } else {
            return DESKTOP;
        }
    }
}