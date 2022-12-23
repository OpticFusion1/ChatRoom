package optic_fusion1.commands.command;

public enum CommandSide {
    CLIENT(0),
    SERVER(1);

    private final int code;

    CommandSide(int code) {
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
