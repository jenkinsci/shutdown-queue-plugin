package cz.muni.fi.xkozubi1;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;

@Extension
public class ShutdownQueueConfiguration extends GlobalConfiguration {

    private boolean checkboxPlugin;
    private boolean checkboxSorter;
    private long milliseconds;

    public ShutdownQueueConfiguration() {
        load();
    }

    public boolean getCheckboxPlugin() {
        return checkboxPlugin;
    }

    public boolean getCheckboxSorter() {
        return checkboxSorter;
    }

    public long getMilliseconds() {
        return milliseconds;
    }

    @Override
    public boolean configure(StaplerRequest staplerRequest, JSONObject json) throws FormException {
        checkboxPlugin = (Boolean) json.get("checkboxPlugin");
        checkboxSorter = (Boolean) json.get("checkboxSorter");
        milliseconds = Long.parseLong(json.getString("seconds")) * 1000; // convert seconds to milliseconds

        System.out.println(checkboxPlugin + " " + checkboxSorter + " " + milliseconds);

        if (!checkboxPlugin) {
            Utils.doReset();
        }

        Utils.handleSorterOn(checkboxSorter);
        save();
        return super.configure(staplerRequest, json);
    }

    public FormValidation doCheckSeconds(@QueryParameter String value) {
        try {
            Long.valueOf(value);
            return FormValidation.ok();
        } catch (NumberFormatException e) {
            return FormValidation.error("Please, enter a number");
        }
    }

    public static ShutdownQueueConfiguration getInstance() {
        return GlobalConfiguration.all().get(ShutdownQueueConfiguration.class);
    }
}
