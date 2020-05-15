package wtf.declan.muzzle.message;

public enum MessageType {

    ENCRYPTED           ("ENCR", "Encrypted"),
    KEY_EXCHANGE        ("KEYE", "Key Exchange"),
    TERMINATE_SESSION   ("ENDS", "Terminate Session"),
    TEXT                ("", "Text Message");

    private final String prefix;
    private final String friendly;

    /**
     * @param prefix Prefix which will be included in MessageHash
     * @param friendly Friendly name for the prefix type to display in UI
     */
    MessageType(String prefix, String friendly) {
        this.prefix = prefix;
        this.friendly = friendly;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getFriendly() {
        return friendly;
    }

}
