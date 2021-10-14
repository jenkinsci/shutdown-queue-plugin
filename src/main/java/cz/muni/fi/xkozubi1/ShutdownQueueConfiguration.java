package cz.muni.fi.xkozubi1;

import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;


@Extension
public class ShutdownQueueConfiguration extends GlobalConfiguration {

    private boolean checkboxPlugin;
    private boolean checkboxSorter;
    private double permeability;
    private long periodRunnable = 10;
    private String strategyOption;

    public ShutdownQueueConfiguration() {
        load();
    }

    public boolean getCheckboxPlugin() {
        return checkboxPlugin;
    }

    public boolean getCheckboxSorter() {
        return checkboxSorter;
    }

    public long getPeriodRunnable() {
        return periodRunnable;
    }

    public String getStrategyOption() {
        return strategyOption;
    }

    @Override
    public boolean configure(StaplerRequest staplerRequest, JSONObject json) throws FormException {
        checkboxPlugin = (Boolean) json.get("checkboxPlugin");
        checkboxSorter = (Boolean) json.get("checkboxSorter");
        permeability = Double.parseDouble(json.getString("permeability"));
        periodRunnable = Long.parseLong(json.getString("periodRunnable"));
        strategyOption = json.get("strategyType").toString();

        System.out.println("plugin: " + checkboxPlugin +
                "\nsorter: " + checkboxSorter + "" +
                "\npermeability: " + permeability +
                "\nperiod: " + periodRunnable + "" +
                "\nstrategy: " + strategyOption);

        if (!checkboxPlugin) {
            Utils.doReset();
        }

        Utils.handleSorterOn(checkboxSorter);

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

        items.add("Sort", "sort");
        items.add("Remove longer", "removeLonger");

        return items;
    }

    public static ShutdownQueueConfiguration getInstance() {
        return GlobalConfiguration.all().get(ShutdownQueueConfiguration.class);
    }
}
