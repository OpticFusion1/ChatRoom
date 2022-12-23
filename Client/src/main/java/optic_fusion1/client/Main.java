package optic_fusion1.client;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fusesource.jansi.AnsiConsole;

import java.net.ConnectException;
import java.util.Objects;

public class Main {

  private static final Logger LOGGER = LogManager.getLogger(Main.class);

  public static void main(String[] args) {
    AnsiConsole.systemInstall();

    OptionParser optionParser = new OptionParser();
    OptionSpec<Void> helpSpec = optionParser.accepts("help").forHelp();
    OptionSpec<String> hostSpec = optionParser.accepts("host").withRequiredArg();
    OptionSpec<String> portSpec = optionParser.accepts("port").withRequiredArg();
    OptionSpec<String> usernameSpec = optionParser.accepts("username").withRequiredArg();
    OptionSpec<String> passwordSpec = optionParser.accepts("password").withRequiredArg();

    try {
      OptionSet optionSet = optionParser.parse(args);

      if (optionSet.has(helpSpec)) {
        optionParser.printHelpOn(System.out);
        return;
      }

      String serverHost = optionSet.valueOf(hostSpec);
      String serverPort = optionSet.valueOf(portSpec);
      String username = optionSet.valueOf(usernameSpec);
      String password = optionSet.valueOf(passwordSpec);

      if (Objects.isNull(serverHost) || Objects.isNull(serverPort)) {
        optionParser.printHelpOn(System.out);
        return;
      }

      new Client(serverHost, Integer.parseInt(serverPort), username, password);
    } catch (ConnectException e) {
      LOGGER.fatal(e.getLocalizedMessage());
    } catch (Exception e) {
      LOGGER.fatal("Failed to start Client!", e);
    }
  }
}
