/*
 * Copyright (C) 2021 Optic_Fusion1
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package optic_fusion1.packets.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import optic_fusion1.packets.IPacket;
import optic_fusion1.packets.OpCode;

public class MessagePacket implements IPacket {

  private OpCode opCode;
  private MessageChatType chatType;
  private String message;

  public MessagePacket() {
  }

  public MessagePacket(OpCode opCode, String message, MessageChatType chatType) {
    this.opCode = opCode;
    this.message = message;
    this.chatType = chatType;
  }

  public enum MessageChatType {
    USER("USER"),
    SYSTEM("SYSTEM"),
    SERVER("SERVER");

    private final String name;

    MessageChatType(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  @Override
  public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
    dataOutputStream.writeUTF(serialize().toString());
  }

  @Override
  public void readPacketData(DataInputStream dataInputStream) throws IOException {
    deserialize(dataInputStream.readUTF());
  }

  public String getMessage() {
    return message;
  }

  public OpCode getOpCode() {
    return opCode;
  }

  public MessageChatType getChatType() {
    return chatType;
  }

  public JsonObject serialize() {
    JsonParser parser = new JsonParser();
    JsonObject object = new JsonObject();

    object.addProperty("type", getOpCode().getCode());
    object.addProperty("chatType", getChatType().getName());
    object.addProperty("message", getMessage());
    return object;
  }

  public void deserialize(String rawPacket) {
    JsonParser parser = new JsonParser();
    JsonObject object = (JsonObject) parser.parse(rawPacket);

    opCode = OpCode.valueOf(object.get("type").getAsInt());
    chatType = MessageChatType.valueOf(object.get("chatType").getAsString());
    message = object.get("message").getAsString();
  }
}
