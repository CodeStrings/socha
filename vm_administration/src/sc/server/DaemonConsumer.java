package sc.server;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import sc.server.configuration.Settings;

public class DaemonConsumer
  implements
    Runnable, IConsumerListener
{

  @Override
  public void run()
  {
    boolean active = true;
    Logger.log("Consumer daemon started...");
    while (active)
      {
        new Thread(new Runnable()
          {
            @Override
            public void run()
            {
              if (Settings.DEBUG_MODE) {
                Logger.log(DaemonConsumer.this.currentProcs + "/" + DaemonConsumer.this.maxProcs + " running");
              }
              if (DaemonConsumer.this.currentProcs < DaemonConsumer.this.maxProcs) {
                if (Settings.DEBUG_MODE) {
                  Logger.log("Checking queue...");
                }
                Consumer c;
                DaemonConsumer.this.currentProcs += 1;
                try
                  {
                    c = new Consumer(DaemonConsumer.this.hostName,
                                     Settings.DEFAULT_PORT);
                    c.addConsumerListener(DaemonConsumer.this);
                    c.bash = DaemonConsumer.this.bashPath;
                    c.command = DaemonConsumer.this.command;
                    c.consume(DaemonConsumer.this.queue);
                    c.free();
                  }
            catch (IOException | TimeoutException e)
                  {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                  }
                finally {
                  DaemonConsumer.this.currentProcs -= 1;
                }
              }
            }

          }).start();
        try
          {
            Thread.sleep(this.interval * 1000);
          }
        catch (InterruptedException e)
          {
            e.printStackTrace();
          }
      }
  }

  protected String	hostName;
  protected String	bashPath;
  protected String	command;
  protected String	queue;
  protected int		interval;
  protected int		maxProcs;
  protected int		currentProcs = 0;

  public static void main(String[] args)
  {
    try
      {
        final Options o = new Options();
        o.addOption(Settings.HOST_OPTION, true,
                    "Server to connect to after launch");
        o.addOption(Settings.BASH_OPTION, true,
                    "Path to bash (normally /bin/bash)");
        o.addOption(Settings.COMMAND_OPTION, true, "Command to execute");
        o.addOption(Settings.QUEUE_OPTION, true, "Queue to consume");
        o.addOption(Settings.INTERVAL_OPTION, true, "How long to wait before consuming next message");
        o.addOption(Settings.MAX_PROC_OPTION, true, "How many processes at same time");
        o.addOption(Settings.DEBUG_OPTION, false, "Enable debug mode");

        CommandLineParser parser = new PosixParser();
        CommandLine cmd = parser.parse(o, args);

        final DaemonConsumer dc = new DaemonConsumer();
        dc.hostName = cmd.getOptionValue(Settings.HOST_OPTION);
        dc.bashPath = cmd.getOptionValue(Settings.BASH_OPTION);
        dc.command = cmd.getOptionValue(Settings.COMMAND_OPTION);
        dc.queue = cmd.getOptionValue(Settings.QUEUE_OPTION);
        dc.interval = Integer.valueOf(cmd.getOptionValue(Settings.INTERVAL_OPTION, "5"));
        dc.maxProcs = Integer.valueOf(cmd.getOptionValue(Settings.MAX_PROC_OPTION, "12"));
        Settings.DEBUG_MODE = cmd.hasOption(Settings.DEBUG_OPTION);

        if (dc.hostName == null || dc.bashPath == null
            || dc.command == null || dc.queue == null)
          {
            Logger.logError("Incorrect usage.");
            System.exit(2);
          }

        new Thread(dc).start();
      }
    catch (Exception ex)
      {
        Logger.logError("Main thread caught exception: " + ex);
        ex.printStackTrace();
        System.exit(1);
      }
  }

  @Override
  public void onStartConsuming() {
    Logger.log("Starting process " + this.currentProcs + "/" + this.maxProcs);
  }
}
