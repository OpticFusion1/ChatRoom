package optic_fusion1.packets;

import java.util.Arrays;
import java.util.Optional;

public enum OpCode {
  UNKNOWN(-1),
  CONNECT(0),
  DISCONNECT(1),
  LOGIN(2),
  LOGGED_IN(3),
  LOGIN_REQUIRED(4),
  MESSAGE(5);

  private final int code;

  OpCode(int code) {
    this.code = code;
  }

  public int getCode() {
    return this.code;
  }

  public static OpCode valueOf(int value) {
    return Arrays.stream(values()).filter(opCode -> opCode.code == value).findFirst().orElse(OpCode.UNKNOWN);
  }
}
