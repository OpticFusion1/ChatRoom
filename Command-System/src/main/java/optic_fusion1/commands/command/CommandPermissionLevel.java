package optic_fusion1.commands.command;

public enum CommandPermissionLevel {
    EVERYONE(0),
    OPERATOR(1),
    SERVER(2);

    private final int code;

    CommandPermissionLevel(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

//    public static CommandType valueOf(int code) {
//        Optional<CommandType> optional = Arrays.stream(values())
//                .filter(type -> type.code == code)
//                .findFirst();
//        return optional.orElse(CommandType.UNKNOWN);
//    }
}
