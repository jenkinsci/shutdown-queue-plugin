package cz.muni.fi.xkozubi1;

import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Settings for the plugin. Can be found in Jenkins global settings.
 *
 * @author Dominik Kozubik
 */
@Extension
public class ShutdownQueueConfiguration extends GlobalConfiguration {
    private static Logger logger = Logger.getLogger(ShutdownQueueConfiguration.class.getName());

    private boolean pluginOn = false;
    private boolean sorterOn = false;
    private String strategyOption = "copying";
    private long periodRunnable = 10;
    private double permeability = 0.7;
    private long timeOpenQueueMillis = 500;

    private static int MAX_APPLY_ATTEMPTS = 200;
    private static List<Exception> applyCounter = Collections.synchronizedList(new ArrayList<>(MAX_APPLY_ATTEMPTS + 1));


    public static ShutdownQueueConfiguration getInstance() {
        return GlobalConfiguration.all().get(ShutdownQueueConfiguration.class);
    }

    public ListBoxModel doFillStrategyTypeItems() {
        ListBoxModel items = new ListBoxModel();
        items.add("Copying", "copying");
        items.add("Remove longer", "removeLonger");
        items.add("Sort and remove longer", "sortRemoveLonger");
        return items;
    }


    public boolean getPluginOn() {
        return pluginOn;
    }

    //some older jenkinses are unabel to read is
    public boolean isPluginOn() {
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

    public boolean isSorterOn() {
        return sorterOn;
    }

    //some older jenkinses are unabel to read is
    public boolean getSorterOn() {
        return sorterOn;
    }

    @DataBoundSetter
    public void setPluginOn(boolean pluginOn) {
        this.pluginOn = pluginOn;
        apply();
    }

    @DataBoundSetter
    public void setSorterOn(boolean sorterOn) {
        this.sorterOn = sorterOn;
        apply();
    }

    @DataBoundSetter
    public void setStrategyOption(String strategyOption) {
        this.strategyOption = strategyOption;
        apply();
    }

    @DataBoundSetter
    public void setPermeability(double permeability) {
        this.permeability = permeability;
        apply();
    }

    @DataBoundSetter
    public void setPeriodRunnable(long periodRunnable) {
        this.periodRunnable = periodRunnable;
        apply();
    }

    @DataBoundSetter
    public void setTimeOpenQueueMillis(long timeOpenQueueMillis) {
        this.timeOpenQueueMillis = timeOpenQueueMillis;
        apply();
    }

    public ShutdownQueueConfiguration() {
        load();
        apply();
    }

    @DataBoundConstructor
    public ShutdownQueueConfiguration(boolean pluginOn, boolean sorterOn, String strategyOption, long periodRunnable, double permeability, long timeOpenQueueMillis) {
        this.pluginOn = pluginOn;
        this.sorterOn = sorterOn;
        this.strategyOption = strategyOption;
        this.periodRunnable = periodRunnable;
        this.permeability = permeability;
        this.timeOpenQueueMillis = timeOpenQueueMillis;
        apply();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        req.bindJSON(this, json);
        save();
        apply();
        return super.configure(req, json);
    }

    private void apply() {
        try {
            if (!pluginOn) {
                Utils.doReset();
            }
            Utils.handleSorterOn(isSorterOn());
            ShutdownQueueComputerListener.changeScheduleInterval(periodRunnable);
            logger.log(Level.WARNING, "apply off ShutdownQueue Plugin settings called OK on attempt " + applyCounter.size());
            applyCounter.clear();
        } catch (Exception ex) {
            applyAsyncLater(ex);
        }
    }

    private void applyAsyncLater(Exception ex) {
        applyCounter.add(ex);
        logger.log(Level.WARNING,
                "apply off ShutdownQueue Plugin settings called to soon: " + applyCounter.size() + "/" + MAX_APPLY_ATTEMPTS + "; now with: " + ex.getMessage() + " occured");
        if (applyCounter.size() >= MAX_APPLY_ATTEMPTS) {
            noMoreTries();
        } else {
            new Thread(() -> applyDelayed()).start();
        }
    }

    private void noMoreTries() {
        logger.log(Level.SEVERE, "apply off ShutdownQueue Plugin settings failed : " + applyCounter.size() + "-times; Max was " + MAX_APPLY_ATTEMPTS + "game over:");
        for (int x = 0; x < applyCounter.size(); x++) {
            Exception oldex = applyCounter.get(x);
            logger.log(Level.SEVERE, x + " ShutdownQueue old exception: ", oldex);
        }
    }

    private void applyDelayed() {
        logger.log(Level.WARNING, "Sleeping 2s to try apply later");
        try {
            Thread.sleep(2000);
        } catch (Exception ex) {
            logger.log(Level.INFO, "sleep gone", ex);
        }
        apply();
    }

    public FormValidation doCheckPermeability(@QueryParameter String value) {
        try {
            double valueD = Double.parseDouble(value);
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
            long valueI = Long.parseLong(value);
            if (valueI < 0) {
                return FormValidation.error("Please, enter a positive integer.");
            }
            return FormValidation.ok();

        } catch (NumberFormatException e) {
            return FormValidation.error("Please, enter an integer.");
        }
    }

}
