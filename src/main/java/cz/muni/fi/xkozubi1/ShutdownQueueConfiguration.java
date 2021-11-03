package cz.muni.fi.xkozubi1;

import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.logging.Logger;


@Extension
public class ShutdownQueueConfiguration extends GlobalConfiguration {
    private static Logger logger = Logger.getLogger(ShutdownQueueConfiguration.class.getName());
    private boolean pluginOn = true;
    private boolean sorterOn;
    private String strategyOption = "default";
    private double permeability = 0.7;
    private long periodRunnable = 10;
    private long timeOpenQueueMillis = 500;

    public ShutdownQueueConfiguration() {
        load();
    }

    public boolean getPluginOn() {
        return pluginOn;
    }

    public String getStrategyOption() {
        return strategyOption;
    }

    public long getPeriodRunnable() {
        return periodRunnable;
    }

    public double getPermeability() {
        return permeability;
    }

    public long getTimeOpenQueueMillis() {
        return timeOpenQueueMillis;
    }

    @Override
    public boolean configure(StaplerRequest staplerRequest, JSONObject json) throws FormException {
        pluginOn = (Boolean) json.get("checkboxPlugin");
        sorterOn = (Boolean) json.get("checkboxSorter");
        strategyOption = json.get("strategyType").toString();
        periodRunnable = Long.parseLong(json.getString("periodRunnable"));
        permeability = Double.parseDouble(json.getString("permeability"));
        timeOpenQueueMillis = Long.parseLong(json.getString("timeOpenQueueMillis"));

        logger.info("Shutdown-queue plugin CONFIGURATION \n" +
                "\nplugin: " + pluginOn +
                "\nsorter: " + sorterOn +
                "\nstrategy: " + strategyOption +
                "\nperiod: " + periodRunnable +
                "\npermeability: " + permeability +
                "\ntimeOpenQueueMillis " + timeOpenQueueMillis
                );

        if (!pluginOn) {
            Utils.doReset();
        }

        Utils.handleSorterOn(sorterOn);

        save();
        ShutdownQueueComputerListener.changeReadInterval(periodRunnable);
        return super.configure(staplerRequest, json);
    }

    public FormValidation doCheckPermeability(@QueryParameter String value) {
        try {
            double valueD = Double.valueOf(value);
            if (valueD < 0 || valueD > 1) {
                return FormValidation.error("Please, enter a number in the interval <0;1>.");
            }
            return FormValidation.ok();

        } catch (NumberFormatException e) {
            return FormValidation.error("Please, enter an integer.");
        }
    }

    public FormValidation doCheckPeriodRunnable(@QueryParameter String value) {
        try {
            long valueI = Long.valueOf(value);
            if (valueI < 0) {
                return FormValidation.error("Please, enter a positive integer.");
            }
            return FormValidation.ok();

        } catch (NumberFormatException e) {
            return FormValidation.error("Please, enter an integer.");
        }
    }

    public ListBoxModel doFillStrategyTypeItems() {
        ListBoxModel items = new ListBoxModel();

        items.add("Default", "default");
        items.add("Remove longer", "removeLonger");

        return items;
    }

    public static ShutdownQueueConfiguration getInstance() {
        return GlobalConfiguration.all().get(ShutdownQueueConfiguration.class);
    }
}
