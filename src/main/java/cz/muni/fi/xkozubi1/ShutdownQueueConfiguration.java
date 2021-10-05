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

    private boolean pluginOn;
    private boolean sorterOn;
    private long milliseconds;

    public ShutdownQueueConfiguration() {
        load();
    }

    public boolean getPluginOn() {
        return pluginOn;
    }

    public long getMilliseconds() {
        return milliseconds;
    }

    @Override
    public boolean configure(StaplerRequest staplerRequest, JSONObject json) throws FormException {
        pluginOn = (Boolean) json.get("pluginOn");
        sorterOn = (Boolean) json.get("sorterOn");
        milliseconds = Long.valueOf(json.getString("seconds")) * 1000; // convert seconds to milliseconds

        System.out.println(pluginOn + " " + sorterOn + " " + milliseconds);

        if (!pluginOn) {
            Utils.doReset();
        }

        Utils.handleSorterOn(sorterOn);
        save();
        return super.configure(staplerRequest, json);
    }

    public FormValidation doCheckSeconds(@QueryParameter String value) throws IOException, ServletException {
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
