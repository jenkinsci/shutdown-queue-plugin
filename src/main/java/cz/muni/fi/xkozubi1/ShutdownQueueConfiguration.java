package cz.muni.fi.xkozubi1;

import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.logging.Logger;


/**
 * Settings for the plugin. Can be found in Jenkins global settings.
 * @Author Dominik Kozubik
 */
@Extension
public class ShutdownQueueConfiguration extends GlobalConfiguration {
    private static Logger logger = Logger.getLogger(ShutdownQueueConfiguration.class.getName());
    
    private boolean pluginOn;
    private boolean sorterOn;
    private String strategyOption;
    private double permeability;
    private long periodRunnable;
    private long timeOpenQueueMillis;

    public ShutdownQueueConfiguration() {
        load();
        setDefaultValues()
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
        pluginOn = (Boolean) json.get("pluginOn");
        sorterOn = (Boolean) json.get("sorterOn");
        strategyOption = json.get("strategyType").toString();
        periodRunnable = Long.parseLong(json.getString("periodRunnable"));
        permeability = Double.parseDouble(json.getString("permeability"));
        timeOpenQueueMillis = Long.parseLong(json.getString("timeOpenQueueMillis"));

        logger.info("\nShutdown-queue plugin CONFIGURATION\n" +
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
        ShutdownQueueComputerListener.changeScheduleInterval(periodRunnable);

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
        items.add("Sort and remove longer", "sortRemoveLonger");

        return items;
    }
    
    private void setDefaultValues()
    {
        pluginOn = true;
        sorterOn = false;
        strategyOption = "default";
        permeability = 0.6;
        periodRunnable = 10;
        timeOpenQueueMillis = 500;
    }

    public static ShutdownQueueConfiguration getInstance() {
        return GlobalConfiguration.all().get(ShutdownQueueConfiguration.class);
    }
}
