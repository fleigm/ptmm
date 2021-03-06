package de.fleigm.transitrouter;

import de.fleigm.transitrouter.commands.EntryCommand;
import de.fleigm.transitrouter.commands.GenerateCommand;
import de.fleigm.transitrouter.commands.StartServerCommand;
import io.quarkus.runtime.annotations.QuarkusMain;
import picocli.CommandLine;

@QuarkusMain
public class Main {

  public static void main(String[] args) {
    CommandLine commandLine = new CommandLine(new EntryCommand())
        .addSubcommand("serve", new StartServerCommand())
        .addSubcommand("generate", new GenerateCommand());

    commandLine.execute(args);
  }
}
